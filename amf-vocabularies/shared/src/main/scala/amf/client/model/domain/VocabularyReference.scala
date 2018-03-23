package amf.client.model.domain

import amf.client.convert.VocabulariesClientConverter._
import amf.client.model.StrField
import amf.plugins.document.vocabularies.model.domain.{VocabularyReference => InternalVocabularyReference}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class VocabularyReference(override private[amf] val _internal: InternalVocabularyReference)
    extends DomainElement {

  @JSExportTopLevel("model.domain.VocabularyReference")
  def this() = this(InternalVocabularyReference())

  def alias: StrField     = _internal.alias
  def reference: StrField = _internal.reference

  def withAlias(alias: String): VocabularyReference         = _internal.withAlias(alias)
  def withReference(reference: String): VocabularyReference = _internal.withReference(reference)
}
