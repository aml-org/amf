package amf.model

import amf.plugins.domain.webapi.models


import scala.scalajs.js.annotation.JSExportAll

/**
  * JS CreativeWork model class.
  */
@JSExportAll
case class CreativeWork private[model] (private val creativeWork: models.CreativeWork) extends DomainElement {

  def this() = this(models.CreativeWork())

  val url: String         = creativeWork.url
  val description: String = creativeWork.description
  val title: String       = creativeWork.title

  override private[amf] def element: models.CreativeWork = creativeWork

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
