package amf.validation

import amf.Raml10Profile
import amf.client.environment.{RAMLConfiguration, WebAPIConfiguration}
import amf.client.remod.AMFGraphConfiguration
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.core.AMFSerializer
import amf.core.model.document.{Document, Module, PayloadFragment}
import amf.core.model.domain.ScalarNode
import amf.core.vocabulary.Namespace
import amf.core.vocabulary.Namespace.Xsd
import amf.facades.Validation
import amf.io.FileAssertionTest
import amf.plugins.domain.shapes.models.{NodeShape, ScalarShape}
import org.mulesoft.common.test.Diff
import org.mulesoft.common.test.Diff.makeString
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.ExecutionContext

class BuilderModelValidationTest extends AsyncFunSuite with FileAssertionTest with Matchers {

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
      report <- validation.validate(module,
                                    Raml10Profile,
                                    new ValidationConfiguration(AMFGraphConfiguration.predefined()))
    } yield {
      report.conforms should be(true)
    }
  }

  test("Build scalar node and render") {
    val scalar   = ScalarNode("1", Some("http://a.ml/vocabularies/shapes#number")).withName("prop")
    val fragment = PayloadFragment(scalar, "application/yaml")

    for {
      _ <- Validation(platform) // in order to initialize
      s <- new AMFSerializer(fragment, "application/amf+yaml", WebAPIConfiguration.WebAPI().renderConfiguration).renderToString
    } yield {
      s should be("1\n") // without cuotes
    }
  }

  test("Build number type with format") {
    val scalar = ScalarShape().withName("myType").withDataType((Namespace.Shapes + "number").iri()).withFormat("int")
    val m      = Module().withDeclaredElement(scalar)

    val e =
      """
        |#%RAML 1.0 Library
        |types:
        | myType:
        |   type: number
        |   format: int""".stripMargin
    for {
      _ <- Validation(platform) // in order to initialize
      s <- new AMFSerializer(m, "application/raml+yaml", RAMLConfiguration.RAML().renderConfiguration).renderToString
    } yield {
      val diffs = Diff.ignoreAllSpace.diff(s, e)
      if (diffs.nonEmpty) fail(s"\ndiff: \n\n${makeString(diffs)}")
      succeed
    }
  }
}
