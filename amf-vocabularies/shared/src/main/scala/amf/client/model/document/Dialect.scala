package amf.client.model.document

import amf.client.convert.VocabulariesClientConverter._
import amf.client.model.StrField
import amf.client.model.domain.{DocumentsModel, DomainElement, External, NodeMapping}
import amf.plugins.document.vocabularies.model.document.{Dialect => InternalDialect, DialectFragment => InternalDialectFragment, DialectLibrary => InternalDialectLibrary}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class Dialect(private[amf] val _internal: InternalDialect) extends BaseUnit with EncodesModel with DeclaresModel {

  @JSExportTopLevel("model.document.Dialect")
  def this() = this(InternalDialect())

  def nameAndVersion(): String = _internal.nameAndVersion()

  def name(): StrField = _internal.name()
  def withName(name: String) = {
    _internal.withName(name)
    this
  }
  def version(): StrField  = _internal.version()
  def withVersion(version: String) = {
    _internal.withVersion(version)
    this
  }
  def externals: ClientList[External] = _internal.externals.asClient
  def withExternals(externals: ClientList[External]) = {
    _internal.withExternals(externals.asInternal)
  }
  def documents(): DocumentsModel = DocumentsModel(_internal.documents())
  def withDocuments(documentsMapping: DocumentsModel) = {
    _internal.withDocuments(documentsMapping._internal)
    this
  }

  def header = _internal.header
  def libraryHeaders = _internal.libraryHeaders.asClient
  def isLibraryHeader(header: String) = _internal.isLibraryHeader(header)
  def fragmentHeaders = _internal.fragmentHeaders.asClient
  def isFragmentHeader(header: String) = _internal.isFragmentHeader(header)
  def allHeaders = _internal.allHeaders.asClient
}

@JSExportAll
class DialectFragment(private[amf] val _internal: InternalDialectFragment) extends BaseUnit with EncodesModel {

  @JSExportTopLevel("model.document.DialectFragment")
  def this() = this(InternalDialectFragment())

  def externals: ClientList[External] = _internal.externals.asClient
  def withExternals(externals: ClientList[External]) = {
    _internal.withExternals(externals.asInternal)
  }

  override def encodes: DomainElement = NodeMapping(_internal.encodes)
  def withEncodes(nodeMapping: NodeMapping) = {
    _internal.withEncodes(nodeMapping._internal)
    this
  }
}

class DialectLibrary(private[amf] val _internal: InternalDialectLibrary) extends BaseUnit with DeclaresModel {

  def externals: ClientList[External] = _internal.externals.asClient
  def withExternals(externals: ClientList[External]) = {
    _internal.withExternals(externals.asInternal)
  }

  /*
  def nodeMappings(): ClientList[NodeMapping] = {
    _internal.declares.filter(_.isInstanceOf[NodeMapping]).asInstanceOf[Seq[NodeMapping]].asClient
  }
  */
  def withNodeMappings(nodeMappings: ClientList[NodeMapping]) = {
    _internal.withDeclares(nodeMappings.asInternal)
    this
  }
}

