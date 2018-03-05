package amf.model.domain

import amf.plugins.domain.shapes.models

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class AnyShape(private[amf] val any: models.AnyShape) extends Shape(any) {

  @JSExportTopLevel("model.domain.AnyShape")
  def this() = this(models.AnyShape())

  override private[amf] def element = any

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: models.AnyShape => new AnyShape(l) })

  override def linkCopy(): DomainElement with Linkable = new AnyShape(element.linkCopy())

  def documentation: CreativeWork     = Option(any.documentation).map(amf.model.domain.CreativeWork).orNull
  def xmlSerialization: XMLSerializer = Option(any.xmlSerialization).map(amf.model.domain.XMLSerializer).orNull
  def examples: js.Iterable[Example]  = Option(any.examples).getOrElse(Nil).map(Example).toJSArray

  def withDocumentation(documentation: CreativeWork): this.type = {
    any.withDocumentation(documentation.element)
    this
  }

  def withXMLSerialization(xmlSerialization: XMLSerializer): this.type = {
    any.withXMLSerialization(xmlSerialization.xmlSerializer)
    this
  }

  def withExamples(examples: js.Iterable[Example]): this.type = {
    any.withExamples(examples.toList.map(_.element))
    this
  }
  def build(shape: models.AnyShape): Shape = platform.wrap[Shape](shape)
}
