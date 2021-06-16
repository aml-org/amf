package amf.apicontract.internal.transformation.compatibility.raml

import amf.apicontract.client.scala.model.domain.{Operation, Response}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.stages.TransformationStep
import amf.plugins.domain.apicontract.models.Response

class DefaultToNumericDefaultResponse() extends TransformationStep {

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    try {
      model.iterator().foreach {
        case operation: Operation =>
          checkDefaultResponse(operation)
        case _ => // ignore
      }
    } catch {
      case e: Throwable => // ignore: we don't want this to break anything
    }
    model
  }

  def checkDefaultResponse(operation: Operation): Unit = {
    operation.responses.find(_.statusCode.value() == "default") match {
      case Some(defaultResponse) =>
        val responsesMap = operation.responses.foldLeft(Map[String, Response]()) {
          case (acc, resp) =>
            acc.updated(resp.statusCode.value(), resp)
        }
        val preferredStatusCodes = Seq("200", "500")
        preferredStatusCodes.find(responsesMap.get(_).isEmpty) match {
          case Some(preferredStatusCode) => defaultResponse.withStatusCode(preferredStatusCode)
          case _ =>
            var nextAvailable = 501
            var found         = false
            while (!found && nextAvailable < 600) {
              if (responsesMap.get(nextAvailable.toString).isEmpty) {
                found = true
                defaultResponse.withStatusCode(nextAvailable.toString)
              } else {
                nextAvailable += 1
              }
            }
        }
      case _ => // ignore
    }
  }
}
