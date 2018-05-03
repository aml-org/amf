package amf.complete

import amf.javaparser.org.raml.ModelResolutionTest

class ConnectExampleTestCase extends ModelResolutionTest {

  override def path: String = "amf-client/shared/src/test/resources/org/oas/connect"

  override def inputFileName: String = "input.json"

  override def outputFileName: String = "output.json"

  override val basePath: String = path

}
