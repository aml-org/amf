package amf.model

import amf.document

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/** Any parsable unit, backed by a source URI. */
@JSExportAll
trait BaseUnit {

  private[amf] val element: amf.document.BaseUnit

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  lazy val references: js.Iterable[BaseUnit] = {
    val units: Seq[BaseUnit] = element.references map {
      case r: document.Module => Module(r)
    }
    units.toJSArray
  }

  /** Returns the file location for the document that has been parsed to generate this model */
  def location: String = element.location

  def usage: String = element.usage
}
