package amf.plugins.document.vocabularies2.model.domain

import amf.core.metamodel.Obj
import amf.core.utils.Strings
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies2.metamodel.domain.VocabularyReferenceModel
import amf.plugins.document.vocabularies2.metamodel.domain.VocabularyReferenceModel._
import org.yaml.model.YMap

case class VocabularyReference(fields: Fields, annotations: Annotations) extends DomainElement {

  override def meta: Obj = VocabularyReferenceModel

  override def adopted(parent: String): VocabularyReference.this.type = Option(alias) match {
    case Some(alias) => withId(parent + "/vocabularyReference/" + alias.urlEncoded)
    case None => throw new Exception("Cannot set ID of VocabularyReference without alias")
  }

  def withAlias(alias: String): this.type = set(Alias, alias)
  def withReference(reference: String): this.type = set(Reference, reference)

  def alias: String = fields(Alias)
  def reference: String = fields(Reference)
}

object VocabularyReference {

  def apply(): VocabularyReference = apply(Annotations())

  def apply(ast: YMap): VocabularyReference = apply(Annotations(ast))

  def apply(annotations: Annotations): VocabularyReference = VocabularyReference(Fields(), annotations)
}
