package amf.builder

import amf.domain.{Fields, Parameter}
import amf.metadata.domain.ParameterModel._

/**
  * Parameter domain element builder.
  */
class ParameterBuilder extends Builder {
  override type T = Parameter

  def withName(name: String): this.type               = set(Name, name)
  def withDescription(description: String): this.type = set(Description, description)
  def withRequired(required: Boolean): this.type      = set(Required, required)
  def withBinding(binding: String): this.type         = set(Binding, binding)
  def withSchema(schema: String): this.type           = set(Schema, schema)

  override def build: Parameter = Parameter(fields)
}

object ParameterBuilder {
  def apply(): ParameterBuilder = new ParameterBuilder()

  def apply(fields: Fields): ParameterBuilder = apply().copy(fields)
}
