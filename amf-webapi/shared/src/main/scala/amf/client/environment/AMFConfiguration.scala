package amf.client.environment

import amf.client.exported.config.AMFLogger
import amf.client.remod.ErrorHandlerProvider
import amf.client.remod.amfcore.config._
import amf.client.remod.amfcore.plugins.AMFPlugin
import amf.client.remod.amfcore.registry.AMFRegistry
import amf.core.resolution.pipelines.TransformationPipeline
import amf.core.validation.core.ValidationProfile
import amf.internal.reference.UnitCache
import amf.internal.resource.ResourceLoader
import amf.plugins.document.webapi._
import amf.plugins.document.webapi.annotations.serializable.WebAPISerializableAnnotations
import amf.plugins.document.webapi.entities.WebAPIEntities
import amf.plugins.document.webapi.resolution.pipelines._
import amf.plugins.document.webapi.resolution.pipelines.compatibility.{
  Oas20CompatibilityPipeline,
  Oas3CompatibilityPipeline,
  Raml08CompatibilityPipeline,
  Raml10CompatibilityPipeline
}
import amf.plugins.document.webapi.validation.ApiValidationProfiles._
import amf.plugins.domain.shapes.annotations.serializable.ShapeSerializableAnnotations
import amf.plugins.domain.shapes.entities.ShapeEntities
import amf.plugins.domain.webapi.annotations.serializable.APISerializableAnnotations
import amf.plugins.domain.webapi.entities.APIEntities

sealed trait APIConfigurationBuilder {

//  will also define APIDomainPlugin, DataShapesDomainPlugin
  private[amf] def common(): AMFConfiguration = {
    val configuration = AMLConfiguration.predefined()
    new AMFConfiguration(
      configuration.resolvers,
      configuration.errorHandlerProvider,
      // TODO - ARM: move shapes entities and annotations to shape module (?)
      configuration.registry
        .withEntities(APIEntities.entities ++ WebAPIEntities.entities ++ ShapeEntities.entities)
        .withAnnotations(
          APISerializableAnnotations.annotations ++ WebAPISerializableAnnotations.annotations ++ ShapeSerializableAnnotations.annotations),
      configuration.logger,
      configuration.listeners,
      configuration.options
    ).withPlugins(List(ExternalJsonYamlRefsParsePlugin, PayloadParsePlugin, JsonSchemaParsePlugin))
  }
}

/**
  * [[APIConfigurationBuilder.common common()]] configuration with all configurations needed for RAML like:
  *   - Validation rules
  *   - Parse and emit plugins
  *   - Transformation Pipelines
  */
object RAMLConfiguration extends APIConfigurationBuilder {
  def RAML10(): AMFConfiguration =
    common()
      .withPlugins(List(Raml10ParsePlugin, Raml10RenderPlugin))
      .withValidationProfile(Raml10ValidationProfile)
      .withValidationProfile(AmfValidationProfile)
      .withTransformationPipelines(
        List(
          Raml10TransformationPipeline(),
          Raml10EditingPipeline(),
          Raml10CompatibilityPipeline(),
          Raml10CachePipeline()
        ))
  def RAML08(): AMFConfiguration =
    common()
      .withPlugins(List(Raml08ParsePlugin, Raml08RenderPlugin))
      .withValidationProfile(Raml08ValidationProfile)
      .withTransformationPipelines(
        List(
          Raml08TransformationPipeline(),
          Raml08EditingPipeline(),
          Raml08CompatibilityPipeline(),
          Raml08CachePipeline()
        ))

  def RAML(): AMFConfiguration = RAML08().merge(RAML10())
}

/**
  * [[APIConfigurationBuilder.common common()]] configuration with all configurations needed for OAS like:
  *  - Validation rules
  *  - Parse and emit plugins
  *  - Transformation Pipelines
  */
