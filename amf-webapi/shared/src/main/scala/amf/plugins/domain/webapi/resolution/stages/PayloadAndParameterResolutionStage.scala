package amf.plugins.domain.webapi.resolution.stages

import amf.core.model.document.{BaseUnit, Document}
import amf.core.parser.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.webapi.models._
import amf.{Oas30Profile, ProfileName}

/**
  * Propagate examples defined in parameters and payloads onto their corresponding shape so they are validated
  * in the examples validation phase
  * Only necessary for OAS 3.0 spec
  */
class PayloadAndParameterResolutionStage(profile: ProfileName)(override implicit val errorHandler: ErrorHandler)
    extends ResolutionStage() {

  override def resolve[T <: BaseUnit](model: T): T = {
    profile match {
      case Oas30Profile => resolveExamples(model).asInstanceOf[T]
      case _            => model
    }
  }

  def resolveExamples(model: BaseUnit): BaseUnit = {
    model match {
      case doc: Document if doc.encodes.isInstanceOf[WebApi] =>
        val webApiSchemas   = searchPayloadAndParams(doc.encodes.asInstanceOf[WebApi])
        val declaredSchemas = searchDeclarations(doc)
        (webApiSchemas ++ declaredSchemas).foreach(setExamplesInSchema)
      case _ =>
    }
    model
  }

  private def searchDeclarations(doc: Document): Seq[SchemaContainer] =
    doc.declares.collect {
      case param: Parameter => param.payloads :+ param
      case req: Request     => req.payloads
      case res: Response    => res.payloads
    }.flatten

  def searchPayloadAndParams(webApi: WebApi): Seq[SchemaContainer] = {
    webApi.endPoints.flatMap { endpoint =>
      val paramSchemas    = endpoint.parameters.flatMap(_.payloads)
      val endpointSchemas = traverseEndpoint(endpoint)
      paramSchemas ++ endpointSchemas ++ endpoint.parameters
    }
  }

  private def traverseEndpoint(endpoint: EndPoint): Seq[SchemaContainer] = {
    endpoint.operations.flatMap { op =>
      val responseSchemas = op.responses.flatMap(_.payloads)
      val requestSchemas = Option(op.request) match {
        case Some(req) => reqSchemas(req)
        case None      => Nil
      }
      responseSchemas ++ requestSchemas
    }
  }

  private def reqSchemas(req: Request): Seq[SchemaContainer] = {
    val reqParams = req.uriParameters ++ req.queryParameters ++ req.cookieParameters
    req.payloads ++ reqParams.flatMap(_.payloads) ++ reqParams
  }

  def setExamplesInSchema(payloadOrParam: SchemaContainer): Unit =
    payloadOrParam.schema match {
      case shape: AnyShape =>
        payloadOrParam.examples.foreach { example =>
          if (!shape.examples.exists(_.id == example.id)) {
            shape.withExamples(shape.examples ++ Seq(example))
            payloadOrParam.removeExamples()
          }
        }
      case _ =>
    }

}
