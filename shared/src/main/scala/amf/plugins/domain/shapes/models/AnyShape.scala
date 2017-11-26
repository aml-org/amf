package amf.plugins.domain.shapes.models

import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.AnyShapeModel
import org.yaml.model.YPart

case class AnyShape(fields: Fields, annotations: Annotations) extends Shape with ShapeHelpers {
  override def adopted(parent: String): this.type = withId(parent + "/any/" + name)

  def documentation: CreativeWork                = fields(Documentation)
  def xmlSerialization: XMLSerializer            = fields(XMLSerialization)
  def examples: Seq[Example]                     = fields(Examples)

  def withDocumentation(documentation: CreativeWork): this.type             = set(Documentation, documentation)
  def withXMLSerialization(xmlSerialization: XMLSerializer): this.type      = set(XMLSerialization, xmlSerialization)
  def withExamples(examples: Seq[Example]): this.type                       = setArray(Examples, examples)

  override def linkCopy(): AnyShape = AnyShape().withId(id)

  override def meta = AnyShapeModel
}

object AnyShape {
  def apply(): AnyShape = apply(Annotations())

  def apply(ast: YPart): AnyShape = apply(Annotations(ast))

  def apply(annotations: Annotations): AnyShape = AnyShape(Fields(), annotations)
}
