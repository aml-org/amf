[![Build Status](https://travis-ci.org/aml-org/amf.svg?branch=develop)](https://travis-ci.org/aml-org/amf)

# AML Modeling Framework

AMF (AML Modeling Framework) is an open-source programming framework, capable of parsing, generating and validating metadata documents defined using [AML](https://a.ml/aml-spec). It can be used as a library in Java or JavaScript projects, or as a stand-alone command-line tool. The modular design of AMF facilitates creating plugins capable of parsing other metadata syntaxes not defined by AML.

## Vision

[AML](https://a.ml/aml-spec) allows users to formally describe different kinds of models, whether syntactic models (i.e. language/specification specific) or semantic models (i.e. domain/industry specific). AMF can parse and generate descriptions of those models as sets of modular documents and store those connected descriptions into a single unified data graph.

## Goals

- Support for multiple formats with a unified programming interface and model
- Support for both syntactic and semantic model layers
- Support for validation at both syntactic and semantic model layers
- Extensible, single syntactic model for multiple semantic models
- Ability to create consistent parsing behaviors across different syntactic models
- Support for bi-directional transformation and export
- Support for custom (parsing/resolution/generation) pipelines

## Native support

AMF natively supports the following formats:
- YAML
- JSON

the following semantic models:
- WebApi (or "Web APIs" as in "APIs accessible over the network")

and the following syntactic models:
- JSON-LD "AMF model"
- RAML 0.8 / 1.0 (mapped to "WebApi")
- OpenAPI (OAS) 2.0 / 3.0 (mapped to "WebApi")
- AsyncAPI 2.0 (beta) (mapped to "WebApi")

The models above and any other models may be extended and supported via custom [AML-defined models](https://a.ml/aml-spec). Other formats and models that cannot be expressed with AML may also be supported via plugins. 

## Guaranteed output

The **only** guaranteed output of AMF is the JSON-LD "AMF model". Any other output such as any output provided natively by the models listed under the section above may change at any time. This means that while the semantic representation of those outputs may remain unchanged, the syntactical expression such as the order in which the outputted metadata is expressed and any other syntax-related constructs may change from one version of AMF to another. If this is an issue for your use-case, you may consider using a custom resolution/generation pipeline.

## Documentation
- [The AML Project](https://a.ml)
- [What is AMF?](https://a.ml/docbook/overview_amf.html)
- [AMF model documentation](documentation/model.md)
- [Validation insights](./documentation/validation.md)
- [Basic use cases - parsing & validating an API](documentation/basic_use_cases.md)
- [Code examples](https://github.com/aml-org/examples)
- [More code examples](https://github.com/mulesoft/amf-examples)

## Usage

### Java artifacts

To use, specify dependency.

Gradle example:

```groovy
dependencies {
    compile 'com.github.amlorg:amf-client_2.12:x.y.z'
}
```

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

NOTE: you may use the `-SNAPSHOT` versions of the artifacts at your own risk since those snapshot versions may contain breaking changes.

### JavaScript artifacts

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
$ sbt buildCommandLine
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

If you want to parse any AML dialect other than RAML 1.0, you can pass a list of dialects to be loaded in the parser through the `dialects` option.

Refer to the usage of the application for additional commands and explanations.

## Contributing
If you are interested in contributing to this project, please make sure to read our [contributing guidelines](./CONTRIBUTING.md).
