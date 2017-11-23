package amf.error

import amf.compiler.AMFCompiler
import amf.framework.parser.Range
import amf.framework.validation.AMFValidationResult
import amf.remote._
import amf.unsafe.PlatformSecrets
import amf.validation.model.ParserSideValidations
import amf.validation.Validation
import org.scalatest.Matchers._
import org.scalatest.{AsyncFunSuite, Succeeded}

import scala.concurrent.ExecutionContext

class RamlParserErrorTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath = "file://shared/src/test/resources/error/"

  test("Test unexpected node types") {
    validate(
      "unexpected-nodes.raml",
      title => {
        title.level should be("Violation")
        title.message should be("Expecting !!str and !!map provided")
        title.position.map(_.range) should be(Some(Range((2, 6), (4, 0))))
      },
      description => {
        description.level should be("Violation")
        description.message should be("Expecting !!str and !!seq provided")
        description.position.map(_.range) should be(Some(Range((4, 13), (4, 24))))
      },
      protocols => {
        protocols.level should be("Violation")
        protocols.message should be("WebAPI 'protocols' property must be a scalar or sequence value")
        protocols.position.map(_.range) should be(Some(Range((5, 10), (7, 0))))
      },
      securedBy => {
        securedBy.level should be("Violation")
        securedBy.message should be("Not a YSequence")
        securedBy.position.map(_.range) should be(Some(Range((7, 11), (7, 16))))
      }
    )
  }

  test("Custom facets work correctly with the closed node detection mechanism") {
    validate(
      "custom-facets.raml",
      erroneousTypeShape => {
        erroneousTypeShape.level should be ("Violation")
        erroneousTypeShape.targetNode should be ("file://shared/src/test/resources/error/custom-facets.raml#/declarations/scalar/ErroneousType")
        erroneousTypeShape.validationId should be (ParserSideValidations.ClosedShapeSpecification.id())
      },
      incorrect1 => {
        incorrect1.level should be ("Violation")
        incorrect1.targetNode should be ("file://shared/src/test/resources/error/custom-facets.raml#/declarations/union/Incorrect1")
        incorrect1.validationId should be (ParserSideValidations.ClosedShapeSpecification.id())
      },
      incorrect2 => {
        incorrect2.level should be ("Violation")
        incorrect2.targetNode should be ("file://shared/src/test/resources/error/custom-facets.raml#/declarations/union/Incorrect2")
        incorrect2.validationId should be (ParserSideValidations.ClosedShapeSpecification.id())
      },
      incorrect3 => {
        incorrect3.level should be ("Violation")
        incorrect3.targetNode should be ("file://shared/src/test/resources/error/custom-facets.raml#/declarations/union/Incorrect3")
        incorrect3.validationId should be (ParserSideValidations.ClosedShapeSpecification.id())
      }
    )
  }

  private def validate(file: String, fixture: (AMFValidationResult => Unit)*) = {
    val validation = Validation(platform)
    AMFCompiler(basePath + file, platform, RamlYamlHint, validation)
      .build()
      .map { _ =>
        val report = validation.aggregatedReport
        report.size should be(fixture.size)
        fixture.zip(report).foreach {
          case (fn, result) => fn(result)
        }
        Succeeded
      }
  }
}
