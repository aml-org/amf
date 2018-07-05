package amf.client.validate

import amf.core.annotations.LexicalInformation
import amf.core.parser.Range
import amf.core.validation.{AMFValidationResult => InternalValidationResult}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class ValidationResult(private[amf] val _internal: InternalValidationResult) {

  @JSExportTopLevel("client.validate.ValidationResult")
  def this(message: String,
           level: String,
           targetNode: String,
           targetProperty: String,
           validationId: String,
           position: Range) =
    this(
      InternalValidationResult(message,
                               level,
                               targetNode,
                               Some(targetProperty),
                               validationId,
                               Some(LexicalInformation(position)),
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
}
