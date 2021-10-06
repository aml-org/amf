package amf.shapes.client.scala

import amf.aml.client.scala.model.document.Dialect
import amf.aml.client.scala.model.document.DialectInstance
import amf.aml.client.scala.model.domain.SemanticExtension
import amf.aml.client.scala.{AMLBaseUnitClient, AMLConfiguration, AMLConfigurationState, AMLElementClient}
import amf.aml.internal.annotations.serializable.AMLSerializableAnnotations
import amf.aml.internal.entities.AMLEntities
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.config._
import amf.core.client.scala.errorhandling.{DefaultErrorHandlerProvider, ErrorHandlerProvider}
import amf.core.client.scala.execution.ExecutionEnvironment
import amf.core.client.scala.model.domain.AnnotationGraphLoader
import amf.core.client.scala.resource.ResourceLoader
import amf.core.client.scala.transform.TransformationPipeline
import amf.core.internal.metamodel.ModelDefaultBuilder
import amf.core.internal.plugins.AMFPlugin
import amf.core.internal.plugins.parse.DomainParsingFallback
import amf.core.internal.registries.AMFRegistry
import amf.core.internal.resource.AMFResolvers
import amf.core.internal.validation.core.ValidationProfile
import amf.shapes.client.scala.plugin.JsonSchemaShapePayloadValidationPlugin
import amf.shapes.internal.annotations.ShapeSerializableAnnotations
import amf.shapes.internal.convert.ShapesRegister
import amf.shapes.internal.entities.ShapeEntities

import scala.concurrent.{ExecutionContext, Future}

