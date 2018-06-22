package amf.javaparser.org.raml.api

import amf.javaparser.org.raml.ModelResolutionTest

/** this suite parse the input api and runs validation. If its ok dump the model to json-ld, if not, dump the report to json? */
// resolution?
class ApiModelParserTestCase extends ModelResolutionTest {
  override val basePath: String = path
  override def path: String     = "amf-client/shared/src/test/resources/org/raml/api"

  override def outputFileName: String = "model.json"

  override def inputFileName: String = "input.raml"
}