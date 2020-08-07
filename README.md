[![Build Status](https://jenkins.build.msap.io/buildStatus/icon?job=application/AMF/amf/master)](https://jenkins.build.msap.io/job/application/job/AMF/job/amf/job/master/)

# AML Modeling Framework

This project aims to provide a common programming interface that lets developers interact with any API specification, whether it is written in OpenAPI Specification (OAS) or RAML, similar to the way the HTML DOM allows programmatic interaction with an HTML document.

## Vision

The API Modeling Framework (AMF) allows users to formally describe different kinds of APIs, parse and generate instances of those APIS as sets of modular documents and  store those connected descriptions into a single unified data graph.

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
- Parse a 0.8 / 1.0 RAML, 2.0 / 3.0 OAS / ASYNC 2.0 and JSON-LD AMF model.
- AMF API design model creation.
- Model edition.
- Export to any of the mentioned standards.

## Usage

To use AMF you should first generate or get the right distribution for your project and import them as dependencies.

## Installation

### Download JVM artifacts

Gradle snippet:

```groovy
// add mulesoft repository
repositories {
    maven {
        url 'https://repository-master.mulesoft.org/nexus/content/repositories/releases'
    }
}
dependencies {
    compile 'com.github.amlorg:amf-client_2.12:x.y.z'
}
```

Maven snippet:

```xml
<dependency>
    <groupId>com.github.amlorg</groupId>
    <artifactId>amf-client_2.12</artifactId>
    <version>x.y.z</version>
</dependency>
```

NOTE: `-SNAPSHOT` versions of the JVM artifacts are available but may contain breaking changes.

### Download JS artifacts

NPM:
```bash
$ npm install --save amf-client-js
```

Yarn:
```bash
$ yarn add --save amf-client-js
```

### To generate artifacts directly from cloned repository

To build into a JVM jar:
```sh
sbt package
```
To build into a JS bundle:
```sh
sbt buildJS
```

### Usage

To use AMF you must first initialize it

With Node.js: 
```javascript
const amf = require('./lib/amf-client-module.js')

await AMF.init();

// ... your code
```

With Java:
```java
import amf.client.AMF;

class App {
    public static void main(String[] args){
      AMF.init().thenApply(() -> {
              // ... your code
      });
    }
}
```

### Command line usage

You can build a standalone Java executable (JAR) running the following SBT target:
```bash
sbt buildCommandLine
```

This will generate an executable JAR at the top level directory that can be used to execute AMF from the command line.

Using this JAR, you can run tasks from command line, for instance:
```bash
$ java -jar amf-x.y.z.jar parse -in "RAML 1.0" -mime-in "application/yaml" yourAPIfile
```
or 
```bash
$ java -jar amf-x.y.z.jar validate -in "RAML 1.0" -mime-in "application/yaml" -p "RAML" yourAPIfile
```
or
```bash
$ java -jar amf-x.y.z.jar translate  yourAPIOASfile --format-in "OAS 3.0" -mime-in "application/json" --format-out "RAML 1.0" -mime-out "application/raml+yaml"
```
To get all available options:
```bash
$ java -jar amf-x.y.z.jar
```

Using this JAR you can execute AMF by passing one of the following commands:

- parse <input_file> -in FORMAT
- translate <input_file> <output_file> -in FORMAT_IN -out FORMAT_OUT
- validate <input_file> -in FORMAT_IN -p VALIDATION_PROFILE

An interactive section can be started using the `repl` command.

If you want to parse any RAML dialect other than RAML 1.0, you can pass a list of dialects to be loaded in the parser through the `dialects` option.

Refer to the usage of the application for additional commands and explanations.

## Examples

Go to [amf examples repository](https://github.com/mulesoft/amf-examples) There are examples for each one of the three usages and a *converter* project that add some UI on top of the library.

## Validation

Validation is one of the key features of AMF. Please check the following link to get more information:

[Validation insights](./documentation/validation.md)

## AML Vocabulary

The AML Vocabulary that could be found in this repository under the **vocabularies** directory has been migrated to the [amf metadata repository](https://github.com/aml-org/amf-metadata)

## Want to learn more?
[Click here for more documentation and playground](https://a.ml)

## Want to contribute?
If you are interested in contributing code to this project, thanks! Please [read and accept the Contributors Agreement](https://api-notebook.anypoint.mulesoft.com/notebooks#380297ed0e474010ff43). This should automatically create a Github issue with the record of your signature [here](https://github.com/mulesoft/contributor-agreements/issues). If for any reason, you do not see your signature there, please contact us.
