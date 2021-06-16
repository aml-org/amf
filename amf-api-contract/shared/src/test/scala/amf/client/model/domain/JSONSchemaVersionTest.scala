package amf.client.model.domain

import amf.shapes.internal.spec.common._
import org.scalatest.{FunSuite, Matchers}

class JSONSchemaVersionTest extends FunSuite with Matchers{

  test("JsonSchemaVersion comparison is correct by ordered list") {
    val versions = Seq(
      JSONSchemaUnspecifiedVersion,
      JSONSchemaDraft3SchemaVersion,
      JSONSchemaDraft4SchemaVersion,
      JSONSchemaDraft6SchemaVersion,
      JSONSchemaDraft7SchemaVersion,
      JSONSchemaDraft201909SchemaVersion
    )
    versions shouldBe sorted
  }

  test("JsonSchemaVersion comparison is correct by unordered list") {
    val versions = Seq(
      JSONSchemaDraft3SchemaVersion,
      JSONSchemaDraft201909SchemaVersion,
      JSONSchemaDraft7SchemaVersion,
      JSONSchemaDraft4SchemaVersion,
      JSONSchemaDraft6SchemaVersion,
      JSONSchemaUnspecifiedVersion,
    )
    versions should not be sorted
  }
}
