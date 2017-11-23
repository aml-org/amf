package amf.document

import amf.domain._
import amf.framework.parser.Annotations
import amf.metadata.document.DocumentModel._
import amf.metadata.document.{ExtensionLikeModel, OverlayModel}

/**
  * A [[Document]] is a parsing Unit that encodes a stand-alone [[amf.domain.DomainElement]] and can include references to other
  * [[amf.domain.DomainElement]]s that reference from the encoded [[amf.domain.DomainElement]]
  */
case class Document(fields: Fields, annotations: Annotations) extends BaseUnit with EncodesModel with DeclaresModel {

  override def references: Seq[BaseUnit] = fields(References)

  override def location: String = fields(Location)

  override def encodes: DomainElement = fields(Encodes)

  override def declares: Seq[DomainElement] = fields(Declares)

  /** Returns the usage comment for de element */
  override def usage: String = fields(Usage)

  override def adopted(parent: String): this.type = withId(parent)
}

object Document {
  def apply(): Document = apply(Annotations())

  def apply(annotations: Annotations): Document = Document(Fields(), annotations)
}

abstract class ExtensionLike(override val fields: Fields, override val annotations: Annotations)
    extends Document(fields, annotations) {
  override def encodes: WebApi = super.encodes.asInstanceOf[WebApi]
  def extend: String           = fields(ExtensionLikeModel.Extends)

  def withExtend(extend: BaseUnit): this.type = set(ExtensionLikeModel.Extends, extend)
}

class Overlay(override val fields: Fields, override val annotations: Annotations)
    extends ExtensionLike(fields, annotations)

object Overlay {
  def apply(): Overlay = apply(Annotations())

  def apply(annotations: Annotations): Overlay = new Overlay(Fields(), annotations)
}

class Extension(override val fields: Fields, override val annotations: Annotations)
    extends ExtensionLike(fields, annotations)

object Extension {
  def apply(): Extension = apply(Annotations())

  def apply(annotations: Annotations): Extension = new Extension(Fields(), annotations)
}
