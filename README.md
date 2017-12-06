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

### Comand line usage

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

### JVM artifacts

To use, specify dependency.

Gradle example:

```groovy
dependencies {
    compile 'org.mulesoft:amf-client_2.12:X_X_X'
}
```

```groovy
repositories {
    ...
    maven {
            url 'https://nexus.build.msap.io/nexus/content/repositories/releases'
            @TODO CHECK
            credentials {
                username = "username"
                password = "password"
            }
        }
    ...
}
```

Maven example:

```xml
<dependency>
    <groupId>org.mulesoft</groupId>
    <artifactId>amf-client_2.12</artifactId>
    <version>X_X_X</version>
</dependency>
```

### JS artifacts

To use, import:

```bash
import amf from '@mulesoft/amf-js'
```

The *amf* package will contain all exported classes:
```javascript
const parser = new amf.RamlParser()
```

### Executable AMF client

The client can be built using `buildCommandLine` SBT task

```bash
sbt buildCommandLine
```
This will generate an executable jar at the top level directory that can be used to execute AMF from the command line.

## Installation

### Requirements
* Scala 2.12.2
* sbt 0.13.15

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
This will generate two *JS artifacts*:
- **Client**: JS file in amf-js/target/artifact/amf-browser.js that can be imported from a script tag to be used on the client side.
- **Server**: JS file in amf-js/target/artifact/amf-module.js that has commonJS modules, and can be installed as a node module.

And two *JVM artifacts*:
- **Jar**: jar file in amf-jvm/target/artifact/amf.jar with the amf library.
- **Javadoc jar**: jar file in amf-jvm/target/artifact/amf-javadoc.jar with the docs of the project in Scaladoc format.

#### Using AMF in a node.js project

##### Private repository registration
See Getting started: https://github.com/mulesoft/data-weave/blob/master/parser-js/dw-parser-js/README.md#getting-started

Then: 
```bash
npm install --save amf-project-location/amf-client-js/
```

If you are using *Node.js* (server side) just import it using:
```bash
import amf from '@mulesoft/amf-client-js'
```

The *amf* package will contain all exported classes:
```javascript
amf.AMF.init()
const parser = amf.AMF.raml10Parser()
```

## Examples

Check [amf examples repository](https://github.com/mulesoft/amf-examples). You will find code for Java and JS along with a converter application that uses AMF.

## Validation

Validation is one of the key features of AMF. Please check the following link to get more information:
[Validation insights](./documentation/validation.md)

