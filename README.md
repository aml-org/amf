[![Build Status](https://jenkins.build.msap.io/buildStatus/icon?job=application/AMF/amf/master)](https://jenkins.build.msap.io/job/application/job/AMF/job/amf/job/master/)

# API Modeling Framework

This project aims to provide a common programming interface that lets developers interact with any API specification, whether it is written in OpenAPI Specification (OAS) or RAML, similar to the way the HTML DOM allows programmatic interaction with an HTML document.

## Vision

The API Modeling Framework (AMF) allows users to formally describe different kinds of APIs, parse and generate instances of those APIS as sets of modular documents and  store those connected descriptions into a single unified data graph.

![Overview](https://raw.githubusercontent.com/raml-org/api-modeling-framework/gh-pages/images/diagram.png)

## Goals

- Support for multiple languages with a unified output API/model for clients
- Support for both, document (RAML modularity) and domain (service clients), layers
- Bi-directional transformation
- Support for validation at document and service layers
- Produce a formal specification for the language
- Extensible, single document model for multiple domain vocabularies
- Consistent parsing behavior

## General scope
The library supports many of the required uses cases:
- Parse a 1.0 RAML, 2.0 OAS and JSON-LD AMF model.
- AMF API design model creation.
- Model edition.
- Export to any of the mentioned standards.

## Usage

To use AMF you should first generate or get the right distribution for your project and import them as dependencies.

## Installation

### Requirements
* Scala 2.12.2
* sbt 0.13.15
* Node

### Useful sbt commands

#### Test
* Tests on jvm and js
```sh
sbt test
```

#### Coverage reports
```sh
sbt coverage test coverageReport
```
### Generate artifacts directly from cloned repository

```sh
sbt package
```
This will generate *jvm* jars in each of the module's targets.

```sh
sbt buildJS
```
This will generate a *js* artifact in ./amf-client/js/amf.js

### JVM artifacts

To use, specify dependency.

Gradle example:

```groovy
dependencies {
    compile 'org.mulesoft:amf-client_2.12:1.0.0'
}
```

```groovy
repositories {
    ...
    maven {
            url 'https://repository-master.mulesoft.org/nexus/content/repositories/releases'
        }
    ...
}
```

Maven example:

```xml
<dependency>
    <groupId>org.mulesoft</groupId>
    <artifactId>amf-client_2.12</artifactId>
    <version>1.0.0</version>
</dependency>
```

### JS artifacts

##### Getting Started

Before you get started, you'll want to register with our private npm repository so you can download @mulesoft modules.

```
npm login --registry=https://nexus3.build.msap.io/repository/npm-internal/ --scope=@mulesoft
```

When prompted, enter your Mulesoft's jenkins username and password. You may then clone and install the project's dependencies.

If you have 2-factor authentication enabled, you'll get a 401 after attempting to login. Temporarily disable 2FA, login with your github credentials, then re-enable 2FA.

Once you are logged in on the scope @mulesoft just execute the command

Then:
```bash
npm install --save @mulesoft/amf-client-js@1.0.0
```

Using *Node.js* just import it using:
```bash
import amf from '@mulesoft/amf-client-js'
```

The *amf* package will contain all exported classes:
```javascript
amf.plugins.document.WebApi.register();
amf.plugins.document.Vocabularies.register();
amf.plugins.features.AMFValidation.register();

amf.Core.init().then(function () {
  // AMF code here
})
```

### Command line usage

You can build a standalone Java executable running the following SBT target:

```bash
sbt buildCommandLine
```
This will create a set of jars:
```bash
/amf/amf.jar
```
This will generate an executable jar at the top level directory that can be used to execute AMF from the command line.

Using this jar, you can run tasks from command line, for instance:
```bash
java -jar amf.jar parse -in "RAML 1.0" -mime-in "application/yaml" yourAPIfile
```
or 
```bash
java -jar amf.jar validate -in "RAML 1.0" -mime-in "application/yaml" yourAPIfile
```
To get all available options:
```bash
java -jar amf.jar
```

Using this jar you can execute AMF by passing one of the following commands:

- parse <input_file> -in FORMAT
- translate <input_file> <output_file> -in FORMAT_IN -out FORMAT_OUT
- validate <input_file> -in FORMAT_IN

An interactive section can be started using the `repl` command.

If you want to parse any RAML dialect other than RAML 1.0, you can pass a list of dialects to be loaded in the parser through the `dialects` option.

Refer to the usage of the application for additional commands and explanations.

## Examples

Go to [amf examples repository](https://github.com/mulesoft/amf-byExample) There are examples for each one of the three usages and a *converter* project that add some UI on top of the library.

## Validation

Validation is one of the key features of AMF. Please check the following link to get more information:
[Validation insights](./documentation/validation.md)

