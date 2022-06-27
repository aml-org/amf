package amf.apicontract.client.scala

import amf.aml.client.scala.AMLConfiguration
import amf.aml.client.scala.model.document.{Dialect, DialectInstance}
import amf.aml.internal.registries.AMLRegistry
import amf.apicontract.internal.annotations.{APISerializableAnnotations, WebAPISerializableAnnotations}
import amf.apicontract.internal.convert.ApiRegister
import amf.apicontract.internal.entities.{APIEntities, FragmentEntities}
import amf.apicontract.internal.plugins.ApiContractFallbackPlugin
import amf.apicontract.internal.spec.async.{Async20ElementRenderPlugin, Async20ParsePlugin, Async20RenderPlugin}
import amf.apicontract.internal.spec.jsonschema.{JsonSchemaParsePlugin, JsonSchemaRenderPlugin}
import amf.apicontract.internal.spec.oas._
import amf.apicontract.internal.spec.raml._
import amf.apicontract.internal.transformation._
import amf.apicontract.internal.transformation.compatibility.{
  Oas20CompatibilityPipeline,
  Oas3CompatibilityPipeline,
  Raml08CompatibilityPipeline,
  Raml10CompatibilityPipeline
}
import amf.apicontract.internal.validation.model.ApiEffectiveValidations._
import amf.apicontract.internal.validation.model.ApiValidationProfiles._
import amf.apicontract.internal.validation.payload.PayloadValidationPlugin
import amf.apicontract.internal.validation.shacl.{ShaclModelValidationPlugin, ViolationModelValidationPlugin}
import amf.core.client.common.validation.ProfileNames
import amf.core.client.common.validation.ProfileNames._
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.config._
import amf.core.client.scala.errorhandling.ErrorHandlerProvider
import amf.core.client.scala.execution.ExecutionEnvironment
import amf.core.client.scala.model.domain.AnnotationGraphLoader
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.resource.ResourceLoader
import amf.core.client.scala.transform.TransformationPipeline
import amf.core.internal.metamodel.ModelDefaultBuilder
import amf.core.internal.plugins.AMFPlugin
import amf.core.internal.plugins.parse.DomainParsingFallback
import amf.core.internal.registries.AMFRegistry
import amf.core.internal.remote.Spec
import amf.core.internal.resource.AMFResolvers
import amf.core.internal.validation.EffectiveValidations
import amf.core.internal.validation.core.ValidationProfile
import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.client.scala.plugin.JsonSchemaShapePayloadValidationPlugin
import amf.shapes.internal.annotations.ShapeSerializableAnnotations
import amf.shapes.internal.entities.ShapeEntities

import scala.concurrent.Future

trait APIConfigurationBuilder {

  protected def unsupportedTransformationsSet(configName: String) = List(
    UnsupportedTransformationPipeline.editing(configName),
    UnsupportedTransformationPipeline.default(configName),
    UnsupportedTransformationPipeline.cache(configName)
  )

//  will also define APIDomainPlugin, DataShapesDomainPlugin
  private[amf] def common(): AMFConfiguration = {
    val configuration = AMLConfiguration.predefined()
    ApiRegister.register() // TODO ARM remove when APIMF-3000 is done
    val coreEntities = AMFGraphConfiguration.predefined().getRegistry.getEntitiesRegistry.domainEntities
    val result = new AMFConfiguration(
      configuration.resolvers,
      configuration.errorHandlerProvider,
      configuration.registry
        .withEntities(APIEntities.entities ++ FragmentEntities.entities ++ ShapeEntities.entities ++ coreEntities)
        .withAnnotations(
          APISerializableAnnotations.annotations ++ WebAPISerializableAnnotations.annotations ++ ShapeSerializableAnnotations.annotations
        ),
      configuration.listeners,
      configuration.options
    ).withPlugins(
      List(
        JsonSchemaShapePayloadValidationPlugin
      )
    ).withFallback(ApiContractFallbackPlugin())
    result
  }
}

