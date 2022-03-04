package amf.jsonldschema.client.scala

import amf.aml.internal.registries.AMLRegistry
import amf.core.client.scala.config.{AMFEventListener, AMFOptions}
import amf.core.client.scala.errorhandling.{DefaultErrorHandlerProvider, ErrorHandlerProvider}
import amf.core.internal.registries.AMFRegistry
import amf.core.internal.resource.AMFResolvers
import amf.jsonldschema.internal.scala.model.metamodel.JsonLdSchemaEncodesModel
import amf.shapes.client.scala.config.{SemanticBaseUnitClient, SemanticJsonSchemaConfiguration}

import scala.concurrent.ExecutionContext

class JsonLDSchemaConfiguration private[amf] (override private[amf] val resolvers: AMFResolvers,
                                              override private[amf] val errorHandlerProvider: ErrorHandlerProvider,
                                              override private[amf] val registry: AMLRegistry,
                                              override private[amf] val listeners: Set[AMFEventListener],
                                              override private[amf] val options: AMFOptions)
    extends SemanticJsonSchemaConfiguration(resolvers, errorHandlerProvider, registry, listeners, options) {

  private implicit val ec: ExecutionContext = this.getExecutionContext

  override protected[amf] def copy(resolvers: AMFResolvers = resolvers,
                                   errorHandlerProvider: ErrorHandlerProvider = errorHandlerProvider,
                                   registry: AMFRegistry = registry,
                                   listeners: Set[AMFEventListener] = listeners,
                                   options: AMFOptions = options): JsonLDSchemaConfiguration =
    new JsonLDSchemaConfiguration(resolvers,
                                  errorHandlerProvider,
                                  registry.asInstanceOf[AMLRegistry],
                                  listeners,
                                  options)

  override def baseUnitClient(): JsonLDSchemaBaseUnitClient = new JsonLDSchemaBaseUnitClient(this)

}

object JsonLDSchemaConfiguration {

  def empty(): JsonLDSchemaConfiguration = {

    new JsonLDSchemaConfiguration(
      AMFResolvers.predefined(),
      DefaultErrorHandlerProvider,
      AMLRegistry.empty,
      Set.empty,
      AMFOptions.default()
    )
  }

  def predefined(): JsonLDSchemaConfiguration = {
    val shapesConfig = SemanticJsonSchemaConfiguration.predefined()
    new JsonLDSchemaConfiguration(
      shapesConfig.resolvers,
      shapesConfig.errorHandlerProvider,
      shapesConfig.registry.withEntities(Map(JsonLdSchemaEncodesModel.`type`.head.iri() -> JsonLdSchemaEncodesModel)),
      shapesConfig.listeners,
      shapesConfig.options
    )
  }
}
