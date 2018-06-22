package amf.client.model.document

import amf.client.convert.VocabulariesClientConverter._
import amf.client.model.domain.{External, NodeMapping}
import amf.plugins.document.vocabularies.model.document.{DialectFragment => InternalDialectFragment}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class DialectFragment(private[amf] val _internal: InternalDialectFragment) extends BaseUnit with EncodesModel {

  @JSExportTopLevel("model.document.DialectFragment")
  def this() = this(InternalDialectFragment())

  override def encodes: NodeMapping   = _internal.encodes
  def externals: ClientList[External] = _internal.externals.asClient

  def withExternals(externals: ClientList[External]): DialectFragment = {
    _internal.withExternals(externals.asInternal)
    this
  }

  def withEncodes(nodeMapping: NodeMapping): DialectFragment = {
    _internal.withEncodes(nodeMapping._internal)
    this
  }
}
