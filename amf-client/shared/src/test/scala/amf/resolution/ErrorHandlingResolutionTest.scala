package amf.resolution
import amf.client.parse.DefaultParserErrorHandler
import amf.core.AMFCompilerRunCount
import amf.core.annotations.LexicalInformation
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.parser.errorhandler.{ParserErrorHandler, UnhandledParserErrorHandler}
import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline.DEFAULT_PIPELINE
import amf.core.services.RuntimeResolver
import amf.core.validation.SeverityLevels
import amf.facades.Validation
import amf.io.FunSuiteCycleTests
import amf.plugins.document.webapi.resolution.pipelines.AmfResolutionPipeline
import amf.plugins.document.webapi.{Oas20Plugin, Oas30Plugin, Raml08Plugin, Raml10Plugin}
import amf.plugins.features.validation.CoreValidations.DeclarationNotFound
import amf.validations.ParserSideValidations.UnknownSecuritySchemeErrorSpecification
import org.scalatest.Assertion
import org.scalatest.Matchers._

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

class ErrorHandlingResolutionTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-client/shared/src/test/resources/resolution/error-apis/"

  test("Unexisting include for resource type") {
    errorCycle(
      "api.raml",
      RamlYamlHint,
      List(
        ErrorContainer(
          DeclarationNotFound.id,
          "",
          None,
          "Cannot find declarations in context 'collectionsTypes",
          None,
          SeverityLevels.VIOLATION,
          None
        )),
      basePath + "unexisting-include/"
    )
  }

  test("Cannot replace variable") {
    errorCycle(
      "api.raml",
      RamlYamlHint,
      List(
        ErrorContainer(
          UnknownSecuritySchemeErrorSpecification.id,
          "file://amf-client/shared/src/test/resources/resolution/error-apis/bad-variable-replace/api.raml#/web-api/end-points/%2Fcatalogs/collection/applied/get/default-requirement_1/oauth_2_0",
          None,
          "Security scheme 'oauth_2_0' not found in declarations.",
          None,
          SeverityLevels.VIOLATION,
          None
        )
      ),
      basePath + "bad-variable-replace/"
    )
  }

  private def errorCycle(source: String, hint: Hint, errors: List[ErrorContainer], path: String) = {
    val config = CycleConfig(source, source, hint, hint.vendor, path, Some(hint.syntax), None)
    val eh     = TestErrorHandler()

    for {
      _ <- Validation(platform)
      u <- build(config, Some(DefaultParserErrorHandler.withRun()), useAmfJsonldSerialisation = true)
      _ <- {
        Future { transform(u, config, eh) }
      }
    } yield {
      assertErrors(errors, eh.errors.toList)
    }
  }

  private def assertErrors(golden: List[ErrorContainer], actual: List[ErrorContainer]): Assertion = {
    actual.size should be(golden.size)
    golden.zip(actual).foreach {
      case (g, ac) => assertError(g, ac)
    }
    succeed
  }

  private def assertError(golden: ErrorContainer, actual: ErrorContainer): Unit = {
    assert(golden.id == actual.id)
    assert(golden.node == actual.node)
    assert(golden.message == actual.message)
    // location and position?
  }

  private def transform(unit: BaseUnit, config: CycleConfig, eh: ErrorHandler): BaseUnit = {
    config.target match {
      case Raml | Raml08 | Raml10 | Oas | Oas20 | Oas30 =>
        RuntimeResolver.resolve(config.target.name, unit, DEFAULT_PIPELINE, eh)
      case Amf    => new AmfResolutionPipeline().transform(unit, eh)
      case target => throw new Exception(s"Cannot resolve $target")
      //    case _ => unit
    }
  }

  case class ErrorContainer(id: String,
                            node: String,
                            property: Option[String],
                            message: String,
                            lexical: Option[LexicalInformation],
                            level: String,
                            location: Option[String])

  case class TestErrorHandler() extends ParserErrorHandler {
    val errors: ListBuffer[ErrorContainer] = ListBuffer()

    override def reportConstraint(id: String,
                                  node: String,
                                  property: Option[String],
                                  message: String,
                                  lexical: Option[LexicalInformation],
                                  level: String,
                                  location: Option[String]): Unit = {
      errors += ErrorContainer(id, node, property, message, lexical, level, location)
    }
    override private[amf] val parserRun = AMFCompilerRunCount.nextRun()
  }
}
