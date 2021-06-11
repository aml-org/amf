package amf.plugins.document.apicontract.resolution.pipelines.compatibility.raml

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.stages.TransformationStep
import amf.plugins.domain.apicontract.metamodel.RequestModel
import amf.plugins.domain.apicontract.models.EndPoint

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

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
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
