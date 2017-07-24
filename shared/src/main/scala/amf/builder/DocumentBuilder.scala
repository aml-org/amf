package amf.builder

import amf.document.{BaseUnit, Document}
import amf.domain.DomainElement
import amf.metadata.document.DocumentModel._

/**
  * Document builder
  */
class DocumentBuilder extends Builder {

  override type T = Document

  def withLocation(location: String): DocumentBuilder = set(Location, location)

  def withReferences(references: Seq[BaseUnit]): DocumentBuilder = set(References, references)

  def withEncodes(element: DomainElement): DocumentBuilder = set(Encodes, element)

  override def build: Document = Document(fields)
}

object DocumentBuilder {
  def apply(): DocumentBuilder = new DocumentBuilder()
}
