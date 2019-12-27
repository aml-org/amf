package amf.plugins.document.webapi.parser.spec.domain

import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, _}
import amf.core.utils.{IdCounter, TemplateUri, _}
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.parser.spec
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.domain.webapi.metamodel.{EndPointModel, OperationModel}
import amf.plugins.domain.webapi.models.{EndPoint, Operation, Parameter}
import amf.validations.ParserSideValidations.{DuplicatedEndpointPath, InvalidEndpointPath, InvalidEndpointType}
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

abstract class OasLikeEndpointParser(entry: YMapEntry,
                                     producer: String => EndPoint,
                                     collector: mutable.ListBuffer[EndPoint])(implicit val ctx: OasLikeWebApiContext)
    extends SpecParserOps {

  def parse(): Unit = {
    val path = entry.key.as[YScalar].text

    val endpoint = producer(path).add(Annotations(entry))

    checkBalancedParams(path, entry.value, endpoint.id, EndPointModel.Path.value.iri(), ctx)
    endpoint.set(EndPointModel.Path, AmfScalar(path, Annotations(entry.key)))

    if (!TemplateUri.isValid(path))
      ctx.eh.violation(InvalidEndpointPath, endpoint.id, TemplateUri.invalidMsg(path), entry.value)

    if (collector.exists(other => other.path.option() exists (identicalPaths(_, path))))
      ctx.eh.violation(DuplicatedEndpointPath, endpoint.id, "Duplicated resource path " + path, entry)
    else parseEndpoint(endpoint)
  }

  /**
    * Verify if two paths are identical.
    */
  protected def identicalPaths(first: String, second: String): Boolean = first == second

  private def parseEndpoint(endpoint: EndPoint) =
    ctx.link(entry.value) match {
      case Left(value) =>
        ctx.obtainRemoteYNode(value).orElse(ctx.declarations.asts.get(value)) match {
          case Some(map) if map.tagType == YType.Map => collector += parseEndpointMap(endpoint, map.as[YMap])
          case Some(n) =>
            ctx.eh.violation(InvalidEndpointType, endpoint.id, "Invalid node for path item", n)
          case None =>
            ctx.eh.violation(InvalidEndpointPath,
                             endpoint.id,
                             s"Cannot find fragment path item ref $value",
                             entry.value)
        }
      case Right(node) if node.tagType == YType.Map => collector += parseEndpointMap(endpoint, node.as[YMap])
      case _                                        => collector += endpoint
    }

  protected def parseEndpointMap(endpoint: EndPoint, map: YMap): EndPoint = {
    ctx.closedShape(endpoint.id, map, "pathItem")

    map.key("description".asOasExtension, EndPointModel.Description in endpoint)

    // TODO ASYNC parameter parser missing here. Is the same that OAS? Then need to extract to OasLikeParameter parser

    AnnotationParser(endpoint, map).parse()

    endpoint

  }
}

