package amf.client.plugins

import amf.ProfileNames
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.Shape
import amf.core.parser.ErrorHandler
import amf.core.validation.SeverityLevels.VIOLATION
import amf.core.validation.{AMFValidationReport, AMFValidationResult, ValidationShapeSet}
import amf.internal.environment.Environment
import org.yaml.model.{SyamlException, YError, YPart}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

trait AMFPayloadValidationPlugin extends AMFPlugin {

  protected def parsePayload(payload: String, mediaType: String, env: Environment, shape: Shape): PayloadFragment

  protected def parsePayloadWithErrorHandler(payload: String,
                                             mediaType: String,
                                             env: Environment,
                                             shape: Shape): PayloadParsingResult

  final def validatePayload(shape: Shape,
                            payload: String,
                            mediaType: String,
                            env: Environment): Future[AMFValidationReport] = {
    val payloadParsingResult = parsePayloadWithErrorHandler(payload, mediaType, env, shape)
    if (payloadParsingResult.hasError)
      Future.successful(AMFValidationReport(conforms = false, payload, ProfileNames.AMF, payloadParsingResult.results))
    else validateSet(ValidationShapeSet(shape, payloadParsingResult.fragment), env)
  }

  def validateSet(set: ValidationShapeSet, env: Environment): Future[AMFValidationReport]

  val payloadMediaType: Seq[String]

  def canValidate(shape: Shape, env: Environment): Boolean
}

case class PayloadErrorHandler() extends ErrorHandler {
  override val currentFile: String = ""
  override val parserCount: Int    = 1

  private val errors: ListBuffer[AMFValidationResult] = ListBuffer()

  override def handle(node: YPart, e: SyamlException): Unit = errors += processError(e.getMessage)

  override def handle[T](error: YError, defaultValue: T): T = {
    errors += processError(error.error)
    defaultValue
  }

  def getErrors: List[AMFValidationResult] = errors.toList

  private def processError(message: String): AMFValidationResult =
    new AMFValidationResult(message, VIOLATION, "", None, "", None, None, "")
}

case class PayloadParsingResult(fragment: PayloadFragment, results: List[AMFValidationResult]) {
  def hasError: Boolean = results.nonEmpty
}
