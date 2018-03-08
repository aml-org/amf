package amf.plugins.domain.shapes.models

import amf.client.model.StrField
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.SchemaShapeModel
import amf.plugins.domain.shapes.metamodel.SchemaShapeModel._
import org.yaml.model.YPart

case class SchemaShape(override val fields: Fields, override val annotations: Annotations)
    extends AnyShape(fields, annotations) {

  def raw: StrField       = fields.field(Raw)
  def mediaType: StrField = fields.field(MediaType)

  def withRaw(text: String): SchemaShape            = set(Raw, text)
  def withMediaType(mediaType: String): SchemaShape = set(MediaType, mediaType)

  override def linkCopy(): SchemaShape = SchemaShape().withId(id)
  override def adopted(parent: String) = withId(parent + "/schema/" + name.value())

  override def meta = SchemaShapeModel
}

object SchemaShape {

  def apply(): SchemaShape = apply(Annotations())

  def apply(ast: YPart): SchemaShape = apply(Annotations(ast))

  def apply(annotations: Annotations): SchemaShape = SchemaShape(Fields(), annotations)
}
