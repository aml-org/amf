package amf.plugins.document.vocabularies2.model.document

import amf.core.metamodel.Obj
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.model.domain.DomainElement
import amf.plugins.document.vocabularies2.metamodel.document.VocabularyModel._
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies2.metamodel.document.VocabularyModel
import amf.plugins.document.vocabularies2.model.domain.External

case class Vocabulary(fields: Fields, annotations: Annotations) extends BaseUnit with DeclaresModel {
  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  override def references: Seq[BaseUnit] = fields(References)
  /** Returns the file location for the document that has been parsed to generate this model */
  override def location: String = fields(Location)
  /** Returns the usage comment for de element */
  override def usage: String = fields(Usage)
  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  override def declares: Seq[DomainElement] = fields(Declares)

  // Vocabulary specific fields
  def name: String = fields(Name)
  def base: String = fields(Base)
  def imports: Seq[Vocabulary] = fields(Imports)
  def externals: Seq[External] = fields(Externals)

  def withExternals(externals: Seq[External]) = setArray(Externals, externals)

  /** Meta data for the document */
  override def meta: Obj = VocabularyModel
  /** Call after object has been adopted by specified parent. */
  override def adopted(parent: String): Vocabulary.this.type = withId(parent)
}


object Vocabulary {
  def apply(): Vocabulary = apply(Annotations())

  def apply(annotations: Annotations): Vocabulary = Vocabulary(Fields(), annotations)
}