abstract class OasEndpointParser(entry: YMapEntry,
                                 producer: String => EndPoint,
                                 collector: mutable.ListBuffer[EndPoint])(override implicit val ctx: OasWebApiContext)
    extends OasLikeEndpointParser(entry, producer, collector) {

  override protected def parseEndpointMap(endpoint: EndPoint, map: YMap): EndPoint = {
    super.parseEndpointMap(endpoint, map)

    // PARAM PARSER

    var parameters = Parameters()
    val entries    = ListBuffer[YMapEntry]()
    // This are the rest of the parameters, this must be simple to be supported by OAS.
    map
      .key("parameters")
      .foreach { entry =>
        entries += entry
        parameters = parameters.add(OasParametersParser(entry.value.as[Seq[YNode]], endpoint.id).parse())
      }
    // This is because there may be complex path parameters coming from RAML1
    map.key("uriParameters".asOasExtension).foreach { entry =>
      entries += entry
      val uriParameters =
        RamlParametersParser(entry.value.as[YMap], (p: Parameter) => p.adopted(endpoint.id))(spec.toRaml(ctx))
          .parse()
          .map(_.withBinding("path"))
      parameters = parameters.add(Parameters(path = uriParameters))
    }
    parameters match {
      case Parameters(query, path, header, cookie, _, _)
          if query.nonEmpty || path.nonEmpty || header.nonEmpty || cookie.nonEmpty =>
        endpoint.set(EndPointModel.Parameters,
                     AmfArray(query ++ path ++ header ++ cookie, Annotations(entries.head.value)),
                     Annotations(entries.head))
      case _ =>
    }
    if (parameters.body.nonEmpty)
      endpoint.set(EndPointModel.Payloads, AmfArray(parameters.body), Annotations(entries.head))

    // PARAM PARSER

    map.key("displayName".asOasExtension, EndPointModel.Name in endpoint)

    map.key("is".asOasExtension,
            (EndPointModel.Extends in endpoint using ParametrizedDeclarationParser
              .parse(endpoint.withTrait)).allowingSingleValue)

    map.key(
      "type".asOasExtension,
      entry =>
        ParametrizedDeclarationParser(entry.value,
                                      endpoint.withResourceType,
                                      ctx.declarations.findResourceTypeOrError(entry.value))
          .parse()
    )

    ctx.factory.serversParser(map, endpoint).parse()

    map.key(
      "security".asOasExtension,
      entry => {
        // TODO check for empty array for resolution ?
        val idCounter = new IdCounter()
        val securedBy = entry.value
          .as[Seq[YNode]]
          .map(s => OasSecurityRequirementParser(s, endpoint.withSecurity, idCounter).parse())
          .collect { case Some(s) => s }

        if (securedBy.nonEmpty)
          endpoint.set(OperationModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.regex(
      "get|patch|put|post|delete|options|head|connect|trace",
      entries => {
        val operations = mutable.ListBuffer[Operation]()
        entries.foreach { entry =>
          val operationParser = ctx.factory.operationParser(entry, endpoint.withOperation)
          operations += operationParser.parse()
        }
        endpoint.set(EndPointModel.Operations, AmfArray(operations))
      }
    )

    endpoint
  }
}

case class Oas20EndpointParser(entry: YMapEntry,
                               producer: String => EndPoint,
                               collector: mutable.ListBuffer[EndPoint])(override implicit val ctx: OasWebApiContext)
    extends OasEndpointParser(entry, producer, collector) {

  override protected def parseEndpointMap(endpoint: EndPoint, map: YMap): EndPoint = {
    super.parseEndpointMap(endpoint, map)
  }

}

case class Oas30EndpointParser(entry: YMapEntry,
                               producer: String => EndPoint,
                               collector: mutable.ListBuffer[EndPoint])(override implicit val ctx: OasWebApiContext)
    extends OasEndpointParser(entry, producer, collector) {

  /**
    * Verify if two paths are identical.
    * In the case of OAS 3.0, paths with the same hierarchy but different templated names are considered identical.
    */
  override protected def identicalPaths(first: String, second: String): Boolean = {
    def stripPathParams(s: String): String = {
      val trimmed = if (s.endsWith("/")) s.init else s
      trimmed.replaceAll("\\{.*?\\}", "")
    }
    stripPathParams(first) == stripPathParams(second)
  }

  override protected def parseEndpointMap(endpoint: EndPoint, map: YMap): EndPoint = {
    super.parseEndpointMap(endpoint, map)
    map.key("summary", EndPointModel.Summary in endpoint)
    map.key("description", EndPointModel.Description in endpoint)
    endpoint
  }
}

case class AsyncEndpointParser(entry: YMapEntry,
                               producer: String => EndPoint,
                               collector: mutable.ListBuffer[EndPoint])(override implicit val ctx: AsyncWebApiContext)
    extends OasLikeEndpointParser(entry, producer, collector) {

  override protected def parseEndpointMap(endpoint: EndPoint, map: YMap): EndPoint = {

    super.parseEndpointMap(endpoint, map)

    // TODO ASYNC add binding parser
    //    map.key("bindings", )

    map.regex(
      "suscribe|publish",
      entries => {
        val operations = mutable.ListBuffer[Operation]()
        entries.foreach { entry =>
          val operationParser = ctx.factory.operationParser(entry, endpoint.withOperation)
          operations += operationParser.parse()
        }
        endpoint.set(EndPointModel.Operations, AmfArray(operations))
      }
    )

    endpoint
  }
}