private[amf] object BaseApiConfiguration extends APIConfigurationBuilder {

  def BASE(): AMFConfiguration =
    common()
      .withValidationProfile(AmfValidationProfile)
      .withTransformationPipelines(unsupportedTransformationsSet("Base"))
}

/** [[APIConfigurationBuilder.common common()]] configuration with all configurations needed for RAML like:
  *   - Validation rules
  *   - Parse and emit plugins
  *   - Transformation Pipelines
  */
object RAMLConfiguration extends APIConfigurationBuilder {

  private val raml = "RAML"

  def RAML10(): AMFConfiguration =
    common()
      .withPlugins(
        List(
          Raml10ParsePlugin,
          Raml10RenderPlugin,
          Raml10ElementRenderPlugin,
          ShaclModelValidationPlugin(ProfileNames.RAML10),
          PayloadValidationPlugin(ProfileNames.RAML10)
        )
      )
      .withValidationProfile(Raml10ValidationProfile, Raml10EffectiveValidations)
      .withTransformationPipelines(
        List(
          Raml10TransformationPipeline(),
          Raml10EditingPipeline(),
          Raml10CompatibilityPipeline(),
          Raml10CachePipeline()
        )
      )
  def RAML08(): AMFConfiguration =
    common()
      .withPlugins(
        List(
          Raml08ParsePlugin,
          Raml08RenderPlugin,
          Raml08ElementRenderPlugin,
          ShaclModelValidationPlugin(ProfileNames.RAML08),
          PayloadValidationPlugin(ProfileNames.RAML08)
        )
      )
      .withValidationProfile(Raml08ValidationProfile, Raml08EffectiveValidations)
      .withTransformationPipelines(
        List(
          Raml08TransformationPipeline(),
          Raml08EditingPipeline(),
          Raml08CompatibilityPipeline(),
          Raml08CachePipeline()
        )
      )

  def RAML(): AMFConfiguration =
    common()
      .withPlugins(List(Raml08ParsePlugin, Raml10ParsePlugin, ViolationModelValidationPlugin(raml)))
      .withTransformationPipelines(unsupportedTransformationsSet(raml))

  def fromSpec(spec: Spec): AMFConfiguration = spec match {
    case Spec.RAML08 => RAMLConfiguration.RAML08()
    case Spec.RAML10 => RAMLConfiguration.RAML10()
    case _ =>
      throw UnrecognizedSpecException(
        s"Spec ${spec.id} not supported by RAMLConfiguration. Supported specs are ${Spec.RAML08.id}, ${Spec.RAML10.id}"
      )
  }
}

/** [[APIConfigurationBuilder.common common()]] configuration with all configurations needed for OAS like:
  *   - Validation rules
  *   - Parse and emit plugins
  *   - Transformation Pipelines
  */
object OASConfiguration extends APIConfigurationBuilder {

  private val oas = "OAS"

  def OAS20(): AMFConfiguration =
    common()
      .withPlugins(
        List(
          Oas20ParsePlugin,
          Oas20RenderPlugin,
          Oas20ElementRenderPlugin,
          ShaclModelValidationPlugin(ProfileNames.OAS20),
          PayloadValidationPlugin(ProfileNames.OAS20)
        )
      )
      .withValidationProfile(Oas20ValidationProfile, Oas20EffectiveValidations)
      .withTransformationPipelines(
        List(
          Oas20TransformationPipeline(),
          Oas20EditingPipeline(),
          Oas20CompatibilityPipeline(),
          Oas20CachePipeline()
        )
      )

  def OAS30(): AMFConfiguration =
    common()
      .withPlugins(
        List(
          Oas30ParsePlugin,
          Oas30RenderPlugin,
          Oas30ElementRenderPlugin,
          ShaclModelValidationPlugin(ProfileNames.OAS30),
          PayloadValidationPlugin(ProfileNames.OAS30)
        )
      )
      .withValidationProfile(Oas30ValidationProfile, Oas30EffectiveValidations)
      .withTransformationPipelines(
        List(
          Oas30TransformationPipeline(),
          Oas3EditingPipeline(),
          Oas3CompatibilityPipeline(),
          Oas3CachePipeline()
        )
      )

