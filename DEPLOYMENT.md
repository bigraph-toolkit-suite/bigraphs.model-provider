# Deployment

**Prerequisites**

The Sonatype account details (username + password) for the deployment must be provided to the
Maven Sonatype Plugin as used in the project's `pom.xml` file.

The Maven GPG plugin is used to sign the components for the deployment.
It relies on the gpg command being installed:
```shell
$ sudo apt install gnupg2
```

and the GPG credentials being available e.g. from `settings.xml` (see [here](https://central.sonatype.org/publish/publish-maven/)).
In `settings.xml` should be a profile and server configuration both with the `<id>ossrh</id>`.

- More information can be found [here](https://central.sonatype.org/publish/requirements/gpg/).
- Listing keys: `gpg --list-keys --keyid-format short`
- The `pom.xml` must also conform to the minimal requirements containing all relevant tags as required by Sonatype.

**Release Deployment**

To perform a release deployment, execute:
```shell
$ mvn clean deploy -P release,central -DskipTests
# mvn clean deploy -P release,central
```
- The SNAPSHOT suffix must be removed from the version tag
- Artifacts must be manually released for Release Deployments in the Nexus Repository Manager.