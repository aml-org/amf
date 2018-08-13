package amf.client.validate

import amf.core.annotations.LexicalInformation
import amf.core.parser.Range
import amf.core.validation.{AMFValidationResult => InternalValidationResult}
import amf.client.plugins.{PayloadParsingResult => InternalPayloadParsingResult}
import amf.core.utils.Strings

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.client.convert.CoreClientConverters._
import amf.client.model.document.PayloadFragment

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

  def message: String        = _internal.message
  def level: String          = _internal.level
  def targetNode: String     = _internal.targetNode
  def targetProperty: String = _internal.targetProperty.orNull
  def validationId: String   = _internal.validationId
  def source: Any            = _internal.source

  def position: Range = _internal.position match {
    case Some(lexical) => lexical.range
    case _             => Range.NONE
  }

  def location: ClientOption[String] = _internal.location.asClient
}

@JSExportAll
class PayloadParsingResult(private[amf] val _internal: InternalPayloadParsingResult) {

  @JSExportTopLevel("client.validate.PayloadParsingResult")
  def this(fragment: PayloadFragment, results: ClientList[ValidationResult]) =
    this(InternalPayloadParsingResult(fragment._internal, results.asInternal.toList))

  def fragment: PayloadFragment             = _internal.fragment
  def results: ClientList[ValidationResult] = _internal.results.asClient

}
