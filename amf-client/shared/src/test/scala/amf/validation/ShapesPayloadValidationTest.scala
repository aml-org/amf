package amf.validation

import amf.client.plugins.{ScalarRelaxedValidationMode, StrictValidationMode, ValidationMode}
import amf.core.AMFSerializer
import amf.core.registries.AMFPluginsRegistry
import amf.core.services.RuntimeValidator
import amf.plugins.document.graph.AMFGraphPlugin
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import amf.plugins.domain.shapes.models.TypeDef.{IntType, StrType}
import amf.plugins.domain.shapes.models._
import amf.plugins.domain.shapes.parser.XsdTypeDefMapping
import amf.plugins.features.validation.custom.AMFValidatorPlugin
import amf.plugins.syntax.SYamlSyntaxPlugin
import org.scalatest.AsyncFunSuite
import org.scalatest.Matchers._

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class SchemaPayloadValidationTest extends AsyncFunSuite with ShapesFixture {

  override val executionContext: ExecutionContext = global

  amf.Core.registerPlugin(PayloadValidatorPlugin)
  RuntimeValidator.register(AMFValidatorPlugin)
  AMFSerializer.init()
  AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)
  AMFPluginsRegistry.registerSyntaxPlugin(SYamlSyntaxPlugin)

  case class ShapeInfo(shape: AnyShape, examples: Seq[ExampleInfo], mode: ValidationMode = StrictValidationMode)
  case class ExampleInfo(name: String, example: String, valid: Boolean)

  val fixtureTest: Seq[ShapeInfo] = Seq(
    ShapeInfo(
      Fixture.SimpleStrScalar,
      Seq(
        ExampleInfo("YamlSimpleStrExample", Fixture.YamlSimpleStrExample, valid = true),
        ExampleInfo("JsonSimpleStrExample", Fixture.JsonSimpleStrExample, valid = true),
        ExampleInfo("XmlSimpleStrExample", Fixture.XmlSimpleStrExample, valid = false),
        ExampleInfo("YamlSimpleIntExample", Fixture.YamlSimpleIntExample, valid = false),
        ExampleInfo("JsonSimpleIntExample", Fixture.JsonSimpleIntExample, valid = false),
        ExampleInfo("XmlSimpleIntExample", Fixture.XmlSimpleIntExample, valid = false),
        ExampleInfo("UserYamlExample", Fixture.UserYamlExample, valid = false),
        ExampleInfo("UserJsonExample", Fixture.UserJsonExample, valid = false),
        ExampleInfo("UserXmlExample", Fixture.UserXmlExample, valid = false),
        ExampleInfo("YamlInvalidStrExample", Fixture.YamlInvalidStrExample, valid = false),
        ExampleInfo("JsonLargeIntExample", Fixture.JsonLargeIntExample, valid = false),
        ExampleInfo("JsonLargeIntStrExample", Fixture.JsonLargeIntStrExample, valid = true)
      )
    ),
    ShapeInfo(
      Fixture.SimpleIntScalar,
      Seq(
        ExampleInfo("YamlSimpleStrExample", Fixture.YamlSimpleStrExample, valid = false),
        ExampleInfo("JsonSimpleStrExample", Fixture.JsonSimpleStrExample, valid = false),
        ExampleInfo("XmlSimpleStrExample", Fixture.XmlSimpleStrExample, valid = false),
        ExampleInfo("YamlSimpleIntExample", Fixture.YamlSimpleIntExample, valid = true),
        ExampleInfo("JsonSimpleIntExample", Fixture.JsonSimpleIntExample, valid = true),
        ExampleInfo("XmlSimpleIntExample", Fixture.XmlSimpleIntExample, valid = false),
        ExampleInfo("UserYamlExample", Fixture.UserYamlExample, valid = false),
        ExampleInfo("UserJsonExample", Fixture.UserJsonExample, valid = false),
        ExampleInfo("UserXmlExample", Fixture.UserXmlExample, valid = false)
      )
    ),
    ShapeInfo(
      Fixture.Customer,
      Seq(
        ExampleInfo("YamlSimpleStrExample", Fixture.YamlSimpleStrExample, valid = false),
        ExampleInfo("JsonSimpleStrExample", Fixture.JsonSimpleStrExample, valid = false),
        ExampleInfo("XmlSimpleStrExample", Fixture.XmlSimpleStrExample, valid = false),
        ExampleInfo("CustomerYamlExample", Fixture.YamlCustomerExample, valid = true),
        ExampleInfo("CustomerInvalidYamlExample", Fixture.YamlInvalidCustomerExample, valid = false),
        ExampleInfo("CustomerNonMandatoryYamlExample", Fixture.YamlCustomerNonMandatoryExample, valid = true),
        ExampleInfo("CustomerJsonExample", Fixture.JsonCustomerExample, valid = true),
        ExampleInfo("CustomerInvalidJsonExample", Fixture.JsonInvalidCustomerExample, valid = false),
        ExampleInfo("CustomerXmlExample", Fixture.XmlCustomerExample, valid = false)
      )
    ),
    ShapeInfo(
      Fixture.XmlShape,
      Seq(
        ExampleInfo("YamlSimpleStrExample", Fixture.YamlSimpleStrExample, valid = false),
        ExampleInfo("JsonSimpleStrExample", Fixture.JsonSimpleStrExample, valid = false),
        ExampleInfo("XmlSimpleStrExample", Fixture.XmlSimpleStrExample, valid = false),
        ExampleInfo("CustomerYamlExample", Fixture.YamlCustomerExample, valid = false),
        ExampleInfo("CustomerJsonExample", Fixture.JsonCustomerExample, valid = false),
        ExampleInfo("CustomerXmlExample", Fixture.XmlCustomerExample, valid = false),
        ExampleInfo("UserYamlExample", Fixture.UserYamlExample, valid = false),
        ExampleInfo("UserJsonExample", Fixture.UserJsonExample, valid = false),
        ExampleInfo("UserXmlExample", Fixture.UserXmlExample, valid = false)
      )
    ),
    ShapeInfo(
      Fixture.UnionStrNil,
      Seq(
        ExampleInfo("SimpleScalarNumberExampleAgainstUnion", "12", valid = true)
      ),
      mode = ScalarRelaxedValidationMode
    ),
    ShapeInfo(
      Fixture.SimpleIntScalar,
      Seq(
        ExampleInfo("SimpleScalarNumberExample", "12", valid = true)
      ),
      mode = ScalarRelaxedValidationMode
    ),
    ShapeInfo(
      Fixture.SimpleStrScalar,
      Seq(
        ExampleInfo("RelaxedLargeInt", Fixture.JsonLargeIntExample, valid = true),
        ExampleInfo("RelaxedLargeIntStr", Fixture.JsonLargeIntStrExample, valid = true)
      ),
      mode = ScalarRelaxedValidationMode
    )
  )

  fixtureTest.foreach { si =>
    si.examples.foreach { ei =>
      test(s"Test ${si.shape.name} with example ${ei.name}") {
        if (si.mode == StrictValidationMode)
          si.shape.validate(ei.example).map { r =>
            r.conforms should be(ei.valid)
          } else
          si.shape.validateParameter(ei.example).map { r =>
            r.conforms should be(ei.valid)
          }
      }
    }
  }

}

