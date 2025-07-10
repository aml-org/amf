package amf.agentfabric.client.scala

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
import amf.agentfabric.internal.plugins.parse.AgentFabricParsePlugin
import amf.agentfabric.internal.plugins.render.AgentFabricRenderPlugin
import amf.agentfabric.internal.plugins.validation.AgentFabricValidationPlugin
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

class AgentFabricConfiguration private[amf] (
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
  ): AgentFabricConfiguration =
    new AgentFabricConfiguration(
      resolvers,
      errorHandlerProvider,
      registry.asInstanceOf[AMLRegistry],
      listeners,
      options,
      idAdopterProvider
    )

  override def baseUnitClient(): AgentFabricBaseUnitClient = new AgentFabricBaseUnitClient(this)

  // Needed to override all these method in order to return the specific AgentFabricConfiguration and not the parent one

  /** Set [[ParsingOptions]]
    * @param parsingOptions
    *   [[ParsingOptions]] to add to configuration object
    * @return
    *   [[AgentFabricConfiguration]] with [[ParsingOptions]] added
    */
  override def withParsingOptions(parsingOptions: ParsingOptions): AgentFabricConfiguration =
    super._withParsingOptions(parsingOptions)

  /** Set [[RenderOptions]]
    * @param renderOptions
    *   [[RenderOptions]] to add to configuration object
    * @return
    *   [[AgentFabricConfiguration]] with [[ParsingOptions]] added
    */
  override def withRenderOptions(renderOptions: RenderOptions): AgentFabricConfiguration =
    super._withRenderOptions(renderOptions)

  /** Add a [[ResourceLoader]]
    * @param rl
    *   [[ResourceLoader]] to add to configuration object
    * @return
    *   [[AgentFabricConfiguration]] with the [[ResourceLoader]] added
    */
  override def withResourceLoader(rl: ResourceLoader): AgentFabricConfiguration =
    super._withResourceLoader(rl)

  /** Set the configuration [[ResourceLoader]]s
    * @param rl
    *   a list of [[ResourceLoader]] to set to the configuration object
    * @return
    *   [[AgentFabricConfiguration]] with [[ResourceLoader]]s set
    */
  override def withResourceLoaders(rl: List[ResourceLoader]): AgentFabricConfiguration =
    super._withResourceLoaders(rl)

  /** Set [[UnitCache]]
    * @param cache
    *   [[UnitCache]] to add to configuration object
    * @return
    *   [[AgentFabricConfiguration]] with [[UnitCache]] added
    */
  override def withUnitCache(cache: UnitCache): AgentFabricConfiguration =
    super._withUnitCache(cache)

  override def withFallback(plugin: DomainParsingFallback): AgentFabricConfiguration = super._withFallback(plugin)

  override def withRootParsePlugin(amfParsePlugin: AMFParsePlugin): AgentFabricConfiguration =
    super._withRootParsePlugin(amfParsePlugin)

  override def withPlugin(amfPlugin: AMFPlugin[_]): AgentFabricConfiguration =
    super._withPlugin(amfPlugin)

  override def withReferenceParsePlugin(plugin: AMFParsePlugin): AgentFabricConfiguration =
    super._withReferenceParsePlugin(plugin)

  override def withRootParsePlugins(amfParsePlugin: List[AMFParsePlugin]): AgentFabricConfiguration =
    super._withRootParsePlugins(amfParsePlugin)

  override def withReferenceParsePlugins(amfPlugin: List[AMFParsePlugin]): AgentFabricConfiguration =
    super._withReferenceParsePlugins(amfPlugin)

  override def withPlugins(plugins: List[AMFPlugin[_]]): AgentFabricConfiguration =
    super._withPlugins(plugins)

  private[amf] override def withValidationProfile(profile: ValidationProfile): AgentFabricConfiguration =
    super._withValidationProfile(profile)

  // Keep AMF internal, done to avoid recomputing validations every time a config is requested
  private[amf] override def withValidationProfile(
      profile: ValidationProfile,
      effective: EffectiveValidations
  ): AgentFabricConfiguration =
    super._withValidationProfile(profile, effective)

  /** Add a [[TransformationPipeline]]
    * @param pipeline
    *   [[TransformationPipeline]] to add to configuration object
    * @return
    *   [[AgentFabricConfiguration]] with [[TransformationPipeline]] added
    */
  override def withTransformationPipeline(pipeline: TransformationPipeline): AgentFabricConfiguration =
    super._withTransformationPipeline(pipeline)

  /** AMF internal method just to facilitate the construction */
  override private[amf] def withTransformationPipelines(pipelines: List[TransformationPipeline]): AgentFabricConfiguration =
    super._withTransformationPipelines(pipelines)

  /** Set [[ErrorHandlerProvider]]
    * @param provider
    *   [[ErrorHandlerProvider]] to set to configuration object
    * @return
    *   [[AgentFabricConfiguration]] with [[ErrorHandlerProvider]] set
    */
  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): AgentFabricConfiguration =
    super._withErrorHandlerProvider(provider)

  /** Add an [[AMFEventListener]]
    * @param listener
    *   [[AMFEventListener]] to add to configuration object
    * @return
    *   [[AgentFabricConfiguration]] with [[AMFEventListener]] added
    */
  override def withEventListener(listener: AMFEventListener): AgentFabricConfiguration = super._withEventListener(listener)

  private[amf] override def withEntities(entities: Map[String, ModelDefaultBuilder]): AgentFabricConfiguration =
    super._withEntities(entities)

  override def withAliases(aliases: NamespaceAliases): AgentFabricConfiguration =
    super._withAliases(aliases)

  private[amf] override def withExtensions(dialect: Dialect): AgentFabricConfiguration = {
    super.withExtensions(dialect).asInstanceOf[AgentFabricConfiguration]
  }

  private[amf] override def withAnnotations(annotations: Map[String, AnnotationGraphLoader]): AgentFabricConfiguration =
    super._withAnnotations(annotations)

  /** Set [[BaseExecutionEnvironment]]
    * @param executionEnv
    *   [[BaseExecutionEnvironment]] to set to configuration object
    * @return
    *   [[AgentFabricConfiguration]] with [[BaseExecutionEnvironment]] set
    */
  override def withExecutionEnvironment(executionEnv: ExecutionEnvironment): AgentFabricConfiguration =
    super._withExecutionEnvironment(executionEnv)

  /** Register a Dialect
    * @param dialect
    *   [[Dialect]] to register
    * @return
    *   [[AgentFabricConfiguration]] with [[Dialect]] registered
    */
  override def withDialect(dialect: Dialect): AgentFabricConfiguration =
    super.withDialect(dialect).asInstanceOf[AgentFabricConfiguration]

  /** Register a Dialect
    * @param url
    *   URL of the Dialect to register
    * @return
    *   A CompletableFuture of [[AgentFabricConfiguration]]
    */
  override def withDialect(url: String): Future[AgentFabricConfiguration] =
    super.withDialect(url).map(_.asInstanceOf[AgentFabricConfiguration])(getExecutionContext)

  /** Register a [[Dialect]] linked from a [[DialectInstance]]
    * @param url
    *   of the [[DialectInstance]]
    * @return
    *   A CompletableFuture of [[AgentFabricConfiguration]]
    */
  override def forInstance(url: String): Future[AgentFabricConfiguration] =
    super.forInstance(url).map(_.asInstanceOf[AgentFabricConfiguration])(getExecutionContext)

  override def withIdAdopterProvider(idAdopterProvider: IdAdopterProvider): AgentFabricConfiguration =
    super._withIdAdopterProvider(idAdopterProvider)
}

object AgentFabricConfiguration {

  def AgentFabric(): AgentFabricConfiguration =
    predefined()
      .withPlugins(
        List(
          AgentFabricParsePlugin,
          AgentFabricRenderPlugin,
          AgentFabricValidationPlugin(),
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

  private def predefined(): AgentFabricConfiguration = {
    val baseConfig = JsonSchemaBasedSpecConfiguration.base()
    new AgentFabricConfiguration(
      baseConfig.resolvers,
      baseConfig.errorHandlerProvider,
      baseConfig.registry,
      baseConfig.listeners,
      baseConfig.options,
      baseConfig.idAdopterProvider
    )
  }
}
