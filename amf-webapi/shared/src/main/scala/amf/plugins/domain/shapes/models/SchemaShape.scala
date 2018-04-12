package amf.plugins.domain.shapes.models

import amf.core.annotations.ExternalSource
import amf.core.metamodel.Field
import amf.core.metamodel.domain.LinkableElementModel
import amf.core.model.StrField
import amf.core.model.domain.{AmfElement, AmfScalar, DynamicDomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.metamodel.SchemaShapeModel
import amf.plugins.domain.shapes.metamodel.SchemaShapeModel._
import org.yaml.model.YPart

case class SchemaShape(override val fields: Fields, override val annotations: Annotations)
    extends AnyShape(fields, annotations)
    with DynamicDomainElement {

  def raw: StrField       = fields.field(Raw)
  def mediaType: StrField = fields.field(MediaType)

  def withRaw(text: String): SchemaShape            = set(Raw, text)
  def withMediaType(mediaType: String): SchemaShape = set(MediaType, mediaType)

  override def linkCopy(): SchemaShape = SchemaShape().withId(id)

  override def meta = SchemaShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/schema/" + name.option().getOrElse("default-schema")

  override def dynamicFields: List[Field] = LinkableElementModel.TargetId :: SchemaShapeModel.fields

  override def dynamicType: List[ValueType] = SchemaShapeModel.`type`

  override def valueForField(f: Field): Option[AmfElement] = f match {
    case Raw if annotations.contains(classOf[ExternalSource]) => None
    case TargetId if annotations.contains(classOf[ExternalSource]) =>
      annotations.find(classOf[ExternalSource]).map(e => AmfScalar(e.origTarget))
    case TargetId => None
    case _        => fields.entry(f).map(_.value.value)
  }
}

object SchemaShape {

  def apply(): SchemaShape = apply(Annotations())

  def apply(ast: YPart): SchemaShape = apply(Annotations(ast))

  def apply(annotations: Annotations): SchemaShape = SchemaShape(Fields(), annotations)
}