trait ShapesFixture {

  protected object Fixture {
    private val strXds: String = XsdTypeDefMapping.xsd(StrType)
    private val intXds: String = XsdTypeDefMapping.xsd(IntType)

    private val Name: ScalarShape =
      ScalarShape().withName("name").withDataType(strXds).withId("http://a.ml/payloadTest/name")
    private val LastName: ScalarShape =
      ScalarShape().withName("lastName").withDataType(strXds).withId("payloadTest/lastName")

    private val Street: ScalarShape =
      ScalarShape().withName("street").withDataType(strXds).withId("http://a.ml/payloadTest/street")
    private val Number: ScalarShape =
      ScalarShape().withName("number").withDataType(intXds).withId("http://a.ml/payloadTest/number")

    private val Address: NodeShape = NodeShape().withName("address").withId("http://a.ml/payloadTest/address")
    Address
      .withProperty(Street.name.value())
      .withMinCount(1)
      .withRange(Street)
      .withId("http://a.ml/payloadTest/property/street")
    Address
      .withProperty(Number.name.value())
      .withMinCount(1)
      .withRange(Number)
      .withId("http://a.ml/payloadTest/property/number")

    val Customer: NodeShape = NodeShape().withName("Customer").withId("http://a.ml/payloadTest/customer")
    Customer
      .withProperty(Name.name.value())
      .withMinCount(1)
      .withRange(Name)
      .withId("http://a.ml/payloadTest/property/name")
    Customer
      .withProperty(LastName.name.value())
      .withMinCount(0)
      .withRange(LastName)
      .withId("http://a.ml/payloadTest/property.lastName")
    Customer
      .withProperty(Address.name.value())
      .withMinCount(1)
      .withRange(Address)
      .withId("http://a.ml/payloadTest/property/address")

