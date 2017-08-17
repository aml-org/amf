package amf.emit

import amf.builder._
import amf.common.AMFToken.{Comment, Entry, Root, StringToken}
import amf.common.{AMFAST, AMFASTNode}
import amf.document.{BaseUnit, Document}
import amf.domain.Annotation._
import amf.domain._
import amf.graph.GraphEmitter
import amf.metadata.domain.{EndPointModel, OperationModel, WebApiModel}
import amf.parser.Range.NONE
import amf.remote.{Amf, Oas, Raml, Vendor}
import amf.spec.raml.RamlSpecEmitter
import amf.spec.{FieldEmitter, Spec}

import scala.collection.mutable.ListBuffer

/**
  * AMF Unit Maker
  */
class AMFUnitMaker {

  def make(unit: BaseUnit, vendor: Vendor): AMFAST = {
    vendor match {
      case Amf        => makeAmfWebApi(unit)
      case Raml | Oas => makeUnitWithSpec(unit, vendor)
    }
  }

  private def makeUnitWithSpec(unit: BaseUnit, vendor: Vendor): AMFAST = {
    RamlSpecEmitter(unit).emitWebApi()
//    unit match {
//      case document: Document => makeWebApiWithSpec(document.encodes, vendor)
//    }
  }

  private def makeWebApiWithSpec(api: WebApi, vendor: Vendor): AMFAST = {
    vendor match {
//      case Raml => makeRamlWebApi(api)
//      case Oas  => makeOasWebApi(api)
      case _ => throw new IllegalStateException("Invalid vendor " + vendor)
    }
  }

  private def makeAmfWebApi(unit: BaseUnit): AMFAST = GraphEmitter.emit(unit)

