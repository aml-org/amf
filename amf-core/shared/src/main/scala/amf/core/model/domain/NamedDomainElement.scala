package amf.core.model.domain

import amf.core.model.StrField

/**
  * All DomainElements supporting name
  */
trait NamedDomainElement {

  /** Return [[DomainElement]] name. */
  def name: StrField

  /** Update [[DomainElement]] name. */
  def withName(name: String): this.type

}
