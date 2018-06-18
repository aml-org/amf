package amf.core.model

import amf.core.parser.Annotations

trait Annotable {

  /** Return annotations. */
  def annotations(): Annotations
}
