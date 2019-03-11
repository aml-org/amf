package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, _}
import amf.core.parser.KnownContextVariables.RESOURCE_TYPE_CONTEXT
import amf.core.utils.{Strings, TemplateUri}
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.contexts.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.domain.webapi.annotations.ParentEndPoint
import amf.plugins.domain.webapi.metamodel.EndPointModel
import amf.plugins.domain.webapi.metamodel.EndPointModel._
import amf.plugins.domain.webapi.models.{EndPoint, Operation, Parameter}
import amf.plugins.features.validation.ParserSideValidations.{
  DuplicatedEndpointPath,
  InvalidEndpointPath,
  UnusedBaseUriParameter
}
import org.yaml.model._

import scala.collection.mutable

/**
  *
  */
case class Raml10EndpointParser(entry: YMapEntry,
                                producer: String => EndPoint,
                                parent: Option[EndPoint],
                                collector: mutable.ListBuffer[EndPoint],
                                parseOptionalOperations: Boolean = false)(implicit ctx: RamlWebApiContext)
    extends RamlEndpointParser(entry, producer, parent, collector, parseOptionalOperations) {

  override protected def uriParametersKey: String = "uriParameters"
}

case class Raml08EndpointParser(entry: YMapEntry,
                                producer: String => EndPoint,
                                parent: Option[EndPoint],
                                collector: mutable.ListBuffer[EndPoint],
                                parseOptionalOperations: Boolean = false)(implicit ctx: RamlWebApiContext)
    extends RamlEndpointParser(entry, producer, parent, collector, parseOptionalOperations) {

  override protected def uriParametersKey: String = "uriParameters|baseUriParameters"
}

