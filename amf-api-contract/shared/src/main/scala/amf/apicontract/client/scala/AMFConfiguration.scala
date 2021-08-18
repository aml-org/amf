package amf.apicontract.client.scala

import amf.aml.client.scala.AMLConfiguration
import amf.aml.client.scala.model.document.Dialect
import amf.apicontract.internal.annotations.{APISerializableAnnotations, WebAPISerializableAnnotations}
import amf.apicontract.internal.convert.ApiRegister
import amf.apicontract.internal.entities.{APIEntities, FragmentEntities}
import amf.apicontract.internal.plugins.{ExternalJsonYamlRefsParsePlugin, JsonSchemaParsePlugin, JsonSchemaRenderPlugin}
import amf.apicontract.internal.spec.async.{Async20ParsePlugin, Async20RenderPlugin}
import amf.apicontract.internal.spec.oas.{Oas20ParsePlugin, Oas20RenderPlugin, Oas30ParsePlugin, Oas30RenderPlugin}
import amf.apicontract.internal.spec.payload.{PayloadParsePlugin, PayloadRenderPlugin}
import amf.apicontract.internal.spec.raml.{Raml08ParsePlugin, Raml08RenderPlugin, Raml10ParsePlugin, Raml10RenderPlugin}
import amf.apicontract.internal.transformation._
import amf.apicontract.internal.transformation.compatibility.{
  Oas20CompatibilityPipeline,
  Oas3CompatibilityPipeline,
  Raml08CompatibilityPipeline,
  Raml10CompatibilityPipeline
}
import amf.apicontract.internal.validation.model.ApiValidationProfiles._
import amf.apicontract.internal.validation.payload.PayloadValidationPlugin
import amf.apicontract.internal.validation.shacl.{ShaclModelValidationPlugin, ViolationModelValidationPlugin}
import amf.core.client.scala.config._
import amf.core.client.scala.errorhandling.ErrorHandlerProvider
import amf.core.client.scala.execution.ExecutionEnvironment
import amf.core.client.scala.model.domain.AnnotationGraphLoader
import amf.core.client.scala.resource.ResourceLoader
import amf.core.client.scala.transform.TransformationPipeline
import amf.core.internal.metamodel.ModelDefaultBuilder
import amf.core.internal.plugins.AMFPlugin
import amf.core.internal.plugins.syntax.AntlrSyntaxParsePlugin
import amf.core.internal.registries.AMFRegistry
import amf.core.internal.resource.AMFResolvers
import amf.core.internal.validation.core.ValidationProfile
import amf.shapes.client.scala.plugin.JsonSchemaShapePayloadValidationPlugin
import amf.shapes.internal.annotations.ShapeSerializableAnnotations
import amf.shapes.internal.entities.ShapeEntities

import scala.concurrent.Future

sealed trait APIConfigurationBuilder {

  protected def unsupportedTransformationsSet(configName: String) = List(
    UnsupportedTransformationPipeline.editing(configName),
    UnsupportedTransformationPipeline.default(configName),
    UnsupportedTransformationPipeline.cache(configName)
  )

//  will also define APIDomainPlugin, DataShapesDomainPlugin
  private[amf] def common(): AMFConfiguration = {
    val configuration = AMLConfiguration.predefined()
    ApiRegister.register() // TODO ARM remove when APIMF-3000 is done
    val result = new AMFConfiguration(
      configuration.resolvers,
      configuration.errorHandlerProvider,
      // TODO - ARM: move shapes entities and annotations to shape module (?)
      configuration.registry
        .withEntities(APIEntities.entities ++ FragmentEntities.entities ++ ShapeEntities.entities)
        .withAnnotations(
          APISerializableAnnotations.annotations ++ WebAPISerializableAnnotations.annotations ++ ShapeSerializableAnnotations.annotations),
      configuration.listeners,
      configuration.options
    ).withPlugins(
        List(
          PayloadValidationPlugin(),
          JsonSchemaShapePayloadValidationPlugin
        ))
      .withFallback(ApiContractFallbackPlugin())
    result
  }
}

