package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.{BoolField, IntField}
import amf.plugins.domain.shapes.models.DataArrangementShape

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
abstract class DataArrangeShape(override private[amf] val _internal: DataArrangementShape)
    extends AnyShape(_internal) {

  def minItems: IntField     = _internal.minItems
  def maxItems: IntField     = _internal.maxItems
  def uniqueItems: BoolField = _internal.uniqueItems

  def withMinItems(minItems: Int): this.type = {
    _internal.withMinItems(minItems)
    this
  }

  def withMaxItems(maxItems: Int): this.type = {
    _internal.withMaxItems(maxItems)
    this
  }

  def withUniqueItems(uniqueItems: Boolean): this.type = {
    _internal.withUniqueItems(uniqueItems)
    this
  }
}
