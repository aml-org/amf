package amf.plugins.document.vocabularies.model.domain

import amf.client.model.StrField
import amf.core.metamodel.Obj
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies.metamodel.domain.{DocumentMappingModel, DocumentsModelModel, PublicNodeMappingModel}
import amf.plugins.document.vocabularies.metamodel.domain.DocumentMappingModel._
import amf.plugins.document.vocabularies.metamodel.domain.PublicNodeMappingModel._
import amf.plugins.document.vocabularies.metamodel.domain.DocumentsModelModel._
import org.yaml.model.{YMap, YMapEntry, YNode}

case class PublicNodeMapping(fields: Fields, annotations: Annotations) extends DomainElement {
  override def meta: Obj = PublicNodeMappingModel
  override def adopted(parent: String): PublicNodeMapping.this.type = withId(parent)

  def name(): StrField = fields.field(Name)
  def withName(name: String) = set(Name, name)
  def mappedNode(): StrField = fields.field(MappedNode)
  def withMappedNode(mappedNode: String) = set(MappedNode, mappedNode)
}

object PublicNodeMapping {
  def apply(): PublicNodeMapping = apply(Annotations())
  def apply(ast: YMapEntry): PublicNodeMapping = apply(Annotations(ast))
  def apply(annotations: Annotations): PublicNodeMapping = PublicNodeMapping(Fields(), annotations)
}

case class DocumentMapping(fields: Fields, annotations: Annotations) extends DomainElement {
  override def meta: Obj = DocumentMappingModel
  override def adopted(parent: String): DocumentMapping.this.type = withId(parent)

  def documentName(): StrField = fields.field(DocumentName)
  def withDocumentName(name: String) = set(DocumentName, name)
  def encoded(): StrField = fields.field(EncodedNode)
  def withEncoded(encodedNode: String) = set(EncodedNode, encodedNode)
  def declaredNodes(): Seq[PublicNodeMapping] = fields.field(DeclaredNodes)
  def withDeclaredNodes(fragments: Seq[PublicNodeMapping]) = setArrayWithoutId(DeclaredNodes, fragments)
}

object DocumentMapping {
  def apply(): DocumentMapping = apply(Annotations())
  def apply(ast: YNode): DocumentMapping = apply(Annotations(ast))
  def apply(annotations: Annotations): DocumentMapping = DocumentMapping(Fields(), annotations)
}


case class DocumentsModel(fields: Fields, annotations: Annotations) extends DomainElement {
  override def meta: Obj = DocumentsModelModel
  override def adopted(parent: String): DocumentsModel.this.type = withId(parent)

  def root(): DocumentMapping = fields.field(Root)
  def withRoot(documentMapping: DocumentMapping) = set(Root, documentMapping)
  def fragments(): Seq[DocumentMapping] = fields.field(Fragments)
  def withFragments(fragments: Seq[DocumentMapping]) = setArrayWithoutId(Fragments, fragments)
  def library(): DocumentMapping = fields.field(Library)
  def withLibrary(library: DocumentMapping) = set(Library, library)
}

object DocumentsModel {
  def apply(): DocumentsModel = apply(Annotations())
  def apply(ast: YMap): DocumentsModel = apply(Annotations(ast))
  def apply(annotations: Annotations): DocumentsModel = DocumentsModel(Fields(), annotations)
}
