package amf.builder

import java.util

import amf.model.{EndPoint, _}

import scala.collection.JavaConverters._

/**
  * JVM WebApiBuilder class.
  */
class WebApiBuilder extends BaseWebApiBuilder {

  def withScheme(scheme: util.List[String]): this.type = super.withSchemes(scheme.asScala.toList)

  def withEndPoint(endPoints: util.List[EndPoint]): this.type = super.withEndPoints(endPoints.asScala.toList)

  override def withName(name: String): this.type = super.withName(name)

  override def withDescription(description: String): WebApiBuilder.this.type = super.withDescription(description)

  override def withHost(host: String): WebApiBuilder.this.type = super.withHost(host)

  override def withBasePath(basePath: String): WebApiBuilder.this.type = super.withBasePath(basePath)

  override def withAccepts(accepts: String): WebApiBuilder.this.type = super.withAccepts(accepts)

  override def withContentType(contentType: String): WebApiBuilder.this.type = super.withContentType(contentType)

  override def withVersion(version: String): WebApiBuilder.this.type = super.withVersion(version)

  override def withTermsOfService(tos: String): WebApiBuilder.this.type =
    super.withTermsOfService(tos)

  override def withProvider(provider: Organization): WebApiBuilder.this.type = super.withProvider(provider)

  override def withLicense(license: License): WebApiBuilder.this.type = super.withLicense(license)

  override def withDocumentation(documentation: CreativeWork): WebApiBuilder.this.type =
    super.withDocumentation(documentation)

  override def build: WebApi = WebApi(fixFields(fields))
}
