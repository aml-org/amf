package amf.builder

import amf.domain.{Fields, Parameter}
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

  override def build: Parameter = Parameter(fields)
}

object ParameterBuilder {
  def apply(): ParameterBuilder = new ParameterBuilder()

  def apply(fields: Fields): ParameterBuilder = apply().copy(fields)
}
