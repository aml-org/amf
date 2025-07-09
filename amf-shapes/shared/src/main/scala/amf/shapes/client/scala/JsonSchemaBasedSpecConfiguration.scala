package amf.shapes.client.scala

import amf.aml.client.scala.model.document.{Dialect, DialectInstance}
import amf.aml.internal.registries.AMLRegistry
import amf.core.client.scala.adoption.IdAdopterProvider
import amf.core.client.scala.config._
import amf.core.client.scala.errorhandling.ErrorHandlerProvider
import amf.core.client.scala.execution.ExecutionEnvironment
import amf.core.client.scala.model.domain.AnnotationGraphLoader
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.resource.ResourceLoader
import amf.core.client.scala.transform.TransformationPipeline
import amf.core.client.scala.vocabulary.NamespaceAliases
import amf.core.internal.metamodel.ModelDefaultBuilder
import amf.core.internal.plugins.AMFPlugin
import amf.core.internal.plugins.parse.DomainParsingFallback
import amf.core.internal.registries.AMFRegistry
import amf.core.internal.resource.AMFResolvers
import amf.core.internal.validation.EffectiveValidations
import amf.core.internal.validation.core.ValidationProfile
import amf.shapes.internal.convert.ShapesRegister

import scala.concurrent.{ExecutionContext, Future}

