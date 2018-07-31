package amf.client.validate

import amf.core.annotations.LexicalInformation
import amf.core.parser.Range
import amf.core.validation.{AMFValidationResult => InternalValidationResult}
import amf.core.utils.Strings
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.client.convert.CoreClientConverters._

@JSExportAll
class ValidationResult(private[amf] val _internal: InternalValidationResult) {

  @JSExportTopLevel("client.validate.ValidationResult")
  def this(message: String,
           level: String,
           targetNode: String,
           targetProperty: String,
           validationId: String,
           position: Range,
           location: String) =
    this(
      InternalValidationResult(message,
                               level,
                               targetNode,
                               targetProperty.option,
                               validationId,
                               Some(LexicalInformation(position)),
                               location.option,
                               null))

  val message: String        = _internal.message
  val level: String          = _internal.level
  val targetNode: String     = _internal.targetNode
  val targetProperty: String = _internal.targetProperty.orNull
  val validationId: String   = _internal.validationId
  val source: Any            = _internal.source

  val position: Range = _internal.position match {
    case Some(lexical) => lexical.range
    case _             => Range.NONE
  }

  val location: ClientOption[String] = _internal.location.asClient
}
