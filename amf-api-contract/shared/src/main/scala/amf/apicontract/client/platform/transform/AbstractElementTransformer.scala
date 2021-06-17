package amf.apicontract.client.platform.transform

import amf.apicontract.client.platform.model.domain.templates.{ResourceType, Trait}
import amf.apicontract.client.platform.model.domain.{EndPoint, Operation}
import amf.core.client.common.validation.{ProfileName, Raml10Profile}
import amf.core.client.platform.model.document.BaseUnit
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.apicontract.client.scala.transform.{AbstractElementTransformer => InternalTransformer}
import amf.core.client.platform.errorhandling.ClientErrorHandler
import amf.core.internal.convert.ClientErrorHandlerConverter
import amf.core.internal.convert.ClientErrorHandlerConverter._

import scala.scalajs.js.annotation.JSExportAll

/**
  * Temporally object to respect new domain internfaces. Probably this will be agroupated at some Domain Element client logic.
  */
@JSExportAll
object AbstractElementTransformer {

  def asEndpoint[T <: BaseUnit](unit: T,
                                rt: ResourceType,
                                errorHandler: ClientErrorHandler,
                                profile: ProfileName = Raml10Profile): EndPoint = {
    InternalTransformer.asEndpoint(unit._internal,
                                   rt._internal,
                                   profile,
                                   ClientErrorHandlerConverter.convert(errorHandler))
  }

  def asOperation[T <: BaseUnit](unit: T,
                                 tr: Trait,
                                 errorHandler: ClientErrorHandler,
                                 profile: ProfileName = Raml10Profile): Operation = {
    InternalTransformer.asOperation(unit._internal,
                                    tr._internal,
                                    profile,
                                    ClientErrorHandlerConverter.convert(errorHandler))

  }

}
