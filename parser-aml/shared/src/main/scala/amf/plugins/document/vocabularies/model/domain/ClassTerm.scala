package amf.plugins.document.vocabularies.model.domain

import amf.core.metamodel.Obj
import amf.core.model.StrField
import amf.core.model.domain.{AmfArray, AmfScalar, DomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies.metamodel.domain.ClassTermModel
import amf.plugins.document.vocabularies.metamodel.domain.ClassTermModel._
import org.yaml.model.YMap

case class ClassTerm(fields: Fields, annotations: Annotations) extends DomainElement {
  override def meta: Obj = ClassTermModel

  override def adopted(parent: String): this.type = {
    if (Option(id).isEmpty) {
      simpleAdoption(parent)
    }
    this
  }

  def name: StrField            = fields.field(Name)
  def displayName: StrField     = fields.field(DisplayName)
  def description: StrField     = fields.field(Description)
  def properties: Seq[StrField] = fields.field(Properties)
  def subClassOf: Seq[StrField] = fields.field(SubClassOf)

  def withName(name: String): ClassTerm                    = set(Name, name)
  def withDisplayName(displayName: String): ClassTerm      = set(DisplayName, displayName)
  def withDescription(description: String): ClassTerm      = set(Description, description)
  def withProperties(properties: Seq[String]): ClassTerm   = set(Properties, AmfArray(properties.map(AmfScalar(_))))
  def withSubClassOf(superClasses: Seq[String]): ClassTerm = set(SubClassOf, AmfArray(superClasses.map(AmfScalar(_))))

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = ""
}

object ClassTerm {
  def apply(): ClassTerm = apply(Annotations())

  def apply(ast: YMap): ClassTerm = apply(Annotations(ast))

  def apply(annotations: Annotations): ClassTerm = ClassTerm(Fields(), annotations)
}
