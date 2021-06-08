package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.{BoolField, StrField}
import amf.core.client.scala.model.domain.{AmfScalar, DomainElement}
import amf.core.internal.metamodel.Obj
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.internal.domain.metamodel.{BaseIRIModel, ContextElementWithIri, ContextMappingModel, CuriePrefixModel, DefaultVocabularyModel, SemanticContextModel}
import org.yaml.model.YPart

trait WithContextIri {
  this: DomainElement =>
  def withIri(iri: String): this.type = set(ContextElementWithIri.IRI, AmfScalar(iri, Annotations()))
  def withIri(iri: String, ann: Annotations): this.type = set(ContextElementWithIri.IRI, AmfScalar(iri, ann))
  def iri: StrField = fields.field(ContextElementWithIri.IRI)
}

class BaseIri(override val fields: Fields, val annotations: Annotations)
  extends DomainElement
    with WithContextIri {

  def withNulled(nulled: Boolean): this.type = set(ContextMappingModel.Nulled, AmfScalar(nulled, Annotations()))
  def nulled: BoolField = fields.field(ContextMappingModel.Nulled)

  override def meta: Obj = BaseIRIModel

  override def componentId: String = "/@base".urlComponentEncoded
}

object BaseIri {

  def apply(): BaseIri = apply(Annotations())

  def apply(ast: YPart): BaseIri = apply(Annotations(ast))

  def apply(annotations: Annotations): BaseIri =
    new BaseIri(Fields(), annotations)
}


class DefaultVocabulary(override val fields: Fields, val annotations: Annotations)
  extends DomainElement
    with WithContextIri {

  override def meta: Obj = DefaultVocabularyModel

  override def componentId: String = "/@vocab".urlComponentEncoded
}


object DefaultVocabulary {
  def apply(): DefaultVocabulary = apply(Annotations())

  def apply(ast: YPart): DefaultVocabulary = apply(Annotations(ast))

  def apply(annotations: Annotations): DefaultVocabulary =
    new DefaultVocabulary(Fields(), annotations)
}


class CuriePrefix(override val fields: Fields, val annotations: Annotations)
  extends DomainElement
    with WithContextIri {

  def withAlias(alias: String): this.type = set(CuriePrefixModel.Alias, AmfScalar(alias, Annotations()))
  def alias: StrField = fields.field(CuriePrefixModel.Alias)

  override def meta: Obj = DefaultVocabularyModel

  override def componentId: String = "/" + iri.value().urlComponentEncoded
}


object CuriePrefix {
  def apply(): CuriePrefix = apply(Annotations())

  def apply(ast: YPart): CuriePrefix = apply(Annotations(ast))

  def apply(annotations: Annotations): CuriePrefix =
    new CuriePrefix(Fields(), annotations)
}


class ContextMapping(override val fields: Fields, val annotations: Annotations)
  extends DomainElement
    with WithContextIri {

  override def meta: Obj = ContextMappingModel

  def withAlias(alias: String): this.type = set(ContextMappingModel.Alias, AmfScalar(alias, Annotations()))
  def alias: StrField = fields.field(ContextMappingModel.Alias)

  def withCoercion(coercion: String): this.type = set(ContextMappingModel.Coercion, AmfScalar(coercion, Annotations()))
  def coercion: StrField = fields.field(ContextMappingModel.Coercion)

  def withNulled(nulled: Boolean): this.type = set(ContextMappingModel.Nulled, AmfScalar(nulled, Annotations()))
  def nulled: BoolField = fields.field(ContextMappingModel.Nulled)

  override def componentId: String = "/" + alias.value().urlComponentEncoded
}

object ContextMapping {
  def apply(): ContextMapping = apply(Annotations())

  def apply(ast: YPart): ContextMapping = apply(Annotations(ast))

  def apply(annotations: Annotations): ContextMapping =
    new ContextMapping(Fields(), annotations)
}


class SemanticContext(override val fields: Fields, val annotations: Annotations)
  extends DomainElement
    with WithContextIri {
  override def meta: Obj = SemanticContextModel

  def withBase(base: BaseIri): this.type  = set(SemanticContextModel.Base, base)
  def base: Option[BaseIri] = Option(fields.field(SemanticContextModel.Base))

  def withVocab(vocab: DefaultVocabulary): this.type  = set(SemanticContextModel.Vocab, vocab)
  def vocab: Option[DefaultVocabulary] = Option(fields.field(SemanticContextModel.Vocab))

  def withCuries(curies: Seq[CuriePrefix]): this.type  = setArray(SemanticContextModel.Curies, curies)
  def curies: Seq[CuriePrefix] = fields.field(SemanticContextModel.Curies)

  def withMapping(mapping: Seq[ContextMapping]): this.type  = setArray(SemanticContextModel.Mapping, mapping)
  def mapping: Seq[ContextMapping] = fields.field(SemanticContextModel.Mapping)

  def withTypeMappings(typeMappings: Seq[String]): this.type = set(SemanticContextModel.TypeMapping, typeMappings)
  def typeMappings: Seq[StrField] = fields.field(SemanticContextModel.TypeMapping)

  override def componentId: String = "/" + "@context".urlComponentEncoded
}

object SemanticContext {
  def apply(): SemanticContext = apply(Annotations())

  def apply(ast: YPart): SemanticContext = apply(Annotations(ast))

  def apply(annotations: Annotations): SemanticContext =
    new SemanticContext(Fields(), annotations)
}
