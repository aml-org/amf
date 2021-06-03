package amf.javaparser.org.raml.parser

import amf.javaparser.org.raml.ModelResolutionTest

class RamlBuilderTestCase extends ModelResolutionTest {

  override val basePath: String = path
  override def path: String     = "amf-client/shared/src/test/resources/org/raml/parser"

  override def inputFileName: String = "input.raml"

  override def outputFileName: String = "output.txt"

}
