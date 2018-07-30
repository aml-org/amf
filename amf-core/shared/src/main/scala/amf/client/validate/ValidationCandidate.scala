package amf.client.validate

import amf.client.convert.CoreClientConverters._
import amf.client.model.document.PayloadFragment
import amf.client.model.domain.Shape
import amf.core.validation.{
  ValidationCandidate => InternalValidationCandidate,
  ValidationShapeSet => InternalValidationShapeSet
}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ValidationShapeSet(private[amf] val _internal: InternalValidationShapeSet) {

  @JSExportTopLevel("client.plugins.ValidationShapeSet")
  def this(candidates: ClientList[ValidationCandidate], defaultSeverity: String) =
    this(InternalValidationShapeSet(candidates.asInternal, defaultSeverity))

  def candidates: ClientList[ValidationCandidate] = _internal.candidates.asClient

  def defaultSeverity: String = _internal.defaultSeverity
}

@JSExportAll
case class ValidationCandidate(private[amf] val _internal: InternalValidationCandidate) {

  @JSExportTopLevel("client.plugins.ValidationCandidate")
  def this(shape: Shape, payload: PayloadFragment) =
    this(InternalValidationCandidate(shape._internal, payload._internal))

  def shape: Shape = _internal.shape

  def payload: PayloadFragment = _internal.payload
}