object OASConfiguration extends APIConfigurationBuilder {
  def OAS20(): AMFConfiguration =
    common()
      .withPlugins(List(Oas20ParsePlugin, Oas20RenderPlugin))
      .withValidationProfile(Oas20ValidationProfile)
      .withTransformationPipelines(
        List(
          Oas20TransformationPipeline(),
          Oas20EditingPipeline(),
          Oas20CompatibilityPipeline(),
          Oas20CachePipeline()
        ))
  def OAS30(): AMFConfiguration =
    common()
      .withPlugins(List(Oas30ParsePlugin, Oas30RenderPlugin))
      .withValidationProfile(Oas30ValidationProfile)
      .withTransformationPipelines(
        List(
          Oas30TransformationPipeline(),
          Oas3EditingPipeline(),
          Oas3CompatibilityPipeline(),
          Oas3CachePipeline()
        ))
  def OAS(): AMFConfiguration = OAS20().merge(OAS30())
}

/** Merged [[OASConfiguration]] and [[RAMLConfiguration]] configurations */
object WebAPIConfiguration {
  def WebAPI(): AMFConfiguration = OASConfiguration.OAS().merge(RAMLConfiguration.RAML())
}

/**
  * [[APIConfigurationBuilder.common common()]] configuration with all configurations needed for AsyncApi like:
  *   - Validation rules
  *   - Parse and emit plugins
  *   - Transformation Pipelines
  */
object AsyncAPIConfiguration extends APIConfigurationBuilder {
  def Async20(): AMFConfiguration =
    common()
      .withPlugins(List(Async20ParsePlugin, Async20RenderPlugin))
      .withValidationProfile(Async20ValidationProfile)
      .withTransformationPipelines(
        List(
          Async20TransformationPipeline(),
          Async20EditingPipeline(),
          Async20EditingPipeline()
        ))
}

/**
  * The AMFConfiguration lets you customize all AMF-specific configurations.
  * Its immutable and created through builders. An instance is needed to use AMF.
  * @see [[AMFClient]]
  */
class AMFConfiguration private[amf] (override private[amf] val resolvers: AMFResolvers,
                                     override private[amf] val errorHandlerProvider: ErrorHandlerProvider,
                                     override private[amf] val registry: AMFRegistry,
                                     override private[amf] val logger: AMFLogger,
                                     override private[amf] val listeners: Set[AMFEventListener],
                                     override private[amf] val options: AMFOptions)
    extends AMLConfiguration(resolvers, errorHandlerProvider, registry, logger, listeners, options) {

  override def createClient(): AMFClient = new AMFClient(this)

  override def withParsingOptions(parsingOptions: ParsingOptions): AMFConfiguration =
    super._withParsingOptions(parsingOptions)

  override def withResourceLoader(rl: ResourceLoader): AMFConfiguration =
    super._withResourceLoader(rl)

  override def withResourceLoaders(rl: List[ResourceLoader]): AMFConfiguration =
    super._withResourceLoaders(rl)

  override def withUnitCache(cache: UnitCache): AMFConfiguration =
    super._withUnitCache(cache)

  override def withPlugin(amfPlugin: AMFPlugin[_]): AMFConfiguration =
    super._withPlugin(amfPlugin)

  override def withPlugins(plugins: List[AMFPlugin[_]]): AMFConfiguration =
    super._withPlugins(plugins)

  override def withValidationProfile(profile: ValidationProfile): AMFConfiguration =
    super._withValidationProfile(profile)

  override def withTransformationPipeline(pipeline: TransformationPipeline): AMFConfiguration =
    super._withTransformationPipeline(pipeline)

  /** AMF internal method just to facilitate the construction */
  override private[amf] def withTransformationPipelines(pipelines: List[TransformationPipeline]): AMFConfiguration =
    super._withTransformationPipelines(pipelines)

  override def withRenderOptions(renderOptions: RenderOptions): AMFConfiguration =
    super._withRenderOptions(renderOptions)

  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): AMFConfiguration =
    super._withErrorHandlerProvider(provider)

  override def withEventListener(listener: AMFEventListener): AMFConfiguration = super._withEventListener(listener)

  override def withLogger(logger: AMFLogger): AMFConfiguration = super._withLogger(logger)

  def merge(other: AMFConfiguration): AMFConfiguration = super._merge(other)

  override protected def copy(resolvers: AMFResolvers,
                              errorHandlerProvider: ErrorHandlerProvider,
                              registry: AMFRegistry,
                              logger: AMFLogger,
                              listeners: Set[AMFEventListener],
                              options: AMFOptions): AMFConfiguration =
    new AMFConfiguration(resolvers, errorHandlerProvider, registry, logger, listeners, options)
}
