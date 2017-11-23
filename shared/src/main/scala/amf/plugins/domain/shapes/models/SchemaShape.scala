package amf.plugins.domain.shapes.models

import amf.domain.Fields
import amf.framework.parser.Annotations
import amf.plugins.domain.shapes.metamodel.SchemaShapeModel
import amf.plugins.domain.shapes.metamodel.SchemaShapeModel._
import org.yaml.model.YPart

case class SchemaShape(fields: Fields, annotations: Annotations) extends Shape {

  def raw: String       = fields(Raw)
  def mediaType: String = fields(MediaType)

  def withRaw(text: String): SchemaShape            = set(Raw, text)
  def withMediaType(mediaType: String): SchemaShape = set(MediaType, mediaType)

  override def linkCopy(): SchemaShape = SchemaShape().withId(id)
  override def adopted(parent: String) = withId(parent + "/schema/" + name)

  override def meta = SchemaShapeModel
}

object SchemaShape {

  def apply(): SchemaShape = apply(Annotations())

  def apply(ast: YPart): SchemaShape = apply(Annotations(ast))

  def apply(annotations: Annotations): SchemaShape = SchemaShape(Fields(), annotations)
}
