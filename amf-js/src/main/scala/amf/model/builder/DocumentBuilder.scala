package amf.model.builder

import amf.document.BaseUnit
import amf.domain.DomainElement
import amf.model.Document

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class DocumentBuilder(private val documentBuilder: amf.builder.DocumentBuilder = amf.builder.DocumentBuilder())
    extends Builder {

  def withLocation(location: String): DocumentBuilder = {
    documentBuilder.withLocation(location)
    this
  }

  def withReferences(references: js.Iterable[BaseUnit]): DocumentBuilder = {
    documentBuilder.withReferences(references.toList)
    this
  }

  def withEncodes(element: DomainElement): DocumentBuilder = {
    documentBuilder.withEncodes(element)
    this
  }

  def build: Document = Document(documentBuilder.build)
}
