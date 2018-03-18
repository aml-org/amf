package amf.client.model.document

import amf.client.convert.VocabulariesClientConverter._
import amf.client.model.StrField
import amf.client.model.domain.{External, VocabularyReference}
import amf.plugins.document.vocabularies.model.document.{Vocabulary => InternalVocabulary}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class Vocabulary(private[amf] val _internal: InternalVocabulary) extends BaseUnit with DeclaresModel {

  @JSExportTopLevel("model.document.Vocabulary")
  def this() = this(InternalVocabulary())

  def name: StrField = _internal.name
  def base: StrField = _internal.base
  def imports: ClientList[VocabularyReference] = _internal.imports.asClient
  def externals: ClientList[External] = _internal.externals.asClient

  def withBase(base: String) = {
    _internal.withBase(base)
    this
  }
  def withExternals(externals: ClientList[External]) = {
    _internal.withExternals(externals.asInternal)
    this
  }
  def withImports(vocabularies: ClientList[VocabularyReference]) = {
    _internal.withImports(vocabularies.asInternal)
    this
  }

}
