package amf.core.model.domain.extensions

import amf.core.metamodel.domain.extensions.ShapeExtensionModel
import amf.core.model.domain.{DataNode, DomainElement}
import amf.core.metamodel.domain.extensions.ShapeExtensionModel._
import amf.core.parser.{Annotations, Fields}
import org.yaml.model.YPart

case class ShapeExtension(fields: Fields, annotations: Annotations) extends DomainElement {

  id = "http://raml.org/vocabularies#document/shape_extension"

  def definedBy: PropertyShape = fields.field(DefinedBy)
  def extension: DataNode      = fields.field(Extension)

  def withDefinedBy(customProperty: PropertyShape): this.type =
    set(DefinedBy, customProperty)

  def withExtension(extension: DataNode): this.type = set(Extension, extension)

  // This element will never be serialised in the JSON-LD graph, it is just a placeholder
  // for the extension point. ID is not required for serialisation
  override def adopted(parent: String): this.type = withId(parent + "/shapeExtension")

  override def meta = ShapeExtensionModel
}

object ShapeExtension {
  def apply(): ShapeExtension = apply(Annotations())

  def apply(ast: YPart): ShapeExtension = apply(Annotations(ast))

  def apply(annotations: Annotations): ShapeExtension = ShapeExtension(Fields(), annotations)
}