class JsonSchemaBasedSpecConfiguration private[amf] (
    override private[amf] val resolvers: AMFResolvers,
    override private[amf] val errorHandlerProvider: ErrorHandlerProvider,
    override private[amf] val registry: AMLRegistry,
    override private[amf] val listeners: Set[AMFEventListener],
    override private[amf] val options: AMFOptions,
    override private[amf] val idAdopterProvider: IdAdopterProvider
) extends ShapesConfiguration(resolvers, errorHandlerProvider, registry, listeners, options, idAdopterProvider) {

  private implicit val ec: ExecutionContext = this.getExecutionContext

  override protected[amf] def copy(
      resolvers: AMFResolvers = resolvers,
      errorHandlerProvider: ErrorHandlerProvider = errorHandlerProvider,
      registry: AMFRegistry = registry,
      listeners: Set[AMFEventListener] = listeners,
      options: AMFOptions = options,
      idAdopterProvider: IdAdopterProvider = idAdopterProvider
  ): JsonSchemaBasedSpecConfiguration =
    new JsonSchemaBasedSpecConfiguration(
      resolvers,
      errorHandlerProvider,
      registry.asInstanceOf[AMLRegistry],
      listeners,
      options,
      idAdopterProvider
    )

  /*
   * IMPLEMENTATIONS SHOULD ADD AN `override def baseUnitClient(): JsonSchemaBasedSpecBaseUnitClient` ON THEIR SIDE.
   * GIVEN THAT `JsonSchemaBasedSpecBaseUnitClient` IS ABSTRACT IT CANNOT BE DONE HERE
   */

  // Needed to override all these method in order to return the specific JsonSchemaBasedSpecConfiguration and not the parent one

  /** Set [[ParsingOptions]]
    * @param parsingOptions
    *   [[ParsingOptions]] to add to configuration object
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with [[ParsingOptions]] added
    */
  override def withParsingOptions(parsingOptions: ParsingOptions): JsonSchemaBasedSpecConfiguration =
    super._withParsingOptions(parsingOptions)

  /** Set [[RenderOptions]]
    * @param renderOptions
    *   [[RenderOptions]] to add to configuration object
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with [[ParsingOptions]] added
    */
  override def withRenderOptions(renderOptions: RenderOptions): JsonSchemaBasedSpecConfiguration =
    super._withRenderOptions(renderOptions)

  /** Add a [[ResourceLoader]]
    * @param rl
    *   [[ResourceLoader]] to add to configuration object
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with the [[ResourceLoader]] added
    */
  override def withResourceLoader(rl: ResourceLoader): JsonSchemaBasedSpecConfiguration =
    super._withResourceLoader(rl)

  /** Set the configuration [[ResourceLoader]]s
    * @param rl
    *   a list of [[ResourceLoader]] to set to the configuration object
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with [[ResourceLoader]]s set
    */
  override def withResourceLoaders(rl: List[ResourceLoader]): JsonSchemaBasedSpecConfiguration =
    super._withResourceLoaders(rl)

  /** Set [[UnitCache]]
    * @param cache
    *   [[UnitCache]] to add to configuration object
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with [[UnitCache]] added
    */
  override def withUnitCache(cache: UnitCache): JsonSchemaBasedSpecConfiguration =
    super._withUnitCache(cache)

  override def withFallback(plugin: DomainParsingFallback): JsonSchemaBasedSpecConfiguration =
    super._withFallback(plugin)

  override def withRootParsePlugin(amfParsePlugin: AMFParsePlugin): JsonSchemaBasedSpecConfiguration =
    super._withRootParsePlugin(amfParsePlugin)

  override def withPlugin(amfPlugin: AMFPlugin[_]): JsonSchemaBasedSpecConfiguration =
    super._withPlugin(amfPlugin)

  override def withReferenceParsePlugin(plugin: AMFParsePlugin): JsonSchemaBasedSpecConfiguration =
    super._withReferenceParsePlugin(plugin)

  override def withRootParsePlugins(amfParsePlugin: List[AMFParsePlugin]): JsonSchemaBasedSpecConfiguration =
    super._withRootParsePlugins(amfParsePlugin)

  override def withReferenceParsePlugins(amfPlugin: List[AMFParsePlugin]): JsonSchemaBasedSpecConfiguration =
    super._withReferenceParsePlugins(amfPlugin)

  override def withPlugins(plugins: List[AMFPlugin[_]]): JsonSchemaBasedSpecConfiguration =
    super._withPlugins(plugins)

  private[amf] override def withValidationProfile(profile: ValidationProfile): JsonSchemaBasedSpecConfiguration =
    super._withValidationProfile(profile)

  // Keep AMF internal, done to avoid recomputing validations every time a config is requested
  private[amf] override def withValidationProfile(
      profile: ValidationProfile,
      effective: EffectiveValidations
  ): JsonSchemaBasedSpecConfiguration =
    super._withValidationProfile(profile, effective)

  /** Add a [[TransformationPipeline]]
    * @param pipeline
    *   [[TransformationPipeline]] to add to configuration object
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with [[TransformationPipeline]] added
    */
  override def withTransformationPipeline(pipeline: TransformationPipeline): JsonSchemaBasedSpecConfiguration =
    super._withTransformationPipeline(pipeline)

  /** AMF internal method just to facilitate the construction */
  override private[amf] def withTransformationPipelines(
      pipelines: List[TransformationPipeline]
  ): JsonSchemaBasedSpecConfiguration =
    super._withTransformationPipelines(pipelines)

  /** Set [[ErrorHandlerProvider]]
    * @param provider
    *   [[ErrorHandlerProvider]] to set to configuration object
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with [[ErrorHandlerProvider]] set
    */
  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): JsonSchemaBasedSpecConfiguration =
    super._withErrorHandlerProvider(provider)

  /** Add an [[AMFEventListener]]
    * @param listener
    *   [[AMFEventListener]] to add to configuration object
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with [[AMFEventListener]] added
    */
  override def withEventListener(listener: AMFEventListener): JsonSchemaBasedSpecConfiguration =
    super._withEventListener(listener)

  private[amf] override def withEntities(entities: Map[String, ModelDefaultBuilder]): JsonSchemaBasedSpecConfiguration =
    super._withEntities(entities)

  override def withAliases(aliases: NamespaceAliases): JsonSchemaBasedSpecConfiguration =
    super._withAliases(aliases)

  private[amf] override def withExtensions(dialect: Dialect): JsonSchemaBasedSpecConfiguration = {
    super.withExtensions(dialect).asInstanceOf[JsonSchemaBasedSpecConfiguration]
  }

  private[amf] override def withAnnotations(
      annotations: Map[String, AnnotationGraphLoader]
  ): JsonSchemaBasedSpecConfiguration =
    super._withAnnotations(annotations)

  /** Set [[BaseExecutionEnvironment]]
    * @param executionEnv
    *   [[BaseExecutionEnvironment]] to set to configuration object
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with [[BaseExecutionEnvironment]] set
    */
  override def withExecutionEnvironment(executionEnv: ExecutionEnvironment): JsonSchemaBasedSpecConfiguration =
    super._withExecutionEnvironment(executionEnv)

  /** Register a Dialect
    * @param dialect
    *   [[Dialect]] to register
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with [[Dialect]] registered
    */
  override def withDialect(dialect: Dialect): JsonSchemaBasedSpecConfiguration =
    super.withDialect(dialect).asInstanceOf[JsonSchemaBasedSpecConfiguration]

  /** Register a Dialect
    * @param url
    *   URL of the Dialect to register
    * @return
    *   A CompletableFuture of [[JsonSchemaBasedSpecConfiguration]]
    */
  override def withDialect(url: String): Future[JsonSchemaBasedSpecConfiguration] =
    super.withDialect(url).map(_.asInstanceOf[JsonSchemaBasedSpecConfiguration])(getExecutionContext)

  /** Register a [[Dialect]] linked from a [[DialectInstance]]
    * @param url
    *   of the [[DialectInstance]]
    * @return
    *   A CompletableFuture of [[JsonSchemaBasedSpecConfiguration]]
    */
  override def forInstance(url: String): Future[JsonSchemaBasedSpecConfiguration] =
    super.forInstance(url).map(_.asInstanceOf[JsonSchemaBasedSpecConfiguration])(getExecutionContext)

  override def withIdAdopterProvider(idAdopterProvider: IdAdopterProvider): JsonSchemaBasedSpecConfiguration =
    super._withIdAdopterProvider(idAdopterProvider)
}

object JsonSchemaBasedSpecConfiguration {

  def base(): JsonSchemaBasedSpecConfiguration = {
    ShapesRegister.register()
    val predefinedShapesConfig = ShapesConfiguration.predefined()
    new JsonSchemaBasedSpecConfiguration(
      predefinedShapesConfig.resolvers,
      predefinedShapesConfig.errorHandlerProvider,
      predefinedShapesConfig.registry,
      predefinedShapesConfig.listeners,
      predefinedShapesConfig.options,
      predefinedShapesConfig.idAdopterProvider
    )
  }
}
