package amf.model

/**
  * JVM License model class.
  */
case class License private[model] (private val license: amf.domain.License) extends DomainElement {

  def this() = this(amf.domain.License())

  val url: String  = license.url
  val name: String = license.name

  override private[amf] def element: amf.domain.License = license

  /** Set url property of this [[License]]. */
  def withUrl(url: String): this.type = {
    license.withUrl(url)
    this
  }

  /** Set name property of this [[License]]. */
  def withName(name: String): this.type = {
    license.withName(name)
    this
  }
}
