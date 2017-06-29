package amf.builder

import amf.model.DomainElement

/**
  * Created by martin.gutierrez on 6/29/17.
  */
trait Builder[T <: DomainElement] {
  def build: T
}
