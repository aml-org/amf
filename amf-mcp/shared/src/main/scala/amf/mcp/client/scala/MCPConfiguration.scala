package amf.mcp.client.scala

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
import amf.mcp.internal.plugins.parse.MCPParsePlugin
import amf.mcp.internal.plugins.validation.MCPValidationPlugin
import amf.mcp.internal.transformation.{MCPCachePipeline, MCPEditingPipeline, MCPTransformationPipeline}
import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.internal.convert.ShapesRegister
import amf.shapes.internal.plugins.parser.AMFJsonLDSchemaGraphParsePlugin
import amf.shapes.internal.plugins.render.AMFJsonLDSchemaGraphRenderPlugin

import scala.concurrent.{ExecutionContext, Future}

class MCPConfiguration private[amf] (
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
  ): MCPConfiguration =
    new MCPConfiguration(
      resolvers,
      errorHandlerProvider,
      registry.asInstanceOf[AMLRegistry],
      listeners,
      options,
      idAdopterProvider
    )

  override def baseUnitClient(): MCPBaseUnitClient = new MCPBaseUnitClient(this)

  // Needed to override all these method in order to return the specific MCPConfiguration and not the parent one

  /** Set [[ParsingOptions]]
    * @param parsingOptions
    *   [[ParsingOptions]] to add to configuration object
    * @return
    *   [[MCPConfiguration]] with [[ParsingOptions]] added
    */
  override def withParsingOptions(parsingOptions: ParsingOptions): MCPConfiguration =
    super._withParsingOptions(parsingOptions)

  /** Set [[RenderOptions]]
    * @param renderOptions
    *   [[RenderOptions]] to add to configuration object
    * @return
    *   [[MCPConfiguration]] with [[ParsingOptions]] added
    */
  override def withRenderOptions(renderOptions: RenderOptions): MCPConfiguration =
    super._withRenderOptions(renderOptions)

  /** Add a [[ResourceLoader]]
    * @param rl
    *   [[ResourceLoader]] to add to configuration object
    * @return
    *   [[MCPConfiguration]] with the [[ResourceLoader]] added
    */
  override def withResourceLoader(rl: ResourceLoader): MCPConfiguration =
    super._withResourceLoader(rl)

  /** Set the configuration [[ResourceLoader]]s
    * @param rl
    *   a list of [[ResourceLoader]] to set to the configuration object
    * @return
    *   [[MCPConfiguration]] with [[ResourceLoader]]s set
    */
  override def withResourceLoaders(rl: List[ResourceLoader]): MCPConfiguration =
    super._withResourceLoaders(rl)

  /** Set [[UnitCache]]
    * @param cache
    *   [[UnitCache]] to add to configuration object
    * @return
    *   [[MCPConfiguration]] with [[UnitCache]] added
    */
  override def withUnitCache(cache: UnitCache): MCPConfiguration =
    super._withUnitCache(cache)

  override def withFallback(plugin: DomainParsingFallback): MCPConfiguration = super._withFallback(plugin)

  override def withRootParsePlugin(amfParsePlugin: AMFParsePlugin): MCPConfiguration =
    super._withRootParsePlugin(amfParsePlugin)

  override def withPlugin(amfPlugin: AMFPlugin[_]): MCPConfiguration =
    super._withPlugin(amfPlugin)

  override def withReferenceParsePlugin(plugin: AMFParsePlugin): MCPConfiguration =
    super._withReferenceParsePlugin(plugin)

  override def withRootParsePlugins(amfParsePlugin: List[AMFParsePlugin]): MCPConfiguration =
    super._withRootParsePlugins(amfParsePlugin)

  override def withReferenceParsePlugins(amfPlugin: List[AMFParsePlugin]): MCPConfiguration =
    super._withReferenceParsePlugins(amfPlugin)

  override def withPlugins(plugins: List[AMFPlugin[_]]): MCPConfiguration =
    super._withPlugins(plugins)

  private[amf] override def withValidationProfile(profile: ValidationProfile): MCPConfiguration =
    super._withValidationProfile(profile)

  // Keep AMF internal, done to avoid recomputing validations every time a config is requested
  private[amf] override def withValidationProfile(
      profile: ValidationProfile,
      effective: EffectiveValidations
  ): MCPConfiguration =
    super._withValidationProfile(profile, effective)

  /** Add a [[TransformationPipeline]]
    * @param pipeline
    *   [[TransformationPipeline]] to add to configuration object
    * @return
    *   [[MCPConfiguration]] with [[TransformationPipeline]] added
    */
  override def withTransformationPipeline(pipeline: TransformationPipeline): MCPConfiguration =
    super._withTransformationPipeline(pipeline)

  /** AMF internal method just to facilitate the construction */
  override private[amf] def withTransformationPipelines(pipelines: List[TransformationPipeline]): MCPConfiguration =
    super._withTransformationPipelines(pipelines)

  /** Set [[ErrorHandlerProvider]]
    * @param provider
    *   [[ErrorHandlerProvider]] to set to configuration object
    * @return
    *   [[MCPConfiguration]] with [[ErrorHandlerProvider]] set
    */
  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): MCPConfiguration =
    super._withErrorHandlerProvider(provider)

  /** Add an [[AMFEventListener]]
    * @param listener
    *   [[AMFEventListener]] to add to configuration object
    * @return
    *   [[MCPConfiguration]] with [[AMFEventListener]] added
    */
  override def withEventListener(listener: AMFEventListener): MCPConfiguration = super._withEventListener(listener)

  private[amf] override def withEntities(entities: Map[String, ModelDefaultBuilder]): MCPConfiguration =
    super._withEntities(entities)

  override def withAliases(aliases: NamespaceAliases): MCPConfiguration =
    super._withAliases(aliases)

  private[amf] override def withExtensions(dialect: Dialect): MCPConfiguration = {
    super.withExtensions(dialect).asInstanceOf[MCPConfiguration]
  }

  private[amf] override def withAnnotations(annotations: Map[String, AnnotationGraphLoader]): MCPConfiguration =
    super._withAnnotations(annotations)

  /** Set [[BaseExecutionEnvironment]]
    * @param executionEnv
    *   [[BaseExecutionEnvironment]] to set to configuration object
    * @return
    *   [[MCPConfiguration]] with [[BaseExecutionEnvironment]] set
    */
  override def withExecutionEnvironment(executionEnv: ExecutionEnvironment): MCPConfiguration =
    super._withExecutionEnvironment(executionEnv)

  /** Register a Dialect
    * @param dialect
    *   [[Dialect]] to register
    * @return
    *   [[MCPConfiguration]] with [[Dialect]] registered
    */
  override def withDialect(dialect: Dialect): MCPConfiguration =
    super.withDialect(dialect).asInstanceOf[MCPConfiguration]

  /** Register a Dialect
    * @param url
    *   URL of the Dialect to register
    * @return
    *   A CompletableFuture of [[MCPConfiguration]]
    */
  override def withDialect(url: String): Future[MCPConfiguration] =
    super.withDialect(url).map(_.asInstanceOf[MCPConfiguration])(getExecutionContext)

  /** Register a [[Dialect]] linked from a [[DialectInstance]]
    * @param url
    *   of the [[DialectInstance]]
    * @return
    *   A CompletableFuture of [[MCPConfiguration]]
    */
  override def forInstance(url: String): Future[MCPConfiguration] =
    super.forInstance(url).map(_.asInstanceOf[MCPConfiguration])(getExecutionContext)

  override def withIdAdopterProvider(idAdopterProvider: IdAdopterProvider): MCPConfiguration =
    super._withIdAdopterProvider(idAdopterProvider)
}

object MCPConfiguration {

  def MCP(): MCPConfiguration =
    predefined()
      .withPlugins(
        List(MCPParsePlugin, AMFJsonLDSchemaGraphRenderPlugin, AMFJsonLDSchemaGraphParsePlugin, MCPValidationPlugin())
      )
      .withTransformationPipelines(
        List(
          MCPTransformationPipeline(),
          MCPEditingPipeline(),
          MCPCachePipeline()
        )
      )

  private def predefined(): MCPConfiguration = {
    ShapesRegister.register()
    val predefinedShapesConfig = ShapesConfiguration.predefined()
    new MCPConfiguration(
      predefinedShapesConfig.resolvers,
      predefinedShapesConfig.errorHandlerProvider,
      predefinedShapesConfig.registry,
      predefinedShapesConfig.listeners,
      predefinedShapesConfig.options,
      predefinedShapesConfig.idAdopterProvider
    )
  }
}
