package amf.validation

import amf.apicontract.client.common.ProvidedMediaType
import amf.apicontract.client.scala.{RAMLConfiguration, WebAPIConfiguration}
import amf.apicontract.internal.spec.payload.PayloadRenderPlugin
import amf.core.client.common.validation.Raml10Profile
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.model.document.{Document, Module, PayloadFragment}
import amf.core.client.scala.model.domain.ScalarNode
import amf.core.client.scala.validation.AMFValidator
import amf.core.client.scala.vocabulary.Namespace
import amf.core.client.scala.vocabulary.Namespace.Xsd
import amf.core.internal.render.AMFSerializer
import amf.io.FileAssertionTest
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}
import org.mulesoft.common.test.Diff
import org.mulesoft.common.test.Diff.makeString
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.ExecutionContext

class BuilderModelValidationTest extends AsyncFunSuite with FileAssertionTest with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val payloadRenderConfig = AMFGraphConfiguration.predefined().withPlugin(PayloadRenderPlugin)

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
      report <- AMFValidator.validate(module, Raml10Profile, RAMLConfiguration.RAML10())
    } yield {
      report.conforms should be(true)
    }
  }

  test("Build scalar node and render") {
    val scalar   = ScalarNode("1", Some("http://a.ml/vocabularies/shapes#number")).withName("prop")
    val fragment = PayloadFragment(scalar, "application/yaml")

    val s =
      new AMFSerializer(fragment, ProvidedMediaType.PayloadYaml, payloadRenderConfig.renderConfiguration).renderToString
    s should be("1\n") // without quotes
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
    val s =
      new AMFSerializer(m, "application/yaml", RAMLConfiguration.RAML10().renderConfiguration).renderToString
    val diffs = Diff.ignoreAllSpace.diff(s, e)
    if (diffs.nonEmpty) fail(s"\ndiff: \n\n${makeString(diffs)}")
    succeed
  }
}
