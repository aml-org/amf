package amf.model.domain

import amf.plugins.domain.webapi.models

/**
  * JS Parameter model class.
  */
case class Parameter private[model] (private val parameter: models.Parameter) extends DomainElement {

  def this() = this(models.Parameter())

  def name: String        = parameter.name
  def description: String = parameter.description
  def required: Boolean   = parameter.required
  def binding: String     = parameter.binding
  def schema: Shape       = Shape(parameter.schema)

  override private[amf] def element: models.Parameter = parameter

  /** Set name property of this [[Parameter]]. */
  def withName(name: String): this.type = {
    parameter.withName(name)
    this
  }

  /** Set description property of this [[Parameter]]. */
  def withDescription(description: String): this.type = {
    parameter.withDescription(description)
    this
  }

  /** Set required property of this [[Parameter]]. */
  def withRequired(required: Boolean): this.type = {
    parameter.withRequired(required)
    this
  }

  /** Set binding property of this [[Parameter]]. */
  def withBinding(binding: String): this.type = {
    parameter.withBinding(binding)
    this
  }

  /** Set schema property of this [[Parameter]]. */
  def withObjectSchema(name: String): NodeShape =
    NodeShape(parameter.withObjectSchema(name))

  def withScalarSchema(name: String): ScalarShape =
    ScalarShape(parameter.withScalarSchema(name))
}
