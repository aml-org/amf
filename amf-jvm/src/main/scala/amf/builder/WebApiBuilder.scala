package amf.builder

import java.util

import amf.model._

import scala.collection.JavaConverters._

/**
  * Created by martin.gutierrez on 7/3/17.
  */
class WebApiBuilder extends BaseWebApiBuilder {

  def withScheme(scheme: util.List[String]): this.type = super.withScheme(scheme.asScala.toList)

  override def withName(name: String): this.type = super.withName(name)

  override def withDescription(description: String): WebApiBuilder.this.type = super.withDescription(description)

  override def withHost(host: String): WebApiBuilder.this.type = super.withHost(host)

  override def withBasePath(basePath: String): WebApiBuilder.this.type = super.withBasePath(basePath)

  override def withAccepts(accepts: String): WebApiBuilder.this.type = super.withAccepts(accepts)

  override def withContentType(contentType: String): WebApiBuilder.this.type = super.withContentType(contentType)

  override def withVersion(version: String): WebApiBuilder.this.type = super.withVersion(version)

  override def withTermsOfService(termsOfService: String): WebApiBuilder.this.type =
    super.withTermsOfService(termsOfService)

  override def withProvider(provider: Organization): WebApiBuilder.this.type = super.withProvider(provider)

  override def withProvider(url: String, name: String, email: String): WebApiBuilder.this.type =
    super.withProvider(url, name, email)

  override def withLicense(license: License): WebApiBuilder.this.type = super.withLicense(license)

  override def withLicense(url: String, name: String): WebApiBuilder.this.type = super.withLicense(url, name)

  override def withDocumentation(documentation: CreativeWork): WebApiBuilder.this.type =
    super.withDocumentation(documentation)

  override def withDocumentation(url: String, description: String): WebApiBuilder.this.type =
    super.withDocumentation(url, description)

  override def build: WebApi =
    WebApi(name,
           description,
           host,
           scheme,
           basePath,
           accepts,
           contentType,
           version,
           termsOfService,
           provider,
           license,
           documentation)
}
