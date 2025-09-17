package amf.agentnetwork.client.scala

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
import amf.agentnetwork.internal.plugins.parse.AgentNetworkParsePlugin
import amf.agentnetwork.internal.plugins.render.AgentNetworkRenderPlugin
import amf.agentnetwork.internal.plugins.validation.AgentNetworkValidationPlugin
import amf.shapes.client.scala.{JsonSchemaBasedSpecConfiguration, ShapesConfiguration}
import amf.shapes.internal.convert.ShapesRegister
import amf.shapes.internal.plugins.parser.AMFJsonLDSchemaGraphParsePlugin
import amf.shapes.internal.plugins.render.AMFJsonLDSchemaGraphRenderPlugin
import amf.shapes.internal.transformation.{
  JsonSchemaBasedSpecCachePipeline,
  JsonSchemaBasedSpecEditingPipeline,
  JsonSchemaBasedSpecTransformationPipeline
}

import scala.concurrent.{ExecutionContext, Future}

class AgentNetworkConfiguration private[amf] (
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
  ): AgentNetworkConfiguration =
    new AgentNetworkConfiguration(
      resolvers,
      errorHandlerProvider,
      registry.asInstanceOf[AMLRegistry],
      listeners,
      options,
      idAdopterProvider
    )

  override def baseUnitClient(): AgentNetworkBaseUnitClient = new AgentNetworkBaseUnitClient(this)

  // Needed to override all these method in order to return the specific AgentNetworkConfiguration and not the parent one

  /** Set [[ParsingOptions]]
    * @param parsingOptions
    *   [[ParsingOptions]] to add to configuration object
    * @return
    *   [[AgentNetworkConfiguration]] with [[ParsingOptions]] added
    */
  override def withParsingOptions(parsingOptions: ParsingOptions): AgentNetworkConfiguration =
    super._withParsingOptions(parsingOptions)

  /** Set [[RenderOptions]]
    * @param renderOptions
    *   [[RenderOptions]] to add to configuration object
    * @return
    *   [[AgentNetworkConfiguration]] with [[ParsingOptions]] added
    */
  override def withRenderOptions(renderOptions: RenderOptions): AgentNetworkConfiguration =
    super._withRenderOptions(renderOptions)

  /** Add a [[ResourceLoader]]
    * @param rl
    *   [[ResourceLoader]] to add to configuration object
    * @return
    *   [[AgentNetworkConfiguration]] with the [[ResourceLoader]] added
    */
  override def withResourceLoader(rl: ResourceLoader): AgentNetworkConfiguration =
    super._withResourceLoader(rl)

  /** Set the configuration [[ResourceLoader]]s
    * @param rl
    *   a list of [[ResourceLoader]] to set to the configuration object
    * @return
    *   [[AgentNetworkConfiguration]] with [[ResourceLoader]]s set
    */
  override def withResourceLoaders(rl: List[ResourceLoader]): AgentNetworkConfiguration =
    super._withResourceLoaders(rl)

  /** Set [[UnitCache]]
    * @param cache
    *   [[UnitCache]] to add to configuration object
    * @return
    *   [[AgentNetworkConfiguration]] with [[UnitCache]] added
    */
  override def withUnitCache(cache: UnitCache): AgentNetworkConfiguration =
    super._withUnitCache(cache)

  override def withFallback(plugin: DomainParsingFallback): AgentNetworkConfiguration = super._withFallback(plugin)

  override def withRootParsePlugin(amfParsePlugin: AMFParsePlugin): AgentNetworkConfiguration =
    super._withRootParsePlugin(amfParsePlugin)

  override def withPlugin(amfPlugin: AMFPlugin[_]): AgentNetworkConfiguration =
    super._withPlugin(amfPlugin)

  override def withReferenceParsePlugin(plugin: AMFParsePlugin): AgentNetworkConfiguration =
    super._withReferenceParsePlugin(plugin)

  override def withRootParsePlugins(amfParsePlugin: List[AMFParsePlugin]): AgentNetworkConfiguration =
    super._withRootParsePlugins(amfParsePlugin)

  override def withReferenceParsePlugins(amfPlugin: List[AMFParsePlugin]): AgentNetworkConfiguration =
    super._withReferenceParsePlugins(amfPlugin)

  override def withPlugins(plugins: List[AMFPlugin[_]]): AgentNetworkConfiguration =
    super._withPlugins(plugins)

  private[amf] override def withValidationProfile(profile: ValidationProfile): AgentNetworkConfiguration =
    super._withValidationProfile(profile)

  // Keep AMF internal, done to avoid recomputing validations every time a config is requested
  private[amf] override def withValidationProfile(
      profile: ValidationProfile,
      effective: EffectiveValidations
  ): AgentNetworkConfiguration =
    super._withValidationProfile(profile, effective)

  /** Add a [[TransformationPipeline]]
    * @param pipeline
    *   [[TransformationPipeline]] to add to configuration object
    * @return
    *   [[AgentNetworkConfiguration]] with [[TransformationPipeline]] added
    */
  override def withTransformationPipeline(pipeline: TransformationPipeline): AgentNetworkConfiguration =
    super._withTransformationPipeline(pipeline)

  /** AMF internal method just to facilitate the construction */
  override private[amf] def withTransformationPipelines(pipelines: List[TransformationPipeline]): AgentNetworkConfiguration =
    super._withTransformationPipelines(pipelines)

  /** Set [[ErrorHandlerProvider]]
    * @param provider
    *   [[ErrorHandlerProvider]] to set to configuration object
    * @return
    *   [[AgentNetworkConfiguration]] with [[ErrorHandlerProvider]] set
    */
  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): AgentNetworkConfiguration =
    super._withErrorHandlerProvider(provider)

  /** Add an [[AMFEventListener]]
    * @param listener
    *   [[AMFEventListener]] to add to configuration object
    * @return
    *   [[AgentNetworkConfiguration]] with [[AMFEventListener]] added
    */
  override def withEventListener(listener: AMFEventListener): AgentNetworkConfiguration = super._withEventListener(listener)

  private[amf] override def withEntities(entities: Map[String, ModelDefaultBuilder]): AgentNetworkConfiguration =
    super._withEntities(entities)

  override def withAliases(aliases: NamespaceAliases): AgentNetworkConfiguration =
    super._withAliases(aliases)

  private[amf] override def withExtensions(dialect: Dialect): AgentNetworkConfiguration = {
    super.withExtensions(dialect).asInstanceOf[AgentNetworkConfiguration]
  }

  private[amf] override def withAnnotations(annotations: Map[String, AnnotationGraphLoader]): AgentNetworkConfiguration =
    super._withAnnotations(annotations)

  /** Set [[BaseExecutionEnvironment]]
    * @param executionEnv
    *   [[BaseExecutionEnvironment]] to set to configuration object
    * @return
    *   [[AgentNetworkConfiguration]] with [[BaseExecutionEnvironment]] set
    */
  override def withExecutionEnvironment(executionEnv: ExecutionEnvironment): AgentNetworkConfiguration =
    super._withExecutionEnvironment(executionEnv)

  /** Register a Dialect
    * @param dialect
    *   [[Dialect]] to register
    * @return
    *   [[AgentNetworkConfiguration]] with [[Dialect]] registered
    */
  override def withDialect(dialect: Dialect): AgentNetworkConfiguration =
    super.withDialect(dialect).asInstanceOf[AgentNetworkConfiguration]

  /** Register a Dialect
    * @param url
    *   URL of the Dialect to register
    * @return
    *   A CompletableFuture of [[AgentNetworkConfiguration]]
    */
  override def withDialect(url: String): Future[AgentNetworkConfiguration] =
    super.withDialect(url).map(_.asInstanceOf[AgentNetworkConfiguration])(getExecutionContext)

  /** Register a [[Dialect]] linked from a [[DialectInstance]]
    * @param url
    *   of the [[DialectInstance]]
    * @return
    *   A CompletableFuture of [[AgentNetworkConfiguration]]
    */
  override def forInstance(url: String): Future[AgentNetworkConfiguration] =
    super.forInstance(url).map(_.asInstanceOf[AgentNetworkConfiguration])(getExecutionContext)

  override def withIdAdopterProvider(idAdopterProvider: IdAdopterProvider): AgentNetworkConfiguration =
    super._withIdAdopterProvider(idAdopterProvider)
}

object AgentNetworkConfiguration {

  def AgentNetwork(): AgentNetworkConfiguration =
    predefined()
      .withPlugins(
        List(
          AgentNetworkParsePlugin,
          AgentNetworkRenderPlugin,
          AgentNetworkValidationPlugin(),
          AMFJsonLDSchemaGraphRenderPlugin,
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

  private def predefined(): AgentNetworkConfiguration = {
    val baseConfig = JsonSchemaBasedSpecConfiguration.base()
    new AgentNetworkConfiguration(
      baseConfig.resolvers,
      baseConfig.errorHandlerProvider,
      baseConfig.registry,
      baseConfig.listeners,
      baseConfig.options,
      baseConfig.idAdopterProvider
    )
  }
}
