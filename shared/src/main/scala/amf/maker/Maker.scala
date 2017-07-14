package amf.maker

import amf.domain.DomainElement
import amf.unsafe.PlatformSecrets

/**
  * Maker class.
  */
trait Maker[T <: DomainElement] extends PlatformSecrets {

  def make: T
}
