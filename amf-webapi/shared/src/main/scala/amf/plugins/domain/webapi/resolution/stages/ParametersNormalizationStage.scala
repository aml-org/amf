package amf.plugins.domain.webapi.resolution.stages

import amf._
import amf.core.annotations.SynthesizedField
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.AmfArray
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.webapi.parser.spec.domain.Parameters
import amf.plugins.domain.webapi.metamodel.{EndPointModel, RequestModel, ServerModel}
import amf.plugins.domain.webapi.models.{EndPoint, Operation, Parameter, WebApi}

/**
  * Place parameter models in the right locations according to the RAML/OpenAPI specs and our own
  * criterium for AMF
  *
  * @param profile target profile
  */
abstract class ParametersNormalizationStage(profile: ProfileName)(override implicit val errorHandler: ErrorHandler)
    extends ResolutionStage() {

  override def resolve[T <: BaseUnit](model: T): T = model match {
    case doc: Document if doc.encodes.isInstanceOf[WebApi] =>
      val webApi = doc.encodes.asInstanceOf[WebApi]
      resolve(webApi)
      doc.asInstanceOf[T]
    case _ => model
  }

  protected def resolve(webApi: WebApi): WebApi = webApi

  protected def pushParamsToEndpointOperations(endpoint: EndPoint, finalParams: Parameters) = {
    endpoint.operations.foreach { op =>
      setRequestParameters(op, finalParams)
    }
  }

  protected def assignPathParametersTo(endpoint: EndPoint, path: Seq[Parameter]) =
    if (path.nonEmpty)
      endpoint.fields.setWithoutId(EndPointModel.Parameters, AmfArray(path))

  private def setRequestParameters(op: Operation, params: Parameters) = {
    val request = Option(op.request).getOrElse(op.withRequest())

    val finalParams = params.merge(Parameters(request.queryParameters, request.uriParameters, request.headers))
    // set the list of parameters at the operation level in the corresponding fields
    if (finalParams.query.nonEmpty)
      request.fields.setWithoutId(RequestModel.QueryParameters, AmfArray(finalParams.query))
    if (finalParams.header.nonEmpty) request.fields.setWithoutId(RequestModel.Headers, AmfArray(finalParams.header))
    if (finalParams.path.nonEmpty) request.fields.setWithoutId(RequestModel.UriParameters, AmfArray(finalParams.path))
  }
}

class OpenApiParametersNormalizationStage(override implicit val errorHandler: ErrorHandler)
    extends ParametersNormalizationStage(OasProfile) {

  /**
    * In OpenAPI we just push the endpoint parameters to the operation level, overwriting the any endpoint parameter
    * with the new definition at the operation level
    *
    * @param webApi WebApi in
    * @return webApi WebApi out
    */
  override protected def resolve(webApi: WebApi): WebApi = {
    // collect endpoint path parameters
    webApi.endPoints.foreach { endpoint =>
      val finalParams = Parameters.classified(endpoint.path.value(), endpoint.parameters)
      // collect operation query parameters
      if (finalParams.nonEmpty && endpoint.operations.nonEmpty) {
        endpoint.fields.removeField(EndPointModel.Parameters)
        pushParamsToEndpointOperations(endpoint, finalParams)
      }
    }
    webApi
  }
}

class AmfParametersNormalizationStage(override implicit val errorHandler: ErrorHandler)
    extends ParametersNormalizationStage(AmfProfile) {

  /**
    * In AMF we push all the parameters at the operation level.
    * Parameter references should be already resolved in previous steps.
    *
    * @param webApi WebApi in
    * @return webApi WebApi out
    */
  override protected def resolve(webApi: WebApi): WebApi = {
    // collect endpoint path parameters
    webApi.endPoints.foreach { endpoint =>
      val finalParams = Parameters(path = removeParamsFromMadeUpServer(webApi))
        .merge(Parameters.classified(endpoint.path.value(), endpoint.parameters))
      endpoint.fields.removeField(EndPointModel.Parameters)
      // collect operation query parameters
      if (finalParams.nonEmpty) pushParamsToEndpointOperations(endpoint, finalParams)
    }
    webApi
  }

  private def removeParamsFromMadeUpServer(webApi: WebApi): Seq[Parameter] = {
    val server = webApi.servers.find(_.annotations.find(classOf[SynthesizedField]).isDefined)

    server
      .map { s =>
        val vars = s.variables
        s.fields.removeField(ServerModel.Variables)
        vars
      }
      .getOrElse(Nil)
  }
}

class Raml10ParametersNormalizationStage(override implicit val errorHandler: ErrorHandler)
    extends ParametersNormalizationStage(AmfProfile) {

  /**
    * In RAML we assign the parameters at the right level according to the RAML spec:
    * - webapi for baseURI parameters
    * - endpoint for the path parameters
    * - operation for the  query, path and header parameters
    * Since parameters can be at any level due to the source of the model being an OpenAPI document
    *
    * @param webApi WebApi in
    * @return webApi WebApi out
    */
  override protected def resolve(webApi: WebApi): WebApi = {
    // collect endpoint path parameters
    webApi.endPoints.foreach { endpoint =>
      val endpointParameters = endpoint.parameters

      // we filter path parameters and the remaining parameters
      val (path, other) = endpointParameters.partition(p => p.binding.is("path"))

      val finalParams = Parameters.classified(endpoint.path.value(), other)
      // collect operation query parameters
      if (finalParams.nonEmpty && endpoint.operations.nonEmpty) {
        endpoint.fields.removeField(EndPointModel.Parameters)
        assignPathParametersTo(endpoint, path)
        pushParamsToEndpointOperations(endpoint, finalParams)
      }
    }
    webApi
  }
}