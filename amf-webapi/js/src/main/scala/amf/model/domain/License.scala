package amf.model.domain

import amf.plugins.domain.webapi.models

import scala.scalajs.js.annotation.JSExportAll

/**
  * JS License model class.
  */
@JSExportAll
case class License private[model] (private val license: models.License) extends DomainElement {

  def this() = this(models.License())

  def url: String  = license.url
  def name: String = license.name

  override protected[amf] def element: models.License = license

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