  def OAS(): AMFConfiguration =
    common()
      .withPlugins(List(Oas30ParsePlugin, Oas20ParsePlugin, ViolationModelValidationPlugin(oas)))
      .withTransformationPipelines(unsupportedTransformationsSet(oas))

  def fromSpec(spec: Spec): AMFConfiguration = spec match {
    case Spec.OAS20 => OASConfiguration.OAS20()
    case Spec.OAS30 => OASConfiguration.OAS30()
    case _ =>
      throw UnrecognizedSpecException(
        s"Spec ${spec.id} not supported by OASConfiguration. Supported specs are ${Spec.OAS20.id}, ${Spec.OAS30.id}"
      )
  }
}

case class UnrecognizedSpecException(msg: String) extends IllegalArgumentException(msg)

/** Merged [[OASConfiguration]] and [[RAMLConfiguration]] configurations */
object WebAPIConfiguration extends APIConfigurationBuilder {

  private val name = "WebAPI"

  def WebAPI(): AMFConfiguration =
    common()
      .withFallback(ApiContractFallbackPlugin(false))
      .withPlugins(
        List(
          Oas30ParsePlugin,
          Oas20ParsePlugin,
          Raml10ParsePlugin,
          Raml08ParsePlugin,
          ViolationModelValidationPlugin(name)
        )
      )
      .withTransformationPipelines(unsupportedTransformationsSet(name))

  def fromSpec(spec: Spec): AMFConfiguration = spec match {
    case Spec.RAML08 => RAMLConfiguration.RAML08()
    case Spec.RAML10 => RAMLConfiguration.RAML10()
    case Spec.OAS20  => OASConfiguration.OAS20()
    case Spec.OAS30  => OASConfiguration.OAS30()
    case _ =>
      throw UnrecognizedSpecException(
        s"Spec ${spec.id} not supported by WebApiConfiguration. Supported specs are ${Spec.RAML08.id}, ${Spec.RAML10.id}, ${Spec.OAS20.id}, ${Spec.OAS30.id}"
      )
  }
}

/** [[APIConfigurationBuilder.common common()]] configuration with all configurations needed for AsyncApi like:
  *   - Validation rules
  *   - Parse and emit plugins
  *   - Transformation Pipelines
  */
object AsyncAPIConfiguration extends APIConfigurationBuilder {
  def Async20(): AMFConfiguration =
    common()
      .withPlugins(
        List(
          Async20ParsePlugin,
          Async20RenderPlugin,
          Async20ElementRenderPlugin,
          ShaclModelValidationPlugin(ASYNC20),
          PayloadValidationPlugin(ASYNC20)
        )
      )
      .withReferenceParsePlugin(Raml10ParsePlugin)
      .withValidationProfile(Async20ValidationProfile, Async20EffectiveValidations)
      .withTransformationPipelines(
        List(
          Async20TransformationPipeline(),
          Async20EditingPipeline(),
          Async20CachePipeline()
        )
      )
}

/** [[APIConfigurationBuilder.common common()]] configuration with all configurations needed for AsyncApi like:
  *   - Validation rules
  *   - Parse and emit plugins
  *   - Transformation Pipelines
  */
object JsonSchemaConfiguration extends APIConfigurationBuilder {
  def JsonSchema(): AMFConfiguration =
    common()
      .withPlugins(
        List(
          JsonSchemaParsePlugin,
          JsonSchemaRenderPlugin
        )
      )
      .withTransformationPipelines(
        List(
          JsonSchemaTransformationPipeline(),
          JsonSchemaEditingPipeline(),
          JsonSchemaCachePipeline()
        )
      )
}

/** Merged [[WebAPIConfiguration]] and [[AsyncAPIConfiguration.Async20()]] configurations */
object APIConfiguration extends APIConfigurationBuilder {

  private val name = "API"

  def API(): AMFConfiguration =
    WebAPIConfiguration
      .WebAPI()
      .withPlugins(List(Async20ParsePlugin, ViolationModelValidationPlugin(name)))
      .withTransformationPipelines(unsupportedTransformationsSet(name))

