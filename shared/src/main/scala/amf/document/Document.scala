package amf.document

import amf.domain.{DomainElement, FieldsInstance, Fields}
import amf.metadata.document.DocumentModel._

/**
  * A [[Document]] is a parsing Unit that encodes a stand-alone [[DomainElement]] and can include references to other
  * [[DomainElement]]s that reference from the encoded [[DomainElement]]
  */
case class Document(fields: Fields) extends BaseUnit with EncodesModel with DeclaresModel {

  override val references: Seq[BaseUnit] = fields(References)

  override val location: String = fields(Location)

  override val encodes: DomainElement = fields(Encodes)

  val declares: Seq[DomainElement] = fields(Declares)
}

object Document {
  def apply(fields: Fields): Document = new Document(fields)
}
