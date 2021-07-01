package amf.apicontract.client.platform
import amf.aml.client.platform.{AMLElementClient, BaseAMLElementClient}
import amf.apicontract.client.platform.model.domain.{EndPoint, Operation}
import amf.apicontract.client.platform.model.domain.templates.{ResourceType, Trait}
import amf.apicontract.client.platform.render.ApiDomainElementEmitter
import amf.apicontract.client.platform.transform.AbstractElementTransformer
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.apicontract.client.scala.{AMFElementClient => InternalAMFElementClient}
import amf.core.client.common.validation.{ProfileName, Raml10Profile}
import amf.core.client.platform.errorhandling.ClientErrorHandler
import amf.core.client.platform.model.document.BaseUnit
import amf.core.client.platform.model.domain.DomainElement
import amf.shapes.client.platform.model.domain.AnyShape
import amf.shapes.client.platform.render.JsonSchemaShapeRenderer
import amf.core.internal.convert.ClientErrorHandlerConverter._
import amf.core.internal.remote.Vendor
import org.yaml.builder.DocBuilder

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class AMFElementClient private[amf] (private val configuration: AMFConfiguration)
    extends BaseAMLElementClient(configuration) {

  def getConfiguration(): AMFConfiguration = configuration

  private def obtainEH: ClientErrorHandler = getConfiguration()._internal.errorHandlerProvider.errorHandler()

  def toJsonSchema(element: AnyShape): String    = JsonSchemaShapeRenderer.toJsonSchema(element, getConfiguration())
  def buildJsonSchema(element: AnyShape): String = JsonSchemaShapeRenderer.buildJsonSchema(element, getConfiguration())

  def renderToBuilder[T](element: DomainElement, mediaType: String, builder: DocBuilder[T]): Unit =
    ApiDomainElementEmitter.emitToBuilder(element, mediaType, obtainEH, builder)

  /** Get this resource type as an endpoint. No variables will be replaced. Pass the BaseUnit that contains this trait to use its declarations and the profile ProfileNames.RAML08 if this is from a raml08 unit. */
  def asEndpoint[T <: BaseUnit](unit: T, rt: ResourceType, profile: ProfileName = Raml10Profile): EndPoint =
    AbstractElementTransformer.asEndpoint(unit, rt, obtainEH, profile)

  /** Get this trait as an operation. No variables will be replaced. Pass the BaseUnit that contains this trait to use its declarations and the profile ProfileNames.RAML08 if this is from a raml08 unit. */
  def asOperation[T <: BaseUnit](unit: T, tr: Trait, profile: ProfileName = Raml10Profile): Operation =
    AbstractElementTransformer.asOperation(unit, tr, obtainEH, profile)

}
