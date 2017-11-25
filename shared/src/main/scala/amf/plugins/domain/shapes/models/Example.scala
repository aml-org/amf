package amf.plugins.domain.shapes.models

import amf.core.model.domain.{DomainElement, Linkable}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.metamodel.ExampleModel._
import org.yaml.model.YPart

/**
  *
  */
case class Example(fields: Fields, annotations: Annotations) extends DomainElement with Linkable {

  def name: String        = fields(Name)
  def displayName: String = fields(DisplayName)
  def description: String = fields(Description)
  def value: String       = fields(ExampleModel.Value)
  def strict: Boolean     = fields(Strict)
  def mediaType: String   = fields(MediaType)

  def withName(name: String): this.type               = set(Name, name)
  def withDisplayName(displayName: String): this.type = set(DisplayName, displayName)
  def withDescription(description: String): this.type = set(Description, description)
  def withValue(value: String): this.type             = set(ExampleModel.Value, value)
  def withStrict(strict: Boolean): this.type          = set(Strict, strict)
  def withMediaType(mediaType: String): this.type     = set(MediaType, mediaType)

  /** Call after object has been adopted by specified parent. */
  override def adopted(parent: String): Example.this.type = withId(parent + "/example/" + name)

  override def linkCopy(): Example = Example().withId(id)

  override def meta = ExampleModel
}

object Example {

  def apply(): Example = apply(Annotations())

  def apply(ast: YPart): Example = apply(Annotations(ast))

  def apply(annotations: Annotations): Example = Example(Fields(), annotations)
}
