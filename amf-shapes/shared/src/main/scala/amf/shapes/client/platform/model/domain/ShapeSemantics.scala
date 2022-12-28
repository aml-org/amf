package amf.shapes.client.platform.model.domain

import amf.core.client.platform.model.domain.DomainElement
import amf.core.client.platform.model.{BoolField, StrField}
import amf.shapes.client.scala.model.domain.{
  BaseIri => InternalBaseIri,
  ContextMapping => InternalContextMapping,
  CuriePrefix => InternalCuriePrefix,
  DefaultVocabulary => InternalDefaultVocabulary,
  SemanticContext => InternalSemanticContext
}
import amf.shapes.internal.convert.ShapeClientConverters.{ClientList, _}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class BaseIri(override private[amf] val _internal: InternalBaseIri) extends DomainElement {
  @JSExportTopLevel("BaseIri")
  def this() = this(InternalBaseIri())

  def withIri(iri: String): this.type = {
    _internal.withIri(iri)
    this
  }

  def iri: StrField = _internal.iri

  def withNulled(nulled: Boolean): this.type = {
    _internal.withNulled(nulled)
    this
  }

  def nulled: BoolField = _internal.nulled
}

@JSExportAll
case class DefaultVocabulary(override private[amf] val _internal: InternalDefaultVocabulary) extends DomainElement {
  @JSExportTopLevel("DefaultVocabulary")
  def this() = this(InternalDefaultVocabulary())

  def withIri(iri: String): this.type = {
    _internal.withIri(iri)
    this
  }

  def iri: StrField = _internal.iri
}

@JSExportAll
case class CuriePrefix(override private[amf] val _internal: InternalCuriePrefix) extends DomainElement {
  @JSExportTopLevel("CuriePrefix")
  def this() = this(InternalCuriePrefix())

  def withIri(iri: String): this.type = {
    _internal.withIri(iri)
    this
  }

  def iri: StrField = _internal.iri

  def withAlias(alias: String): this.type = {
    _internal.withAlias(alias)
    this
  }

  def alias: StrField = _internal.alias
}

@JSExportAll
case class ContextMapping(override private[amf] val _internal: InternalContextMapping) extends DomainElement {
  @JSExportTopLevel("ContextMapping")
  def this() = this(InternalContextMapping())

  def withIri(iri: String): this.type = {
    _internal.withIri(iri)
    this
  }

  def iri: StrField = _internal.iri

  def withAlias(alias: String): this.type = {
    _internal.withAlias(alias)
    this
  }

  def alias: StrField = {
    asClient(_internal.alias)
  }

  def withCoercion(coersion: String): this.type = {
    _internal.withCoercion(coersion)
    this
  }

  def coercion: StrField = _internal.coercion

  def withNulled(nulled: Boolean): this.type = {
    _internal.withNulled(nulled)
    this
  }

  def nulled: BoolField = _internal.nulled

  def withContainers(containers: ClientList[String]): this.type = {
    _internal.withContainers(containers.asInternal)
    this
  }

  def containers: ClientList[StrField] = _internal.containers.asClient

}

@JSExportAll
case class SemanticContext(override private[amf] val _internal: InternalSemanticContext) extends DomainElement {
  @JSExportTopLevel("SemanticContext")
  def this() = this(InternalSemanticContext())

  def withIri(iri: String): this.type = {
    _internal.withIri(iri)
    this
  }

  def iri: StrField = _internal.iri

  def withBase(base: BaseIri): this.type = {
    _internal.withBase(base) // es as Internal o asClient?
    this
  }

  def base: ClientOption[BaseIri] = _internal.base.asClient

  def withVocab(vocab: DefaultVocabulary): this.type = {
    _internal.withVocab(vocab._internal)
    this
  }

  def vocab: ClientOption[DefaultVocabulary] = _internal.vocab.asClient

  def withCuries(curies: ClientList[CuriePrefix]): this.type = {
    _internal.withCuries(curies.asInternal)
    this
  }

  def curies: ClientList[CuriePrefix] = _internal.curies.asClient

  def withMapping(mapping: ClientList[ContextMapping]): this.type = {
    _internal.withMapping(mapping.asInternal)
    this
  }

  def mapping: ClientList[ContextMapping] = _internal.mapping.asClient

  def withTypeMappings(typeMappings: ClientList[String]): this.type = {
    _internal.withTypeMappings(typeMappings.asInternal)
    this
  }

  def typeMappings: ClientList[StrField] = _internal.typeMappings.asClient

  def withOverrideMappings(overrideMappings: ClientList[String]): this.type = {
    _internal.withOverrideMappings(overrideMappings.asInternal)
    this
  }

  def overrideMappings: ClientList[StrField] = _internal.overrideMappings.asClient

}
