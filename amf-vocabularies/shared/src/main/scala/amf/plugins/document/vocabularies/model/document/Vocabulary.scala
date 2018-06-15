package amf.plugins.document.vocabularies.model.document

import amf.core.metamodel.Obj
import amf.core.model.StrField
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies.metamodel.document.VocabularyModel
import amf.plugins.document.vocabularies.metamodel.document.VocabularyModel._
import amf.plugins.document.vocabularies.model.domain.{External, VocabularyReference}

case class Vocabulary(fields: Fields, annotations: Annotations) extends BaseUnit with DeclaresModel {

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  override def references: Seq[BaseUnit] = fields.field(References)

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  override def declares: Seq[DomainElement] = fields.field(Declares)

  // Vocabulary specific fields
  def name: StrField                    = fields.field(Name)
  def base: StrField                    = fields.field(Base)
  def imports: Seq[VocabularyReference] = fields.field(Imports)
  def externals: Seq[External]          = fields.field(Externals)

  def withName(name: String) = set(Name, name)
  def withBase(base: String) = {
    set(Base, base)
  }
  def withExternals(externals: Seq[External])             = setArray(Externals, externals)
  def withImports(vocabularies: Seq[VocabularyReference]) = setArray(Imports, vocabularies)

  /** Meta data for the document */
  override def meta: Obj = VocabularyModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = ""
}

object Vocabulary {
  def apply(): Vocabulary = apply(Annotations())

  def apply(annotations: Annotations): Vocabulary = Vocabulary(Fields(), annotations)
}
