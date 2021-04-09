package amf.client.environment

import amf.client.remod.amfcore.config.{AMFEventListener, AMFLogger, AMFOptions, AMFResolvers}
import amf.client.remod.amfcore.registry.AMFRegistry
import amf.client.remod.amfcore.resolution.TransformationPipeline
import amf.client.remod.{AMFGraphConfiguration, ErrorHandlerProvider}
import amf.plugins.document.webapi._
import amf.plugins.document.webapi.resolution.pipelines.compatibility.{
  Oas3CompatibilityPipeline,
  OasCompatibilityPipeline,
  RamlCompatibilityPipeline
}
import amf.plugins.document.webapi.resolution.pipelines.{
  UnifiedCachePipeline,
  UnifiedDefaultPipeline,
  UnifiedEditingPipeline
}

sealed trait APIConfigurationBuilder {

//  will also define APIDomainPlugin, DataShapesDomainPlugin
  private[amf] def common(): AMFGraphConfiguration =
    AMLConfiguration
      .AML()
      .withPlugins(List(ExternalJsonYamlRefsParsePlugin, PayloadParsePlugin, JsonSchemaParsePlugin))
      .withTransformationPipelines(Map(
        TransformationPipeline.DEFAULT -> UnifiedDefaultPipeline(),
        TransformationPipeline.EDITING -> UnifiedEditingPipeline(),
        TransformationPipeline.CACHE   -> new UnifiedCachePipeline()
      ))
}
// TODO: ARM remove private[amf]
private[amf] object RAMLConfiguration extends APIConfigurationBuilder {
  def RAML10(): AMFGraphConfiguration =
    common()
      .withPlugins(List(Raml10ParsePlugin, Raml10RenderPlugin))
      .withTransformationPipeline(TransformationPipeline.OAS_TO_RAML10, new RamlCompatibilityPipeline())
  def RAML08(): AMFGraphConfiguration = common().withPlugins(List(Raml08ParsePlugin, Raml08RenderPlugin))
  def RAML(): AMFGraphConfiguration   = RAML08().merge(RAML10())
}

// TODO: ARM remove private[amf]
private[amf] object OASConfiguration extends APIConfigurationBuilder {
  def OAS20(): AMFGraphConfiguration =
    common()
      .withPlugins(List(Oas20ParsePlugin, Oas20RenderPlugin))
      .withTransformationPipeline(TransformationPipeline.RAML_TO_OAS20, new OasCompatibilityPipeline())
  def OAS30(): AMFGraphConfiguration =
    common()
      .withPlugins(List(Oas30ParsePlugin, Oas30RenderPlugin))
      .withTransformationPipeline(TransformationPipeline.RAML_TO_OAS30, new Oas3CompatibilityPipeline())
  def OAS(): AMFGraphConfiguration = OAS20().merge(OAS30())
}

// TODO: ARM remove private[amf]
private[amf] object WebAPIConfiguration {
  def WebAPI(): AMFGraphConfiguration = OASConfiguration.OAS().merge(RAMLConfiguration.RAML())
}

private[amf] object AsyncAPIConfiguration extends APIConfigurationBuilder {
  def Async20(): AMFGraphConfiguration = common().withPlugins(List(Async20ParsePlugin, Async20RenderPlugin))
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
