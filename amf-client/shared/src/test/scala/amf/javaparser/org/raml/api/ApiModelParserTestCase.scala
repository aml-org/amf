package amf.javaparser.org.raml.api

import amf.javaparser.org.raml.ModelTest

class ApiModelParserTestCase extends ModelTest {
  override val basePath: String = path
  override def path: String     = "amf-client/shared/src/test/resources/org/raml/api"
}