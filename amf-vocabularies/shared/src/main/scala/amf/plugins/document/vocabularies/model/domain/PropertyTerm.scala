package amf.plugins.document.vocabularies.model.domain

import amf.core.metamodel.Obj
import amf.core.model.StrField
import amf.core.model.domain.{AmfArray, AmfScalar, DomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies.metamodel.domain.ObjectPropertyTermModel._
import amf.plugins.document.vocabularies.metamodel.domain.{DatatypePropertyTermModel, ObjectPropertyTermModel}
import org.yaml.model.YMap

abstract class PropertyTerm extends DomainElement {

  override def adopted(parent: String): PropertyTerm.this.type = {
    if (Option(id).isEmpty) {
      simpleAdoption(parent)
    }
    this
  }

  override def componentId: String = ""
  def name: StrField               = fields.field(Name)
  def displayName: StrField        = fields.field(DisplayName)
  def description: StrField        = fields.field(Description)
  def range: StrField              = fields.field(Range)
  def subPropertyOf: Seq[StrField] = fields.field(SubPropertyOf)

  def withName(name: String): PropertyTerm               = set(Name, name)
  def withDisplayName(displayName: String): PropertyTerm = set(DisplayName, displayName)
  def withDescription(description: String): PropertyTerm = set(Description, description)
  def withRange(range: String): PropertyTerm             = set(Range, range)
  def withSubClassOf(superProperties: Seq[String]): PropertyTerm =
    set(SubPropertyOf, AmfArray(superProperties.map(AmfScalar(_))))
}

case class ObjectPropertyTerm(fields: Fields, annotations: Annotations) extends PropertyTerm {
  override def meta: Obj = ObjectPropertyTermModel
}

object ObjectPropertyTerm {

  def apply(): ObjectPropertyTerm = apply(Annotations())

  def apply(ast: YMap): ObjectPropertyTerm = apply(Annotations(ast))

  def apply(annotations: Annotations): ObjectPropertyTerm = ObjectPropertyTerm(Fields(), annotations)
}

case class DatatypePropertyTerm(fields: Fields, annotations: Annotations) extends PropertyTerm {
  override def meta: Obj = DatatypePropertyTermModel
}

object DatatypePropertyTerm {

  def apply(): DatatypePropertyTerm = apply(Annotations())

  def apply(ast: YMap): DatatypePropertyTerm = apply(Annotations(ast))

  def apply(annotations: Annotations): DatatypePropertyTerm = DatatypePropertyTerm(Fields(), annotations)
}
