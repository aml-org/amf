package amf.validation

import amf.apicontract.client.scala.RAMLConfiguration
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.model.document.{Document, Module, PayloadFragment}
import amf.core.client.scala.model.domain.ScalarNode
import amf.core.client.scala.validation.AMFValidator
import amf.core.client.scala.vocabulary.Namespace
import amf.core.client.scala.vocabulary.Namespace.Xsd
import amf.core.internal.remote.Mimes._
import amf.core.internal.render.AMFSerializer
import amf.core.io.FileAssertionTest
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}
import amf.shapes.internal.spec.payload.PayloadRenderPlugin
import org.mulesoft.common.test.Diff
import org.mulesoft.common.test.Diff.makeString
import org.scalatest.matchers.should.Matchers

class BuilderModelValidationTest extends FileAssertionTest with Matchers {

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
      report <- AMFValidator.validate(module, RAMLConfiguration.RAML10())
    } yield {
      report.conforms should be(true)
    }
  }

  test("Build scalar node and render") {
    val scalar   = ScalarNode("1", Some("http://a.ml/vocabularies/shapes#number")).withName("prop")
    val fragment = PayloadFragment(scalar, `application/yaml`)

    val s =
      new AMFSerializer(fragment, payloadRenderConfig.renderConfiguration, Some(`application/yaml`)).renderToString
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
      new AMFSerializer(m, RAMLConfiguration.RAML10().renderConfiguration, None).renderToString
    val diffs = Diff.ignoreAllSpace.diff(s, e)
    if (diffs.nonEmpty) fail(s"\ndiff: \n\n${makeString(diffs)}")
    succeed
  }
}
