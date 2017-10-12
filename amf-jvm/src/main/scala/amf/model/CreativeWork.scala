package amf.model

/**
  * JVM CreativeWork model class.
  */
case class CreativeWork private[model] (private val creativeWork: amf.domain.CreativeWork) extends DomainElement {

  def this() = this(amf.domain.CreativeWork())

  val url: String         = creativeWork.url
  val description: String = creativeWork.description
  val title: String       = creativeWork.title

  override private[amf] def element: amf.domain.CreativeWork = creativeWork

  /** Set url property of this [[CreativeWork]]. */
  def withUrl(url: String): this.type = {
    creativeWork.withUrl(url)
    this
  }

  /** Set title property of this [[CreativeWork]]. */
  def withTitle(title: String): this.type = {
    creativeWork.withTitle(title)
    this
  }

  /** Set description property of this [[CreativeWork]]. */
  def withDescription(description: String): this.type = {
    creativeWork.withDescription(description)
    this
  }
}
