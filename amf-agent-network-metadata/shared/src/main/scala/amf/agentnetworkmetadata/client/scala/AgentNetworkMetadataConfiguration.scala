package amf.agentnetworkmetadata.client.scala

import amf.agentnetworkmetadata.internal.plugins.parse.AgentNetworkMetadataParsePlugin
import amf.agentnetworkmetadata.internal.plugins.render.AgentNetworkMetadataRenderPlugin
import amf.agentnetworkmetadata.internal.plugins.validation.AgentNetworkMetadataValidationPlugin
import amf.aml.client.scala.model.document.Dialect
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

class AgentNetworkMetadataConfiguration private[amf] (
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
  ): AgentNetworkMetadataConfiguration =
    new AgentNetworkMetadataConfiguration(
        resolvers,
        errorHandlerProvider,
        registry.asInstanceOf[AMLRegistry],
        listeners,
        options,
        idAdopterProvider
    )

  override def baseUnitClient(): AgentNetworkMetadataBaseUnitClient = new AgentNetworkMetadataBaseUnitClient(this)

  override def withParsingOptions(parsingOptions: ParsingOptions): AgentNetworkMetadataConfiguration =
    super._withParsingOptions(parsingOptions)

  override def withRenderOptions(renderOptions: RenderOptions): AgentNetworkMetadataConfiguration =
    super._withRenderOptions(renderOptions)

  override def withResourceLoader(rl: ResourceLoader): AgentNetworkMetadataConfiguration =
    super._withResourceLoader(rl)

  override def withResourceLoaders(rl: List[ResourceLoader]): AgentNetworkMetadataConfiguration =
    super._withResourceLoaders(rl)

  override def withUnitCache(cache: UnitCache): AgentNetworkMetadataConfiguration =
    super._withUnitCache(cache)

  override def withFallback(plugin: DomainParsingFallback): AgentNetworkMetadataConfiguration = super._withFallback(plugin)

  override def withRootParsePlugin(amfParsePlugin: AMFParsePlugin): AgentNetworkMetadataConfiguration =
    super._withRootParsePlugin(amfParsePlugin)

  override def withPlugin(amfPlugin: AMFPlugin[_]): AgentNetworkMetadataConfiguration =
    super._withPlugin(amfPlugin)

  override def withReferenceParsePlugin(plugin: AMFParsePlugin): AgentNetworkMetadataConfiguration =
    super._withReferenceParsePlugin(plugin)

  override def withRootParsePlugins(amfParsePlugin: List[AMFParsePlugin]): AgentNetworkMetadataConfiguration =
    super._withRootParsePlugins(amfParsePlugin)

  override def withReferenceParsePlugins(amfPlugin: List[AMFParsePlugin]): AgentNetworkMetadataConfiguration =
    super._withReferenceParsePlugins(amfPlugin)

  override def withPlugins(plugins: List[AMFPlugin[_]]): AgentNetworkMetadataConfiguration =
    super._withPlugins(plugins)

  private[amf] override def withValidationProfile(profile: ValidationProfile): AgentNetworkMetadataConfiguration =
    super._withValidationProfile(profile)

  private[amf] override def withValidationProfile(
      profile: ValidationProfile,
      effective: EffectiveValidations
  ): AgentNetworkMetadataConfiguration =
    super._withValidationProfile(profile, effective)

  override def withTransformationPipeline(pipeline: TransformationPipeline): AgentNetworkMetadataConfiguration =
    super._withTransformationPipeline(pipeline)

  override private[amf] def withTransformationPipelines(pipelines: List[TransformationPipeline]): AgentNetworkMetadataConfiguration =
    super._withTransformationPipelines(pipelines)

  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): AgentNetworkMetadataConfiguration =
    super._withErrorHandlerProvider(provider)

  override def withEventListener(listener: AMFEventListener): AgentNetworkMetadataConfiguration = super._withEventListener(listener)

  private[amf] override def withEntities(entities: Map[String, ModelDefaultBuilder]): AgentNetworkMetadataConfiguration =
    super._withEntities(entities)

  override def withAliases(aliases: NamespaceAliases): AgentNetworkMetadataConfiguration =
    super._withAliases(aliases)

  private[amf] override def withExtensions(dialect: Dialect): AgentNetworkMetadataConfiguration = {
    super.withExtensions(dialect).asInstanceOf[AgentNetworkMetadataConfiguration]
  }

  private[amf] override def withAnnotations(annotations: Map[String, AnnotationGraphLoader]): AgentNetworkMetadataConfiguration =
    super._withAnnotations(annotations)

  override def withExecutionEnvironment(executionEnv: ExecutionEnvironment): AgentNetworkMetadataConfiguration =
    super._withExecutionEnvironment(executionEnv)

  override def withDialect(dialect: Dialect): AgentNetworkMetadataConfiguration =
    super.withDialect(dialect).asInstanceOf[AgentNetworkMetadataConfiguration]

  override def withDialect(url: String): Future[AgentNetworkMetadataConfiguration] =
    super.withDialect(url).map(_.asInstanceOf[AgentNetworkMetadataConfiguration])(getExecutionContext)

  override def forInstance(url: String): Future[AgentNetworkMetadataConfiguration] =
    super.forInstance(url).map(_.asInstanceOf[AgentNetworkMetadataConfiguration])(getExecutionContext)

  override def withIdAdopterProvider(idAdopterProvider: IdAdopterProvider): AgentNetworkMetadataConfiguration =
    super._withIdAdopterProvider(idAdopterProvider)
}

object AgentNetworkMetadataConfiguration {

  def AgentNetworkMetadata(): AgentNetworkMetadataConfiguration =
    predefined()
      .withPlugins(
          List(
              AgentNetworkMetadataParsePlugin,
              AgentNetworkMetadataRenderPlugin,
              AgentNetworkMetadataValidationPlugin(),
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

  private def predefined(): AgentNetworkMetadataConfiguration = {
    val baseConfig = JsonSchemaBasedSpecConfiguration.base()
    new AgentNetworkMetadataConfiguration(
        baseConfig.resolvers,
        baseConfig.errorHandlerProvider,
        baseConfig.registry,
        baseConfig.listeners,
        baseConfig.options,
        baseConfig.idAdopterProvider
    )
  }
}
