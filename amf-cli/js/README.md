[![GitHub license](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://github.com/aml-org/amf/blob/master/LICENSE) [![Build Status](https://jenkins.build.msap.io/buildStatus/icon?job=application/AMF/amf/master)](https://jenkins.build.msap.io/job/application/job/AMF/job/amf/job/master/) [![Version](https://img.shields.io/github/v/release/aml-org/amf)](https://github.com/aml-org/amf/releases)

# AML Modeling Framework
AMF (AML Modeling Framework) is an open-source programming framework, capable of parsing, generating and validating metadata documents defined using [AML](https://a.ml/aml-spec). It can be used as a library in Scala, Java, or JavaScript projects. The modular design of AMF facilitates creating plugins capable of parsing other metadata syntaxes not defined by AML.


# 📃 Documentation
- [The AML Project](https://a.ml)
- [AMF Documentation website](https://a.ml/docs)
- [AMF model documentation](./documentation/model.md)
- [GitHub repository of AMF code examples](https://github.com/aml-org/examples)

# 📦 Artifacts
[![NPMJS](https://img.shields.io/npm/v/amf-client-js.svg)](https://www.npmjs.com/package/amf-client-js)
[![github releases](https://img.shields.io/github/v/release/aml-org/amf?label=nexus)](https://repository-master.mulesoft.org/nexus/content/repositories/releases/com/github/amlorg/amf-api-contract_2.12)


## Gradle
```groovy
// add mulesoft repository
repositories {
    maven {
        url 'https://repository-master.mulesoft.org/nexus/content/repositories/releases'
    }
}
dependencies {
    compile 'com.github.amlorg:amf-api-contract_2.12:x.y.z'
}
```

## Maven
```xml
<dependency>
    <groupId>com.github.amlorg</groupId>
    <artifactId>amf-api-contract_2.12</artifactId>
    <version>x.y.z</version>
</dependency>
```

NOTE: you may use the `-SNAPSHOT` versions of the artifacts at your own risk since those snapshot versions may contain breaking changes.

## JavaScript
```bash
$ npm install --save amf-client-js
```

## Generate artifacts directly from cloned repository

To build into a JVM jar:
```sh
sbt package
```
To build into a JS bundle:
```sh
sh js-build.sh
```

More info on how to add AMF to your project [here](https://a.ml/docs/amf/using-amf/amf_setup).


# AMF Native support

AMF natively supports the following formats:
- YAML
- JSON

the following semantic models:
- WebApi (or "Web APIs" as in "APIs accessible over the network")
- AsyncApi

and the following syntactic models:
- JSON-LD "AMF model"
- RAML 0.8 / 1.0 (mapped to "WebApi")
- OpenAPI (OAS) 2.0 / 3.0 (mapped to "WebApi")
- AsyncAPI 2.0 (mapped to "AsyncApi")

The models above and any other models may be extended and supported via custom [AML-defined models](https://a.ml/aml-spec). Other formats and models that cannot be expressed with AML may also be supported via plugins.

## Guaranteed output

The **only** guaranteed output of AMF is the JSON-LD "AMF model". Any other output such as any output provided natively by the models listed under the section above may change at any time. This means that while the semantic representation of those outputs may remain unchanged, the syntactical expression such as the order in which the outputted metadata is expressed and any other syntax-related constructs may change from one version of AMF to another. If this is an issue for your use-case, you may consider using a custom resolution/generation pipeline.

# AMF ecosystem modules
The following image shows each module in the AMF ecosystem as a dependency graph.

For AMF adopters it is recommended to use the `amf-api-contract` module which contains transitive dependencies with every
module in the ecosystem except the CLI. For AML adopters (with no Web API nor Custom validation features usage) it is recommended to
adopt the `amf-aml` module which includes parsing, validation & resolution for AML documents only. For more details on
AML visit the [AML repository]("https://github.com/aml-org/amf-aml").

![AMF ecosystem modules](./amf-ecosystem-modules.png)
The `amf-api-contract` and `amf-aml` are the recommended modules for AMF and AML adopters respectively.

## Contributing
If you are interested in contributing to this project, please make sure to read our [contributing guidelines](./CONTRIBUTING.md).