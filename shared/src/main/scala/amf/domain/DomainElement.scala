package amf.domain

import amf.builder.Builder
import amf.model.AmfElement

/**
  * Internal model for any domain element
  */
trait DomainElement extends AmfElement {

  type T

  def toBuilder: Builder
}