private[amf] object BaseApiConfiguration extends APIConfigurationBuilder {

  def BASE(): AMFConfiguration =
    common()
      .withValidationProfile(AmfValidationProfile)
      .withTransformationPipelines(unsupportedTransformationsSet("Base"))
}

/**
  * [[APIConfigurationBuilder.common common()]] configuration with all configurations needed for RAML like:
  *   - Validation rules
  *   - Parse and emit plugins
  *   - Transformation Pipelines
  */
object RAMLConfiguration extends APIConfigurationBuilder {

  private val raml = "RAML"

  def RAML10(): AMFConfiguration =
    common()
      .withPlugins(List(Raml10ParsePlugin, Raml10RenderPlugin, Raml10ElementRenderPlugin, ShaclModelValidationPlugin))
      .withValidationProfile(Raml10ValidationProfile)
      .withTransformationPipelines(
        List(
          Raml10TransformationPipeline(),
          Raml10EditingPipeline(),
          Raml10CompatibilityPipeline(),
          Raml10CachePipeline()
        ))
  def RAML08(): AMFConfiguration =
    common()
      .withPlugins(List(Raml08ParsePlugin, Raml08RenderPlugin, Raml08ElementRenderPlugin, ShaclModelValidationPlugin))
      .withValidationProfile(Raml08ValidationProfile)
      .withTransformationPipelines(
        List(
          Raml08TransformationPipeline(),
          Raml08EditingPipeline(),
          Raml08CompatibilityPipeline(),
          Raml08CachePipeline()
        ))

  def RAML(): AMFConfiguration =
    common()
      .withPlugins(List(Raml08ParsePlugin, Raml10ParsePlugin, ViolationModelValidationPlugin(raml)))
      .withTransformationPipelines(unsupportedTransformationsSet(raml))
}

/**
  * [[APIConfigurationBuilder.common common()]] configuration with all configurations needed for OAS like:
  *  - Validation rules
  *  - Parse and emit plugins
  *  - Transformation Pipelines
  */
object OASConfiguration extends APIConfigurationBuilder {

  private val oas = "OAS"

  def OAS20(): AMFConfiguration =
    common()
      .withPlugins(List(Oas20ParsePlugin, Oas20RenderPlugin, Oas20ElementRenderPlugin, ShaclModelValidationPlugin))
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
      .withPlugins(List(Oas30ParsePlugin, Oas30RenderPlugin, Oas30ElementRenderPlugin, ShaclModelValidationPlugin))
      .withValidationProfile(Oas30ValidationProfile)
      .withTransformationPipelines(
        List(
          Oas30TransformationPipeline(),
          Oas3EditingPipeline(),
          Oas3CompatibilityPipeline(),
          Oas3CachePipeline()
        ))

  def OAS(): AMFConfiguration =
    common()
      .withPlugins(List(Oas30ParsePlugin, Oas20ParsePlugin, ViolationModelValidationPlugin(oas)))
      .withTransformationPipelines(unsupportedTransformationsSet(oas))
}

object GRPCConfiguration extends APIConfigurationBuilder {
  def GRPC(): AMFConfiguration =
    common()
      .withPlugins(List(GrpcParsePlugin, AntlrSyntaxParsePlugin, GrpcRenderPlugin))
}

/** Merged [[OASConfiguration]] and [[RAMLConfiguration]] configurations */
object WebAPIConfiguration extends APIConfigurationBuilder {

  private val name = "WebAPI"

  def WebAPI(): AMFConfiguration =
    common()
      .withFallback(ApiContractFallbackPlugin(false))
      .withPlugins(
        List(Oas30ParsePlugin,
             Oas20ParsePlugin,
             Raml10ParsePlugin,
             Raml08ParsePlugin,
             ViolationModelValidationPlugin(name)))
      .withTransformationPipelines(unsupportedTransformationsSet(name))
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
      .withPlugins(
        List(Async20ParsePlugin, Async20RenderPlugin, Async20ElementRenderPlugin, ShaclModelValidationPlugin))
      .withValidationProfile(Async20ValidationProfile)
      .withTransformationPipelines(
        List(
          Async20TransformationPipeline(),
          Async20EditingPipeline(),
          Async20CachePipeline()
        ))
}

