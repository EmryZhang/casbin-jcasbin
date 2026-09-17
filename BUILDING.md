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

Use a JDK 8 installation and Maven 3.9.x. Set `JAVA_HOME` to the JDK and
place Maven on `PATH`. Dependencies are downloaded from Maven repositories.

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

The source tree carries the placeholder version `0.0.0`; the real version is
supplied by the release tag. To build artifacts with a specific version, pass
`-Drevision=X.Y.Z` (see [RELEASING.md](RELEASING.md)). An RC tag
`vX.Y.Z-rcN` and the corresponding final tag must refer to the reviewed commit.
The source archive name omits the RC number so promotion preserves the voted
bytes. The archive, checksum and signature actually approved by the community
must be promoted unchanged; a successful local build is not release approval.

Javadoc additionally requires JDK 25; see the documentation build and
license verification instructions in [RELEASING.md](RELEASING.md).
