package amf.validation

import amf.core.model.document.{Document, Module}
import amf.core.unsafe.PlatformSecrets
import amf.core.vocabulary.Namespace.Xsd
import amf.facades.Validation
import amf.plugins.domain.shapes.models.{NodeShape, ScalarShape}
import amf.{RAMLProfile, RAMLStyle}
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.ExecutionContext

class BuilderModelValidationTest extends AsyncFunSuite with PlatformSecrets with Matchers {

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
}
