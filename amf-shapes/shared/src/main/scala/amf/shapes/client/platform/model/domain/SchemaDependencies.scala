package amf.shapes.client.platform.model.domain

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.{DomainElement, Shape}
import amf.shapes.client.scala.model.domain.{SchemaDependencies => InternalSchemaDependencies}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Schema dependencies model class
  */
@JSExportAll
case class SchemaDependencies(override private[amf] val _internal: InternalSchemaDependencies) extends DomainElement {

  @JSExportTopLevel("SchemaDependencies")
  def this() = this(InternalSchemaDependencies())

  def source: StrField = _internal.propertySource
  def target: Shape    = _internal.schemaTarget

  def withPropertySource(propertySource: String): this.type = {
    _internal.withPropertySource(propertySource)
    this
  }

  def withSchemaTarget(schema: Shape): this.type = {
    _internal.withSchemaTarget(schema)
    this
  }
}
