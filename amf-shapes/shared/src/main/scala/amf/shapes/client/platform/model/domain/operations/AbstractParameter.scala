package amf.shapes.client.platform.model.domain.operations

import amf.core.client.platform.model.{Annotations, BoolField, StrField}
import amf.core.client.platform.model.domain.{DataNode, DomainElement, NamedAmfObject, Shape}
import amf.shapes.client.platform.model.domain.{NodeShape, ScalarShape}
import amf.shapes.client.scala.model.domain.operations.{AbstractParameter => InternalAbstractParameter}
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
abstract class AbstractParameter(override private[amf] val _internal: InternalAbstractParameter)
    extends DomainElement
    with NamedAmfObject
    with PlatformSecrets {

  def parameterName: StrField = _internal.parameterName
  def description: StrField   = _internal.description
  def required: BoolField     = _internal.required
  def schema: Shape           = _internal.schema
  def binding: StrField       = _internal.binding
  def defaultValue: DataNode  = _internal.defaultValue

  def withBinding(binding: String): this.type = {
    _internal.withBinding(binding)
    this
  }

  def withParameterName(name: String): this.type = {
    _internal.withParameterName(name)
    this
  }

  def withDescription(description: String): this.type = {
    _internal.withDescription(description)
    this
  }

  def withRequired(required: Boolean): this.type = {
    _internal.withRequired(required)
    this
  }

  def withSchema(schema: Shape): this.type = {
    _internal.withSchema(schema)
    this
  }

  def withDefaultValue(defaultValue: DataNode): this.type = {
    _internal.withDefaultValue(defaultValue)
    this
  }

  def cloneParameter(parent: String): this.type = {
    _internal.cloneParameter(parent)
    this
  }

  protected def buildParameter(ann: Annotations): this.type = {
    _internal.buildParameter(ann)
    this
  }

  def withObjectSchema(name: String): NodeShape   = _internal.withObjectSchema(name)
  def withScalarSchema(name: String): ScalarShape = _internal.withScalarSchema(name)

  /** Return DomainElement name. */
  override def name: StrField = _internal.name

  /** Update DomainElement name. */
  override def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }
}