  def fromSpec(spec: Spec): AMFConfiguration = spec match {
    case Spec.RAML08  => RAMLConfiguration.RAML08()
    case Spec.RAML10  => RAMLConfiguration.RAML10()
    case Spec.OAS20   => OASConfiguration.OAS20()
    case Spec.OAS30   => OASConfiguration.OAS30()
    case Spec.ASYNC20 => AsyncAPIConfiguration.Async20()
    case _ =>
      throw UnrecognizedSpecException(
        s"Spec ${spec.id} not supported by APIConfiguration. Supported specs are ${Spec.RAML08.id}, ${Spec.RAML10.id}, ${Spec.OAS20.id}, ${Spec.OAS30.id}, ${Spec.ASYNC20.id}"
      )
  }
}

/** The AMFConfiguration lets you customize all AMF-specific configurations. Its immutable and created through builders.
  * An instance is needed to use AMF.
  * @see
  *   [[AMFBaseUnitClient]]
  */
class AMFConfiguration private[amf] (
    override private[amf] val resolvers: AMFResolvers,
    override private[amf] val errorHandlerProvider: ErrorHandlerProvider,
    override private[amf] val registry: AMLRegistry,
    override private[amf] val listeners: Set[AMFEventListener],
    override private[amf] val options: AMFOptions
) extends ShapesConfiguration(resolvers, errorHandlerProvider, registry, listeners, options) {

  /** Contains common AMF graph operations associated to documents */
  override def baseUnitClient(): AMFBaseUnitClient = new AMFBaseUnitClient(this)

  /** Contains functionality associated with specific elements of the AMF model */
  override def elementClient(): AMFElementClient = new AMFElementClient(this)

  /** Contains methods to get information about the current state of the configuration */
  override def configurationState(): AMFConfigurationState = new AMFConfigurationState(this)

  /** Set [[ParsingOptions]]
    * @param parsingOptions
    *   [[ParsingOptions]] to add to configuration object
    * @return
    *   [[AMFConfiguration]] with [[ParsingOptions]] added
    */
  override def withParsingOptions(parsingOptions: ParsingOptions): AMFConfiguration =
    super._withParsingOptions(parsingOptions)

  /** Add a [[ResourceLoader]]
    * @param rl
    *   [[ResourceLoader]] to add to configuration object
    * @return
    *   [[AMFConfiguration]] with the [[ResourceLoader]] added
    */
  override def withResourceLoader(rl: ResourceLoader): AMFConfiguration =
    super._withResourceLoader(rl)

  /** Set the configuration [[ResourceLoader]]s
    * @param rl
    *   a list of [[ResourceLoader]] to set to the configuration object
    * @return
    *   [[AMFConfiguration]] with [[ResourceLoader]]s set
    */
  override def withResourceLoaders(rl: List[ResourceLoader]): AMFConfiguration =
    super._withResourceLoaders(rl)

  /** Set [[UnitCache]]
    * @param cache
    *   [[UnitCache]] to add to configuration object
    * @return
    *   [[AMFConfiguration]] with [[UnitCache]] added
    */
  override def withUnitCache(cache: UnitCache): AMFConfiguration =
    super._withUnitCache(cache)

  override def withFallback(plugin: DomainParsingFallback): AMFConfiguration = super._withFallback(plugin)

  override def withPlugin(amfPlugin: AMFPlugin[_]): AMFConfiguration =
    super._withPlugin(amfPlugin)

  override def withReferenceParsePlugin(amfPlugin: AMFParsePlugin): AMFConfiguration =
    super._withReferenceParsePlugin(amfPlugin)

  override def withPlugins(plugins: List[AMFPlugin[_]]): AMFConfiguration =
    super._withPlugins(plugins)

  private[amf] override def withEntities(entities: Map[String, ModelDefaultBuilder]): AMFConfiguration =
    super._withEntities(entities)

  private[amf] override def withAnnotations(annotations: Map[String, AnnotationGraphLoader]): AMFConfiguration =
    super._withAnnotations(annotations)

  private[amf] override def withExtensions(dialect: Dialect): AMFConfiguration =
    super.withExtensions(dialect).asInstanceOf[AMFConfiguration]

  private[amf] override def withValidationProfile(profile: ValidationProfile): AMFConfiguration =
    super._withValidationProfile(profile)

  // Keep AMF internal, done to avoid recomputing validations every time a config is requested
  private[amf] override def withValidationProfile(
      profile: ValidationProfile,
      effective: EffectiveValidations
  ): AMFConfiguration =
    super._withValidationProfile(profile, effective)

  /** Add a [[TransformationPipeline]]
    * @param pipeline
    *   [[TransformationPipeline]] to add to configuration object
    * @return
    *   [[AMFConfiguration]] with [[TransformationPipeline]] added
    */
  override def withTransformationPipeline(pipeline: TransformationPipeline): AMFConfiguration =
    super._withTransformationPipeline(pipeline)

  /** AMF internal method just to facilitate the construction */
  override private[amf] def withTransformationPipelines(pipelines: List[TransformationPipeline]): AMFConfiguration =
    super._withTransformationPipelines(pipelines)

  /** Set [[RenderOptions]]
    * @param renderOptions
    *   [[RenderOptions]] to set to configuration object
    * @return
    *   [[AMFConfiguration]] with [[RenderOptions]] added
    */
  override def withRenderOptions(renderOptions: RenderOptions): AMFConfiguration =
    super._withRenderOptions(renderOptions)

  /** Set [[ErrorHandlerProvider]]
    * @param provider
    *   [[ErrorHandlerProvider]] to set to configuration object
    * @return
    *   [[AMFConfiguration]] with [[ErrorHandlerProvider]] set
    */
  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): AMFConfiguration =
    super._withErrorHandlerProvider(provider)

  /** Add an [[AMFEventListener]]
    * @param listener
    *   [[AMFEventListener]] to add to configuration object
    * @return
    *   [[AMFConfiguration]] with [[AMFEventListener]] added
    */
  override def withEventListener(listener: AMFEventListener): AMFConfiguration = super._withEventListener(listener)

  /** Register a Dialect
    * @param url
    *   URL of the Dialect to register
    * @return
    *   A CompletableFuture of [[AMFConfiguration]]
    */
  override def withDialect(url: String): Future[AMFConfiguration] =
    super.withDialect(url).map(_.asInstanceOf[AMFConfiguration])(getExecutionContext)

  /** Register a Dialect
    * @param dialect
    *   [[Dialect]] to register
    * @return
    *   [[AMFConfiguration]] with [[Dialect]] registered
    */
  override def withDialect(dialect: Dialect): AMFConfiguration =
    super.withDialect(dialect).asInstanceOf[AMFConfiguration]

  /** Register a [[Dialect]] linked from a [[DialectInstance]]
    * @param url
    *   of the [[DialectInstance]]
    * @return
    *   A CompletableFuture of [[AMFConfiguration]]
    */
  override def forInstance(url: String): Future[AMFConfiguration] =
    super.forInstance(url).map(_.asInstanceOf[AMFConfiguration])(getExecutionContext)

  /** Set [[BaseExecutionEnvironment]]
    * @param executionEnv
    *   [[BaseExecutionEnvironment]] to set to configuration object
    * @return
    *   [[AMFConfiguration]] with [[BaseExecutionEnvironment]] set
    */
  override def withExecutionEnvironment(executionEnv: ExecutionEnvironment): AMFConfiguration =
    super._withExecutionEnvironment(executionEnv)

  override protected[amf] def copy(
      resolvers: AMFResolvers,
      errorHandlerProvider: ErrorHandlerProvider,
      registry: AMFRegistry,
      listeners: Set[AMFEventListener],
      options: AMFOptions
  ): AMFConfiguration =
    new AMFConfiguration(resolvers, errorHandlerProvider, registry.asInstanceOf[AMLRegistry], listeners, options)
}
