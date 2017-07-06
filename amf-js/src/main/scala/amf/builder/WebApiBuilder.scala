package amf.builder

import amf.model.WebApi

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * WebApiBuilder wrapper for JS.
  */
@JSExportAll
class WebApiBuilder extends BaseWebApiBuilder {

  def withScheme(scheme: js.Array[String]): this.type = super.withSchemes(scheme.toList)

  override def build: WebApi = WebApi(fields)
}
