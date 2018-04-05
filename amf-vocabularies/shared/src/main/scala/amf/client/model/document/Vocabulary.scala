package amf.client.model.document

import amf.client.convert.VocabulariesClientConverter._
import amf.client.model.StrField
import amf.client.model.domain._
import amf.core.parser.Annotations
import amf.plugins.document.vocabularies.metamodel.document.VocabularyModel
import amf.plugins.document.vocabularies.model.document.{Vocabulary => InternalVocabulary}
import amf.plugins.document.vocabularies.model.domain.{
  ClassTerm => InternalClassTerm,
  DatatypePropertyTerm => InternalDatatypePropertyTerm,
  ObjectPropertyTerm => InternalObjectPropertyTerm
}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class Vocabulary(private[amf] val _internal: InternalVocabulary) extends BaseUnit with DeclaresModel {

  @JSExportTopLevel("model.document.Vocabulary")
  def this() = this(InternalVocabulary())

  def name: StrField        = _internal.name
  def description: StrField = _internal.usage // Just an alias... do we need description field?

  def base: StrField                           = _internal.base
  def imports: ClientList[VocabularyReference] = _internal.imports.asClient
  def externals: ClientList[External]          = _internal.externals.asClient

  def withName(name: String): Vocabulary = {
    _internal.withName(name)
    this
  }
  def withBase(base: String): Vocabulary = {
    _internal.withBase(base)
    this
  }
  def withExternals(externals: ClientList[External]): Vocabulary = {
    _internal.withExternals(externals.asInternal)
    this
  }
  def withImports(vocabularies: ClientList[VocabularyReference]): Vocabulary = {
    _internal.withImports(vocabularies.asInternal)
    this
  }

  def objectPropertyTerms(): ClientList[ObjectPropertyTerm] =
    _internal.declares.collect { case term: InternalObjectPropertyTerm => term }.asClient

  def datatypePropertyTerms(): ClientList[DatatypePropertyTerm] =
    _internal.declares.collect { case term: InternalDatatypePropertyTerm => term }.asClient

  def classTerms(): ClientList[ClassTerm] =
    _internal.declares.collect { case term: InternalClassTerm => term }.asClient
}
