package amf.model.builder

import amf.document.BaseUnit
import amf.model.{Document, DomainElement}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class DocumentBuilder(private val documentBuilder: amf.builder.DocumentBuilder = amf.builder.DocumentBuilder())
    extends Builder {

  def this() = this(amf.builder.DocumentBuilder())

  def withLocation(location: String): DocumentBuilder = {
    documentBuilder.withLocation(location)
    this
  }

  def withReferences(references: js.Iterable[BaseUnit]): DocumentBuilder = {
    documentBuilder.withReferences(references.toList)
    this
  }

  def withEncodes(element: DomainElement): DocumentBuilder = {
    documentBuilder.withEncodes(element.element)
    this
  }

  def build: Document = Document(documentBuilder.build)
}
