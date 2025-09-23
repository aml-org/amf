package amf.llmmetadata.client.scala

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
import amf.llmmetadata.internal.plugins.parse.LLMMetadataParsePlugin
import amf.llmmetadata.internal.plugins.render.LLMMetadataRenderPlugin
import amf.llmmetadata.internal.plugins.validation.LLMMetadataValidationPlugin
import amf.shapes.client.scala.JsonSchemaBasedSpecConfiguration
import amf.shapes.internal.plugins.parser.AMFJsonLDSchemaGraphParsePlugin
import amf.shapes.internal.plugins.render.JsonSchemaBasedSpecGraphRenderPlugin
import amf.shapes.internal.transformation.{
  JsonSchemaBasedSpecCachePipeline,
  JsonSchemaBasedSpecEditingPipeline,
  JsonSchemaBasedSpecTransformationPipeline
}

import scala.concurrent.{ExecutionContext, Future}

class LLMMetadataConfiguration private[amf] (
    override private[amf] val resolvers: AMFResolvers,
    override private[amf] val errorHandlerProvider: ErrorHandlerProvider,
    override private[amf] val registry: AMLRegistry,
    override private[amf] val listeners: Set[AMFEventListener],
    override private[amf] val options: AMFOptions,
    override private[amf] val idAdopterProvider: IdAdopterProvider
) extends JsonSchemaBasedSpecConfiguration(
        resolvers,
        errorHandlerProvider,
        registry,
        listeners,
        options,
        idAdopterProvider
    ) {

  private implicit val ec: ExecutionContext = this.getExecutionContext

  override protected[amf] def copy(
      resolvers: AMFResolvers = resolvers,
      errorHandlerProvider: ErrorHandlerProvider = errorHandlerProvider,
      registry: AMFRegistry = registry,
      listeners: Set[AMFEventListener] = listeners,
      options: AMFOptions = options,
      idAdopterProvider: IdAdopterProvider = idAdopterProvider
  ): LLMMetadataConfiguration =
    new LLMMetadataConfiguration(
        resolvers,
        errorHandlerProvider,
        registry.asInstanceOf[AMLRegistry],
        listeners,
        options,
        idAdopterProvider
    )

  override def baseUnitClient(): LLMMetadataBaseUnitClient = new LLMMetadataBaseUnitClient(this)

  // Needed to override all these method in order to return the specific LLMMetadataConfiguration and not the parent one

  /** Set [[ParsingOptions]]
    * @param parsingOptions
    *   [[ParsingOptions]] to add to configuration object
    * @return
    *   [[LLMMetadataConfiguration]] with [[ParsingOptions]] added
    */
  override def withParsingOptions(parsingOptions: ParsingOptions): LLMMetadataConfiguration =
    super._withParsingOptions(parsingOptions)

  /** Set [[RenderOptions]]
    * @param renderOptions
    *   [[RenderOptions]] to add to configuration object
    * @return
    *   [[LLMMetadataConfiguration]] with [[ParsingOptions]] added
    */
  override def withRenderOptions(renderOptions: RenderOptions): LLMMetadataConfiguration =
    super._withRenderOptions(renderOptions)

  /** Add a [[ResourceLoader]]
    * @param rl
    *   [[ResourceLoader]] to add to configuration object
    * @return
    *   [[LLMMetadataConfiguration]] with the [[ResourceLoader]] added
    */
  override def withResourceLoader(rl: ResourceLoader): LLMMetadataConfiguration =
    super._withResourceLoader(rl)

  /** Set the configuration [[ResourceLoader]]s
    * @param rl
    *   a list of [[ResourceLoader]] to set to the configuration object
    * @return
    *   [[LLMMetadataConfiguration]] with [[ResourceLoader]]s set
    */
  override def withResourceLoaders(rl: List[ResourceLoader]): LLMMetadataConfiguration =
    super._withResourceLoaders(rl)

  /** Set [[UnitCache]]
    * @param cache
    *   [[UnitCache]] to add to configuration object
    * @return
    *   [[LLMMetadataConfiguration]] with [[UnitCache]] added
    */
  override def withUnitCache(cache: UnitCache): LLMMetadataConfiguration =
    super._withUnitCache(cache)

  override def withFallback(plugin: DomainParsingFallback): LLMMetadataConfiguration = super._withFallback(plugin)

  override def withRootParsePlugin(amfParsePlugin: AMFParsePlugin): LLMMetadataConfiguration =
    super._withRootParsePlugin(amfParsePlugin)

  override def withPlugin(amfPlugin: AMFPlugin[_]): LLMMetadataConfiguration =
    super._withPlugin(amfPlugin)

  override def withReferenceParsePlugin(plugin: AMFParsePlugin): LLMMetadataConfiguration =
    super._withReferenceParsePlugin(plugin)

  override def withRootParsePlugins(amfParsePlugin: List[AMFParsePlugin]): LLMMetadataConfiguration =
    super._withRootParsePlugins(amfParsePlugin)

  override def withReferenceParsePlugins(amfPlugin: List[AMFParsePlugin]): LLMMetadataConfiguration =
    super._withReferenceParsePlugins(amfPlugin)

  override def withPlugins(plugins: List[AMFPlugin[_]]): LLMMetadataConfiguration =
    super._withPlugins(plugins)

  private[amf] override def withValidationProfile(profile: ValidationProfile): LLMMetadataConfiguration =
    super._withValidationProfile(profile)

  // Keep AMF internal, done to avoid recomputing validations every time a config is requested
  private[amf] override def withValidationProfile(
      profile: ValidationProfile,
      effective: EffectiveValidations
  ): LLMMetadataConfiguration =
    super._withValidationProfile(profile, effective)

  /** Add a [[TransformationPipeline]]
    * @param pipeline
    *   [[TransformationPipeline]] to add to configuration object
    * @return
    *   [[LLMMetadataConfiguration]] with [[TransformationPipeline]] added
    */
  override def withTransformationPipeline(pipeline: TransformationPipeline): LLMMetadataConfiguration =
    super._withTransformationPipeline(pipeline)

  /** AMF internal method just to facilitate the construction */
  override private[amf] def withTransformationPipelines(
      pipelines: List[TransformationPipeline]): LLMMetadataConfiguration =
    super._withTransformationPipelines(pipelines)

  /** Set [[ErrorHandlerProvider]]
    * @param provider
    *   [[ErrorHandlerProvider]] to set to configuration object
    * @return
    *   [[LLMMetadataConfiguration]] with [[ErrorHandlerProvider]] set
    */
  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): LLMMetadataConfiguration =
    super._withErrorHandlerProvider(provider)

  /** Add an [[AMFEventListener]]
    * @param listener
    *   [[AMFEventListener]] to add to configuration object
    * @return
    *   [[LLMMetadataConfiguration]] with [[AMFEventListener]] added
    */
  override def withEventListener(listener: AMFEventListener): LLMMetadataConfiguration =
    super._withEventListener(listener)

  private[amf] override def withEntities(entities: Map[String, ModelDefaultBuilder]): LLMMetadataConfiguration =
    super._withEntities(entities)

  override def withAliases(aliases: NamespaceAliases): LLMMetadataConfiguration =
    super._withAliases(aliases)

  private[amf] override def withExtensions(dialect: Dialect): LLMMetadataConfiguration = {
    super.withExtensions(dialect).asInstanceOf[LLMMetadataConfiguration]
  }

  private[amf] override def withAnnotations(annotations: Map[String, AnnotationGraphLoader]): LLMMetadataConfiguration =
    super._withAnnotations(annotations)

  /** Set [[BaseExecutionEnvironment]]
    * @param executionEnv
    *   [[BaseExecutionEnvironment]] to set to configuration object
    * @return
    *   [[LLMMetadataConfiguration]] with [[BaseExecutionEnvironment]] set
    */
  override def withExecutionEnvironment(executionEnv: ExecutionEnvironment): LLMMetadataConfiguration =
    super._withExecutionEnvironment(executionEnv)

  /** Register a Dialect
    * @param dialect
    *   [[Dialect]] to register
    * @return
    *   [[LLMMetadataConfiguration]] with [[Dialect]] registered
    */
  override def withDialect(dialect: Dialect): LLMMetadataConfiguration =
    super.withDialect(dialect).asInstanceOf[LLMMetadataConfiguration]

  /** Register a Dialect
    * @param url
    *   URL of the Dialect to register
    * @return
    *   A CompletableFuture of [[LLMMetadataConfiguration]]
    */
  override def withDialect(url: String): Future[LLMMetadataConfiguration] =
    super.withDialect(url).map(_.asInstanceOf[LLMMetadataConfiguration])(getExecutionContext)

  /** Register a [[Dialect]] linked from a [[DialectInstance]]
    * @param url
    *   of the [[DialectInstance]]
    * @return
    *   A CompletableFuture of [[LLMMetadataConfiguration]]
    */
  override def forInstance(url: String): Future[LLMMetadataConfiguration] =
    super.forInstance(url).map(_.asInstanceOf[LLMMetadataConfiguration])(getExecutionContext)

  override def withIdAdopterProvider(idAdopterProvider: IdAdopterProvider): LLMMetadataConfiguration =
    super._withIdAdopterProvider(idAdopterProvider)
}

object LLMMetadataConfiguration {

  def LLMMetadata(): LLMMetadataConfiguration =
    predefined()
      .withPlugins(
          List(
              LLMMetadataParsePlugin,
              LLMMetadataRenderPlugin,
              LLMMetadataValidationPlugin(),
              JsonSchemaBasedSpecGraphRenderPlugin,
              AMFJsonLDSchemaGraphParsePlugin
          )
      )
      .withTransformationPipelines(
          List(
              JsonSchemaBasedSpecTransformationPipeline(),
              JsonSchemaBasedSpecEditingPipeline(),
              JsonSchemaBasedSpecCachePipeline()
          )
      )

  private def predefined(): LLMMetadataConfiguration = {
    val baseConfig = JsonSchemaBasedSpecConfiguration.base()
    new LLMMetadataConfiguration(
        baseConfig.resolvers,
        baseConfig.errorHandlerProvider,
        baseConfig.registry,
        baseConfig.listeners,
        baseConfig.options,
        baseConfig.idAdopterProvider
    )
  }
}
