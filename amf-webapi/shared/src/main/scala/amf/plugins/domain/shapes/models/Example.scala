package amf.plugins.domain.shapes.models

import amf.client.model.{BoolField, StrField}
import amf.core.metamodel.Obj
import amf.core.model.domain.{DataNode, DomainElement, Linkable, NamedDomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.metamodel.ExampleModel._
import org.yaml.model.YPart

/**
  *
  */
case class Example(fields: Fields, annotations: Annotations)
    extends DomainElement
    with Linkable
    with NamedDomainElement {

  def name: StrField        = fields.field(Name)
  def displayName: StrField = fields.field(DisplayName)
  def description: StrField = fields.field(Description)
  def value: StrField       = fields.field(ExampleModel.Value)
  def structuredValue: DataNode = fields(ExampleModel.StructuredValue)
  def strict: BoolField     = fields.field(Strict)
  def mediaType: StrField   = fields.field(MediaType)

  def withName(name: String): this.type               = set(Name, name)
  def withDisplayName(displayName: String): this.type = set(DisplayName, displayName)
  def withDescription(description: String): this.type = set(Description, description)
  def withValue(value: String): this.type             = set(ExampleModel.Value, value)
  def withStructuredValue(value: DataNode): this.type = set(ExampleModel.StructuredValue, value)
  def withStrict(strict: Boolean): this.type          = set(Strict, strict)
  def withMediaType(mediaType: String): this.type     = set(MediaType, mediaType)

  /** Call after object has been adopted by specified parent. */
  override def adopted(parent: String): Example.this.type = withId(parent + "/example/" + name.value())

  override def linkCopy(): Example = Example().withId(id)

  override def meta: Obj = ExampleModel
}

object Example {

  def apply(): Example = apply(Annotations())

  def apply(ast: YPart): Example = apply(Annotations(ast))

  def apply(annotations: Annotations): Example = Example(Fields(), annotations)
}
