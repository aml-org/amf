package amf.domain.extensions

import amf.domain.{Annotations, DomainElement, Fields}
import amf.metadata.domain.extensions.ShapeExtensionModel._
import amf.shape.PropertyShape
import org.yaml.model.YPart

case class ShapeExtension (fields: Fields, annotations: Annotations) extends DomainElement {

  id = "http://raml.org/vocabularies#document/shape_extension"

  def definedBy: PropertyShape = fields(DefinedBy)
  def extension: DataNode      = fields(Extension)

  def withDefinedBy(customProperty: PropertyShape): this.type =
    set(DefinedBy, customProperty)

  def withExtension(extension: DataNode): this.type = set(Extension, extension)

  // This element will never be serialised in the JSON-LD graph, it is just a placeholder
  // for the extension point. ID is not required for serialisation
  override def adopted(parent: String): this.type = withId(parent + "/shapeExtension")
}

object ShapeExtension {
  def apply(): ShapeExtension = apply(Annotations())

  def apply(ast: YPart): ShapeExtension = apply(Annotations(ast))

  def apply(annotations: Annotations): ShapeExtension = ShapeExtension(Fields(), annotations)
}

