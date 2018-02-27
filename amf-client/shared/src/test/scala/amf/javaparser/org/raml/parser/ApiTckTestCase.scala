package amf.javaparser.org.raml.parser

import amf.javaparser.org.raml.ModelTest

class ApiTckTestCase extends ModelTest {
  override val basePath: String = path
  override def path: String     = "amf-client/shared/src/test/resources/org/raml/parser"

}