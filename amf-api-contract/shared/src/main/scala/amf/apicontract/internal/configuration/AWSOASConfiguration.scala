package amf.apicontract.internal.configuration

import amf.apicontract.client.platform.{AMFConfiguration => PlatformAMFConfiguration}
import amf.apicontract.client.scala.AMFConfiguration
import amf.apicontract.client.scala.OASConfiguration.common
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.apicontract.internal.spec.oas.AwsOas30ParsePlugin
import amf.apicontract.internal.transformation.compatibility.Oas3CompatibilityPipeline
import amf.apicontract.internal.transformation.{Oas30TransformationPipeline, Oas3CachePipeline, Oas3EditingPipeline}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("AWSOASConfiguration")
object AWSOASConfiguration {

  def forScala(): Future[AMFConfiguration] =
    common()
      .withPlugins(
        List(
          AwsOas30ParsePlugin
        )
      )
      .withTransformationPipelines(
        List(
          Oas30TransformationPipeline(),
          Oas3EditingPipeline(),
          Oas3CompatibilityPipeline(),
          Oas3CachePipeline()
        )
      )
      .withDialect("file://amf-api-contract/shared/src/main/resources/aws-oas-dialect.yaml")

  @JSExport
  def forPlatform(): ClientFuture[PlatformAMFConfiguration] = forScala().asClient

}
