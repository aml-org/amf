package amf.document

import amf.builder.DocumentBuilder
import amf.domain._
import amf.metadata.document.DocumentModel._
import amf.transform.MutableElement

/**
  * A [[Document]] is a parsing Unit that encodes a stand-alone [[DomainElement]] and can include references to other
  * [[DomainElement]]s that reference from the encoded [[DomainElement]]
  */
case class Document(fields: Fields, annotations: Annotations)
    extends BaseUnit
    with MutableElement
    with EncodesModel
    with DeclaresModel {

  override def references: Seq[BaseUnit] = fields(References)

  override def location: String = fields(Location)

  override def encodes: WebApi = fields(Encodes)

  override def declares: Seq[DomainElement] = fields(Declares)

  def withLocation(location: String): this.type            = set(Location, location)
  def withReferences(references: Seq[BaseUnit]): this.type = setArray(References, references)
  def withEncodes(element: DomainElement): this.type       = set(Encodes, element)
}

object Document {
  def apply(): Document = new Document(Fields(), Annotations())

  def apply(fields: Fields, annotations: Annotations): Document = new Document(fields, annotations)
}
