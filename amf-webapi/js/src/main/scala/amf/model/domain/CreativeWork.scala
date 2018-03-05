package amf.model.domain

import amf.plugins.domain.shapes.models

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * JS CreativeWork model class.
  */
@JSExportAll
case class CreativeWork private[model] (private val creativeWork: models.CreativeWork) extends DomainElement {

  @JSExportTopLevel("model.domain.CreativeWork")
  def this() = this(models.CreativeWork())

  def url: String         = creativeWork.url
  def description: String = creativeWork.description
  def title: String       = creativeWork.title

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
