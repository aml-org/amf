package amf.plugins.domain.shapes.models

import amf.core.metamodel.Obj
import amf.core.model.{BoolField, StrField}
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

  def name: StrField            = fields.field(Name)
  def displayName: StrField     = fields.field(DisplayName)
  def description: StrField     = fields.field(Description)
  def value: StrField           = fields.field(Value)
  def structuredValue: DataNode = fields.field(StructuredValue)
  def strict: BoolField         = fields.field(Strict)
  def mediaType: StrField       = fields.field(MediaType)

  def withName(name: String): this.type               = set(Name, name)
  def withDisplayName(displayName: String): this.type = set(DisplayName, displayName)
  def withDescription(description: String): this.type = set(Description, description)
  def withValue(value: String): this.type             = set(Value, value)
  def withStructuredValue(value: DataNode): this.type = set(StructuredValue, value)
  def withStrict(strict: Boolean): this.type          = set(Strict, strict)
  def withMediaType(mediaType: String): this.type     = set(MediaType, mediaType)

  override def linkCopy(): Example = Example().withId(id)

  override def meta: Obj = ExampleModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/example/" + name.option().getOrElse("default-example")
}

object Example {

  def apply(): Example = apply(Annotations())

  def apply(ast: YPart): Example = apply(Annotations(ast))

  def apply(annotations: Annotations): Example = Example(Fields(), annotations)
}
