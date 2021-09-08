package amf.shapes.client.scala

import amf.aml.client.scala.model.document.Dialect
import amf.aml.client.scala.model.domain.SemanticExtension
import amf.aml.client.scala.{AMLBaseUnitClient, AMLConfiguration, AMLConfigurationState, AMLElementClient}
import amf.aml.internal.annotations.serializable.AMLSerializableAnnotations
import amf.aml.internal.entities.AMLEntities
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

  override def baseUnitClient(): ShapesBaseUnitClient      = new ShapesBaseUnitClient(this)
  override def elementClient(): ShapesElementClient        = new ShapesElementClient(this)
  override def configurationState(): AMLConfigurationState = new AMLConfigurationState(this)

  override def withParsingOptions(parsingOptions: ParsingOptions): ShapesConfiguration =
    super._withParsingOptions(parsingOptions)

  override def withResourceLoader(rl: ResourceLoader): ShapesConfiguration =
    super._withResourceLoader(rl)

  override def withResourceLoaders(rl: List[ResourceLoader]): ShapesConfiguration =
    super._withResourceLoaders(rl)

  override def withUnitCache(cache: UnitCache): ShapesConfiguration =
    super._withUnitCache(cache)

  override def withFallback(plugin: DomainParsingFallback): ShapesConfiguration = super._withFallback(plugin)

  override def withPlugin(amfPlugin: AMFPlugin[_]): ShapesConfiguration =
    super._withPlugin(amfPlugin)

  override def withPlugins(plugins: List[AMFPlugin[_]]): ShapesConfiguration =
    super._withPlugins(plugins)

  private[amf] override def withValidationProfile(profile: ValidationProfile): ShapesConfiguration =
    super._withValidationProfile(profile)

  override def withTransformationPipeline(pipeline: TransformationPipeline): ShapesConfiguration =
    super._withTransformationPipeline(pipeline)

  /**
    * AMF internal method just to facilitate the construction
    * @param pipelines
    * @return
    */
  override private[amf] def withTransformationPipelines(pipelines: List[TransformationPipeline]): ShapesConfiguration =
    super._withTransformationPipelines(pipelines)

  override def withRenderOptions(renderOptions: RenderOptions): ShapesConfiguration =
    super._withRenderOptions(renderOptions)

  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): ShapesConfiguration =
    super._withErrorHandlerProvider(provider)

  override def withEventListener(listener: AMFEventListener): ShapesConfiguration = super._withEventListener(listener)

  private[amf] override def withEntities(entities: Map[String, ModelDefaultBuilder]): ShapesConfiguration =
    super._withEntities(entities)

  override private[amf] def withExtensions(extensions: Seq[SemanticExtension]): ShapesConfiguration =
    copy(registry = registry.withExtensions(extensions))

  private[amf] override def withAnnotations(annotations: Map[String, AnnotationGraphLoader]): ShapesConfiguration =
    super._withAnnotations(annotations)

  override def withExecutionEnvironment(executionEnv: ExecutionEnvironment): ShapesConfiguration =
    super._withExecutionEnvironment(executionEnv)

  override def withDialect(url: String): Future[ShapesConfiguration] =
    super.withDialect(url).map(_.asInstanceOf[ShapesConfiguration])(getExecutionContext)

  override def withDialect(dialect: Dialect): ShapesConfiguration =
    super.withDialect(dialect).asInstanceOf[ShapesConfiguration]

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
    // TODO ARM: validate plugin and payload plugin of api?
    val predefinedAMLConfig = AMLConfiguration.predefined()

    new ShapesConfiguration(
      predefinedAMLConfig.resolvers,
      predefinedAMLConfig.errorHandlerProvider,
      predefinedAMLConfig.registry
        .withEntities(AMLEntities.entities)
        .withAnnotations(AMLSerializableAnnotations.annotations),
      predefinedAMLConfig.listeners,
      predefinedAMLConfig.options
    ).withEntities(ShapeEntities.entities)
      .withAnnotations(ShapeSerializableAnnotations.annotations)
      .withPlugin(JsonSchemaShapePayloadValidationPlugin)
  }
}
