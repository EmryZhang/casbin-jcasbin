# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

"""Build/check unsigned Maven candidates; never sign, deploy, tag or publish."""
import argparse
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
from check_maven_artifacts import check


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('version')
    parser.add_argument('--javadoc-home', type=Path, required=True)
    parser.add_argument('--maven', default=shutil.which('mvn') or shutil.which('mvn.cmd'))
    parser.add_argument('--repository', type=Path)
    parser.add_argument('--offline', action='store_true')
    parser.add_argument('--skip-tests', action='store_true', help='For the separate docs CI job; not full release verification')
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[2]
    sdk = args.javadoc_home.resolve()
    if not re.fullmatch(r'\d+\.\d+\.\d+', args.version):
        parser.error('Use the base Maven version X.Y.Z, without an RC suffix')
    if not args.maven:
        parser.error('Maven was not found')
    main_home = os.environ.get('JAVA_HOME')
    if not main_home:
        parser.error('Set JAVA_HOME to a Java8 JDK')
    java = Path(main_home) / ('bin/java.exe' if os.name == 'nt' else 'bin/java')
    run = subprocess.run([str(java), '-version'], capture_output=True, text=True)
    if run.returncode or 'version "1.8.' not in run.stderr + run.stdout:
        parser.error('JAVA_HOME must select Java8 for compilation/tests')
    release = (sdk / 'release').read_text(encoding='utf-8')
    if 'IMPLEMENTOR="Eclipse Adoptium"' not in release or 'JAVA_RUNTIME_VERSION="25.0.4.1+1-LTS"' not in release:
        parser.error('Use Temurin OpenJDK 25.0.4.1+1 for the pinned documentation resources')
    executable = sdk / ('bin/javadoc.exe' if os.name == 'nt' else 'bin/javadoc')
    if not executable.is_file():
        parser.error('Javadoc executable missing')
    command = [args.maven, '-B', '-ntp', 'clean', 'verify', '-Prelease-javadoc',
               '-Drevision=' + args.version, '-Dgpg.skip=true', '-Dorg.slf4j.simpleLogger.defaultLogLevel=warn',
               '-Djavadoc.tool.path=' + str(executable)]
    if args.repository:
        command.append('-Dmaven.repo.local=' + str(args.repository.resolve()))
    if args.offline:
        command.append('-o')
    if args.skip_tests:
        command.append('-DskipTests=true')
    subprocess.run(command, cwd=root, check=True)
    result = check(root, args.version, sdk)
    result['tests_skipped'] = args.skip_tests
    path = root / 'target/release-maven-check.json'
    path.write_text(json.dumps(result, indent=2), encoding='utf-8')
    print('Unsigned candidates checked; report: ' + str(path))


if __name__ == '__main__':
    main()
