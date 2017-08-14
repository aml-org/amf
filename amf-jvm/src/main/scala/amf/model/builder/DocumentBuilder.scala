package amf.model.builder

import amf.document.BaseUnit
import amf.model.{Document, DomainElement}

import scala.collection.JavaConverters._

case class DocumentBuilder private (
    private val documentBuilder: amf.builder.DocumentBuilder = amf.builder.DocumentBuilder())
    extends Builder {

  def this() = this(amf.builder.DocumentBuilder())

  def withLocation(location: String): DocumentBuilder = {
    documentBuilder.withLocation(location)
    this
  }

  def withReferences(references: java.util.List[BaseUnit]): DocumentBuilder = {
    documentBuilder.withReferences(references.asScala)
    this
  }

  def withEncodes(element: DomainElement): DocumentBuilder = {
    documentBuilder.withEncodes(element.element)
    this
  }

  def build: Document = Document(documentBuilder.build)
}
