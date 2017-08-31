[![Build Status](https://jenkins.build.msap.io/buildStatus/icon?job=application/AMF/amf/master)](https://jenkins.build.msap.io/job/application/job/AMF/job/amf/job/master/)

# API Modeling Framework

This project aims to provide a common programming interface that lets developers interact with any API specification, whether it is written in OpenAPI Specification (OAS) or RAML, in a similar way to how the HTML DOM allows programmatic interaction with an HTML document.

## Vision

The API Modeling Framework (AMF) allows users to formally describe different kind of APIs, parse and generate instances of those APIS as sets of modular documents and to store those connected descriptions into a single unified data graph.

![Overview](https://raw.githubusercontent.com/raml-org/api-modeling-framework/gh-pages/images/diagram.png)

## Status

AMF is under active development.
Artifacts have been pushed to private repositories. Can be built from the source code as well, as described bellow.
Changes to the current interfaces and vocabularies are to be expected, as well as a possible split of the library into smaller units.

## Goals

- Support for multiple languages with a unified output API/model for clients
- Support for both, document (RAML modularity) and domain (service clients), layers
- Bi-directional transformation
- Support for validation at document and service layers
- Produce a formal specification for the language
- Extensible, single document model for multiple domain vocabularies
- Consistent parsing behaviour

## Usage

To use AMF you should first generate or get the right distribution for your project and import them as dependencies.

### JVM artifacts (private repository)

To use, specify dependency. 

Gradle example:

```groovy
dependencies {
    compile 'org.mulesoft:amf_2.12:0.0.1-SNAPSHOT'
}
```

Maven example:

```xml
<dependency>
    <groupId>org.mulesoft</groupId>
    <artifactId>amf_2.12</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

#### Private repository registration

Add the mulesoft ci-snapshots repository and its credentials to the repositories.

Gradle example:

```groovy
maven {
        url 'https://repository-master.mulesoft.org/nexus/content/repositories/ci-snapshots'
        credentials {
            username = "username"
            password = "password"
        }
    }
```



### JS artifacts (private repository)

To use, import:

```javascript
import amf from '@mulesoft/amf-js'
```

The *amf* package will contain all exported classes:
```javascript
const parser = new amf.RamlParser()
```

#### Private repository registration

See Getting started: https://github.com/mulesoft/data-weave/blob/master/parser-js/dw-parser-js/README.md#getting-started

And then:

```bash
npm install --save @mulesoft/amf-js@latest
```


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
sbt generate
```
This will generate two *JS artifacts*:
- **Client**: JS file in amf-js/target/artifact/amf-browser.js that can be imported from a script tag to be used in client side.
- **Server**: JS file in amf-js/target/artifact/amf-module.js that has commonJS modules, and can be installed as a node module.

And two *JVM artifacts*:
- **Jar**: jar file in amf-jvm/target/artifact/amf.jar with the amf library.
- **Javadoc jar**: jar file in amf-jvm/target/artifact/amf-javadoc.jar with the docs of the project in Scaladoc format.

#### Using AMF in a node.js project

```bash
npm install --save amf-project-location/amf-js/
```

If you are using *Node.js* (server side) just import it using:
```javascript
import amf from '@mulesoft/amf-js'
```

The *amf* package will contain all exported classes:
```javascript
const parser = new amf.RamlParser()
```

### Using AMF from client browser

Just import the generated JS file in a script tag
```html
<script src="amf-browser.js"></script>
```

and use the exported classes as if they were global ones, for example:
```javascript
const parser = new RamlParser()
```

## Examples

Inside the *usage* folder we have an example for each of the three usages and a *converter* project to give the library some UI.

### Browser-client

It shows a static *html* file that imports the *amf-browser.js* artifact as a script and uses its globally exported classes.

#### Usage

Just open the *html* file in any browser and play with what's inside the script tag!

### NodeJS Client

This example has a *node.js* file **index.js** where amf library will be imported and can be used as a node module.

#### Usage

1. Run *cd usage/jsClient* while standing on the root of the project
2. Run *npm install*
3. Make sure that artifacts for amf have been generated (Run *sbt generate*)
4. Run *npm install ../../amf-js/* (This will pick up configurations from *package.json* to install the *amf-module.js* file as a node module in the jsClient project)
5. Run *node start*
6. Open *localhost:3000* in the browser

You can see *index.js* with examples of use and play with the module!
Modify *parser* and generator *modules* to change the use of the library server side.

### JVM Client

This is a simple example that uses the **JVM jar artifact** in a gradle projects' main file.

#### Usage

1. Make sure that artifacts for amf have been generated (Run *sbt generate*), as the project will pick up the jar from the generated location
2. Play with the library in java files! (There's a main class in src/main/java/ with some examples)

### Converter

This is a node project that demonstrates how amf parses and generates an OAS/RAML document.

#### Usage

1. Make sure that artifacts for amf have been generated (Run *sbt generate*)

2. Inside the *site* directory (*cd usage/site*):
    - Create directory *"build"*
    - Run *npm install*
    - Run *npm start*
    - Open *localhost:3000* in the browser

**Note**: if *npm start* fails, check if you have a *"public/build"* in the *site* directory.
