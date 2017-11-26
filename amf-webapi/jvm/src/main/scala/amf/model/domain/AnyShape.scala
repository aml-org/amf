package amf.model.domain

import amf.plugins.domain.shapes.models

import scala.collection.JavaConverters._

class AnyShape(private[amf] val any: models.AnyShape) extends Shape(any) {
  override private[amf] def element = any

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: models.AnyShape => new AnyShape(l) })

  override def linkCopy(): DomainElement with Linkable = new AnyShape(element.linkCopy())

  val documentation: CreativeWork     = Option(any.documentation).map(amf.model.domain.CreativeWork).orNull
  val xmlSerialization: XMLSerializer = Option(any.xmlSerialization).map(amf.model.domain.XMLSerializer).orNull
  val examples: java.util.List[Example]  = any.examples.map(Example).asJava

  def withDocumentation(documentation: CreativeWork): this.type = {
    any.withDocumentation(documentation.element)
    this
  }

  def withXMLSerialization(xmlSerialization: XMLSerializer): this.type = {
    any.withXMLSerialization(xmlSerialization.xmlSerializer)
    this
  }

  def withExamples(examples: java.util.List[Example]): this.type = {
    any.withExamples(examples.asScala.map(_.element))
    this
  }
  def build(shape: models.AnyShape): Shape = platform.wrap(shape)
}
