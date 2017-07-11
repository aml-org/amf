package amf.model

import amf.builder.WebApiBuilder

import scala.annotation.meta.field
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExport

/**
  * JS WebApi class.
  */
case class WebApi(private val fs: Fields) extends BaseWebApi(fs) {

  @(JSExport @field)
  val schemesArray: js.Iterable[String] = schemes.toJSArray
  @(JSExport @field)
  val endPointsArray: js.Iterable[EndPoint] = endPoints.toJSArray

  override def toBuilder: WebApiBuilder = new WebApiBuilder().copy(fs)
}
