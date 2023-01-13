package amf.shapes.client.platform.config

import amf.aml.client.platform.model.document.Dialect
import amf.core.client.platform.config.AMFEventListener
import amf.core.client.platform.errorhandling.ErrorHandlerProvider
import amf.core.client.platform.execution.ExecutionEnvironment
import amf.core.client.platform.reference.UnitCache
import amf.core.client.platform.resource.ResourceLoader
import amf.core.client.scala.transform._
import amf.core.client.platform.config._
import amf.core.internal.convert.CoreClientConverters.ClientFuture
import amf.shapes.client.platform.{BaseShapesConfiguration, ShapesConfiguration}
import amf.shapes.client.scala.config.{JsonLDSchemaConfiguration => InternalJsonLDSchemaDocumentConfiguration, JsonLDSchemaConfigurationClient => InternalJsonLDSchemaConfigurationClient}
import amf.shapes.internal.convert.ShapeClientConverters._
import amf.core.internal.convert.ClientErrorHandlerConverter._



import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class JsonLDSchemaConfiguration private [amf](
    private [amf] override val _internal: InternalJsonLDSchemaDocumentConfiguration
) extends BaseShapesConfiguration(_internal) {

  /** Contains common AMF graph operations associated to documents */
  override def baseUnitClient(): JsonLDSchemaConfigurationClient = _internal.baseUnitClient()

  override def withParsingOptions(parsingOptions: ParsingOptions): JsonLDSchemaConfiguration = _internal.withParsingOptions(parsingOptions)

  override def withRenderOptions(renderOptions: RenderOptions): JsonLDSchemaConfiguration = _internal.withRenderOptions(renderOptions)

/** Add a [[ResourceLoader]]
  * @param rl
  *   [[ResourceLoader]] to add to configuration object
  * @return
  *   [[ShapesConfiguration]] with the [[ResourceLoader]] added
  */

  override def withResourceLoader(rl: ResourceLoader): JsonLDSchemaConfiguration = _internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl))

  /** Set the configuration [[ResourceLoader]]s
    *
    * @param rl
    *   a list of [[ResourceLoader]] to set to the configuration object
    * @return
    *   [[ShapesConfiguration]] with [[ResourceLoader]]s set
    */

  override def withResourceLoaders(rl: ClientList[ResourceLoader]): JsonLDSchemaConfiguration = _internal.withResourceLoaders(rl.asInternal.toList)

  override def withUnitCache(cache: UnitCache): JsonLDSchemaConfiguration = _internal.withUnitCache(UnitCacheMatcher.asInternal(cache))

  def withTransformationPipeline(pipeline: TransformationPipeline): JsonLDSchemaConfiguration = _internal.withTransformationPipeline(pipeline)

  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): JsonLDSchemaConfiguration = _internal.withErrorHandlerProvider(() => provider.errorHandler())

  override def withEventListener(listener: AMFEventListener): JsonLDSchemaConfiguration = _internal.withEventListener(listener)

  def withExecutionEnvironment(executionEnv: ExecutionEnvironment): JsonLDSchemaConfiguration = _internal.withExecutionEnvironment(executionEnv._internal)

  override def withDialect(dialect: Dialect): JsonLDSchemaConfiguration = _internal.withDialect(dialect)

  def withDialect(url: String): ClientFuture[JsonLDSchemaConfiguration] = _internal.withDialect(url).asClient

  def forInstance(url:String): ClientFuture[JsonLDSchemaConfiguration] = _internal.forInstance(url).asClient

}
@JSExportAll
@JSExportTopLevel("JsonLDSchemaConfiguration")
object JsonLDSchemaConfiguration {
  def JsonLDSchema(): JsonLDSchemaConfiguration = InternalJsonLDSchemaDocumentConfiguration.JsonLDSchema()
}
