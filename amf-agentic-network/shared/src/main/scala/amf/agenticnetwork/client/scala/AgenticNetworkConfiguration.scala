package amf.agenticnetwork.client.scala

import amf.agenticnetwork.internal.plugins.parse.AgenticNetworkParsePlugin
import amf.agenticnetwork.internal.plugins.render.AgenticNetworkRenderPlugin
import amf.agenticnetwork.internal.plugins.validation.AgenticNetworkValidationPlugin
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
import amf.core.internal.plugins.parse.{DomainParsingFallback, ExternalFragmentDomainFallback}
import amf.core.internal.registries.AMFRegistry
import amf.core.internal.resource.AMFResolvers
import amf.core.internal.validation.EffectiveValidations
import amf.core.internal.validation.core.ValidationProfile
import amf.shapes.client.scala.JsonSchemaBasedSpecConfiguration
import amf.shapes.internal.plugins.parser.JsonSchemaBasedSpecGraphParsePlugin
import amf.shapes.internal.plugins.render.JsonSchemaBasedSpecGraphRenderPlugin
import amf.shapes.internal.transformation.{
  JsonSchemaBasedSpecCachePipeline,
  JsonSchemaBasedSpecEditingPipeline,
  JsonSchemaBasedSpecTransformationPipeline
}

import scala.concurrent.{ExecutionContext, Future}

