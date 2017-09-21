package amf.model

import scala.scalajs.js.annotation.JSExportAll

/**
  * JS CreativeWork model class.
  */
@JSExportAll
case class CreativeWork private[model] (private val creativeWork: amf.domain.CreativeWork) extends DomainElement {

  def this() = this(amf.domain.CreativeWork())

  val url: String         = creativeWork.url
  val description: String = creativeWork.description

  override private[amf] def element: amf.domain.CreativeWork = creativeWork

  /** Set url property of this [[CreativeWork]]. */
  def withUrl(url: String): this.type = {
    creativeWork.withUrl(url)
    this
  }

  /** Set description property of this [[CreativeWork]]. */
  def withDescription(description: String): this.type = {
    creativeWork.withDescription(description)
    this
  }
}
