package amf.builder

import amf.model.{EndPoint, WebApi}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * JS WebApiBuilder class.
  */
@JSExportAll
class WebApiBuilder extends BaseWebApiBuilder {

  def withScheme(scheme: js.Array[String]): this.type        = super.withSchemes(scheme.toList)
  def withEndPoint(endPoints: js.Array[EndPoint]): this.type = super.withEndPoints(endPoints.toList)

  override def build: WebApi = WebApi(fixFields(fields))
}
