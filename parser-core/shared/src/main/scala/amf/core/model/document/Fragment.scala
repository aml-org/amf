package amf.core.model.document

import amf.core.metamodel.Obj
import amf.core.metamodel.document.{DocumentModel, FragmentModel}
import amf.core.model.domain.{AmfObject, DomainElement}

/**
  * Fragments: Units encoding domain fragments
  */
trait Fragment extends BaseUnit with EncodesModel {

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  override def references: Seq[BaseUnit] = fields(DocumentModel.References)

  override def encodes: DomainElement = fields(FragmentModel.Encodes)

  override def meta: Obj = FragmentModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = ""
}

trait EncodesModel extends AmfObject {

  /** Encoded [[DomainElement]] described in the document element. */
  def encodes: DomainElement

  def withEncodes(encoded: DomainElement): this.type = set(FragmentModel.Encodes, encoded)
}
