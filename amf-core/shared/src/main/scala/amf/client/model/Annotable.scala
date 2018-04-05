package amf.client.model

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait Annotable {

  /** Return annotations. */
  def annotations(): Annotations
}
