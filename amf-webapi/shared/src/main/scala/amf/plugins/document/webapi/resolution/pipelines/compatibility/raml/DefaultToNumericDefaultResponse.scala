package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.models.{Operation, Response}

class DefaultToNumericDefaultResponse() extends ResolutionStage {

  override def resolve[T <: BaseUnit](model: T, errorHandler: ErrorHandler): T = {
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
