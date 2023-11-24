package amf.apicontract.internal.configuration

import amf.aml.client.scala.AMLConfiguration
import amf.aml.client.scala.model.document.Dialect
import amf.apicontract.client.platform.{AMFConfiguration => PlatformAMFConfiguration}
import amf.apicontract.client.scala.AMFConfiguration
import amf.apicontract.client.scala.OASConfiguration.common
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.apicontract.internal.spec.oas.AwsOas30ParsePlugin
import amf.apicontract.internal.transformation.compatibility.Oas3CompatibilityPipeline
import amf.apicontract.internal.transformation.{Oas30TransformationPipeline, Oas3CachePipeline, Oas3EditingPipeline}
import amf.core.client.scala.execution.ExecutionEnvironment

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("AWSOASConfiguration")
object AWSOASConfiguration {

  private implicit val ec: ExecutionContext = ExecutionEnvironment().context

  def forScala(): Future[AMFConfiguration] = {
    val baseConfiguration = common()
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

    AMLConfiguration
      .predefined()
      .baseUnitClient()
      .parseContent(AwsOasDialect.content)
      .map { parsingResult =>
        val dialect = parsingResult.baseUnit.asInstanceOf[Dialect]
        baseConfiguration.withDialect(dialect)
      }
  }

  @JSExport
  def forPlatform(): ClientFuture[PlatformAMFConfiguration] = forScala().asClient

}
