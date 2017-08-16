package amf.builder

import amf.domain.{Annotation, Fields, Parameter}
import amf.metadata.domain.ParameterModel._

/**
  * Parameter domain element builder.
  */
class ParameterBuilder extends Builder {
  override type T = Parameter

  def withName(name: String): ParameterBuilder = set(Name, name)

  def withDescription(description: String): ParameterBuilder = set(Description, description)

  def withRequired(required: Boolean): ParameterBuilder = set(Required, required)

  def withBinding(binding: String): ParameterBuilder = set(Binding, binding)

  def withSchema(schema: String): ParameterBuilder = set(Schema, schema)

  override def resolveId(container: String): this.type = {
    val name: String = fields(Name)
    withId(container + "/parameter/" + name)
  }

  override def build: Parameter = null
}

object ParameterBuilder {
  def apply(): ParameterBuilder = apply(Nil)

  def apply(fields: Fields, annotations: List[Annotation] = Nil): ParameterBuilder = apply(annotations).copy(fields)

  def apply(annotations: List[Annotation]): ParameterBuilder = new ParameterBuilder().withAnnotations(annotations)
}
