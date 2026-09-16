<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements. See the NOTICE file
distributed with this work for additional information
regarding copyright ownership. The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License. You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License.
-->

# Building JCasbin from a source archive

Use JDK 8 to compile and test, Maven 3.9.x, and JDK 25 for the standard
Javadoc doclet. Set `JAVA_HOME` to JDK 8 and put Maven on `PATH`. Register
JDK 25 in Maven toolchains with version `25`, or add
`-DjavadocExecutable=/path/to/jdk-25/bin/javadoc` to the commands below.
Dependencies are downloaded from Maven repositories. GitHub CI installs both
JDKs; the last setup-java version (8) runs Maven.

From the extracted archive's top-level directory, run:

```sh
mvn -B -ntp clean verify -Dgpg.skip=true
```

In PowerShell, quote the property argument:

```powershell
mvn -B -ntp clean verify '-Dgpg.skip=true'
```

This compiles Java sources, runs the TestNG suite, and builds the library,
source and Javadoc JARs under `target/`. The property disables artifact
signing for an ordinary build; this command does not deploy artifacts.
Do not use `deploy` when reviewing a release candidate.

Some persistence tests rewrite example policy files. Run tests on a disposable
extraction and preserve the original downloaded archive and its signature.

Release preparation commits the intended Maven version to `pom.xml`. An RC tag
`vX.Y.Z-rcN` and the corresponding final tag must refer to the reviewed commit.
The source archive name omits the RC number so promotion preserves the voted
bytes. The archive, checksum and signature actually approved by the community
must be promoted unchanged; a successful local build is not release approval.

## Documentation licenses

The Javadoc JAR contains complete Apache-2.0, UPL-1.0 and jQuery/jQuery UI
MIT license texts in `META-INF/LICENSE` and `legal/LICENSE`. Its generated
resource headers are retained; fonts and syntax highlighting are not included.
Run `python3 .github/scripts/verify_javadoc.py target/jcasbin-*-javadoc.jar`
after building. Review resource versions and the license template together
when changing the JDK. The main and sources JAR use the project LICENSE.
