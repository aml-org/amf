Please [read and accept the Contributors Agreement](https://api-notebook.anypoint.mulesoft.com/notebooks#380297ed0e474010ff43). This should automatically create a Github issue with the record of your signature [here](https://github.com/mulesoft/contributor-agreements/issues). If for any reason, you do not see your signature there, please contact us.

## Development Requirements
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
#### Generate artifacts (from cloned repository)

```sh
sbt package
```
This will generate *jvm* JARs in each of the module's targets.

```sh
sbt buildJS
```
This will generate a *js* artifact in ./file://amf-client/js/amf.js