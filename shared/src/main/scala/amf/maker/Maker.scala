package amf.maker

import amf.model.DomainElement
import amf.unsafe.PlatformSecrets

/**
  * Maker class.
  */
trait Maker[T <: DomainElement[_, _]] extends PlatformSecrets {

  def make: T
}
