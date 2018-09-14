package amf.client.model.domain

import amf.client.convert.VocabulariesClientConverter._
import amf.client.model.{BoolField, StrField}
import amf.plugins.document.vocabularies.model.domain.{DocumentMapping => InternalDocumentMapping, DocumentsModel => InternalDocumentsModel, PublicNodeMapping => InternalPublicNodeMapping}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class PublicNodeMapping(override private[amf] val _internal: InternalPublicNodeMapping) extends DomainElement {

  @JSExportTopLevel("model.domain.PublicNodeMapping")
  def this() = this(InternalPublicNodeMapping())

  def name(): StrField = _internal.name()
  def withName(name: String) = {
    _internal.withName(name)
    this
  }
  def mappedNode(): StrField = _internal.mappedNode()
  def withMappedNode(mappedNode: String) = {
    _internal.withMappedNode(mappedNode)
    this
  }
}

@JSExportAll
case class DocumentMapping(override private[amf] val _internal: InternalDocumentMapping) extends DomainElement {

  @JSExportTopLevel("model.domain.DocumentMapping")
  def this() = this(InternalDocumentMapping())

  def documentName(): StrField = _internal.documentName()
  def withDocumentName(name: String) = {
    _internal.withDocumentName(name)
    this
  }
  def encoded(): StrField = _internal.encoded()
  def withEncoded(encodedNode: String) = {
    _internal.withEncoded(encodedNode)
    this
  }
  def declaredNodes(): ClientList[PublicNodeMapping] = _internal.declaredNodes().asClient
  def withDeclaredNodes(declarations: ClientList[PublicNodeMapping]) = {
    _internal.withDeclaredNodes(declarations.asInternal)
  }
}

@JSExportAll
case class DocumentsModel(override private[amf] val _internal: InternalDocumentsModel) extends DomainElement {

  @JSExportTopLevel("model.domain.DocumentsModel")
  def this() = this(InternalDocumentsModel())

  def root(): DocumentMapping = DocumentMapping(_internal.root())
  def withRoot(documentMapping: DocumentMapping) = {
    _internal.withRoot(documentMapping._internal)
  }
  def fragments(): ClientList[DocumentMapping] = _internal.fragments().asClient
  def withFragments(fragments: ClientList[DocumentMapping]) = {
    _internal.withFragments(fragments.asInternal)
  }
  def library(): DocumentMapping = DocumentMapping(_internal.library())
  def withLibrary(library: DocumentMapping) = {
    _internal.withLibrary(library._internal)
  }

  def selfEncoded(): BoolField = _internal.selfEncoded()
  def withSelfEncoded(selfEncoded: Boolean): DocumentsModel = {
    _internal.withSelfEncoded(selfEncoded)
  }
}
