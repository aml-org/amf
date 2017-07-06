package amf.model

import amf.builder.{WebApiBuilder}

import scala.annotation.meta.field
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExport

/**
  * Created by martin.gutierrez on 7/3/17.
  */
case class WebApi(private val fields: Fields) extends BaseWebApi(fields) {

  @(JSExport @field)
  val schemesArray: js.Iterable[String] = schemes.toJSArray

  override def toBuilder: WebApiBuilder = new WebApiBuilder().copy(fields)
}
