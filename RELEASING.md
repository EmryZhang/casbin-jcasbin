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

# Releasing JCasbin

Prepare all scoped changes in one preparation PR. Verify the actual merged
candidate, source/legal files and extracted build before creating an RC.
Keep Java 8 compatibility and use BUILDING.md for the build. Run release-helper
tests with `python3 -m unittest discover -s .github/scripts -p 'test_*.py'`.

## Identity and permissions

`org.apache.casbin:jcasbin` starts with 1.100.0; Java imports are unchanged.
Both `vX.Y.Z-rcN` and `vX.Y.Z` use `-Drevision=X.Y.Z`; the flattened POM must
contain that resolved version. Verify its disclaimer, Apache developer, license
and SCM together with embedded JAR metadata.

Confirm the actual ASF Nexus staging profile covers `org.apache.casbin`, its
project LDAP mapping and the release manager's deployment permission. ASF login
and old `org.casbin`/Central Portal permissions are not proof. If necessary,
request Infra to check the existing configuration before creating a duplicate.
Keep private settings outside the repository; server ID is
`apache.releases.https`. Signing/KEYS, ASF distribution and Nexus permissions
are separate. Never expose secrets in logs or commit them.

## Source and Maven review staging

Create an unused `vX.Y.Z-rcN` at the reviewed merged commit. The tag creates a
GitHub prerelease with tar.gz and ZIP source archives and SHA-512 sidecars.
Download/inspect/build the actual archives; sign and stage both formats with
their `.asc` and `.sha512` under the new ASF dev RC directory. Never overwrite
an existing candidate or reuse cancelled votes.

Build main, sources and Javadoc JARs plus the flattened POM from that exact
source with the checked entry point in BUILDING.md, using Java8 for Maven and
Temurin OpenJDK 25.0.4.1+1 only for Javadoc. Run without --skip-tests for the
full candidate verification. Check every artifact's legal files, metadata, source correspondence
and generated documentation resources. Record the actual JDK/doclet provenance
and applicable licensing; successful Javadoc generation is not legal clearance.
Named human review must cover these binaries before staging them for the vote.

The POM deploys to the ASF review staging endpoint using the ordinary Maven
Deploy Plugin. Sign the reviewed files separately and preserve their hashes.
With private settings and permissions, use the pinned `deploy:deploy-file`
goal to upload those existing main/sources/Javadoc JARs, flattened POM and
their signatures. Do not run the build lifecycle's `mvn deploy`: it can rebuild
the reviewed files or silently use ordinary Java8 documentation. This upload
is an authenticated release operation, not a test command. Confirm the
actual staging repository created for the project, inspect its contents, then
close it in Nexus for review. Deploy does not close or promote this repository.
Do not override the staging URL. Do not upload SHA-256/SHA-512 sidecars to
Nexus; retain SHA-512 separately in the evidence manifest.

The Maven upload consists of the existing main JAR as `file`, the existing
flattened POM as `pomFile`, and six attached files. For Deploy Plugin 3.2.0,
the attachments are main JAR signature, sources JAR, sources JAR signature,
Javadoc JAR, Javadoc JAR signature and POM signature, with matching classifiers
`,sources,sources,javadoc,javadoc,` and types
`jar.asc,jar,jar.asc,jar,jar.asc,pom.asc`. Use the absolute reviewed paths and
the same `apache.releases.https` server ID and ASF staging endpoint in the POM.
Verify all eight uploaded originals and their signatures after closing; inspect
the real remote state before any retry. Do not generate a replacement POM.

Inspect the real closed repository ID and URL. Download its JARs/POM/signatures
independently, verifying hashes, public KEYS, GAV, legal material and source
correspondence. Closing makes artifacts available for review; it does not
release them to Central. Inspect partial remote state before any retry.

## Votes and publication

The ballots include exact source/tag/commit and Maven staging links, KEYS,
hashes and test scope. Sequential general@ requests link the dev ballot and
result; concurrent requests link the active dev ballot and say endorsement is
pending. Count PPMC and IPMC votes separately and start fresh ballots for a
replacement RC. Only publish after both required approvals.

Promote the voted source originals to ASF release distribution, and promote
the reviewed closed Maven repository separately. Never rebuild the Maven
artifacts after voting. Wait for public distribution and verify all outputs.
The final `vX.Y.Z` tag must identify the reviewed RC commit; its workflow reuses
and verifies the promoted ASF source originals for GitHub Release. It performs
no Maven rebuild/deploy. The old automatic Central publishing/retry workflow
is removed so this route cannot publish a second set of artifacts.

Before promotion/retries, verify exact approval scope, repository state and
existing GAV; do not overwrite a release or drop another person's repository.
Complete public display, consumer resolution and authorized announcement.

References: [ASF Maven guide](https://infra.apache.org/publishing-maven-artifacts.html),
[ASF release policy](https://www.apache.org/legal/release-policy.html),
[Incubation Policy](https://incubator.apache.org/policy/incubation.html#releases).
