package amf.client.environment

import amf.client.remod.amfcore.config.{
  AMFEventListener,
  AMFLogger,
  AMFOptions,
  AMFResolvers,
  ParsingOptions,
  RenderOptions
}
import amf.client.remod.amfcore.plugins.AMFPlugin
import amf.client.remod.amfcore.plugins.parse.AMFParsePlugin
import amf.client.remod.amfcore.registry.AMFRegistry
import amf.client.remod.{AMFConfiguration, ErrorHandlerProvider}
import amf.core.validation.core.ValidationProfile
import amf.internal.reference.UnitCache
import amf.internal.resource.ResourceLoader
import amf.plugins.document.webapi._

sealed trait APIConfigurationBuilder {

//  will also define APIDomainPlugin, DataShapesDomainPlugin
  private[amf] def common(): AMFConfiguration =
    AMFConfiguration
      .predefined()
      .withPlugins(List(ExternalJsonYamlRefsParsePlugin, PayloadParsePlugin, JsonSchemaParsePlugin))
}
// TODO: ARM remove private[amf]
private[amf] object RAMLConfiguration extends APIConfigurationBuilder {
  def RAML10(): AMFConfiguration = common().withPlugin(Raml10ParsePlugin)
  def RAML08(): AMFConfiguration = common().withPlugin(Raml08ParsePlugin)
  def RAML(): AMFConfiguration   = RAML08().merge(RAML10())
}

// TODO: ARM remove private[amf]
private[amf] object OASConfiguration extends APIConfigurationBuilder {
  def OAS20(): AMFConfiguration = common().withPlugin(Oas20ParsePlugin)
  def OAS30(): AMFConfiguration = common().withPlugin(Oas30ParsePlugin)
  def OAS(): AMFConfiguration   = OAS20().merge(OAS30())
}

// TODO: ARM remove private[amf]
private[amf] object WebAPIConfiguration {
  def WebAPI(): AMFConfiguration = OASConfiguration.OAS().merge(RAMLConfiguration.RAML())
}

private[amf] object AsyncAPIConfiguration extends APIConfigurationBuilder {
  def Async20(): AMFConfiguration = common().withPlugin(Async20ParsePlugin)
}

class APIConfiguration private[amf] (override private[amf] val resolvers: AMFResolvers,
                                     override private[amf] val errorHandlerProvider: ErrorHandlerProvider,
                                     override private[amf] val registry: AMFRegistry,
                                     override private[amf] val logger: AMFLogger,
                                     override private[amf] val listeners: Set[AMFEventListener],
                                     override private[amf] val options: AMFOptions)
    extends AMLConfiguration(resolvers, errorHandlerProvider, registry, logger, listeners, options) {

  override def createClient(): AMFAPIClient = new AMFAPIClient(this)

  override protected def copy(resolvers: AMFResolvers,
                              errorHandlerProvider: ErrorHandlerProvider,
                              registry: AMFRegistry,
                              logger: AMFLogger,
                              listeners: Set[AMFEventListener],
                              options: AMFOptions): APIConfiguration =
    new APIConfiguration(resolvers, errorHandlerProvider, registry, logger, listeners, options)
}
