package amf.validation

import amf.core.AMF
import amf.core.annotations.ScalarType
import amf.core.model.DataType._
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import amf.plugins.domain.shapes.models.{AnyShape, ArrayShape, NodeShape, ScalarShape}
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class ScalarValidationTest extends AsyncFunSuite with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val APPLICATION_JSON = "application/json"
  val APPLICATION_YAML = "application/yaml"

  val Node  = "NodeObject"
  val Array = "ArrayObject"

  val expectedConforms = List(
    ExpectedConforms("1", List(String, Integer, Number)),
    ExpectedConforms("\"1\"", List(String)),
    ExpectedConforms("some", List(String)),
    ExpectedConforms("\"some\"", List(String)),
    ExpectedConforms("true", List(String, Boolean)),
    ExpectedConforms("\"true\"", List(String)),
    ExpectedConforms("null", List(String, Nil)),
    ExpectedConforms("\"null\"", List(String)),
    ExpectedConforms("2019-01-01", List(String, Date)),
    ExpectedConforms("2015-07-04T21:00:00", List(String, DateTimeOnly)),
    ExpectedConforms("2016-02-28T16:41:41.090Z", List(String, DateTime)),
    ExpectedConforms("\"2016-02-28T16:41:41.090Z\"", List(String, DateTime)),
    ExpectedConforms("-5.5", List(String, Number)),
    ExpectedConforms("\"-5.5\"", List(String)),
    ExpectedConforms("1.07E7", List(String, Number), List(Integer))
  )

  case class ExpectedConforms(value: Any,
                              typesThatShouldConform: List[String],
                              platformDiscrepancies: List[String] = List())

  val shapes = Map(
    String       -> new ScalarShape(Fields(), Annotations()).withDataType(String),
    Integer      -> new ScalarShape(Fields(), Annotations()).withDataType(Integer),
    Boolean      -> new ScalarShape(Fields(), Annotations()).withDataType(Boolean),
    Nil          -> new ScalarShape(Fields(), Annotations()).withDataType(Nil),
    Date         -> new ScalarShape(Fields(), Annotations()).withDataType(Date),
    DateTime     -> new ScalarShape(Fields(), Annotations()).withDataType(DateTime),
    DateTimeOnly -> new ScalarShape(Fields(), Annotations()).withDataType(DateTimeOnly),
    Number       -> new ScalarShape(Fields(), Annotations()).withDataType(Number),
    Node         -> new NodeShape(Fields(), Annotations()),
    Array        -> new ArrayShape(Fields(), Annotations())
  )

  makeFixtureRuns.map {
    case Fixture(scalarType, toValidate, conforms, platformDifferences) => {
      val conformMessage = if (conforms) "to conform in json and yaml" else "not to conform in json and yaml"
      if (platformDifferences.contains(scalarType))
        ignore(
          s"Validate ${surroundWithSimpleQuotes(toValidate.toString)} as ${formatDataType(scalarType)} is ignored due to platform differences") {
          succeed
        } else
        test(
          s"Validate ${surroundWithSimpleQuotes(toValidate.toString)} as ${formatDataType(scalarType)} ${conformMessage}") {
          initAmf.flatMap { _ =>
            val shape                   = shapes(scalarType)
            val jsonPayloadValidation   = validateWithJsonPayload(scalarType, toValidate, shape)
            val yamlParameterValidation = validateWithYamlParameter(toValidate, shape)
            val actual                  = Result(jsonValidation = jsonPayloadValidation, yamlValidation = yamlParameterValidation)
            actual shouldEqual Result(jsonValidation = conforms, yamlValidation = conforms)
          }
        }
    }
  }

  private def validateWithYamlParameter(toValidate: Any, shape: AnyShape) = {
    val validator = shape.parameterValidator(APPLICATION_YAML).get
    validator.syncValidate(APPLICATION_YAML, toValidate.toString).conforms
  }

  private def validateWithJsonPayload(scalarType: String, toValidate: Any, shape: AnyShape) = {
    val validator = shape.payloadValidator(APPLICATION_JSON).get
    val valueForJson = converters
      .get(scalarType)
      .map(c => c.format(toValidate.toString))
      .getOrElse(toValidate.toString)
    validator.syncValidate(APPLICATION_JSON, valueForJson).conforms
  }

  private def formatDataType(dataType: String) = dataType.split("#").last

  private def initAmf: Future[Unit] =
    AMF
      .init()
      .map(_ => {
        AMF.registerPlugin(PayloadValidatorPlugin)
      })

  case class Result(jsonValidation: Boolean, yamlValidation: Boolean)

  case class Fixture(scalarType: String, toValidate: Any, result: Boolean, platformDifferences: Seq[String])

  private def makeFixtureRuns: List[Fixture] = {
    expectedConforms.iterator.toList.flatMap({
      case ExpectedConforms(example, typesThatShouldConform, platformDifferences) =>
        val availableTypes = shapes.keySet
        availableTypes.map(t => Fixture(t, example, typesThatShouldConform.contains(t), platformDifferences))
    })
  }

  private def surroundWithSimpleQuotes(s: String) = "\'" + s + "\'"

  val converters = Map(
    String       -> new StringFormatter,
    Date         -> new StringFormatter,
    DateTimeOnly -> new StringFormatter,
    DateTime     -> new StringFormatter
  )

  sealed trait Formatter {
    def format(format: String): String
  }

  class StringFormatter extends Formatter {
    override def format(format: String): String =
      if (!startsAndEndsWith("\"", format) && !startsAndEndsWith("\'", format)) quote(format)
      else if (startsAndEndsWith("\"", format) && format.length == 1) quote(format)
      else format

    private def startsAndEndsWith(symbol: String, toCheck: String) =
      toCheck.startsWith(symbol) && toCheck.endsWith(symbol)

    private def quote(something: String): String = "\"" + something + "\""
  }
}
