package amf.resolution
import amf.apicontract.client.scala.AMFConfiguration
import amf.apicontract.internal.transformation.AmfTransformationPipeline
import amf.apicontract.internal.validation.definitions.ParserSideValidations.UnknownSecuritySchemeErrorSpecification
import amf.core.client.common.transform.PipelineId
import amf.core.client.common.validation.SeverityLevels
import amf.core.client.scala.errorhandling.DefaultErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.TransformationPipelineRunner
import amf.core.client.scala.validation.AMFValidationResult
import amf.core.internal.annotations.LexicalInformation
import amf.core.internal.remote._
import amf.core.internal.validation.CoreValidations.DeclarationNotFound
import amf.io.FunSuiteCycleTests
import amf.testing.TargetProvider
import amf.testing.TargetProvider.defaultTargetFor
import org.scalatest.Assertion
import org.scalatest.Matchers._

import scala.concurrent.Future

class ErrorHandlingResolutionTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/resolution/error-apis/"

  test("Unexisting include for resource type") {
    errorCycle(
      "api.raml",
      Raml10YamlHint,
      List(
        AMFValidationResult(
          "Cannot find declarations in context 'collectionsTypes",
          SeverityLevels.VIOLATION,
          "",
          None,
          DeclarationNotFound.id,
          None,
          None,
          null
        )),
      basePath + "unexisting-include/"
    )
  }

  test("Cannot replace variable") {
    errorCycle(
      "api.raml",
      Raml10YamlHint,
      List(
        AMFValidationResult(
          "Security scheme 'oauth_2_0' not found in declarations.",
          SeverityLevels.VIOLATION,
          "file://amf-cli/shared/src/test/resources/resolution/error-apis/bad-variable-replace/api.raml#/web-api/end-points/%2Fcatalogs/collection/applied/get/default-requirement_1/oauth_2_0",
          None,
          UnknownSecuritySchemeErrorSpecification.id,
          None,
          None,
          null
        )
      ),
      basePath + "bad-variable-replace/"
    )
  }

  private def errorCycle(source: String, hint: Hint, errors: List[AMFValidationResult], path: String) = {
    val config    = CycleConfig(source, source, hint, defaultTargetFor(hint.vendor), path, None)
    val eh        = DefaultErrorHandler()
    val amfConfig = buildConfig(None, None) // need to ignore parsing errors, apparently
    for {
      u <- build(config, amfConfig)
      _ <- {
        Future { transform(u, config, amfConfig.withErrorHandlerProvider(() => eh)) }
      }
    } yield {
      assertErrors(errors, eh.getResults)
    }
  }

  private def assertErrors(golden: List[AMFValidationResult], actual: List[AMFValidationResult]): Assertion = {
    actual.size should be(golden.size)
    golden.zip(actual).foreach {
      case (g, ac) => assertError(g, ac)
    }
    succeed
  }

  private def assertError(golden: AMFValidationResult, actual: AMFValidationResult): Unit = {
    assert(golden.validationId == actual.validationId)
    assert(golden.targetNode == actual.targetNode)
    assert(golden.message == actual.message)
    // location and position?
  }

  override def transform(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): BaseUnit = {
    config.renderTarget.spec match {
      case Raml08 | Raml10 | Oas20 | Oas30 =>
        amfConfig.baseUnitClient().transform(unit, PipelineId.Default).baseUnit
      case Amf =>
        TransformationPipelineRunner(amfConfig.errorHandlerProvider.errorHandler())
          .run(unit, AmfTransformationPipeline())
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
                            location: Option[String]) {
    def toResult: AMFValidationResult =
      AMFValidationResult(message, level, node, property, id, lexical, location, null)
  }
}
