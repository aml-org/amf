package amf.client.model

import amf.client.convert.CoreClientConverters._
import amf.core.model.{IntField => InternalIntField}

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class IntField(private val _internal: InternalIntField) extends BaseAnyValField[Int] {

  override protected val _option: Option[Int] = _internal.option()

  /** Return value as option. */
  override val option: ClientOption[Int] = _option.asClient

  /** Return annotations. */
  override def annotations(): Annotations = _internal.annotations()

  /** Return int value or `0` if value is null or undefined. */
  override def value(): Int = _option match {
    case Some(v) => v
    case _       => 0
  }
}
