package amf.model.builder

import amf.document.BaseUnit
import amf.domain.DomainElement
import amf.model.Document
import scala.collection.JavaConverters._

case class DocumentBuilder(private val documentBuilder: amf.builder.DocumentBuilder = amf.builder.DocumentBuilder())
    extends Builder {

  def withLocation(location: String): DocumentBuilder = {
    documentBuilder.withLocation(location)
    this
  }

  def withReferences(references: java.util.List[BaseUnit]): DocumentBuilder = {
    documentBuilder.withReferences(references.asScala)
    this
  }

  def withEncodes(element: DomainElement): DocumentBuilder = {
    documentBuilder.withEncodes(element)
    this
  }

  def build: Document = Document(documentBuilder.build)
}
