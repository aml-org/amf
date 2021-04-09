package amf.client.environment

import amf.{Oas30Profile, OasProfile, RamlProfile}
import amf.client.remod.amfcore.config.{AMFEventListener, AMFLogger, AMFOptions, AMFResolvers}
import amf.client.remod.amfcore.registry.AMFRegistry
import amf.client.remod.amfcore.resolution.{PipelineInfo, PipelineName}
import amf.client.remod.{AMFGraphConfiguration, ErrorHandlerProvider}
import amf.core.remote.{AsyncApi20, Oas20, Oas30, Raml08, Raml10}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.plugins.document.webapi.Async20Plugin.vendor
import amf.plugins.document.webapi._
import amf.plugins.document.webapi.resolution.pipelines.{
  Async20EditingPipeline,
  Async20ResolutionPipeline,
  Oas30EditingPipeline,
  Oas30ResolutionPipeline,
  OasEditingPipeline,
  OasResolutionPipeline,
  Raml08EditingPipeline,
  Raml08ResolutionPipeline,
  Raml10EditingPipeline,
  Raml10ResolutionPipeline
}
import amf.plugins.document.webapi.resolution.pipelines.compatibility.CompatibilityPipeline

sealed trait APIConfigurationBuilder {

  //  will also define APIDomainPlugin, DataShapesDomainPlugin
  private[amf] def common(): AMFGraphConfiguration =
    AMLConfiguration
      .predefined()
      .withPlugins(List(ExternalJsonYamlRefsParsePlugin, PayloadParsePlugin, JsonSchemaParsePlugin))
}
object RAMLConfiguration extends APIConfigurationBuilder {
  def RAML10(): AMFGraphConfiguration =
    common()
      .withPlugins(List(Raml10ParsePlugin, Raml10RenderPlugin))
      .withTransformationPipelines(Map(
        PipelineName.from(Raml10.name, ResolutionPipeline.DEFAULT_PIPELINE) -> new Raml10ResolutionPipeline(),
        PipelineName.from(Raml10.name, ResolutionPipeline.EDITING_PIPELINE) -> new Raml10EditingPipeline(),
        PipelineName.from(Raml10.name, ResolutionPipeline.COMPATIBILITY_PIPELINE) -> new CompatibilityPipeline(
          RamlProfile),
        PipelineName.from(Raml10.name, ResolutionPipeline.CACHE_PIPELINE) -> new Raml10EditingPipeline(false)
      ))
  def RAML08(): AMFGraphConfiguration =
    common()
      .withPlugins(List(Raml08ParsePlugin, Raml08RenderPlugin))
      .withTransformationPipelines(Map(
        PipelineName.from(Raml08.name, ResolutionPipeline.DEFAULT_PIPELINE) -> new Raml08ResolutionPipeline(),
        PipelineName.from(Raml08.name, ResolutionPipeline.EDITING_PIPELINE) -> new Raml08EditingPipeline(),
        PipelineName.from(Raml08.name, ResolutionPipeline.COMPATIBILITY_PIPELINE) -> new CompatibilityPipeline(
          RamlProfile),
        PipelineName.from(Raml08.name, ResolutionPipeline.CACHE_PIPELINE) -> new Raml08EditingPipeline(false)
      ))

  def RAML(): AMFGraphConfiguration = RAML08().merge(RAML10())
}

object OASConfiguration extends APIConfigurationBuilder {
  def OAS20(): AMFGraphConfiguration =
    common()
      .withPlugins(List(Oas20ParsePlugin, Oas20RenderPlugin))
      .withTransformationPipelines(Map(
        PipelineName.from(Oas20.name, ResolutionPipeline.DEFAULT_PIPELINE) -> new OasResolutionPipeline(),
        PipelineName.from(Oas20.name, ResolutionPipeline.EDITING_PIPELINE) -> new OasEditingPipeline(),
        PipelineName.from(Oas20.name, ResolutionPipeline.COMPATIBILITY_PIPELINE) -> new CompatibilityPipeline(
          OasProfile),
        PipelineName.from(Oas20.name, ResolutionPipeline.CACHE_PIPELINE) -> new OasEditingPipeline(false)
      ))
  def OAS30(): AMFGraphConfiguration =
    common()
      .withPlugins(List(Oas30ParsePlugin, Oas30RenderPlugin))
      .withTransformationPipelines(Map(
        PipelineName.from(Oas30.name, ResolutionPipeline.DEFAULT_PIPELINE) -> new Oas30ResolutionPipeline(),
        PipelineName.from(Oas30.name, ResolutionPipeline.EDITING_PIPELINE) -> new Oas30EditingPipeline(),
        PipelineName.from(Oas30.name, ResolutionPipeline.COMPATIBILITY_PIPELINE) -> new CompatibilityPipeline(
          Oas30Profile),
        PipelineName.from(Oas30.name, ResolutionPipeline.CACHE_PIPELINE) -> new Oas30EditingPipeline(false)
      ))
  def OAS(): AMFGraphConfiguration = OAS20().merge(OAS30())
}

object WebAPIConfiguration {
  def WebAPI(): AMFGraphConfiguration = OASConfiguration.OAS().merge(RAMLConfiguration.RAML())
}

object AsyncAPIConfiguration extends APIConfigurationBuilder {
  def Async20(): AMFGraphConfiguration =
    common()
      .withPlugins(List(Async20ParsePlugin, Async20RenderPlugin))
      .withTransformationPipelines(Map(
        PipelineName.from(AsyncApi20.name, ResolutionPipeline.DEFAULT_PIPELINE) -> new Async20ResolutionPipeline(),
        PipelineName.from(AsyncApi20.name, ResolutionPipeline.EDITING_PIPELINE) -> new Async20EditingPipeline(),
        PipelineName.from(AsyncApi20.name, ResolutionPipeline.CACHE_PIPELINE)   -> new Async20EditingPipeline(false)
      ))
}

class AMFConfiguration private[amf] (override private[amf] val resolvers: AMFResolvers,
                                     override private[amf] val errorHandlerProvider: ErrorHandlerProvider,
                                     override private[amf] val registry: AMFRegistry,
                                     override private[amf] val logger: AMFLogger,
                                     override private[amf] val listeners: Set[AMFEventListener],
                                     override private[amf] val options: AMFOptions)
    extends AMLConfiguration(resolvers, errorHandlerProvider, registry, logger, listeners, options) {

  override def createClient(): AMFClient = new AMFClient(this)

  override protected def copy(resolvers: AMFResolvers,
                              errorHandlerProvider: ErrorHandlerProvider,
                              registry: AMFRegistry,
                              logger: AMFLogger,
                              listeners: Set[AMFEventListener],
                              options: AMFOptions): AMFConfiguration =
    new AMFConfiguration(resolvers, errorHandlerProvider, registry, logger, listeners, options)
}
