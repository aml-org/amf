package amf.agentgraph.client.scala

import amf.agentgraph.internal.plugins.parse.AgentGraphParsePlugin
import amf.agentgraph.internal.plugins.render.AgentGraphRenderPlugin
import amf.agentgraph.internal.plugins.validation.AgentGraphValidationPlugin
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
import amf.shapes.client.scala.JsonSchemaBasedSpecConfiguration
import amf.shapes.internal.plugins.parser.JsonSchemaBasedSpecGraphParsePlugin
import amf.shapes.internal.plugins.render.JsonSchemaBasedSpecGraphRenderPlugin
import amf.shapes.internal.transformation.{
  JsonSchemaBasedSpecCachePipeline,
  JsonSchemaBasedSpecEditingPipeline,
  JsonSchemaBasedSpecTransformationPipeline
}

import scala.concurrent.{ExecutionContext, Future}

class AgentGraphConfiguration private[amf] (
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
  ): AgentGraphConfiguration =
    new AgentGraphConfiguration(
        resolvers,
        errorHandlerProvider,
        registry.asInstanceOf[AMLRegistry],
        listeners,
        options,
        idAdopterProvider
    )

  override def baseUnitClient(): AgentGraphBaseUnitClient = new AgentGraphBaseUnitClient(this)

  // Needed to override all these method in order to return the specific AgentGraphConfiguration and not the parent one

  /** Set [[ParsingOptions]]
    * @param parsingOptions
    *   [[ParsingOptions]] to add to configuration object
    * @return
    *   [[AgentGraphConfiguration]] with [[ParsingOptions]] added
    */
  override def withParsingOptions(parsingOptions: ParsingOptions): AgentGraphConfiguration =
    super._withParsingOptions(parsingOptions)

  /** Set [[RenderOptions]]
    * @param renderOptions
    *   [[RenderOptions]] to add to configuration object
    * @return
    *   [[AgentGraphConfiguration]] with [[ParsingOptions]] added
    */
  override def withRenderOptions(renderOptions: RenderOptions): AgentGraphConfiguration =
    super._withRenderOptions(renderOptions)

  /** Add a [[ResourceLoader]]
    * @param rl
    *   [[ResourceLoader]] to add to configuration object
    * @return
    *   [[AgentGraphConfiguration]] with the [[ResourceLoader]] added
    */
  override def withResourceLoader(rl: ResourceLoader): AgentGraphConfiguration =
    super._withResourceLoader(rl)

  /** Set the configuration [[ResourceLoader]]s
    * @param rl
    *   a list of [[ResourceLoader]] to set to the configuration object
    * @return
    *   [[AgentGraphConfiguration]] with [[ResourceLoader]]s set
    */
  override def withResourceLoaders(rl: List[ResourceLoader]): AgentGraphConfiguration =
    super._withResourceLoaders(rl)

  /** Set [[UnitCache]]
    * @param cache
    *   [[UnitCache]] to add to configuration object
    * @return
    *   [[AgentGraphConfiguration]] with [[UnitCache]] added
    */
  override def withUnitCache(cache: UnitCache): AgentGraphConfiguration =
    super._withUnitCache(cache)

  override def withFallback(plugin: DomainParsingFallback): AgentGraphConfiguration = super._withFallback(plugin)

  override def withRootParsePlugin(amfParsePlugin: AMFParsePlugin): AgentGraphConfiguration =
    super._withRootParsePlugin(amfParsePlugin)

  override def withPlugin(amfPlugin: AMFPlugin[_]): AgentGraphConfiguration =
    super._withPlugin(amfPlugin)

  override def withReferenceParsePlugin(plugin: AMFParsePlugin): AgentGraphConfiguration =
    super._withReferenceParsePlugin(plugin)

  override def withRootParsePlugins(amfParsePlugin: List[AMFParsePlugin]): AgentGraphConfiguration =
    super._withRootParsePlugins(amfParsePlugin)

  override def withReferenceParsePlugins(amfPlugin: List[AMFParsePlugin]): AgentGraphConfiguration =
    super._withReferenceParsePlugins(amfPlugin)

  override def withPlugins(plugins: List[AMFPlugin[_]]): AgentGraphConfiguration =
    super._withPlugins(plugins)

  private[amf] override def withValidationProfile(profile: ValidationProfile): AgentGraphConfiguration =
    super._withValidationProfile(profile)

  // Keep AMF internal, done to avoid recomputing validations every time a config is requested
  private[amf] override def withValidationProfile(
      profile: ValidationProfile,
      effective: EffectiveValidations
  ): AgentGraphConfiguration =
    super._withValidationProfile(profile, effective)

  /** Add a [[TransformationPipeline]]
    * @param pipeline
    *   [[TransformationPipeline]] to add to configuration object
    * @return
    *   [[AgentGraphConfiguration]] with [[TransformationPipeline]] added
    */
  override def withTransformationPipeline(pipeline: TransformationPipeline): AgentGraphConfiguration =
    super._withTransformationPipeline(pipeline)

  /** AMF internal method just to facilitate the construction */
  override private[amf] def withTransformationPipelines(
      pipelines: List[TransformationPipeline]): AgentGraphConfiguration =
    super._withTransformationPipelines(pipelines)

  /** Set [[ErrorHandlerProvider]]
    * @param provider
    *   [[ErrorHandlerProvider]] to set to configuration object
    * @return
    *   [[AgentGraphConfiguration]] with [[ErrorHandlerProvider]] set
    */
  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): AgentGraphConfiguration =
    super._withErrorHandlerProvider(provider)

  /** Add an [[AMFEventListener]]
    * @param listener
    *   [[AMFEventListener]] to add to configuration object
    * @return
    *   [[AgentGraphConfiguration]] with [[AMFEventListener]] added
    */
  override def withEventListener(listener: AMFEventListener): AgentGraphConfiguration =
    super._withEventListener(listener)

  private[amf] override def withEntities(entities: Map[String, ModelDefaultBuilder]): AgentGraphConfiguration =
    super._withEntities(entities)

  override def withAliases(aliases: NamespaceAliases): AgentGraphConfiguration =
    super._withAliases(aliases)

  private[amf] override def withExtensions(dialect: Dialect): AgentGraphConfiguration = {
    super.withExtensions(dialect).asInstanceOf[AgentGraphConfiguration]
  }

  private[amf] override def withAnnotations(annotations: Map[String, AnnotationGraphLoader]): AgentGraphConfiguration =
    super._withAnnotations(annotations)

  /** Set [[BaseExecutionEnvironment]]
    * @param executionEnv
    *   [[BaseExecutionEnvironment]] to set to configuration object
    * @return
    *   [[AgentGraphConfiguration]] with [[BaseExecutionEnvironment]] set
    */
  override def withExecutionEnvironment(executionEnv: ExecutionEnvironment): AgentGraphConfiguration =
    super._withExecutionEnvironment(executionEnv)

  /** Register a Dialect
    * @param dialect
    *   [[Dialect]] to register
    * @return
    *   [[AgentGraphConfiguration]] with [[Dialect]] registered
    */
  override def withDialect(dialect: Dialect): AgentGraphConfiguration =
    super.withDialect(dialect).asInstanceOf[AgentGraphConfiguration]

  /** Register a Dialect
    * @param url
    *   URL of the Dialect to register
    * @return
    *   A CompletableFuture of [[AgentGraphConfiguration]]
    */
  override def withDialect(url: String): Future[AgentGraphConfiguration] =
    super.withDialect(url).map(_.asInstanceOf[AgentGraphConfiguration])(getExecutionContext)

  /** Register a [[Dialect]] linked from a [[DialectInstance]]
    * @param url
    *   of the [[DialectInstance]]
    * @return
    *   A CompletableFuture of [[AgentGraphConfiguration]]
    */
  override def forInstance(url: String): Future[AgentGraphConfiguration] =
    super.forInstance(url).map(_.asInstanceOf[AgentGraphConfiguration])(getExecutionContext)

  override def withIdAdopterProvider(idAdopterProvider: IdAdopterProvider): AgentGraphConfiguration =
    super._withIdAdopterProvider(idAdopterProvider)
}

object AgentGraphConfiguration {

  def AgentGraph(): AgentGraphConfiguration =
    predefined()
      .withPlugins(
          List(
              AgentGraphParsePlugin,
              AgentGraphRenderPlugin,
              AgentGraphValidationPlugin(),
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

  private def predefined(): AgentGraphConfiguration = {
    val baseConfig = JsonSchemaBasedSpecConfiguration.base()
    new AgentGraphConfiguration(
        baseConfig.resolvers,
        baseConfig.errorHandlerProvider,
        baseConfig.registry,
        baseConfig.listeners,
        baseConfig.options,
        baseConfig.idAdopterProvider
    )
  }
}
