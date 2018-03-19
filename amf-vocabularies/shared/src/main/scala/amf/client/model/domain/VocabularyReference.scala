package amf.client.model.domain

import amf.client.model.StrField
import amf.plugins.document.vocabularies.model.domain.{VocabularyReference => InternalVocabularyReference}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}


@JSExportAll
case class VocabularyReference(override private[amf] val _internal: InternalVocabularyReference) extends DomainElement {

  @JSExportTopLevel("model.domain.VocabularyReference")
  def this() = this(InternalVocabularyReference())

  def withAlias(alias: String) = {
    _internal.withAlias(alias)
    this
  }
  def withReference(reference: String) = {
    _internal.withReference(reference)
    this
  }

  def alias: StrField = _internal.alias
  def reference: StrField = _internal.reference
}
