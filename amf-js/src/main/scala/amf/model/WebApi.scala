package amf.model

import amf.builder.{WebApiBuilder}

import scala.annotation.meta.field
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExport

/**
  * Created by martin.gutierrez on 7/3/17.
  */
case class WebApi(name: String,
                  description: String,
                  host: String,
                  schemeList: List[String],
                  basePath: String,
                  accepts: String,
                  contentType: String,
                  version: String,
                  termsOfService: String,
                  provider: Organization,
                  license: License,
                  documentation: CreativeWork)
    extends WebApiModel {

  override protected def createBuilder(): WebApiBuilder = new WebApiBuilder

  @(JSExport @field)
  val scheme: js.Array[String] = schemeList.toJSArray

  override def toBuilder: WebApiBuilder =
    createBuilder()
      .withName(name)
      .withDescription(description)
      .withHost(host)
      .withScheme(schemeList)
      .withBasePath(basePath)
      .withAccepts(accepts)
      .withContentType(contentType)
      .withVersion(version)
      .withTermsOfService(termsOfService)
      .withProvider(provider)
      .withLicense(license)
      .withDocumentation(documentation)
}
