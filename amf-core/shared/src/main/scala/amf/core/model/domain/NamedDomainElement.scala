package amf.core.model.domain

/**
  * All DomainElements supporting name
  */
trait NamedDomainElement {

  /** Return [[DomainElement]] name. */
  def name: String

  /** Update [[DomainElement]] name. */
  def withName(name: String): this.type

}
