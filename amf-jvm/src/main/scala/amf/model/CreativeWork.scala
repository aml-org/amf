package amf.model

/**
  * CreativeWork jvm class
  */
case class CreativeWork private[model] (private val creativeWork: amf.domain.CreativeWork) extends DomainElement {

  def this() = this(amf.domain.CreativeWork())

  val url: String         = creativeWork.url
  val description: String = creativeWork.description

  override def equals(other: Any): Boolean = other match {
    case that: CreativeWork =>
      (that canEqual this) &&
        creativeWork == that.creativeWork
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[CreativeWork]

  override private[amf] def element: amf.domain.CreativeWork = creativeWork

  def withUrl(url: String): this.type = {
    creativeWork.withUrl(url)
    this
  }

  def withDescription(description: String): this.type = {
    creativeWork.withDescription(description)
    this
  }
}
