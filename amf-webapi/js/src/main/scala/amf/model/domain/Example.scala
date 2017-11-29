package amf.model.domain

import amf.plugins.domain.shapes.models

import scala.scalajs.js.annotation.JSExportAll

/**
  * Js example model class
  */
@JSExportAll
case class Example private[model] (private val example: models.Example) extends DomainElement with Linkable {

  def this() = this(models.Example())

  def name: String        = element.name
  def displayName: String = element.displayName
  def description: String = element.description
  def value: String       = element.value
  def strict: Boolean     = element.strict
  def mediaType: String   = element.mediaType

  def withName(name: String): this.type = {
    example.withName(name)
    this
  }
  def withDisplayName(displayName: String): this.type = {
    example.withDisplayName(displayName)
    this
  }
  def withDescription(description: String): this.type = {
    example.withDescription(description)
    this
  }
  def withValue(value: String): this.type = {
    example.withValue(value)
    this
  }
  def withStrict(strict: Boolean): this.type = {
    example.withStrict(strict)
    this
  }
  def withMediaType(mediaType: String): this.type = {
    example.withMediaType(mediaType)
    this
  }

  override private[amf] def element = example

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: models.Example => Example(l) })

  override def linkCopy(): DomainElement with Linkable = Example(element.linkCopy())
}
