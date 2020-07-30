package amf.parser

import amf.core.model.document.BaseUnit
import amf.core.parser._
import amf.core.rdf.RdfModel
import amf.core.services.ValidationOptions
import amf.core.validation.core.{SHACLValidator, ValidationReport, ValidationSpecification}
import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.yaml.model._
import org.yaml.parser.YamlParser

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class ParserTest extends FunSuite {

  private val `RAML/yaml`: String =
    """#%RAML 1.0
      |a: 1
      |b: !include include1.yaml
      |c:
      |  - 2
      |  - 3
      |d: !include include2.yaml""".stripMargin

  private val `OAS/json`: String =
    """{
      |  "a": 1,
      |  "b": {
      |    "$ref": "include1.json"
      |  },
      |  "c": [
      |    2,
      |    3
      |  ],
      |  "d": {
      |    "$ref": "include2.json"
      |  }
      |}""".stripMargin

  private val `OAS/yaml`: String =
    """a: 1
      |b:
      |  $ref: include1.yaml
      |c:
      |  - 2
      |  - 3
      |d:
      |  $ref: include2.yaml""".stripMargin

  test("Test RAML/yaml") {
    val doc = YamlParser(`RAML/yaml`).document()
    doc.children.size shouldBe 2

    doc.headComment should be("%RAML 1.0")

    val nodeValue = doc.node.value
    nodeValue shouldNot be(YNode.Null)
    nodeValue shouldBe a[YMap]

    assertDocumentRoot(nodeValue.asInstanceOf[YMap], assertRamlInclude)
  }

  test("Test OAS/json") {
    val document = YamlParser(`OAS/json`).document()
    document.children.size shouldBe 1

    val nodeValue = document.node.value
    nodeValue shouldNot be(YNode.Null)
    nodeValue shouldBe a[YMap]

    assertDocumentRoot(nodeValue.asInstanceOf[YMap], assertOasInclude)
  }

  test("Test OAS/yaml") {
    val document = YamlParser(`OAS/yaml`).document()
    document.children.size shouldBe 1

    document.node.value shouldNot be(YNode.Null)
    document.node.value shouldBe a[YMap]

    assertDocumentRoot(document.node.value.asInstanceOf[YMap], assertOasInclude)
  }

  private def assertRamlInclude(entry: YMapEntry) = {
    entry.key.value shouldBe a[YScalar]
    Some(entry.key.value.asInstanceOf[YScalar].text) should contain oneOf ("b", "d")

    entry.value.value shouldBe a[YScalar]
    //todo parser: missing property for tag!
    entry.value.as[YScalar].text should startWith("include")
  }

  private def assertOasInclude(entry: YMapEntry) = {
    entry.key.value shouldBe a[YScalar]
    Some(entry.key.value.asInstanceOf[YScalar].text) should contain oneOf ("b", "d")

    entry.value.value shouldBe a[YMap]
    val include = entry.value.value.asInstanceOf[YMap].entries.head
    include.key.value shouldBe a[YScalar]
    include.key.value.asInstanceOf[YScalar].text shouldBe "$ref"
    include.value.value shouldBe a[YScalar]
    include.value.value.asInstanceOf[YScalar].text should startWith("include")
  }

  private def assertDocumentRoot(content: YMap, include: YMapEntry => Unit) = {
    content.entries.size should be(4)

    val first = content.entries(0)
    first.key.value shouldBe a[YScalar]
    first.key.value.asInstanceOf[YScalar].text shouldBe "a"
    first.value.value shouldBe a[YScalar]
    first.value.value.asInstanceOf[YScalar].text shouldBe "1"

    include(content.entries(1))

    val third = content.entries(2)
    third.key.value shouldBe a[YScalar]
    third.key.as[String] shouldBe "c"
    third.value.value shouldBe a[YSequence]

    val sequence = third.value.value.asInstanceOf[YSequence]
    sequence.nodes.size should be(2)
    sequence.nodes.head.value shouldBe a[YScalar]
    sequence.nodes.head.as[Int] shouldBe 2
    sequence.nodes(1).value shouldBe a[YScalar]
    sequence.nodes(1).as[Int] shouldBe 3

    include(content.entries(3))

    class TestValidator extends SHACLValidator {
      override def validate(data: String, dataMediaType: String, shapes: String, shapesMediaType: String)(
          implicit executionContext: ExecutionContext): Future[String] =
        throw new Exception("Validation not supported")

      override def report(data: String, dataMediaType: String, shapes: String, shapesMediaType: String)(
          implicit executionContext: ExecutionContext): Future[ValidationReport] =
        throw new Exception("Validation not supported")

      override def registerLibrary(url: String, code: String): Unit = throw new Exception("Validation not supported")

      override def validate(data: BaseUnit, shapes: Seq[ValidationSpecification], options: ValidationOptions)(
          implicit executionContext: ExecutionContext): Future[String] =
        throw new Exception("Validation not supported")

      override def report(data: BaseUnit, shapes: Seq[ValidationSpecification], options: ValidationOptions)(
          implicit executionContext: ExecutionContext): Future[ValidationReport] =
        throw new Exception("Validation not supported")

      override def emptyRdfModel(): RdfModel = throw new Exception("Validation not supported")

      override def shapes(shapes: Seq[ValidationSpecification], functionsUrl: String): RdfModel =
        throw new Exception("Validation not supported")

      override def supportsJSFunctions: Boolean = false
    }
  }
}
