package amf.apicontract.internal.transformation.compatibility.raml

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.internal.metamodel.domain.RequestModel
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.TransformationStep

class PushSingleOperationPathParams() extends TransformationStep {

  def checkUriParams(endpoint: EndPoint): EndPoint = {
    if (endpoint.operations.size == 1 && Option(endpoint.operations.head.request).isDefined) {
      val operation = endpoint.operations.head
      val uriParams = operation.request.uriParameters
      if (uriParams.nonEmpty) {
        operation.request.fields.removeField(RequestModel.UriParameters)
        endpoint.withParameters(uriParams.map { param =>
          param.withRequired(true) // URI parameters are always required
        })
      } else endpoint
    } else endpoint
  }

  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    try {
      model.iterator().foreach {
        case endpoint: EndPoint =>
          checkUriParams(endpoint)
        case _ => // ignore
      }
    } catch {
      case _: Throwable => // ignore: we don't want this to break anything
    }
    model
  }
}
