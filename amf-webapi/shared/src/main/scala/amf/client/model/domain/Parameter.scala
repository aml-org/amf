package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.{BoolField, StrField}
import amf.plugins.domain.webapi.models.{Parameter => InternalParameter}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Parameter model class.
  */
@JSExportAll
case class Parameter(override private[amf] val _internal: InternalParameter) extends DomainElement {

  @JSExportTopLevel("model.domain.Parameter")
  def this() = this(InternalParameter())

  def name: StrField        = _internal.name
  def description: StrField = _internal.description
  def required: BoolField   = _internal.required
  def binding: StrField     = _internal.binding
  def schema: Shape         = _internal.schema

  /** Set name property of this Parameter. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  /** Set description property of this Parameter. */
  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

  /** Set required property of this Parameter. */
  def withRequired(required: Boolean): this.type = {
    _internal.withRequired(required)
    this
  }

  /** Set binding property of this Parameter. */
  def withBinding(binding: String): this.type = {
    _internal.withBinding(binding)
    this
  }

  /** Set schema property of this Parameter. */
  def withObjectSchema(name: String): NodeShape = _internal.withObjectSchema(name)

  def withScalarSchema(name: String): ScalarShape =
    _internal.withScalarSchema(name)
}
