package amf.plugins.document.vocabularies2.model.domain

import amf.core.metamodel.Obj
import amf.core.model.domain.{AmfArray, AmfScalar, DomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies2.metamodel.domain.ClassTermModel
import amf.plugins.document.vocabularies2.metamodel.domain.ClassTermModel._
import org.yaml.model.YMap

case class ClassTerm(fields: Fields, annotations: Annotations) extends DomainElement {
  override def meta: Obj = ClassTermModel
  override def adopted(parent: String): ClassTerm.this.type = withId(parent)

  def name: String            = fields(Name)
  def displayName: String     = fields(DisplayName)
  def description: String     = fields(Description)
  def properties: Seq[String] = fields(Properties)
  def subClassOf: Seq[String] = fields(SubClassOf)

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