    val SimpleStrScalar: ScalarShape =
      ScalarShape().withName("simpleStr").withDataType(strXds).withId("http://a.ml/payloadTest/simpleStr")
    val SimpleIntScalar: ScalarShape =
      ScalarShape().withName("simpleInt").withDataType(intXds).withId("http://a.ml/payloadTest/simpleInt")

    val NilS: NilShape = NilShape().withName("nil").withId("http://a.ml/payloadTest/nilShape")
    val UnionStrNil: UnionShape =
      UnionShape().withName("union").withId("http://a.ml/payloadTest/simpleStr").withAnyOf(Seq(SimpleStrScalar, NilS))

    private val xmlUser: String =
      """|<?xml version="1.0"?>
        |<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" attributeFormDefault="unqualified" elementFormDefault="qualified">
        |    <xs:element name="User" type="UserType"/>
        |    <xs:complexType name="UserType">
        |        <xs:sequence>
        |            <xs:element type="xs:string" name="name"/>
        |            <xs:element type="xs:string" name="lastName"/>
        |        </xs:sequence>
        |    </xs:complexType>
        |</xs:schema>""".stripMargin

    val XmlShape: SchemaShape = SchemaShape().withName("xml").withMediaType("application/xml").withRaw(xmlUser)

    val UserXmlExample: String =
      """|<User>
         |  <name1>Tato</name1>
         |  <lastName>Bores</lastName>
         |</User>""".stripMargin

    val UserJsonExample: String =
      """|{
         |  "name1": "Tato",
         |  "lastName": "Bores"
         |}""".stripMargin

    val UserYamlExample: String =
      """|name1: Tato
         |lastName: Bores""".stripMargin

    val XmlCustomerExample: String =
      """|<Customer>
         |  <name>juan roman</name>
         |  <lastName>riquelme</lastName>
         |  <address>
         |    <street>tigre</street>
         |    <number>10</number>
         |  </address>
         |</Customer>""".stripMargin

    val JsonCustomerExample: String =
      """|{
         |  "name": "juan roman",
         |  "lastName": "riquelme",
         |  "address": {
         |    "street": "tigre",
         |    "number": 10
         |   }
         |}""".stripMargin

    val JsonInvalidCustomerExample: String =
      """|{
         |  "name": true,
         |  "lastName": 100,
         |  "address": {
         |    "street": 111,
         |    "number": "roman"
         |   }
         |}""".stripMargin

    val YamlCustomerExample: String =
      """|name: juan roman
         |lastName: riquelme
         |address:
         |  street: tigre
         |  number: 10""".stripMargin

    val YamlCustomerNonMandatoryExample: String =
      """|name: juan roman
         |address:
         |  street: tigre
         |  number: 10""".stripMargin

    val YamlInvalidCustomerExample: String =
      """|name: 10
         |lastName: 111
         |address:
         |  street: 111
         |  number: falsa""".stripMargin

    val YamlSimpleStrExample = "simple"

    val YamlInvalidStrExample = "%7Bversion%7D"

    val JsonSimpleStrExample: String = "\"simple\""

    val JsonLargeIntExample: String = "9223372036854775808"

    val JsonLargeIntStrExample: String = "\"9223372036854775808\""

    val XmlSimpleStrExample = "<simpleStr>simple</simpleStr>" // validate this

    val YamlSimpleIntExample = "10"

    val JsonSimpleIntExample: String = "10"

    val XmlSimpleIntExample = "<simpleInt>10</simpleInt>" // validate this
  }

}
