package amf.javaparser.org.raml.v08.parser

import amf.core.remote.{Raml08, Vendor}
import amf.javaparser.org.raml.ModelResolutionTest

/** Original test in java parser: Calls raml builder, that runs all facets but return the Node tree,
  * for us is the same not build the result */
class Raml08BuilderTestCase extends ModelResolutionTest {
  override val basePath: String = path
  override def path: String     = "amf-client/shared/src/test/resources/org/raml/v08/parser"

  override val target: Vendor = Raml08

  override def inputFileName: String = "input.raml"

  override def outputFileName: String = "output.txt"

}
