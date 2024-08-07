package amf.apicontract.client.platform

import amf.apicontract.client.platform.model.domain.templates.{ResourceType, Trait}
import amf.apicontract.client.platform.model.domain.{EndPoint, Operation}
import amf.apicontract.client.platform.transform.AbstractElementTransformer
import amf.apicontract.client.scala.{AMFElementClient => InternalAMFElementClient}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.common.validation.{ProfileName, Raml10Profile}
import amf.core.client.platform.errorhandling.ClientErrorHandler
import amf.core.client.platform.model.document.BaseUnit
import amf.core.client.platform.model.domain.DomainElement
import amf.core.client.platform.render.AMFElementRenderer
import amf.core.internal.convert.ClientErrorHandlerConverter._
import amf.shapes.client.platform.BaseShapesElementClient
import org.yaml.builder.DocBuilder

import scala.scalajs.js.annotation.JSExportAll

/** Contains common AML operations not related to document. */
@JSExportAll
class AMFElementClient private[amf] (private val _internal: InternalAMFElementClient)
    extends BaseShapesElementClient(_internal) {

  private[amf] def this(configuration: AMFConfiguration) = {
    this(new InternalAMFElementClient(configuration))
  }

  override def getConfiguration(): AMFConfiguration = _internal.getConfiguration

  private def obtainEH: ClientErrorHandler = getConfiguration()._internal.errorHandlerProvider.errorHandler()

  override def renderToBuilder[T](element: DomainElement, builder: DocBuilder[T]): Unit =
    AMFElementRenderer.renderToBuilder(element, builder, getConfiguration())

  /** Get this resource type as an endpoint. No variables will be replaced. Pass the BaseUnit that contains this trait
    * to use its declarations and the profile ProfileNames.RAML08 if this is from a raml08 unit.
    */
  def asEndpoint[T <: BaseUnit](unit: T, rt: ResourceType, profile: ProfileName = Raml10Profile): EndPoint =
    AbstractElementTransformer.asEndpoint(unit, rt, obtainEH, getConfiguration(), profile)

  /** Get this trait as an operation. No variables will be replaced. Pass the BaseUnit that contains this trait to use
    * its declarations and the profile ProfileNames.RAML08 if this is from a raml08 unit.
    */
  def asOperation[T <: BaseUnit](unit: T, tr: Trait, profile: ProfileName = Raml10Profile): Operation =
    AbstractElementTransformer.asOperation(unit, tr, obtainEH, getConfiguration(), profile)
}
