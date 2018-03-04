package amf.javaparser.org.raml.parser

import amf.core.remote.{Raml10, Vendor}
import amf.javaparser.org.raml.ModelResolutionTest

/** test in java parser build the tree and then calls the tck emitter.Thats serialize the model to a json, with doc and errors.
  * For us, we just can emit the raml or the errors.
  */
class ApiTckTestCase extends ModelResolutionTest {
  override val basePath: String = path
  override def path: String     = "amf-client/shared/src/test/resources/org/raml/parser"

  override val target: Vendor = Raml10

  override def inputFileName: String = "input.raml"

  override def outputFileName: String = "api-tck.json"
}