package amf.shapes.client.jsonldschema.cycle

import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.annotations.SourceYPart
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.utils.RangeOps
import amf.core.io.FileAssertionTest
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration, JsonLDSchemaConfigurationClient}
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDObject
import org.mulesoft.common.client.lexical.PositionRange
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.Future

class JsonLDSchemaInstanceCycleWithSourceMapsTest extends AsyncFunSuite with FileAssertionTest {

  private val basePath: String = "amf-shapes/shared/src/test/resources"
  val testPath                 = s"$basePath"

  val client: JsonLDSchemaConfigurationClient =
    JsonLDSchemaConfiguration
      .JsonLDSchema()
      .withRenderOptions(RenderOptions().withPrettyPrint.withSourceMaps)
      .baseUnitClient()

  def assertAnnotationRange(actual: Any, expectedRange: PositionRange): Assertion = {
    val annotation = actual.asInstanceOf[Annotations].lexical()
    assertRange(annotation.range, expectedRange)
  }

  test("Test properties") {
    val instanceLocation = s"file://$testPath/sourcemaps-annotations/instance.json"
    val schemaLocation   = s"file://$testPath/sourcemaps-annotations/schema.json"
    val asserPropertyFunction: (JsonLDObject, String, PositionRange, PositionRange) => Assertion =
      assertPropertyAnnotation(instanceLocation)
    parseJsonLSDSchemaAndInstance(schemaLocation, instanceLocation).flatMap(rootObject => {
      // annotations del instance
      assertAnnotations(instanceLocation, PositionRange((1, 0), (7, 1)), rootObject.annotations)

      asserPropertyFunction(
        rootObject,
        "http://a.ml/vocabularies/core#prop1",
        PositionRange((2, 2), (2, 20)),
        PositionRange((2, 11), (2, 20))
      )
      asserPropertyFunction(
        rootObject,
        "http://a.ml/vocabularies/core#prop-array",
        PositionRange((3, 2), (3, 29)),
        PositionRange((3, 16), (3, 29))
      )
      asserPropertyFunction(
        rootObject,
        "http://a.ml/vocabularies/core#prop-obj",
        PositionRange((4, 2), (6, 3)),
        PositionRange((4, 14), (6, 3))
      )

      // annotations de array
      val propArrayValue = rootObject.fields.getValueAsOption("http://a.ml/vocabularies/core#prop-array").head
      val array          = propArrayValue.value.asInstanceOf[AmfArray]
      assertRange(array.values(0).annotations.lexical().range, PositionRange((3, 17), (3, 22)))
      assertRange(array.values(1).annotations.lexical().range, PositionRange((3, 23), (3, 28)))

      //        Object Annotation
      val propertyObject = rootObject.fields.getValueAsOption("http://a.ml/vocabularies/core#prop-obj").head
      asserPropertyFunction(
        propertyObject.value.asInstanceOf[JsonLDObject],
        "http://a.ml/vocabularies/core#key",
        PositionRange((5, 4), (5, 12)),
        PositionRange((5, 11), (5, 12))
      )

    })
  }

  private def assertRange(actual: PositionRange, expected: PositionRange) = {
    assert(actual.start.line == expected.start.line)
    assert(actual.start.column == expected.start.column)
    assert(actual.end.line == expected.end.line)
    assert(actual.end.column == expected.end.column)
  }

  def assertPropertyAnnotation(
      location: String
  )(obj: JsonLDObject, uri: String, propertyRange: PositionRange, valueRange: PositionRange): Assertion = {
    val property = obj.fields.getValueAsOption(uri).head
    assertAnnotations(location, propertyRange, property.annotations)
    assertAnnotations(location, valueRange, property.value.annotations)
  }

  test("Test properties clash") {
    val schemaPath   = s"file://$testPath/sourcemaps-clash-annotations/schema-term-with-same-iri-as-prop.json"
    val instancePath = s"file://$testPath/sourcemaps-clash-annotations/instance-term-with-same-iri-as-prop.json"
    parseJsonLSDSchemaAndInstance(schemaPath, instancePath).flatMap(rootObject => {
      val propertyField = rootObject.fields.getValueAsOption("http://a.ml/vocabularies/core#prop2").get
      assert(propertyField.isInferred)
      assert(propertyField.value.annotations.isVirtual)
    })
  }

  def parseJsonLSDSchemaAndInstance(schemaPath: String, instancePath: String): Future[JsonLDObject] = {
    client
      .parseJsonLDSchema(schemaPath)
      .map(_.jsonDocument)
      .flatMap(schema => {
        client
          .parseJsonLDInstance(instancePath, schema)
          .map(result => {
            result.baseUnit.asInstanceOf[JsonLDInstanceDocument].encodes.head.asInstanceOf[JsonLDObject]

          })
      })
  }

  private def assertAnnotations(location: String, propertyRange: PositionRange, annotations: Annotations) = {
    val sourceLocation = annotations.sourceLocation
    assertRange(sourceLocation.range, propertyRange)
    assert(sourceLocation.sourceName == location)
    assert(annotations.find(classOf[SourceYPart]).isDefined)
  }
}
