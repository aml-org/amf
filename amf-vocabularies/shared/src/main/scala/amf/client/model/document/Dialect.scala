package amf.client.model.document

import amf.client.convert.VocabulariesClientConverter._
import amf.client.model.StrField
import amf.client.model.domain.{DocumentsModel, DomainElement, External, NodeMapping}
import amf.plugins.document.vocabularies.model.document.{
  Dialect => InternalDialect,
  DialectLibrary => InternalDialectLibrary
}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class Dialect(private[amf] val _internal: InternalDialect) extends BaseUnit with EncodesModel with DeclaresModel {

  @JSExportTopLevel("model.document.Dialect")
  def this() = this(InternalDialect())

  def name(): StrField                          = _internal.name()
  def version(): StrField                       = _internal.version()
  def nameAndVersion(): String                  = _internal.nameAndVersion()
  def header: String                            = _internal.header
  def isLibraryHeader(header: String): Boolean  = _internal.isLibraryHeader(header)
  def isFragmentHeader(header: String): Boolean = _internal.isFragmentHeader(header)
  def libraryHeaders: ClientList[String]        = _internal.libraryHeaders.asClient
  def fragmentHeaders: ClientList[String]       = _internal.fragmentHeaders.asClient
  def allHeaders: ClientList[String]            = _internal.allHeaders.asClient
  def externals: ClientList[External]           = _internal.externals.asClient
  def documents(): DocumentsModel               = DocumentsModel(_internal.documents())

  def withName(name: String): Dialect = {
    _internal.withName(name)
    this
  }

  def withVersion(version: String): Dialect = {
    _internal.withVersion(version)
    this
  }

  def withExternals(externals: ClientList[External]): Dialect = {
    _internal.withExternals(externals.asInternal)
    this
  }

  def withDocuments(documentsMapping: DocumentsModel): Dialect = {
    _internal.withDocuments(documentsMapping._internal)
    this
  }
}
