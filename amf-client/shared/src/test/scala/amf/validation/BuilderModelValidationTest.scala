package amf.validation

import amf.core.AMFSerializer
import amf.core.emitter.RenderOptions
import amf.core.model.document.{Document, Module, PayloadFragment}
import amf.core.model.domain.ScalarNode
import amf.core.vocabulary.Namespace.Xsd
import amf.facades.Validation
import amf.io.FileAssertionTest
import amf.plugins.domain.shapes.models.{NodeShape, ScalarShape}
import amf.{RAMLProfile, RAMLStyle}
import org.scalatest.Matchers

import scala.concurrent.ExecutionContext

class BuilderModelValidationTest extends FileAssertionTest with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Test node shape with https id for js validation functions") {

    val module = Module().withId("https://remote.com/dec/")

    val nodeShape = NodeShape()

    module.withDeclaredElement(nodeShape)
    val shape = ScalarShape().withDataType((Xsd + "string").iri())
    nodeShape.withProperty("name").withRange(shape)

    val doc = Document().withId("file://mydocument.com/")
    doc.withDeclares(Seq(nodeShape))
    doc.withReferences(Seq(module))

    for {
      validation <- Validation(platform)
      report     <- validation.validate(module, RAMLProfile, RAMLStyle)
    } yield {
      report.conforms should be(true)
    }
  }

  test("Build scalar node and render") {
    val scalar   = ScalarNode("1", Some("http://a.ml/vocabularies/shapes#number")).withName("prop")
    val fragment = PayloadFragment(scalar, "application/yaml")

    for {
      _ <- Validation(platform) // in order to initialize
      s <- new AMFSerializer(fragment, "application/amf+yaml", "YAML Payload", RenderOptions()).renderToString
    } yield {
      s should be("1\n") // without cuotes
    }
  }
}