  /*private def fixBindingInParameters(parameters: List[Parameter],
                                     bind: String,
                                     fn: Option[(Parameter => Boolean)]): List[Parameter] = {

    var filtered = new ListBuffer[Parameter]
    if (fn.isDefined)
      filtered ++= parameters.filter(fn.get)
    else
      filtered ++= parameters

    parameters.map(p => { p.toBuilder.withBinding(bind) })

  }

  private def makeOasWebApi(api: WebApi) = {
    val apiBuilder = api.toBuilder

    if (api.endPoints != null && api.endPoints != Nil)
      apiBuilder.set(WebApiModel.EndPoints,
                     processEndpoints(api.endPoints, oasProcessEndpointFn),
                     api.fields.getValue(WebApiModel.EndPoints).annotations)

    val ast = Spec(Oas).emitter.emit(apiBuilder.build.fields).build
    new AMFASTNode(Root, "", null, List(new AMFASTNode(ast.`type`, "", null, swaggerEntry() +: ast.children)))
  }

  private def makeRamlWebApi(api: WebApi) = { //TODO:Unify in one make (change up)?
    val apiBuilder = api.toBuilder

    val baseUriParameters = api.baseUriParameters.filter(bup =>
      api.fields.getAnnotationForValue(WebApiModel.BaseUriParameters, bup, classOf[UriParameters]).isDefined)
    apiBuilder.withBaseUriParameters(baseUriParameters)

    if (api.endPoints != null && api.endPoints != Nil)
      // old -> new
      apiBuilder.set(WebApiModel.EndPoints,
                     processEndpoints(api.endPoints, ramlProcessEndPointFn),
                     api.fields.getValue(WebApiModel.EndPoints).annotations)

    val ast = Spec(Raml).emitter.emit(apiBuilder.build.fields).build
    new AMFASTNode(Root, "", null, new AMFASTNode(Comment, "%RAML 1.0", null) +: List(ast))
  }

  private def processEndpoints(endPoints: Seq[EndPoint], processFunction: (EndPoint => EndPoint)): List[EndPoint] = {
    // old -> new
    endPoints
      .map(e => {
        processFunction(e)
      })
      .toList
  }

  val ramlProcessEndPointFn: (EndPoint) => EndPoint = (e) => {
    val uriParameters = e.parameters.filter(p =>
      e.fields.getAnnotationForValue(EndPointModel.Parameters, p, classOf[UriParameters]).isDefined)
    val headers         = e.parameters.filter(_.isHeader)
    val queryParameters = e.parameters.filter(_.isQuery)

    val builders: Seq[Builder] = e.operations
      .map(o => {
        val builder = o.toBuilder
        if (o.request != null) {
          val parameters = o.request.queryParameters
          val reqHeaders = o.request.headers
          val requestBuilded = o.request.toBuilder
            .withQueryParameters(
              parameters ++
                queryParameters.filter(q => !parameters.map(_.name).contains(q.name))
            )
            .withHeaders(
              reqHeaders ++
                headers.filter(h => !reqHeaders.map(_.name).contains(h.name))
            )
            .build
          builder.set(OperationModel.Request, requestBuilded, o.fields.getValue(OperationModel.Request).annotations)

        }
        builder
      })

    e.toBuilder.withParameters(uriParameters).withOperations(builders.map(_.build.asInstanceOf[Operation])).build

  }

  val oasProcessEndpointFn: (EndPoint) => EndPoint = e => {

    val pBuilders = fixBindingInParameters(e.parameters.toList, "path", Some({ p: Parameter =>
      e.fields.getAnnotationForValue(EndPointModel.Parameters, p, classOf[UriParameters]).isDefined
    }))

    val overridedEndpoints = new ListBuffer[OverrideEndPointBodyParameter]

    val operatioBuilders: Seq[OperationBuilder] = e.operations.map(o => {
      val builder = o.toBuilder.asInstanceOf[OperationBuilder]
      if (o.request != null) {
        val annotations = o.request.payloads
          .filter(p => p.annotations.exists(a => a.isInstanceOf[OperationBodyParameter]))
          .map(p => {
            (p.annotations.find(a => a.isInstanceOf[OperationBodyParameter]), p.mediaType)
          })
        val opBodyParameter = annotations match {
          case head :: tail if head._1.isDefined =>
            val parameter = head._1.get.asInstanceOf[OperationBodyParameter].asParameter
            List(ParameterBuilder(parameter.fields, MediaType(head._2) :: parameter.annotations).build)
          case _ => Nil
        }
        //Only can have one parameter with binding body in endpoint.
        overridedEndpoints ++= o.request.payloads.flatMap(p => {
          p.annotations
            .filter(a => a.isInstanceOf[OverrideEndPointBodyParameter])
            .map(_.asInstanceOf[OverrideEndPointBodyParameter])
        })

        overridedEndpoints ++= o.request.payloads
          .filter(p => p.annotations.exists(a => a.isInstanceOf[EndPointBodyParameter]))
          .map(p => {
            val endpointAnn = p.annotations.find(a => a.isInstanceOf[EndPointBodyParameter]).get
            OverrideEndPointBodyParameter(endpointAnn.asInstanceOf[EndPointBodyParameter].asParameter, p)
          })

        //default parameter coming from raml

        val defaultP = FieldEmitter.defaultPayload(
          o.request.payloads
            .filter(p =>
              !p.annotations.exists(a =>
                a.isInstanceOf[OperationBodyParameter] || a.isInstanceOf[EndPointBodyParameter])))
        val defaultPayloadParameterB = defaultP.map(p => {
          ParameterBuilder(List(MediaType(p.mediaType)))
            .withBinding("body")
            .withSchema(p.schema)
        })

        builder.withRequest(
          o.request.toBuilder
            .withHeaders(Nil)
            .withQueryParameters(
              fixBindingInParameters(o.request.queryParameters.toList, "query", None).map(_.build)
                ++ fixBindingInParameters(o.request.headers.toList, "header", None).map(_.build) ++ opBodyParameter
                ++ defaultPayloadParameterB.map(_.build))
            .withPayloads(o.request.payloads.filter(p =>
              !p.annotations.exists(a =>
                a.isInstanceOf[OperationBodyParameter] || a.isInstanceOf[EndPointBodyParameter])))
            .build
        )
      }
      builder
    })

    val payloadEndpointParameter = overridedEndpoints.toList match {
      case head :: tail =>
        List(
          ParameterBuilder(head.asParameter.fields,
                           MediaType(head.asPayload.mediaType) :: head.asParameter.annotations).build)
      case _ => Nil
    }
    e.toBuilder
      .withParameters(pBuilders.map(_.build) ++ payloadEndpointParameter)
      .withOperations(operatioBuilders.map(_.build))
      .build
  }

  private def swaggerEntry(): AMFAST = {
    new AMFASTNode(Entry,
                   "",
                   NONE,
                   List(
                     new AMFASTNode(StringToken, "swagger", NONE),
                     new AMFASTNode(StringToken, "2.0", NONE)
                   ))
  }*/
}

object AMFUnitMaker {
  def apply(unit: BaseUnit, vendor: Vendor): AMFAST = new AMFUnitMaker().make(unit, vendor)
}
