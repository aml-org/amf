package amf.model.builder

import amf.model.Parameter

/**
  * Parameter domain element builder.
  */
case class ParameterBuilder(
    private[amf] val internalBuilder: amf.builder.ParameterBuilder = amf.builder.ParameterBuilder())
    extends Builder {

  def withName(name: String): ParameterBuilder = {
    internalBuilder.withName(name)
    this
  }

  def withDescription(description: String): ParameterBuilder = {
    internalBuilder.withDescription(description)
    this
  }

  def withRequired(required: Boolean): ParameterBuilder = {
    internalBuilder.withRequired(required)
    this
  }

  def withBinding(binding: String): ParameterBuilder = {
    internalBuilder.withBinding(binding)
    this
  }

  def withSchema(schema: String): ParameterBuilder = {
    internalBuilder.withSchema(schema)
    this
  }

  def build: Parameter = Parameter(internalBuilder.build)
}