class ShapesConfiguration private[amf] (override private[amf] val resolvers: AMFResolvers,
                                        override private[amf] val errorHandlerProvider: ErrorHandlerProvider,
                                        override private[amf] val registry: AMFRegistry,
                                        override private[amf] val listeners: Set[AMFEventListener],
                                        override private[amf] val options: AMFOptions)
    extends AMLConfiguration(resolvers, errorHandlerProvider, registry, listeners, options) {

  private implicit val ec: ExecutionContext = this.getExecutionContext

  override protected def copy(resolvers: AMFResolvers = resolvers,
                              errorHandlerProvider: ErrorHandlerProvider = errorHandlerProvider,
                              registry: AMFRegistry = registry,
                              listeners: Set[AMFEventListener] = listeners,
                              options: AMFOptions = options): ShapesConfiguration =
    new ShapesConfiguration(resolvers, errorHandlerProvider, registry, listeners, options)

  /** Contains common AMF graph operations associated to documents */
  override def baseUnitClient(): ShapesBaseUnitClient = new ShapesBaseUnitClient(this)

  /** Contains functionality associated with specific elements of the AMF model */
  override def elementClient(): ShapesElementClient = new ShapesElementClient(this)

  /** Contains methods to get information about the current state of the configuration */
  override def configurationState(): AMLConfigurationState = new AMLConfigurationState(this)

  /**
    * Set [[ParsingOptions]]
    * @param parsingOptions [[ParsingOptions]] to add to configuration object
    * @return [[ShapesConfiguration]] with [[ParsingOptions]] added
    */
  override def withParsingOptions(parsingOptions: ParsingOptions): ShapesConfiguration =
    super._withParsingOptions(parsingOptions)

  /**
    * Set [[RenderOptions]]
    * @param renderOptions [[RenderOptions]] to add to configuration object
    * @return [[ShapesConfiguration]] with [[ParsingOptions]] added
    */
  override def withRenderOptions(renderOptions: RenderOptions): ShapesConfiguration =
    super._withRenderOptions(renderOptions)

  /**
    * Add a [[ResourceLoader]]
    * @param rl [[ResourceLoader]] to add to configuration object
    * @return [[ShapesConfiguration]] with the [[ResourceLoader]] added
    */
  override def withResourceLoader(rl: ResourceLoader): ShapesConfiguration =
    super._withResourceLoader(rl)

  /**
    * Set the configuration [[ResourceLoader]]s
    * @param rl a list of [[ResourceLoader]] to set to the configuration object
    * @return [[ShapesConfiguration]] with [[ResourceLoader]]s set
    */
  override def withResourceLoaders(rl: List[ResourceLoader]): ShapesConfiguration =
    super._withResourceLoaders(rl)

  /**
    * Set [[UnitCache]]
    * @param cache [[UnitCache]] to add to configuration object
    * @return [[ShapesConfiguration]] with [[UnitCache]] added
    */
  override def withUnitCache(cache: UnitCache): ShapesConfiguration =
    super._withUnitCache(cache)

  override def withFallback(plugin: DomainParsingFallback): ShapesConfiguration = super._withFallback(plugin)

  override def withPlugin(amfPlugin: AMFPlugin[_]): ShapesConfiguration =
    super._withPlugin(amfPlugin)

  override def withPlugins(plugins: List[AMFPlugin[_]]): ShapesConfiguration =
    super._withPlugins(plugins)

  private[amf] override def withValidationProfile(profile: ValidationProfile): ShapesConfiguration =
    super._withValidationProfile(profile)

  /**
    * Add a [[TransformationPipeline]]
    * @param pipeline [[TransformationPipeline]] to add to configuration object
    * @return [[ShapesConfiguration]] with [[TransformationPipeline]] added
    */
  override def withTransformationPipeline(pipeline: TransformationPipeline): ShapesConfiguration =
    super._withTransformationPipeline(pipeline)

  /** AMF internal method just to facilitate the construction */
  override private[amf] def withTransformationPipelines(pipelines: List[TransformationPipeline]): ShapesConfiguration =
    super._withTransformationPipelines(pipelines)

  /**
    * Set [[ErrorHandlerProvider]]
    * @param provider [[ErrorHandlerProvider]] to set to configuration object
    * @return [[ShapesConfiguration]] with [[ErrorHandlerProvider]] set
    */
  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): ShapesConfiguration =
    super._withErrorHandlerProvider(provider)

  /**
    * Add an [[AMFEventListener]]
    * @param listener [[AMFEventListener]] to add to configuration object
    * @return [[ShapesConfiguration]] with [[AMFEventListener]] added
    */
  override def withEventListener(listener: AMFEventListener): ShapesConfiguration = super._withEventListener(listener)

  private[amf] override def withEntities(entities: Map[String, ModelDefaultBuilder]): ShapesConfiguration =
    super._withEntities(entities)

  override private[amf] def withExtensions(extensions: Seq[SemanticExtension]): ShapesConfiguration =
    copy(registry = registry.withExtensions(extensions))

  private[amf] override def withAnnotations(annotations: Map[String, AnnotationGraphLoader]): ShapesConfiguration =
    super._withAnnotations(annotations)

  /**
    * Set [[BaseExecutionEnvironment]]
    * @param executionEnv [[BaseExecutionEnvironment]] to set to configuration object
    * @return [[ShapesConfiguration]] with [[BaseExecutionEnvironment]] set
    */
  override def withExecutionEnvironment(executionEnv: ExecutionEnvironment): ShapesConfiguration =
    super._withExecutionEnvironment(executionEnv)

  /**
    * Register a Dialect
    * @param dialect [[Dialect]] to register
    * @return [[ShapesConfiguration]] with [[Dialect]] registered
    */
  override def withDialect(dialect: Dialect): ShapesConfiguration =
    super.withDialect(dialect).asInstanceOf[ShapesConfiguration]

  /**
    * Register a Dialect
    * @param url URL of the Dialect to register
    * @return A CompletableFuture of [[ShapesConfiguration]]
    */
  override def withDialect(url: String): Future[ShapesConfiguration] =
    super.withDialect(url).map(_.asInstanceOf[ShapesConfiguration])(getExecutionContext)

  /**
    * Register a [[Dialect]] linked from a [[DialectInstance]]
    * @param url of the [[DialectInstance]]
    * @return A CompletableFuture of [[ShapesConfiguration]]
    */
  override def forInstance(url: String): Future[ShapesConfiguration] =
    super.forInstance(url).map(_.asInstanceOf[ShapesConfiguration])(getExecutionContext)
}

object ShapesConfiguration {

  def empty(): ShapesConfiguration = {
    new ShapesConfiguration(
      AMFResolvers.predefined(),
      DefaultErrorHandlerProvider,
      AMFRegistry.empty,
      Set.empty,
      AMFOptions.default()
    )
  }

  def predefined(): ShapesConfiguration = {
    ShapesRegister.register() // TODO ARM remove when APIMF-3000 is done
    val predefinedAMLConfig = AMLConfiguration.predefined()
    val coreEntities        = AMFGraphConfiguration.predefined().getRegistry.entitiesRegistry.domainEntities
    new ShapesConfiguration(
      predefinedAMLConfig.resolvers,
      predefinedAMLConfig.errorHandlerProvider,
      predefinedAMLConfig.registry
        .withEntities(AMLEntities.entities)
        .withAnnotations(AMLSerializableAnnotations.annotations),
      predefinedAMLConfig.listeners,
      predefinedAMLConfig.options
    ).withEntities(ShapeEntities.entities ++ coreEntities)
      .withAnnotations(ShapeSerializableAnnotations.annotations)
      .withPlugin(JsonSchemaShapePayloadValidationPlugin)
  }
}
