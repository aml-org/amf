package amf.shape

import amf.domain.Fields
import amf.framework.parser.Annotations
import org.yaml.model.YPart
import amf.metadata.shape.SchemaShapeModel._

case class SchemaShape(fields: Fields, annotations: Annotations) extends Shape {

  def raw: String       = fields(Raw)
  def mediaType: String = fields(MediaType)

  def withRaw(text: String): SchemaShape            = set(Raw, text)
  def withMediaType(mediaType: String): SchemaShape = set(MediaType, mediaType)

  override def linkCopy(): SchemaShape = SchemaShape().withId(id)
  override def adopted(parent: String) = withId(parent + "/schema/" + name)
}

object SchemaShape {

  def apply(): SchemaShape = apply(Annotations())

  def apply(ast: YPart): SchemaShape = apply(Annotations(ast))

  def apply(annotations: Annotations): SchemaShape = SchemaShape(Fields(), annotations)
}
