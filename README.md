# Bigraph Model Provider

> **Latest Version:** 1.1.0

The Bigraph Model Provider is a flexible library designed for native bigraphical applications that are based on so-called _world models_. 
This library offers a set of standardized interfaces for querying, managing and integrating bigraphical models, making it easy to connect with various data sources and sinks.

### Features
- Unified Interface for Bigraph Management: A simple and consistent interface for querying and managing bigraphical models.
- Custom Provider Support: Create custom providers by subclassing specific abstract classes or interfaces for specialized bigraph data handling needs.
- Data Source Integration: Attach providers to various data access technologies, including databases, web services, message queues, protocols, or other data storage and retrieval solutions.
- Composable Model Providers: Modular design is supported by combining smaller models into larger, complex structures. Finally, to create large _world models_.

## Overview

The Bigraph Model Provider library supports querying, and storing of bigraphical models.
Bigraphical models are expressive graph models, which can represent, for example, complex system designs or interactions of agent behavior. 
The generic, extensible class hierarchy of this library that allows users to integrate a wide range of data sources.

### Providers

Providers come in different flavors:
- Signatures (Syntax): They specify the types and constraints of a bigraphical model.
- Host Bigraphs (Data): Allow for the representation of individual bigraph instances.
- Rules (Behavior): Define constrained transformations applied to bigraphs.
- Composites: Some model providers can even be composed to construct larger models from smaller sub-models.

Technically,
all providers are signature providers since every bigraphical model
(e.g., a rule, or a host bigraph) is specified over a signature.

## Usage

**Maven Configuration**

```xml
<dependency>
    <groupId>org.bigraphs.model.provider</groupId>
    <artifactId>model-provider-core</artifactId>
    <version>${version}</version>
</dependency>
```

## Building from Source

Execute the following goals to run the build:
```shell
mvn clean install
```
The `*.jar` can be found inside the `./target/` folder of this project.
The dependency will be also installed in the local Maven repository and
can be used in other projects by following the instruction given [above](#Usage)

## Deployment

**Prerequisites**

The Sonatype account details (username + password) for the deployment must be provided to the
Maven Sonatype Plugin as used in the project's `pom.xml` file.

The Maven GPG plugin is used to sign the components for the deployment.
It relies on the gpg command being installed:
```shell
sudo apt install gnupg2
```

and the GPG credentials being available e.g. from `settings.xml` (see [here](https://central.sonatype.org/publish/publish-maven/)).
In `settings.xml` should be a profile and server configuration both with the `<id>ossrh</id>`.

- More information can be found [here](https://central.sonatype.org/publish/requirements/gpg/).
- Listing keys: `gpg --list-keys --keyid-format short`
- The `pom.xml` must also conform to the minimal requirements containing all relevant tags as required by Sonatype.

[//]: # (**Snapshot Deployment**)

[//]: # ()
[//]: # (Execute the following goals to deploy a SNAPSHOT release of the Java artifact to the snapshot repository:)

[//]: # ()
[//]: # (```shell)

[//]: # (# Use the default settings.xml located at ~/.m2/)

[//]: # (mvn clean deploy -P central -DskipTests)

[//]: # (# mvn clean deploy -P ossrh)

[//]: # (```)

[//]: # ()
[//]: # (- The version tag in the `pom.xml` must be suffixed with `-SNAPSHOT`.´)

**Release Deployment**

To perform a release deployment, execute:
```shell
mvn clean deploy -P release,central -DskipTests
# mvn clean deploy -P release,central
```
- The SNAPSHOT suffix must be removed from the version tag
- Artifacts must be manually released for Release Deployments in the Nexus Repository Manager.
