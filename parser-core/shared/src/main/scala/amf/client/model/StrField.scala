package amf.client.model

import amf.client.convert.CoreClientConverters._
import amf.core.model.{StrField => InternalStrField}

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class StrField(private val _internal: InternalStrField) extends BaseField with ValueField[String] {

  override protected val _option: Option[String] = _internal.option()

  /** Return value as option. */
  override val option: ClientOption[String] = _option.asClient

  /** Return annotations. */
  override def annotations(): Annotations = _internal.annotations()

  /** Return value or null if value is null or undefined. */
  override def value(): String = _option.orNull

  /** Return true if string value is null or empty. */
  def isNullOrEmpty: Boolean = _option.fold(true)(_.isEmpty)

  /** Return true if string value is not null and not empty. */
  def nonEmpty: Boolean = !isNullOrEmpty

  override def remove(): Unit = _internal.remove()
}
