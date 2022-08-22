package amf.shapes.client.scala.config

import amf.aml.client.scala.model.document.Dialect
import amf.aml.internal.registries.AMLRegistry
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config._
import amf.core.client.scala.errorhandling.ErrorHandlerProvider
import amf.core.client.scala.execution.ExecutionEnvironment
import amf.core.client.scala.model.domain.AnnotationGraphLoader
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.resource.ResourceLoader
import amf.core.client.scala.transform.TransformationPipeline
import amf.core.internal.metamodel.ModelDefaultBuilder
import amf.core.internal.plugins.AMFPlugin
import amf.core.internal.plugins.parse.DomainParsingFallback
import amf.core.internal.registries.AMFRegistry
import amf.core.internal.resource.AMFResolvers
import amf.core.internal.validation.EffectiveValidations
import amf.core.internal.validation.core.ValidationProfile
import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.spec.jsonldschema.JsonLDSchemaParsePlugin
import amf.shapes.internal.transformation.JsonLDSchemaEditingPipeline

import scala.concurrent.{ExecutionContext, Future}

class JsonLDSchemaConfiguration private[amf] (
    override private[amf] val resolvers: AMFResolvers,
    override private[amf] val errorHandlerProvider: ErrorHandlerProvider,
    override private[amf] val registry: AMLRegistry,
    override private[amf] val listeners: Set[AMFEventListener],
    override private[amf] val options: AMFOptions
) extends ShapesConfiguration(resolvers, errorHandlerProvider, registry, listeners, options) {

  private implicit val ec: ExecutionContext = this.getExecutionContext

  override protected[amf] def copy(
      resolvers: AMFResolvers = resolvers,
      errorHandlerProvider: ErrorHandlerProvider = errorHandlerProvider,
      registry: AMFRegistry = registry,
      listeners: Set[AMFEventListener] = listeners,
      options: AMFOptions = options
  ): JsonLDSchemaConfiguration =
    new JsonLDSchemaConfiguration(
      resolvers,
      errorHandlerProvider,
      registry.asInstanceOf[AMLRegistry],
      listeners,
      options
    )
  override def baseUnitClient(): JsonLDSchemaConfigurationClient = new JsonLDSchemaConfigurationClient(this)

  def withJsonLDSchema(jsonDocument: JsonSchemaDocument): JsonLDSchemaConfiguration = {
    val transformed = if (!jsonDocument.processingData.transformed.value()) transform(jsonDocument) else jsonDocument

    withPlugin(new JsonLDSchemaParsePlugin(transformed))
  }

  private def transform(jsonSchemaDocument: JsonSchemaDocument): JsonSchemaDocument = {
    baseUnitClient().transform(jsonSchemaDocument, PipelineId.Editing).baseUnit match {
      case jsd: JsonSchemaDocument => jsd
      case _                       => jsonSchemaDocument
    }
  }

  override def withParsingOptions(parsingOptions: ParsingOptions): JsonLDSchemaConfiguration =
    super._withParsingOptions(parsingOptions)

  /** Set [[RenderOptions]]
    *
    * @param renderOptions
    *   [[RenderOptions]] to add to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[ParsingOptions]] added
    */
  override def withRenderOptions(renderOptions: RenderOptions): JsonLDSchemaConfiguration =
    super._withRenderOptions(renderOptions)

  /** Add a [[ResourceLoader]]
    *
    * @param rl
    *   [[ResourceLoader]] to add to configuration object
    * @return
    *   [[ShapesConfiguration]] with the [[ResourceLoader]] added
    */
  override def withResourceLoader(rl: ResourceLoader): JsonLDSchemaConfiguration =
    super._withResourceLoader(rl)

  /** Set the configuration [[ResourceLoader]]s
    *
    * @param rl
    *   a list of [[ResourceLoader]] to set to the configuration object
    * @return
    *   [[ShapesConfiguration]] with [[ResourceLoader]]s set
    */
  override def withResourceLoaders(rl: List[ResourceLoader]): JsonLDSchemaConfiguration =
    super._withResourceLoaders(rl)

  /** Set [[UnitCache]]
    *
    * @param cache
    *   [[UnitCache]] to add to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[UnitCache]] added
    */
  override def withUnitCache(cache: UnitCache): JsonLDSchemaConfiguration =
    super._withUnitCache(cache)

  override def withFallback(plugin: DomainParsingFallback): JsonLDSchemaConfiguration = super._withFallback(plugin)

  override def withRootParsePlugin(amfParsePlugin: AMFParsePlugin): JsonLDSchemaConfiguration =
    super._withRootParsePlugin(amfParsePlugin)

  override def withPlugin(amfPlugin: AMFPlugin[_]): JsonLDSchemaConfiguration =
    super._withPlugin(amfPlugin)

  override def withReferenceParsePlugin(plugin: AMFParsePlugin): JsonLDSchemaConfiguration =
    super._withReferenceParsePlugin(plugin)

  override def withPlugins(plugins: List[AMFPlugin[_]]): JsonLDSchemaConfiguration =
    super._withPlugins(plugins)

  private[amf] override def withValidationProfile(profile: ValidationProfile): JsonLDSchemaConfiguration =
    super._withValidationProfile(profile)

  // Keep AMF internal, done to avoid recomputing validations every time a config is requested
  private[amf] override def withValidationProfile(
      profile: ValidationProfile,
      effective: EffectiveValidations
  ): JsonLDSchemaConfiguration =
    super._withValidationProfile(profile, effective)

  /** Add a [[TransformationPipeline]]
    *
    * @param pipeline
    *   [[TransformationPipeline]] to add to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[TransformationPipeline]] added
    */
  override def withTransformationPipeline(pipeline: TransformationPipeline): JsonLDSchemaConfiguration =
    super._withTransformationPipeline(pipeline)

  /** AMF internal method just to facilitate the construction */
  override private[amf] def withTransformationPipelines(
      pipelines: List[TransformationPipeline]
  ): JsonLDSchemaConfiguration =
    super._withTransformationPipelines(pipelines)

  /** Set [[ErrorHandlerProvider]]
    *
    * @param provider
    *   [[ErrorHandlerProvider]] to set to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[ErrorHandlerProvider]] set
    */
  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): JsonLDSchemaConfiguration =
    super._withErrorHandlerProvider(provider)

  /** Add an [[AMFEventListener]]
    *
    * @param listener
    *   [[AMFEventListener]] to add to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[AMFEventListener]] added
    */
  override def withEventListener(listener: AMFEventListener): JsonLDSchemaConfiguration =
    super._withEventListener(listener)

  private[amf] override def withEntities(entities: Map[String, ModelDefaultBuilder]): JsonLDSchemaConfiguration =
    super._withEntities(entities)

  private[amf] override def withExtensions(dialect: Dialect): ShapesConfiguration = {
    super.withExtensions(dialect).asInstanceOf[JsonLDSchemaConfiguration]
  }

  private[amf] override def withAnnotations(
      annotations: Map[String, AnnotationGraphLoader]
  ): JsonLDSchemaConfiguration =
    super._withAnnotations(annotations)

  /** Set [[BaseExecutionEnvironment]]
    *
    * @param executionEnv
    *   [[BaseExecutionEnvironment]] to set to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[BaseExecutionEnvironment]] set
    */
  override def withExecutionEnvironment(executionEnv: ExecutionEnvironment): JsonLDSchemaConfiguration =
    super._withExecutionEnvironment(executionEnv)

  /** Register a Dialect
    *
    * @param dialect
    *   [[Dialect]] to register
    * @return
    *   [[JsonLDSchemaConfiguration]] with [[Dialect]] registered
    */
  override def withDialect(dialect: Dialect): JsonLDSchemaConfiguration =
    super.withDialect(dialect).asInstanceOf[JsonLDSchemaConfiguration]

  /** Register a Dialect
    *
    * @param url
    *   URL of the Dialect to register
    * @return
    *   A CompletableFuture of [[JsonLDSchemaConfiguration]]
    */
  override def withDialect(url: String): Future[JsonLDSchemaConfiguration] =
    super.withDialect(url).map(_.asInstanceOf[JsonLDSchemaConfiguration])(getExecutionContext)

  /** Register a [[Dialect]] linked from a [[DialectInstance]]
    *
    * @param url
    *   of the [[DialectInstance]]
    * @return
    *   A CompletableFuture of [[JsonLDSchemaConfiguration]]
    */
  override def forInstance(url: String): Future[JsonLDSchemaConfiguration] =
    super.forInstance(url).map(_.asInstanceOf[JsonLDSchemaConfiguration])(getExecutionContext)
}

object JsonLDSchemaConfiguration {
  def JsonLDSchema(): JsonLDSchemaConfiguration = {
    val base = JsonSchemaConfiguration.JsonSchema()
    new JsonLDSchemaConfiguration(
      base.resolvers,
      base.errorHandlerProvider,
      base.registry,
      base.listeners,
      base.options
    ).withTransformationPipeline(JsonLDSchemaEditingPipeline())

  }
}
