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

### Excutable AMF client

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

Go to [amf examples repository](https://github.com/mulesoft/amf-byExample) There are examples for each one of the three usages and a *converter* project that add some UI on top of the library.

### Browser-client

It shows a static *html* file that imports the *amf-browser.js* artifact as a script and uses its globally exported classes.

#### Usage

Just open the *html* file in any browser and play with what's inside the script tag.

### NodeJS Client

This example has a *node.js* file **index.js** where the amf library will be imported and can be used as a node module.

#### Usage //@TODO check

1. Run *cd usage/jsClient* while positioned at the root of the project.
2. Run *npm install*.
3. Make sure that artifacts for amf have been generated (Run *sbt generate*).
4. Run *npm install ../../amf-js/*. (This will pick up configurations from *package.json* to install the *amf-module.js* file as a node module in the jsClient project.)
5. Run *node start*.
6. Open *localhost:3000* in the browser.

You can see *index.js* with examples of use and play with the module.
Modify *parser* and generator *modules* to change the server-side use of the library.

### JVM Client

This is a simple example that uses the **JVM jar artifact** in a gradle project main file.

#### Usage

1. Make sure that artifacts for amf have been generated (Run *sbt generate*), as the project will pick up the jar from the generated location.
2. Play with the library in java files. (There's a main class in src/main/java/ with some examples.)

### Converter

This is a node project that demonstrates how amf parses and generates an OAS/RAML document. Note that it's not a conversion tool per se as you can quickly see in the code. AMF will build the model every time, dumping the required spec output when selected.

#### Usage

1. Make sure that artifacts for amf have been generated (Run *sbt generate*).

2. Inside the *site* directory (*cd usage/site*):
    - Create directory *"build"*.
    - Run *npm install*.
    - Check if you have a *"public/build"* in the *site* directory. In not, create it.
    - Run *npm start* from *site* directory.
    - Open *localhost:3000* in the browser.

You can now start trying AMF by reading and dumping from/to different API Design specs.

## Validation

Validation is one of the key features of AMF. Please check the following link to get more information:
[Validation insights](./documentation/validation.md)