class AgenticNetworkConfiguration private[amf] (
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
  ): AgenticNetworkConfiguration =
    new AgenticNetworkConfiguration(
        resolvers,
        errorHandlerProvider,
        registry.asInstanceOf[AMLRegistry],
        listeners,
        options,
        idAdopterProvider
    )

  override def baseUnitClient(): AgenticNetworkBaseUnitClient = new AgenticNetworkBaseUnitClient(this)

  // Needed to override all these method in order to return the specific AgenticNetworkConfiguration and not the parent one

  /** Set [[ParsingOptions]]
    * @param parsingOptions
    *   [[ParsingOptions]] to add to configuration object
    * @return
    *   [[AgenticNetworkConfiguration]] with [[ParsingOptions]] added
    */
  override def withParsingOptions(parsingOptions: ParsingOptions): AgenticNetworkConfiguration =
    super._withParsingOptions(parsingOptions)

  /** Set [[RenderOptions]]
    * @param renderOptions
    *   [[RenderOptions]] to add to configuration object
    * @return
    *   [[AgenticNetworkConfiguration]] with [[ParsingOptions]] added
    */
  override def withRenderOptions(renderOptions: RenderOptions): AgenticNetworkConfiguration =
    super._withRenderOptions(renderOptions)

  /** Add a [[ResourceLoader]]
    * @param rl
    *   [[ResourceLoader]] to add to configuration object
    * @return
    *   [[AgenticNetworkConfiguration]] with the [[ResourceLoader]] added
    */
  override def withResourceLoader(rl: ResourceLoader): AgenticNetworkConfiguration =
    super._withResourceLoader(rl)

  /** Set the configuration [[ResourceLoader]]s
    * @param rl
    *   a list of [[ResourceLoader]] to set to the configuration object
    * @return
    *   [[AgenticNetworkConfiguration]] with [[ResourceLoader]]s set
    */
  override def withResourceLoaders(rl: List[ResourceLoader]): AgenticNetworkConfiguration =
    super._withResourceLoaders(rl)

  /** Set [[UnitCache]]
    * @param cache
    *   [[UnitCache]] to add to configuration object
    * @return
    *   [[AgenticNetworkConfiguration]] with [[UnitCache]] added
    */
  override def withUnitCache(cache: UnitCache): AgenticNetworkConfiguration =
    super._withUnitCache(cache)

  override def withFallback(plugin: DomainParsingFallback): AgenticNetworkConfiguration = super._withFallback(plugin)

  override def withRootParsePlugin(amfParsePlugin: AMFParsePlugin): AgenticNetworkConfiguration =
    super._withRootParsePlugin(amfParsePlugin)

  override def withPlugin(amfPlugin: AMFPlugin[_]): AgenticNetworkConfiguration =
    super._withPlugin(amfPlugin)

  override def withReferenceParsePlugin(plugin: AMFParsePlugin): AgenticNetworkConfiguration =
    super._withReferenceParsePlugin(plugin)

  override def withRootParsePlugins(amfParsePlugin: List[AMFParsePlugin]): AgenticNetworkConfiguration =
    super._withRootParsePlugins(amfParsePlugin)

  override def withReferenceParsePlugins(amfPlugin: List[AMFParsePlugin]): AgenticNetworkConfiguration =
    super._withReferenceParsePlugins(amfPlugin)

  override def withPlugins(plugins: List[AMFPlugin[_]]): AgenticNetworkConfiguration =
    super._withPlugins(plugins)

  private[amf] override def withValidationProfile(profile: ValidationProfile): AgenticNetworkConfiguration =
    super._withValidationProfile(profile)

  // Keep AMF internal, done to avoid recomputing validations every time a config is requested
  private[amf] override def withValidationProfile(
      profile: ValidationProfile,
      effective: EffectiveValidations
  ): AgenticNetworkConfiguration =
    super._withValidationProfile(profile, effective)

  /** Add a [[TransformationPipeline]]
    * @param pipeline
    *   [[TransformationPipeline]] to add to configuration object
    * @return
    *   [[AgenticNetworkConfiguration]] with [[TransformationPipeline]] added
    */
  override def withTransformationPipeline(pipeline: TransformationPipeline): AgenticNetworkConfiguration =
    super._withTransformationPipeline(pipeline)

  /** AMF internal method just to facilitate the construction */
  override private[amf] def withTransformationPipelines(
      pipelines: List[TransformationPipeline]): AgenticNetworkConfiguration =
    super._withTransformationPipelines(pipelines)

  /** Set [[ErrorHandlerProvider]]
    * @param provider
    *   [[ErrorHandlerProvider]] to set to configuration object
    * @return
    *   [[AgenticNetworkConfiguration]] with [[ErrorHandlerProvider]] set
    */
  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): AgenticNetworkConfiguration =
    super._withErrorHandlerProvider(provider)

  /** Add an [[AMFEventListener]]
    * @param listener
    *   [[AMFEventListener]] to add to configuration object
    * @return
    *   [[AgenticNetworkConfiguration]] with [[AMFEventListener]] added
    */
  override def withEventListener(listener: AMFEventListener): AgenticNetworkConfiguration =
    super._withEventListener(listener)

  private[amf] override def withEntities(entities: Map[String, ModelDefaultBuilder]): AgenticNetworkConfiguration =
    super._withEntities(entities)

  override def withAliases(aliases: NamespaceAliases): AgenticNetworkConfiguration =
    super._withAliases(aliases)

  private[amf] override def withExtensions(dialect: Dialect): AgenticNetworkConfiguration = {
    super.withExtensions(dialect).asInstanceOf[AgenticNetworkConfiguration]
  }

  private[amf] override def withAnnotations(annotations: Map[String, AnnotationGraphLoader]): AgenticNetworkConfiguration =
    super._withAnnotations(annotations)

  /** Set [[BaseExecutionEnvironment]]
    * @param executionEnv
    *   [[BaseExecutionEnvironment]] to set to configuration object
    * @return
    *   [[AgenticNetworkConfiguration]] with [[BaseExecutionEnvironment]] set
    */
  override def withExecutionEnvironment(executionEnv: ExecutionEnvironment): AgenticNetworkConfiguration =
    super._withExecutionEnvironment(executionEnv)

  /** Register a Dialect
    * @param dialect
    *   [[Dialect]] to register
    * @return
    *   [[AgenticNetworkConfiguration]] with [[Dialect]] registered
    */
  override def withDialect(dialect: Dialect): AgenticNetworkConfiguration =
    super.withDialect(dialect).asInstanceOf[AgenticNetworkConfiguration]

  /** Register a Dialect
    * @param url
    *   URL of the Dialect to register
    * @return
    *   A CompletableFuture of [[AgenticNetworkConfiguration]]
    */
  override def withDialect(url: String): Future[AgenticNetworkConfiguration] =
    super.withDialect(url).map(_.asInstanceOf[AgenticNetworkConfiguration])(getExecutionContext)

  /** Register a [[Dialect]] linked from a [[DialectInstance]]
    * @param url
    *   of the [[DialectInstance]]
    * @return
    *   A CompletableFuture of [[AgenticNetworkConfiguration]]
    */
  override def forInstance(url: String): Future[AgenticNetworkConfiguration] =
    super.forInstance(url).map(_.asInstanceOf[AgenticNetworkConfiguration])(getExecutionContext)

  override def withIdAdopterProvider(idAdopterProvider: IdAdopterProvider): AgenticNetworkConfiguration =
    super._withIdAdopterProvider(idAdopterProvider)
}

object AgenticNetworkConfiguration {

  def AgenticNetwork(): AgenticNetworkConfiguration =
    predefined()
      .withPlugins(
          List(
              AgenticNetworkParsePlugin,
              AgenticNetworkRenderPlugin,
              AgenticNetworkValidationPlugin(),
              JsonSchemaBasedSpecGraphRenderPlugin,
              JsonSchemaBasedSpecGraphParsePlugin
          )
      )
      .withTransformationPipelines(
          List(
              JsonSchemaBasedSpecTransformationPipeline(),
              JsonSchemaBasedSpecEditingPipeline(),
              JsonSchemaBasedSpecCachePipeline()
          )
      )

  private def predefined(): AgenticNetworkConfiguration = {
    val baseConfig = JsonSchemaBasedSpecConfiguration.base()
    new AgenticNetworkConfiguration(
        baseConfig.resolvers,
        baseConfig.errorHandlerProvider,
        baseConfig.registry,
        baseConfig.listeners,
        baseConfig.options,
        baseConfig.idAdopterProvider
    )
  }
}
