package amf.apicontract.internal.spec.common.transformation.stage

import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation, Parameter}
import amf.apicontract.internal.metamodel.domain.{EndPointModel, RequestModel, ServerModel}
import amf.apicontract.internal.spec.common.Parameters
import amf.apicontract.internal.validation.definitions.ResolutionSideValidations.DuplicatedParameterWarning
import amf.core.client.common.validation.{AmfProfile, Oas20Profile, ProfileName}
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.AmfArray
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.annotations.SynthesizedField

/** Place parameter models in the right locations according to the RAML/OpenAPI specs and our own criterium for AMF
  *
  * @param profile
  *   target profile
  */
abstract class ParametersNormalizationStage(profile: ProfileName) extends TransformationStep() {

  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = model match {
    case doc: Document if doc.encodes.isInstanceOf[Api] =>
      val api = doc.encodes.asInstanceOf[Api]
      transform(api)(errorHandler)
      doc
    case _ => model
  }

  protected def transform(api: Api)(implicit errorHandler: AMFErrorHandler): Api = api

  protected def pushParamsToEndpointOperations(endpoint: EndPoint, finalParams: Parameters)(implicit
      errorHandler: AMFErrorHandler
  ) = {
    endpoint.operations.foreach { op =>
      setRequestParameters(op, finalParams)
    }
  }

  protected def assignPathParametersTo(endpoint: EndPoint, path: Seq[Parameter]) =
    if (path.nonEmpty)
      endpoint.fields.setWithoutId(EndPointModel.Parameters, AmfArray(path))

  private def setRequestParameters(op: Operation, params: Parameters)(implicit errorHandler: AMFErrorHandler) = {
    val request           = Option(op.request).getOrElse(op.withRequest())
    val requestParameters = Parameters(request.queryParameters, request.uriParameters, request.headers, request.cookieParameters)
    checkDuplicatedParameters(params, requestParameters)
    val finalParams = params.merge(requestParameters)
    // set the list of parameters at the operation level in the corresponding fields
    if (finalParams.query.nonEmpty)
      request.fields.setWithoutId(RequestModel.QueryParameters, AmfArray(finalParams.query))
    if (finalParams.header.nonEmpty) request.fields.setWithoutId(RequestModel.Headers, AmfArray(finalParams.header))
    val pathParams = finalParams.baseUri08 ++ finalParams.path
    if (pathParams.nonEmpty) request.fields.setWithoutId(RequestModel.UriParameters, AmfArray(pathParams))
    if (finalParams.cookie.nonEmpty) request.fields.setWithoutId(RequestModel.CookieParameters, AmfArray(finalParams.cookie))
  }

  private def checkDuplicatedParameters(endPointParams: Parameters, requestParams: Parameters)(implicit
      errorHandler: AMFErrorHandler
  ): Unit = {
    val duplicates = endPointParams.findDuplicatesIn(requestParams)
    duplicates.foreach({ parameter =>
      {
        addWarningForDuplicateParameter(parameter)
      }
    })
  }

  private def addWarningForDuplicateParameter(param: Parameter)(implicit errorHandler: AMFErrorHandler): Unit = {
    errorHandler.warning(
      DuplicatedParameterWarning,
      param,
      None,
      s"An operation's parameter with the same name: ${param.name.value()} and binding: ${param.binding.value()} as one from the endpoint was found",
      param.position(),
      param.location()
    )
  }
}

class OpenApiParametersNormalizationStage extends ParametersNormalizationStage(Oas20Profile) {

  /** In OpenAPI we just push the endpoint parameters to the operation level, overwriting the any endpoint parameter
    * with the new definition at the operation level
    *
    * @param api
    *   WebApi in
    * @return
    *   api WebApi out
    */
  override protected def transform(api: Api)(implicit errorHandler: AMFErrorHandler): Api = {
    // collect endpoint path parameters
    api.endPoints.foreach { endpoint =>
      val finalParams = Parameters.classified(endpoint.path.value(), endpoint.parameters)
      // collect operation query parameters
      if (finalParams.nonEmpty && endpoint.operations.nonEmpty) {
        endpoint.fields.removeField(EndPointModel.Parameters)
        pushParamsToEndpointOperations(endpoint, finalParams)
      }
    }
    api
  }
}

class AmfParametersNormalizationStage extends ParametersNormalizationStage(AmfProfile) {

  /** In AMF we push all the parameters at the operation level. Parameter references should be already resolved in
    * previous steps.
    *
    * @param api
    *   BaseApi in
    * @return
    *   api BaseApi out
    */
  override protected def transform(api: Api)(implicit errorHandler: AMFErrorHandler): Api = {
    // collect endpoint path parameters
    api.endPoints.foreach { endpoint =>
      val finalParams = Parameters(path = removeParamsFromMadeUpServer(api))
        .merge(Parameters.classified(endpoint.path.value(), endpoint.parameters))
      endpoint.fields.removeField(EndPointModel.Parameters)
      // collect operation query parameters
      if (finalParams.nonEmpty) pushParamsToEndpointOperations(endpoint, finalParams)
    }
    api
  }

  private def removeParamsFromMadeUpServer(api: Api): Seq[Parameter] = {
    val server = api.servers.find(_.annotations.find(classOf[SynthesizedField]).isDefined)

    server
      .map { s =>
        val vars = s.variables
        s.fields.removeField(ServerModel.Variables)
        vars
      }
      .getOrElse(Nil)
  }
}

class Raml10ParametersNormalizationStage extends ParametersNormalizationStage(AmfProfile) {

  /** In RAML we assign the parameters at the right level according to the RAML spec:
    *   - webapi for baseURI parameters
    *   - endpoint for the path parameters
    *   - operation for the query, path and header parameters Since parameters can be at any level due to the source of
    *     the model being an OpenAPI document
    *
    * @param baseApi
    *   Api in
    * @return
    *   baseApi Api out
    */
  override protected def transform(baseApi: Api)(implicit errorHandler: AMFErrorHandler): Api = {
    // collect endpoint path parameters
    baseApi.endPoints.foreach { endpoint =>
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
    baseApi
  }
}
