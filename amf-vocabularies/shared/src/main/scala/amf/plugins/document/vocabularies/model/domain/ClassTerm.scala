package amf.plugins.document.vocabularies.model.domain

import amf.client.model.StrField
import amf.core.metamodel.Obj
import amf.core.model.domain.{AmfArray, AmfScalar, DomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies.metamodel.domain.ClassTermModel
import amf.plugins.document.vocabularies.metamodel.domain.ClassTermModel._
import org.yaml.model.YMap

case class ClassTerm(fields: Fields, annotations: Annotations) extends DomainElement {
  override def meta: Obj = ClassTermModel
  override def adopted(parent: String): ClassTerm.this.type = {
    if (Option(id).isEmpty) {
      withId(parent)
    }
    this
  }

  def name: StrField            = fields.field(Name)
  def displayName: StrField     = fields.field(DisplayName)
  def description: StrField     = fields.field(Description)
  def properties: Seq[StrField] = fields.field(Properties)
  def subClassOf: Seq[StrField] = fields.field(SubClassOf)

  def withName(name: String)                   = set(Name, name)
  def withDisplayName(displayName: String)     = set(DisplayName, displayName)
  def withDescription(description: String)     = set(Description, description)
  def withProperties(properties: Seq[String])  = set(Properties, AmfArray(properties.map(AmfScalar(_))))
  def withSubClasOf(superClasses: Seq[String]) = set(SubClassOf, AmfArray(superClasses.map(AmfScalar(_))))
}

object ClassTerm {
  def apply(): ClassTerm = apply(Annotations())

  def apply(ast: YMap): ClassTerm = apply(Annotations(ast))

  def apply(annotations: Annotations): ClassTerm = ClassTerm(Fields(), annotations)
}