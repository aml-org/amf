package amf.plugins.document.vocabularies.model.domain

import amf.core.metamodel.Obj
import amf.core.model.{BoolField, StrField}
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies.metamodel.domain.DocumentMappingModel._
import amf.plugins.document.vocabularies.metamodel.domain.DocumentsModelModel._
import amf.plugins.document.vocabularies.metamodel.domain.PublicNodeMappingModel._
import amf.plugins.document.vocabularies.metamodel.domain.{
  DocumentMappingModel,
  DocumentsModelModel,
  PublicNodeMappingModel
}
import org.yaml.model.{YMap, YMapEntry, YNode}

case class PublicNodeMapping(fields: Fields, annotations: Annotations) extends DomainElement {

  def name(): StrField       = fields.field(Name)
  def mappedNode(): StrField = fields.field(MappedNode)

  def withName(name: String): PublicNodeMapping             = set(Name, name)
  def withMappedNode(mappedNode: String): PublicNodeMapping = set(MappedNode, mappedNode)

  override def meta: Obj = PublicNodeMappingModel

  override def adopted(parent: String): this.type = {
    if (Option(id).isEmpty) {
      simpleAdoption(parent)
    }
    this
  }

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = ""
}

object PublicNodeMapping {
  def apply(): PublicNodeMapping                         = apply(Annotations())
  def apply(ast: YMapEntry): PublicNodeMapping           = apply(Annotations(ast))
  def apply(annotations: Annotations): PublicNodeMapping = PublicNodeMapping(Fields(), annotations)
}

case class DocumentMapping(fields: Fields, annotations: Annotations) extends DomainElement {

  def documentName(): StrField                = fields.field(DocumentName)
  def encoded(): StrField                     = fields.field(EncodedNode)
  def declaredNodes(): Seq[PublicNodeMapping] = fields.field(DeclaredNodes)

  def withDocumentName(name: String): DocumentMapping   = set(DocumentName, name)
  def withEncoded(encodedNode: String): DocumentMapping = set(EncodedNode, encodedNode)
  def withDeclaredNodes(fragments: Seq[PublicNodeMapping]): DocumentMapping =
    setArrayWithoutId(DeclaredNodes, fragments)

  override def meta: Obj = DocumentMappingModel

  override def adopted(parent: String): this.type = {
    if (Option(id).isEmpty) {
      simpleAdoption(parent)
    }
    this
  }

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = ""
}

object DocumentMapping {
  def apply(): DocumentMapping                         = apply(Annotations())
  def apply(ast: YNode): DocumentMapping               = apply(Annotations(ast))
  def apply(annotations: Annotations): DocumentMapping = DocumentMapping(Fields(), annotations)
}

case class DocumentsModel(fields: Fields, annotations: Annotations) extends DomainElement {
  def root(): DocumentMapping           = fields.field(Root)
  def library(): DocumentMapping        = fields.field(Library)
  def fragments(): Seq[DocumentMapping] = fields.field(Fragments)
  def selfEncoded(): BoolField          = fields.field(SelfEncoded)
  def declarationsPath(): StrField      = fields.field(DeclarationsPath)

  def withRoot(documentMapping: DocumentMapping): DocumentsModel     = set(Root, documentMapping)
  def withLibrary(library: DocumentMapping): DocumentsModel          = set(Library, library)
  def withFragments(fragments: Seq[DocumentMapping]): DocumentsModel = setArrayWithoutId(Fragments, fragments)
  def withSelfEncoded(selfEncoded: Boolean): DocumentsModel          = set(SelfEncoded, selfEncoded)
  def withDeclarationsPath(declarationsPath: String): DocumentsModel = set(DeclarationsPath, declarationsPath)

  override def meta: Obj = DocumentsModelModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/documents"
}

object DocumentsModel {
  def apply(): DocumentsModel                         = apply(Annotations())
  def apply(ast: YMap): DocumentsModel                = apply(Annotations(ast))
  def apply(annotations: Annotations): DocumentsModel = DocumentsModel(Fields(), annotations)
}