/** Merged [[WebAPIConfiguration]] and [[AsyncAPIConfiguration.Async20()]] configurations */
object APIConfiguration extends APIConfigurationBuilder {

  private val name = "API"

  def API(): AMFConfiguration =
    WebAPIConfiguration
      .WebAPI()
      .withPlugins(List(Async20ParsePlugin, ViolationModelValidationPlugin(name)))
      .withTransformationPipelines(unsupportedTransformationsSet(name))
}

/**
  * The AMFConfiguration lets you customize all AMF-specific configurations.
  * Its immutable and created through builders. An instance is needed to use AMF.
  *
  * @see [[AMFBaseUnitClient]]
  */
class AMFConfiguration private[amf] (override private[amf] val resolvers: AMFResolvers,
                                     override private[amf] val errorHandlerProvider: ErrorHandlerProvider,
                                     override private[amf] val registry: AMFRegistry,
                                     override private[amf] val listeners: Set[AMFEventListener],
                                     override private[amf] val options: AMFOptions)
    extends AMLConfiguration(resolvers, errorHandlerProvider, registry, listeners, options) {

  override def baseUnitClient(): AMFBaseUnitClient         = new AMFBaseUnitClient(this)
  override def elementClient(): AMFElementClient           = new AMFElementClient(this)
  override def configurationState(): AMFConfigurationState = new AMFConfigurationState(this)

  override def withParsingOptions(parsingOptions: ParsingOptions): AMFConfiguration =
    super._withParsingOptions(parsingOptions)

  override def withResourceLoader(rl: ResourceLoader): AMFConfiguration =
    super._withResourceLoader(rl)

  override def withResourceLoaders(rl: List[ResourceLoader]): AMFConfiguration =
    super._withResourceLoaders(rl)

  override def withUnitCache(cache: UnitCache): AMFConfiguration =
    super._withUnitCache(cache)

  override def withFallback(plugin: DomainParsingFallback): AMFConfiguration = super._withFallback(plugin)

  override def withPlugin(amfPlugin: AMFPlugin[_]): AMFConfiguration =
    super._withPlugin(amfPlugin)

  override def withPlugins(plugins: List[AMFPlugin[_]]): AMFConfiguration =
    super._withPlugins(plugins)

  private[amf] override def withEntities(entities: Map[String, ModelDefaultBuilder]): AMFConfiguration =
    super._withEntities(entities)

  private[amf] override def withAnnotations(annotations: Map[String, AnnotationGraphLoader]): AMFConfiguration =
    super._withAnnotations(annotations)

  private[amf] override def withValidationProfile(profile: ValidationProfile): AMFConfiguration =
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

  override def withDialect(path: String): Future[AMFConfiguration] =
    super.withDialect(path).map(_.asInstanceOf[AMFConfiguration])(getExecutionContext)

  override def withDialect(dialect: Dialect): AMFConfiguration =
    super.withDialect(dialect).asInstanceOf[AMFConfiguration]

  override def forInstance(url: String): Future[AMFConfiguration] =
    super.forInstance(url).map(_.asInstanceOf[AMFConfiguration])(getExecutionContext)

  override def withExecutionEnvironment(executionEnv: ExecutionEnvironment): AMFConfiguration =
    super._withExecutionEnvironment(executionEnv)

  override protected def copy(resolvers: AMFResolvers,
                              errorHandlerProvider: ErrorHandlerProvider,
                              registry: AMFRegistry,
                              listeners: Set[AMFEventListener],
                              options: AMFOptions): AMFConfiguration =
    new AMFConfiguration(resolvers, errorHandlerProvider, registry, listeners, options)
}
