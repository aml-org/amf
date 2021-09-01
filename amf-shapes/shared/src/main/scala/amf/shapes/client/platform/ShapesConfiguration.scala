package amf.shapes.client.platform

import amf.aml.client.platform.model.document.Dialect
import amf.aml.client.platform.{AMLBaseUnitClient, AMLConfigurationState}
import amf.aml.client.scala.{AMLBaseUnitClient => InternalAMLBaseUnitClient}
import amf.aml.internal.convert.VocabulariesClientConverter.{ClientFuture, ClientList}
import amf.core.client.platform.config.{AMFEventListener, ParsingOptions, RenderOptions}
import amf.core.client.platform.errorhandling.ErrorHandlerProvider
import amf.core.client.platform.execution.BaseExecutionEnvironment
import amf.core.client.platform.reference.UnitCache
import amf.core.client.platform.resource.ResourceLoader
import amf.core.client.platform.transform.TransformationPipeline
import amf.core.client.platform.validation.payload.AMFShapePayloadValidationPlugin
import amf.core.internal.convert.ClientErrorHandlerConverter._
import amf.core.internal.convert.PayloadValidationPluginConverter.PayloadValidationPluginMatcher
import amf.core.internal.convert.TransformationPipelineConverter._
import amf.shapes.client.scala.{ShapesConfiguration => InternalShapesConfiguration}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class ShapesConfiguration private[amf] (private[amf] override val _internal: InternalShapesConfiguration)
    extends BaseShapesConfiguration(_internal) {

  override def baseUnitClient(): AMLBaseUnitClient  = new AMLBaseUnitClient(new InternalAMLBaseUnitClient(_internal))
  override def elementClient(): ShapesElementClient = new ShapesElementClient(this)
  def configurationState(): AMLConfigurationState   = new AMLConfigurationState(_internal.configurationState())

  override def withParsingOptions(parsingOptions: ParsingOptions): ShapesConfiguration =
    _internal.withParsingOptions(parsingOptions)

  override def withRenderOptions(renderOptions: RenderOptions): ShapesConfiguration =
    _internal.withRenderOptions(renderOptions)

  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): ShapesConfiguration =
    _internal.withErrorHandlerProvider(() => provider.errorHandler())

  override def withResourceLoader(rl: ResourceLoader): ShapesConfiguration =
    _internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl))

  override def withResourceLoaders(rl: ClientList[ResourceLoader]): ShapesConfiguration =
    _internal.withResourceLoaders(rl.asInternal.toList)

  override def withUnitCache(cache: UnitCache): ShapesConfiguration =
    _internal.withUnitCache(UnitCacheMatcher.asInternal(cache))

  override def withTransformationPipeline(pipeline: TransformationPipeline): ShapesConfiguration =
    _internal.withTransformationPipeline(pipeline)

  override def withEventListener(listener: AMFEventListener): ShapesConfiguration =
    _internal.withEventListener(listener)

  override def withDialect(dialect: Dialect): ShapesConfiguration = _internal.withDialect(dialect)

  override def withExecutionEnvironment(executionEnv: BaseExecutionEnvironment): ShapesConfiguration =
    _internal.withExecutionEnvironment(executionEnv._internal)

  def withDialect(path: String): ClientFuture[ShapesConfiguration] = _internal.withDialect(path).asClient

  def forInstance(url: String): ClientFuture[ShapesConfiguration] = _internal.forInstance(url).asClient

  override def withShapePayloadPlugin(plugin: AMFShapePayloadValidationPlugin): ShapesConfiguration =
    _internal.withPlugin(PayloadValidationPluginMatcher.asInternal(plugin))
}

@JSExportAll
@JSExportTopLevel("ShapesConfiguration")
object ShapesConfiguration {

  def empty(): ShapesConfiguration = InternalShapesConfiguration.empty()

  /** Predefined environment to deal with AML documents based on ShapesConfiguration predefined() method */
  def predefined(): ShapesConfiguration = InternalShapesConfiguration.predefined()
}
