package amf.core.model.document

import amf.core.metamodel.Obj
import amf.core.metamodel.document.DocumentModel._
import amf.core.metamodel.document.{DocumentModel, ExtensionLikeModel}
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}

/**
  * A [[Document]] is a parsing Unit that encodes a stand-alone [[DomainElement]] and can include references to other
  * [[DomainElement]]s that reference from the encoded [[DomainElement]]
  */
case class Document(fields: Fields, annotations: Annotations) extends BaseUnit with EncodesModel with DeclaresModel {

  override def references: Seq[BaseUnit] = fields(References)

  override def encodes: DomainElement = fields(Encodes)

  override def declares: Seq[DomainElement] = fields(Declares)

  /** Meta data for the document */
  override def meta: Obj = DocumentModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = ""
}

object Document {
  def apply(): Document = apply(Annotations())

  def apply(annotations: Annotations): Document = Document(Fields(), annotations)
}

abstract class ExtensionLike[T <: DomainElement](override val fields: Fields, override val annotations: Annotations)
    extends Document(fields, annotations) {
  override def encodes: T = super.encodes.asInstanceOf[T]
  def extend: String      = fields(ExtensionLikeModel.Extends)

  def withExtend(extend: BaseUnit): this.type = set(ExtensionLikeModel.Extends, extend)
}
