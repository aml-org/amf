package amf.javaparser.org.raml.json_schema

import amf.core.remote.{Oas, Vendor}
import amf.javaparser.org.raml.{DirectoryTest, ModelResolutionTest}

/* this test parse a raml only with declared types, resolve them and serialize a json schema.*/

class TypeToJsonSchemaTest extends ModelResolutionTest with IgnorableModelTest {
//  override val basePath: String = path
//  override def path: String     = "amf-client/shared/src/test/resources/org/raml/json_schema"
  override val target: Vendor = Oas

  override def path: String = "amf-client/shared/src/test/resources/org/raml/json_schema"

  override def inputFileName: String = "input.raml"

  override def outputFileName: String = "output.json"

  override val basePath: String = path
}

trait IgnorableModelTest extends DirectoryTest {
  override protected def ignoreDir(d: String): Boolean = true
}
