package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{LexicalInformation, SynthesizedField}
import amf.core.model.DataType
import amf.core.model.domain.{AmfArray, AmfScalar, DataNode, Shape, ScalarNode => ScalarDataNode}
import amf.core.parser.{Annotations, _}
import amf.core.utils.{AmfStrings, IdCounter, TemplateUri}
import amf.plugins.document.webapi.annotations.{EndPointResourceTypeEntry, EndPointTraitEntry}
import amf.plugins.document.webapi.contexts.parser.raml.{
  Raml08WebApiContext,
  Raml10WebApiContext,
  RamlWebApiContext,
  RamlWebApiContextType
}
import amf.plugins.document.webapi.parser.spec
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.vocabulary.VocabularyMappings
import amf.plugins.domain.shapes.models.ScalarShape
import amf.plugins.domain.webapi.annotations.ParentEndPoint
import amf.plugins.domain.webapi.metamodel.EndPointModel._
import amf.plugins.domain.webapi.metamodel.{EndPointModel, ParameterModel}
import amf.plugins.domain.webapi.models.{EndPoint, Operation, Parameter}
import amf.validations.ParserSideValidations.{
  DuplicatedEndpointPath,
  InvalidEndpointPath,
  SlashInUriParameterValues,
  UnusedBaseUriParameter
}
import amf.validations.ResolutionSideValidations.NestedEndpoint
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
      ctx.eh.violation(InvalidEndpointPath, endpoint.id, TemplateUri.invalidMsg(path), entry.value)

    if (collector.exists(e => e.path.is(path)))
      ctx.eh.violation(DuplicatedEndpointPath, endpoint.id, "Duplicated resource path " + path, entry)
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
    val isResourceType = ctx.contextType == RamlWebApiContextType.RESOURCE_TYPE
    ctx.closedShape(endpoint.id, map, if (isResourceType) "resourceType" else "endPoint")

    map.key("displayName", (EndPointModel.Name in endpoint).allowingAnnotations)
    map.key("description", (EndPointModel.Description in endpoint).allowingAnnotations)

    map.key(
      "is",
      (e: YMapEntry) => {
        endpoint.annotations += EndPointTraitEntry(Range(e.range))
        (EndPointModel.Extends in endpoint using ParametrizedDeclarationParser
          .parse(endpoint.withTrait)).allowingSingleValue.optional(e)
      }
    )

    map.key(
      "type",
      entry => {
        endpoint.annotations += EndPointResourceTypeEntry(Range(entry.range))
        ParametrizedDeclarationParser(entry.value,
                                      endpoint.withResourceType,
                                      ctx.declarations.findResourceTypeOrError(entry.value))
          .parse()
      }
    )

    val optionalMethod = if (parseOptionalOperations) "\\??" else ""

    map.regex(
      s"(get|patch|put|post|delete|options|head|connect|trace)$optionalMethod",
      entries => {
        val operations = mutable.ListBuffer[Operation]()
        entries.foreach(entry => {

          val operationContext = ctx match {
            case _: Raml08WebApiContext =>
              new Raml08WebApiContext(ctx.loc,
                                      ctx.refs,
                                      ParserContext(eh = ctx.eh),
                                      Some(ctx.declarations),
                                      ctx.contextType,
                                      ctx.options)
            case _ =>
              new Raml10WebApiContext(ctx.loc,
                                      ctx.refs,
                                      ParserContext(eh = ctx.eh),
                                      Some(ctx.declarations),
                                      ctx.contextType,
                                      ctx.options)
          }
          operationContext.nodeRefIds ++= ctx.nodeRefIds
          val operation = RamlOperationParser(entry, endpoint.withOperation, parseOptionalOperations)(operationContext)
            .parse()
          operations += operation
          ctx.operationContexts.put(operation.id.stripSuffix("%3F"), operationContext)
        })
        endpoint.set(EndPointModel.Operations, AmfArray(operations))
      }
    )

    val idCounter         = new IdCounter()
    val RequirementParser = RamlSecurityRequirementParser.parse(endpoint.withSecurity, idCounter) _
    map.key("securedBy", (EndPointModel.Security in endpoint using RequirementParser).allowingSingleValue)

    var parameters               = Parameters()
    var annotations: Annotations = Annotations()

    val entries = map.regex(uriParametersKey)
    val implicitExplicitPathParams = entries.collectFirst({ case e if e.value.tagType == YType.Map => e }) match {
      case None =>
        implicitPathParamsOrdered(endpoint, isResourceType)

      case Some(e) =>
        annotations = Annotations(e.value)

        val explicitParameters = e.value
          .as[YMap]
          .entries
          .map(entry => {
            val param = ctx.factory
              .parameterParser(entry, (p: Parameter) => p.adopted(endpoint.id), false)
              .parse()
              .withBinding("path")
            param.fields ? [Shape] ParameterModel.Schema foreach (schema => validateSlashsInSchema(schema, entry))
            param
          })

        implicitPathParamsOrdered(endpoint,
                                  isResourceType,
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
      case Parameters(query, path, header, _, _, _) if parameters.nonEmpty =>
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

    AnnotationParser(endpoint,
                     map,
                     if (isResourceType) List(VocabularyMappings.resourceType)
                     else List(VocabularyMappings.endpoint)).parse()

    val nestedEndpointRegex = "^/.*"
    map.regex(
      nestedEndpointRegex,
      entries => {
        if (isResourceType) {
          entries.foreach { entry =>
            val nestedEndpointName = entry.key.toString()
            ctx.eh.violation(
              NestedEndpoint,
              endpoint.id.stripSuffix("/applied"),
              None,
              s"Nested endpoint in resourceType: '$nestedEndpointName'",
              Some(LexicalInformation(Range(entry.key.range))),
              Some(map.sourceName)
            )
          }
        } else {
          entries.foreach(ctx.factory.endPointParser(_, producer, Some(endpoint), collector, false).parse())
        }
      }
    )
  }

  private def validateSlashsInSchema(shape: Shape, entry: YMapEntry): Unit = {
    //default values
    Option(shape.default).foreach(validateSlashInDataNode(_, entry))
    //enum values
    shape.values.foreach(value => validateSlashInDataNode(value, entry))
    //example values
    shape match {
      case scalar: ScalarShape =>
        scalar.examples.foreach(example => Option(example.structuredValue).foreach(validateSlashInDataNode(_, entry)))
      case _ =>
    }
  }

  private def validateSlashInDataNode(node: DataNode, entry: YMapEntry): Unit = node match {
    case scalar: ScalarDataNode if scalar.value.option().exists(_.contains('/')) =>
      ctx.eh.violation(SlashInUriParameterValues,
                       node.id,
                       s"Value '${scalar.value.value()}' of uri parameter must not contain '/' character",
                       entry.value)
    case _ =>
  }

  def generateParam(endpoint: EndPoint, variable: String): Option[Parameter] = {
    val operationsDefineParam: Boolean =
      endpoint.operations.nonEmpty && endpoint.operations.forall { op =>
        Option(op.request).exists(_.uriParameters.exists(_.parameterName.option().contains(variable)))
      }

    if (operationsDefineParam) None
    else {
      val pathParam = endpoint.withParameter(variable).withBinding("path").withRequired(true)
      pathParam.withScalarSchema(variable).withDataType(DataType.String)
      pathParam.annotations += SynthesizedField()
      Some(pathParam)
    }
  }

  private def implicitPathParamsOrdered(endpoint: EndPoint,
                                        isResourceType: Boolean,
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
      .flatMap { variable =>
        parentParams.get(variable) match {
          case Some(param) =>
            val pathParam = param.cloneParameter(endpoint.id)
            pathParam.annotations += SynthesizedField()
            Some(pathParam)
          case None =>
            explicitParams.find(p => p.name.value().equals(variable) && p.binding.value().equals("path")) match {
              case Some(p) => Some(p)
              case None    => generateParam(endpoint, variable)
            }
        }
      }
    if (!isResourceType) {
      checkParamsUsage(endpoint, pathParams, explicitParams)
    }
    params ++ explicitParams.filter(!params.contains(_))

  }

  private def checkParamsUsage(endpoint: EndPoint, pathParams: Seq[String], endpointParams: Seq[Parameter]): Unit = {
    endpointParams.foreach { p =>
      if (!p.name.option().exists(n => pathParams.contains(n)))
        ctx.eh.warning(UnusedBaseUriParameter,
                       p.id,
                       None,
                       s"Unused uri parameter ${p.name.value()}",
                       p.position(),
                       p.location())
    }

    endpoint.operations.flatMap(o => Option(o.request)).flatMap(_.uriParameters).foreach { p =>
      if (!p.name.option().exists(n => pathParams.contains(n))) {
        ctx.eh.warning(UnusedBaseUriParameter,
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
