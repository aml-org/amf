package amf.document

import amf.domain._
import amf.metadata.document.DocumentModel._

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

  def withLocation(location: String): this.type                 = set(Location, location)
  def withReferences(references: Seq[BaseUnit]): this.type      = setArray(References, references)
  def withEncodes(encoded: DomainElement): this.type            = set(Encodes, encoded)
  def withDeclares(declarations: Seq[DomainElement]): this.type = setArrayWithoutId(Declares, declarations)
  def withUsage(usage: String): this.type                       = set(Usage, usage)

  override def adopted(parent: String): this.type = withId(parent)
}

object Document {
  def apply(): Document = apply(Annotations())

  def apply(annotations: Annotations): Document = new Document(Fields(), annotations)
}
