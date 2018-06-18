package amf.client.model

import amf.client.convert.CoreClientConverters._
import amf.core.model.{BoolField => InternalBoolField}

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class BoolField(private val _internal: InternalBoolField) extends BaseAnyValField[Boolean] {

  override protected val _option: Option[Boolean] = _internal.option()

  /** Return value as option. */
  override val option: ClientOption[Boolean] = _option.asClient

  /** Return annotations. */
  override def annotations(): Annotations = _internal.annotations()

  /** Return boolean value or `false` if value is null or undefined. */
  override def value(): Boolean = _option match {
    case Some(v) => v
    case _       => false
  }

  override def remove(): Unit = _internal.remove()
}
