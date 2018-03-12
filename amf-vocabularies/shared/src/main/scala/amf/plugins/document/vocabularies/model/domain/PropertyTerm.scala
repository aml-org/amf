package amf.plugins.document.vocabularies.model.domain

import amf.core.metamodel.Obj
import amf.core.model.domain.{AmfArray, AmfScalar, DomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies.metamodel.domain.{DatatypePropertyTermModel, ObjectPropertyTermModel}
import amf.plugins.document.vocabularies.metamodel.domain.ObjectPropertyTermModel._
import org.yaml.model.YMap

abstract class PropertyTerm extends DomainElement {

  override def adopted(parent: String): PropertyTerm.this.type = {
    if (Option(id).isEmpty) {
      withId(parent)
    }
    this
  }

  def name: String               = fields(Name)
  def displayName: String        = fields(DisplayName)
  def description: String        = fields(Description)
  def range: String              = fields(Range)
  def subPropertyOf: Seq[String] = fields(SubPropertyOf)

  def withName(name: String)                      = set(Name, name)
  def withDisplayName(displayName: String)        = set(DisplayName, displayName)
  def withDescription(description: String)        = set(Description, description)
  def withRange(range: String)                    = set(Range, range)
  def withSubClasOf(superProperties: Seq[String]) =  set(SubPropertyOf, AmfArray(superProperties.map(AmfScalar(_))))
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

