package amf.plugins.domain.webapi.resolution.stages

import amf.ProfileNames
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.AmfArray
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.webapi.parser.spec.domain.Parameters
import amf.plugins.domain.webapi.metamodel.{EndPointModel, RequestModel, WebApiModel}
import amf.plugins.domain.webapi.models.{Operation, WebApi}

/**
  * Place parameter models in the right locations according to the RAML/OpenAPI specs and our own
  * criterium for AMF
  * @param profile target profile
  */
class ParametersNormalizationStage(profile: String) extends ResolutionStage(profile) {

  override def resolve(model: BaseUnit): BaseUnit = {
    profile match {
      case ProfileNames.RAML                      => parametersRaml10(model)
      case ProfileNames.OAS | ProfileNames.RAML08 => parametersOpenApi(model)
      case ProfileNames.AMF                       => parametersAmf(model)
      case _                                      => throw new Exception(s"Unknown profile $profile")
    }
  }

  /**
    * In AMF we push all the parameters at the operation level.
    * Parameter references should be already resolved in previous steps.
    * @param unit BaseUnit in
    * @return unit BaseUnit out
    */
  protected def parametersAmf(unit: BaseUnit): BaseUnit = {
    unit match {
      case doc: Document if doc.encodes.isInstanceOf[WebApi] =>
        // collect baseUri parameters
        val webApi      = doc.encodes.asInstanceOf[WebApi]
        var finalParams = Parameters(path = Option(webApi.baseUriParameters).getOrElse(Seq()))
        webApi.fields.remove(WebApiModel.BaseUriParameters)
        // collect endpoint parameters
        webApi.endPoints.foreach { endpoint =>
          finalParams = finalParams.merge(Parameters.classified(Option(endpoint.parameters).getOrElse(Seq())))
          endpoint.fields.remove(EndPointModel.Parameters)
          // collect operation query parameters
          if (finalParams.nonEmpty)
            endpoint.operations.foreach { op =>
              setRequestParameters(op, finalParams)
            }
        }
        doc
      case _ => unit
    }
  }

  private def setRequestParameters(op: Operation, params: Parameters) = {
    val request = Option(op.request).getOrElse(op.withRequest())

    val finalParams = params.merge(Parameters(request.queryParameters, request.uriParameters, request.headers))
    // set the list of parameters at the operation level in the corresponding fields
    if (finalParams.query.nonEmpty)
      request.fields.setWithoutId(RequestModel.QueryParameters, AmfArray(finalParams.query))
    if (finalParams.header.nonEmpty) request.fields.setWithoutId(RequestModel.Headers, AmfArray(finalParams.header))
    if (finalParams.path.nonEmpty) request.fields.setWithoutId(RequestModel.UriParameters, AmfArray(finalParams.path))
  }

  /**
    * In OpenAPI we just push the endpoint parameters to the operation level, overwriting the any endpoint parameter
    * with the new definition at the operation level
    *
    * @param unit BaseUnit in
    * @return unit BaseUnit out
    */
  protected def parametersOpenApi(unit: BaseUnit): BaseUnit = {
    unit match {
      case doc: Document if doc.encodes.isInstanceOf[WebApi] =>
        val webApi = doc.encodes.asInstanceOf[WebApi]
        // collect endpoint path parameters
        webApi.endPoints.foreach { endpoint =>
          val finalParams = Parameters.classified(Option(endpoint.parameters).getOrElse(Seq()))
          // collect operation query parameters
          if (finalParams.nonEmpty && endpoint.operations.nonEmpty) {
            endpoint.fields.remove(EndPointModel.Parameters)
            endpoint.operations.foreach { op =>
              setRequestParameters(op, finalParams)
            }
          }
        }
        doc
      case _ => unit
    }
  }

  /**
    * In RAML we assign the parameters at the right level according to the RAML spec:
    * - webapi for baseURI parameters
    * - endpoint for the path parameters
    * - operation for the  query, path and header parameters
    * Since parameters can be at any level due to the source of the model being an OpenAPI document
    * @param unit BaseUnit in
    * @return unit BaseUnit out
    */
  protected def parametersRaml10(unit: BaseUnit): BaseUnit = {
    unit match {
      case doc: Document if doc.encodes.isInstanceOf[WebApi] =>
        val webApi = doc.encodes.asInstanceOf[WebApi]
        // collect endpoint path parameters
        webApi.endPoints.foreach { endpoint =>
          val endpointParameters = Option(endpoint.parameters).getOrElse(Seq())

          // we filter path parameters and the remaining parameters
          val (path, other) = endpointParameters.partition(p => p.binding == "path")

          val finalParams = Parameters.classified(other)
          // collect operation query parameters
          if (finalParams.nonEmpty && endpoint.operations.nonEmpty) {
            endpoint.fields.remove(EndPointModel.Parameters)

            // we re-assign path parameters at the endpoint, we push the rest
            if (path.nonEmpty)
              endpoint.fields.setWithoutId(EndPointModel.Parameters, AmfArray(path))

            endpoint.operations.foreach { op =>
              setRequestParameters(op, finalParams)
            }
          }
        }
        doc
      case _ => unit
    }
  }
}
