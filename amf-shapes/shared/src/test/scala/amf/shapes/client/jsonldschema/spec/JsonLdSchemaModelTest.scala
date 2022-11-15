package amf.shapes.client.jsonldschema.spec

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.metamodel.{Field, Type}
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient}
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDObject
import org.scalatest.{Assertion, Succeeded}
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.Checkpoints.Checkpoint
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class JsonLdSchemaModelTest extends AsyncFunSuite with Matchers {

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath: String = "amf-shapes/shared/src/test/resources/jsonld-schema"
  private val renderOptions    = RenderOptions().withPrettyPrint
  private val client: JsonLDSchemaConfigurationClient =
    JsonLDSchemaConfiguration.JsonLDSchema().withRenderOptions(renderOptions).baseUnitClient()

  test("Compacted array inner type is computed for instance") {
    val expected = Map(
      "stringArray" -> Type.Array(Type.Str),
      "intArray"    -> Type.Array(Type.Int),
      "boolArray"   -> Type.Array(Type.Bool),
      "anyArray"    -> Type.Array(Type.Any),
      "anyArray2"   -> Type.Array(Type.Any)
    )
    val assertion = (doc: JsonLDInstanceDocument) => {
      val cp  = new Checkpoint()
      val obj = doc.encodes.head.asInstanceOf[JsonLDObject]

      obj.fields.fieldsMeta().foreach { field =>
        val property     = extractPropertyName(field)
        val expectedType = expected(property)
        cp { field.`type` should be(expectedType) }
      }
      cp.reportAll()
      Succeeded // shouldn't reach this point if it has errors, API isn't the best
    }
    run("compacted-array-values.json", "compacted-array-values.json", assertion)
  }

  // we can do this because we don't define semantics in the schema
  private def extractPropertyName(field: Field) = field.value.iri().split("#").last

  private def run(
      schemaPath: String,
      instancePath: String,
      assertion: JsonLDInstanceDocument => Assertion
  ): Future[Assertion] = {
    for {
      schema   <- client.parseJsonLDSchema(path(s"$basePath/schemas/model", schemaPath)).map(_.jsonDocument)
      instance <- client.parseJsonLDInstance(path(s"$basePath/instances/model", instancePath), schema).map(_.instance)
    } yield (assertion(instance))
  }

  private def path(base: String, rest: String): String = s"file://${base}/$rest"
}