abstract class RamlEndpointParser(entry: YMapEntry,
                                  producer: String => EndPoint,
                                  parent: Option[EndPoint],
                                  collector: mutable.ListBuffer[EndPoint],
                                  parseOptionalOperations: Boolean = false)(implicit ctx: RamlWebApiContext)
    extends SpecParserOps {

  def parse(): Unit = {

    val path = parsePath()

    val endpoint = producer(path).add(Annotations(entry))
    parent.map(p => endpoint.add(ParentEndPoint(p)))

    checkBalancedParams(path, entry.value, endpoint.id, EndPointModel.Path.value.iri(), ctx)
    endpoint.set(Path, AmfScalar(path, Annotations(entry.key)))

    if (!TemplateUri.isValid(path))
      ctx.violation(InvalidEndpointPath, endpoint.id, TemplateUri.invalidMsg(path), entry.value)

    if (collector.exists(e => e.path.is(path)))
      ctx.violation(DuplicatedEndpointPath, endpoint.id, "Duplicated resource path " + path, entry)
    else {
      entry.value.tagType match {
        case YType.Null => collector += endpoint
        case _ =>
          val map = entry.value.as[YMap]
          parseEndpoint(endpoint, map)
      }
    }
  }

  protected def parseEndpoint(endpoint: EndPoint, map: YMap): Unit = {
    val isResourceType = ctx.variables.get[Boolean](RESOURCE_TYPE_CONTEXT).getOrElse(false)

    if (isResourceType) {
      ctx.closedShape(endpoint.id, map, "resourceType")
    } else {
      ctx.closedShape(endpoint.id, map, "endPoint")
    }

    map.key("displayName", (EndPointModel.Name in endpoint).allowingAnnotations)
    map.key("description", (EndPointModel.Description in endpoint).allowingAnnotations)

    map.key("is",
            (EndPointModel.Extends in endpoint using ParametrizedDeclarationParser
              .parse(endpoint.withTrait)).allowingSingleValue.optional)

    map.key(
      "type",
      entry =>
        ParametrizedDeclarationParser(entry.value,
                                      endpoint.withResourceType,
                                      ctx.declarations.findResourceTypeOrError(entry.value))
          .parse()
    )

    val optionalMethod = if (parseOptionalOperations) "\\??" else ""

    map.regex(
      s"(get|patch|put|post|delete|options|head|connect|trace)$optionalMethod",
      entries => {
        val operations = mutable.ListBuffer[Operation]()
        entries.foreach(entry => {

          operations += ctx.factory
            .operationParser(entry, endpoint.withOperation, parseOptionalOperations)
            .parse()
        })
        endpoint.set(EndPointModel.Operations, AmfArray(operations))
      }
    )

    val SchemeParser = RamlParametrizedSecuritySchemeParser.parse(endpoint.withSecurity) _
    map.key("securedBy", (EndPointModel.Security in endpoint using SchemeParser).allowingSingleValue)

    var parameters               = Parameters()
    var annotations: Annotations = Annotations()

    val entries = map.regex(uriParametersKey)
    val implicitExplicitPathParams = entries.collectFirst({ case e if e.value.tagType == YType.Map => e }) match {
      case None =>
        implicitPathParamsOrdered(endpoint)

      case Some(e) =>
        annotations = Annotations(e.value)

        val explicitParameters =
          RamlParametersParser(e.value.as[YMap], (p: Parameter) => p.adopted(endpoint.id))
            .parse()
            .map(_.withBinding("path"))

        implicitPathParamsOrdered(endpoint,
                                  variable => !explicitParameters.exists(_.name.is(variable)),
                                  explicitParameters)
    }

    parameters = parameters.add(Parameters(path = implicitExplicitPathParams))

    map.key(
      "parameters".asRamlAnnotation,
      entry => {
        parameters =
          parameters.add(OasParametersParser(entry.value.as[Seq[YNode]], endpoint.id)(spec.toOas(ctx)).parse())
        annotations = Annotations(entry.value)
      }
    )

    parameters match {
      case Parameters(query, path, header, _, _) if parameters.nonEmpty =>
        endpoint.set(EndPointModel.Parameters, AmfArray(query ++ path ++ header, annotations), annotations)
      case _ =>
    }

    map.key(
      "payloads".asRamlAnnotation,
      entry => {
        endpoint.set(EndPointModel.Payloads,
                     AmfArray(Seq(Raml10PayloadParser(entry, endpoint.withPayload).parse()), Annotations(entry.value)),
                     Annotations(entry))
      }
    )

    collector += endpoint

    AnnotationParser(endpoint, map).parse()

    map.regex(
      "^/.*",
      entries => {
        entries.foreach(ctx.factory.endPointParser(_, producer, Some(endpoint), collector, false).parse())
      }
    )
  }

  private def implicitPathParamsOrdered(endpoint: EndPoint,
                                        filter: String => Boolean = _ => true,
                                        explicitParams: Seq[Parameter] = Nil): Seq[Parameter] = {
    val parentParams: Map[String, Parameter] = parent
      .map(
        _.parameters
          .filter(_.binding.value() == "path")
          .foldLeft(Map[String, Parameter]()) {
            case (acc, p) =>
              acc.updated(p.name.value(), p)
          }
      )
      .getOrElse(Map())

    val pathParams: Seq[String] = TemplateUri.variables(parsePath())
    val params: Seq[Parameter] = pathParams
      .filter(filter)
      .map { variable =>
        val implicitParam: Parameter = parentParams.get(variable) match {
          case Some(param) =>
            val pathParam = param.cloneParameter(endpoint.id)
            pathParam.annotations += SynthesizedField()
            pathParam
          case None =>
            explicitParams.find(p => p.name.value().equals(variable) && p.binding.value().equals("path")) match {
              case Some(p) => p
              case None =>
                val pathParam = endpoint.withParameter(variable).withBinding("path").withRequired(true)
                pathParam.withScalarSchema(variable).withDataType((Namespace.Xsd + "string").iri())
                pathParam.annotations += SynthesizedField()
                pathParam
            }
        }
        implicitParam
      }
    checkParamsUsage(endpoint, pathParams, explicitParams)
    params ++ explicitParams.filter(!params.contains(_))

  }

  private def checkParamsUsage(endpoint: EndPoint, pathParams: Seq[String], endpointParams: Seq[Parameter]): Unit = {
    endpointParams.foreach { p =>
      if (!p.name.option().exists(n => pathParams.contains(n)))
        ctx.warning(UnusedBaseUriParameter,
                    p.id,
                    None,
                    s"Unused uri parameter ${p.name.value()}",
                    p.position(),
                    p.location())
    }

    endpoint.operations.flatMap(o => Option(o.request)).flatMap(_.uriParameters).foreach { p =>
      if (!p.name.option().exists(n => pathParams.contains(n))) {
        ctx.warning(UnusedBaseUriParameter,
                    p.id,
                    None,
                    s"Unused operation uri parameter ${p.name.value()}",
                    p.position(),
                    p.location())
      }
    }
  }
  protected def parsePath(): String = parent.map(_.path.value()).getOrElse("") + entry.key.as[YScalar].text

  protected def uriParametersKey: String
}
