package amf.core.model.domain

import amf.core.metamodel.Field
import amf.core.metamodel.domain.ExternalSourceElementModel._
import amf.core.model.StrField
import amf.core.parser.{Annotations, Fields, Value}
import amf.core.vocabulary.ValueType

trait ExternalSourceElement extends DynamicDomainElement {
  val fields: Fields
  val annotations: Annotations
  def raw: StrField         = fields.field(Raw)         //we should set this while parsing
  def referenceId: StrField = fields.field(ReferenceId) /// only for graph parser logic

  override def dynamicFields: List[Field] = meta.fields

  override def dynamicType: List[ValueType] = meta.`type`

  override def valueForField(f: Field): Option[Value] = f match {
    case Raw if isLinkToSource => None
//    case ReferenceId if isLinkToSource => fields(ReferenceId) // not necessary filter?
//    case ReferenceId => None
    case _ => fields.entry(f).map(_.value)
  }

  private def isLinkToSource = fields.entry(ReferenceId).isDefined

  def withReference(id: String): this.type = set(ReferenceId, id)
}
