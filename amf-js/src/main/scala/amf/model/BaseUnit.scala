package amf.model

import org.scalajs.dom.experimental.URL

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/** Any parseable unit, backed by a source URI. */
@JSExportAll
trait BaseUnit {

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  val references: js.Iterable[BaseUnit]

  /** Returns the file location for the document that has been parsed to generate this model */
  val location: URL

  def id(parent: String): String = parent
}
