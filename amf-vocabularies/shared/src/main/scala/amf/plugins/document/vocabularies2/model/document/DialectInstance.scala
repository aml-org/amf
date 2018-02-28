package amf.plugins.document.vocabularies2.model.document

import amf.core.metamodel.Obj
import amf.core.model.document.{BaseUnit, DeclaresModel, EncodesModel}
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies2.metamodel.document.DialectInstanceModel
import amf.plugins.document.vocabularies2.metamodel.document.DialectInstanceModel._

case class DialectInstance(fields: Fields, annotations: Annotations) extends BaseUnit with DeclaresModel with EncodesModel  {

  override def meta: Obj = DialectInstanceModel

  def references: Seq[BaseUnit] = fields(References)
  def location: String = fields(Location)
  def encodes: DomainElement = fields(Encodes)
  def declares: Seq[DomainElement] = fields(Declares)
  def usage: String = fields(Usage)
  def adopted(parent: String): this.type = withId(parent)

  def definedBy(): String = fields(DefinedBy)
  def withDefinedBy(dialectId: String) = set(DefinedBy, dialectId)

}

object DialectInstance {
  def apply(): DialectInstance = apply(Annotations())

  def apply(annotations: Annotations): DialectInstance = DialectInstance(Fields(), annotations)
}

