package amf.client.model

import amf.core.parser.Annotations

import scala.scalajs.js.annotation.JSExportAll
@JSExportAll
trait Annotable {

  /** Return annotations. */
  def annotations(): Annotations
}
