package amf.javaparser.org.raml.api

import amf.core.remote.{Hint, Raml08YamlHint, Raml10YamlHint}
import amf.javaparser.org.raml.ModelResolutionTest

/** this suite parse the input api and runs validation. If its ok dump the model to json-ld, if not, dump the report to json? */
// resolution?
trait ApiModelParserTestCase extends ModelResolutionTest {
  override val basePath: String = path

  override def outputFileName: String = "model.json"

  override def inputFileName: String = "input.raml"
}

case class Raml10ApiModelParserTestCase() extends ApiModelParserTestCase {
  override def path: String = "amf-client/shared/src/test/resources/org/raml/api/v10"

  override def hint: Hint = Raml10YamlHint
}

case class Raml08ApiModelParserTestCase() extends ApiModelParserTestCase {
  override def path: String = "amf-client/shared/src/test/resources/org/raml/api/v08"

  override def hint: Hint = Raml08YamlHint
}
