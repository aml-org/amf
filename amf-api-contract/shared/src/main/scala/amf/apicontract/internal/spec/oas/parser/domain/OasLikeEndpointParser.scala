package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.security.SecurityRequirement
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation, Parameter}
import amf.apicontract.internal.metamodel.domain.{EndPointModel, OperationModel}
import amf.apicontract.internal.spec.async.parser.bindings.AsyncChannelBindingsParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.async.parser.domain.AsyncParametersParser
import amf.apicontract.internal.spec.common.Parameters
import amf.apicontract.internal.spec.common.parser._
import amf.apicontract.internal.spec.oas.parser.context.{OasLikeWebApiContext, OasWebApiContext, RemoteNodeNavigation}
import amf.apicontract.internal.spec.raml.parser.domain.ParametrizedDeclarationParser
import amf.apicontract.internal.spec.spec.toRaml
import amf.apicontract.internal.validation.definitions.ParserSideValidations.{DuplicatedEndpointPath, InvalidEndpointPath, InvalidEndpointType}
import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.core.internal.utils.{AmfStrings, IdCounter, TemplateUri}
import amf.shapes.internal.spec.common.parser.{AnnotationParser, YMapEntryLike}
import org.yaml.model._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

abstract class OasLikeEndpointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint])(
    implicit val ctx: OasLikeWebApiContext)
    extends SpecParserOps {

  type ConcreteContext <: OasLikeWebApiContext
  def apply(entry: YMapEntry, parentId: String, collector: List[EndPoint])(ctx: ConcreteContext): OasLikeEndpointParser

  def parse(): Option[EndPoint] = {
    val path     = ScalarNode(entry.key).text()
    val pathText = path.toString
    val endpoint = EndPoint(Annotations(entry)).setWithoutId(EndPointModel.Path, path, Annotations.inferred())

    checkBalancedParams(pathText, entry.value, endpoint, EndPointModel.Path.value.iri(), ctx)

    if (!TemplateUri.isValid(pathText))
      ctx.eh.violation(InvalidEndpointPath, endpoint, TemplateUri.invalidMsg(pathText), entry.value.location)

    val duplicated = collector.find(other => other.path.option() exists (identicalPaths(_, pathText)))
    duplicated match {
      case Some(other) =>
        ctx.eh.violation(DuplicatedEndpointPath, other, "Duplicated resource path " + pathText, entry.location)
        None
      case None =>
        parseEndpoint(endpoint)
    }
  }

  /**
    * Verify if two paths are identical.
    */
  protected def identicalPaths(first: String, second: String): Boolean = first == second

  private def parseEndpoint(endpoint: EndPoint): Option[EndPoint] =
    ctx.link(entry.value) match {
      case Left(value) =>
        ctx.navigateToRemoteYNode(value) match {
          case Some(result) if isMap(result.remoteNode) =>
            val node = result.remoteNode.as[YMap]
            Some(buildNewParser(result).parseEndpointMap(endpoint, node))
          case Some(n) =>
            invalidNodeViolation(endpoint, n.remoteNode)
            None
          case None => getNodeFromDeclarations(endpoint, value)
        }
      case Right(node) => Some(parseEndpointMap(endpoint, node.as[YMap]))
    }

  private def getNodeFromDeclarations(endpoint: EndPoint, value: String) = {
    ctx.declarations.asts.get(value) match {
      case Some(node) if isMap(node) => Some(parseEndpointMap(endpoint, node.as[YMap]))
      case Some(notMapNode)              =>
        invalidNodeViolation(endpoint, notMapNode)
        None
      case None =>
        cannotFindReferencedEndPointViolation(endpoint, value)
        None
    }
  }

  private def buildNewParser(result: RemoteNodeNavigation[OasLikeWebApiContext]): OasLikeEndpointParser = {
    val newCtx            = result.context.asInstanceOf[ConcreteContext]
    val unreferencedEntry = YMapEntry(entry.key, result.remoteNode)
    this.apply(unreferencedEntry, parentId, collector)(newCtx)
  }

  private def isMap(node: YNode) = node.tagType == YType.Map

  private def invalidNodeViolation(endpoint: EndPoint, node: YNode): Unit = {
    ctx.eh.violation(InvalidEndpointType, endpoint, "Invalid node for path item", node.location)
  }

  private def cannotFindReferencedEndPointViolation(endpoint: EndPoint, value: String): Unit = {
    ctx.eh.violation(InvalidEndpointPath,
      endpoint,
      s"Cannot find fragment path item ref $value",
      entry.value.location)
  }

  protected def parseEndpointMap(endpoint: EndPoint, map: YMap): EndPoint = {
    ctx.closedShape(endpoint, map, "pathItem")

    map.key("description".asOasExtension, EndPointModel.Description in endpoint)

    // TODO ASYNC parameter parser missing here. Is the same that OAS? Then need to extract to OasLikeParameter parser

    AnnotationParser(endpoint, map)(WebApiShapeParserContextAdapter(ctx)).parse()

    endpoint

  }
}

abstract class OasEndpointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint])(
    override implicit val ctx: OasWebApiContext)
    extends OasLikeEndpointParser(entry, parentId, collector) {

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
        parameters = parameters.add(OasParametersParser(entry.value.as[Seq[YNode]], endpoint.id).parse(true))
      }
    // This is because there may be complex path parameters coming from RAML1
    map.key("uriParameters".asOasExtension).foreach { entry =>
      entries += entry
      val uriParameters =
        RamlParametersParser(entry.value.as[YMap], (p: Parameter) => Unit, binding = "path")(
          toRaml(ctx))
          .parse()
      parameters = parameters.add(Parameters(path = uriParameters))
    }
    parameters match {
      case Parameters(query, path, header, cookie, _, _)
          if query.nonEmpty || path.nonEmpty || header.nonEmpty || cookie.nonEmpty =>
        endpoint.setWithoutId(EndPointModel.Parameters,
                     AmfArray(query ++ path ++ header ++ cookie, Annotations(entries.head.value)),
                     Annotations(entries.head))
      case _ =>
    }
    if (parameters.body.nonEmpty)
      endpoint.setWithoutId(EndPointModel.Payloads, AmfArray(parameters.body), Annotations(entries.head))

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
          .flatMap(s =>
            OasLikeSecurityRequirementParser(s, (se: SecurityRequirement) => Unit, idCounter)
              .parse())

        endpoint.setWithoutId(OperationModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.regex(
      "get|patch|put|post|delete|options|head|connect|trace",
      entries => {
        val operations = mutable.ListBuffer[Operation]()
        entries.foreach { entry =>
          val operationParser = ctx.factory.operationParser(entry, (o: Operation) => o)
          operations += operationParser.parse()
        }
        endpoint.setWithoutId(EndPointModel.Operations, AmfArray(operations, Annotations.inferred()), Annotations.inferred())
      }
    )

    endpoint
  }
}

case class Oas20EndpointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint])(
    override implicit val ctx: OasWebApiContext)
    extends OasEndpointParser(entry, parentId, collector) {

  override type ConcreteContext = OasWebApiContext


  override def apply(entry: YMapEntry,
                     parentId: String,
                     collector: List[EndPoint])(
                      ctx: ConcreteContext): Oas20EndpointParser = {
    Oas20EndpointParser(entry, parentId, collector)(ctx)
  }

  override protected def parseEndpointMap(endpoint: EndPoint, map: YMap): EndPoint = {
    super.parseEndpointMap(endpoint, map)
  }

}

case class Oas30EndpointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint])(
    override implicit val ctx: OasWebApiContext)
    extends OasEndpointParser(entry, parentId, collector) {

  override type ConcreteContext = OasWebApiContext

  override def apply(entry: YMapEntry,
                     parentId: String,
                     collector: List[EndPoint])(
                      ctx: ConcreteContext): Oas30EndpointParser = {
    Oas30EndpointParser(entry, parentId, collector)(ctx)
  }

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

case class AsyncEndpointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint])(
    override implicit val ctx: AsyncWebApiContext)
    extends OasLikeEndpointParser(entry, parentId, collector) {

  override type ConcreteContext = AsyncWebApiContext

  override def apply(entry: YMapEntry,
                     parentId: String,
                     collector: List[EndPoint])(
                      ctx: ConcreteContext): AsyncEndpointParser = {
    AsyncEndpointParser(entry, parentId, collector)(ctx)
  }

  override protected def parseEndpointMap(endpoint: EndPoint, map: YMap): EndPoint = {

    super.parseEndpointMap(endpoint, map)

    map.key("bindings").foreach { entry =>
      val bindings = AsyncChannelBindingsParser(YMapEntryLike(entry.value)).parse()
      endpoint.setWithoutId(EndPointModel.Bindings, bindings, Annotations(entry))

      AnnotationParser(endpoint, map)(WebApiShapeParserContextAdapter(ctx)).parseOrphanNode("bindings")
    }

    map.key("description", EndPointModel.Description in endpoint)
    map.key(
      "parameters",
      entry => {
        val parameters = AsyncParametersParser(endpoint.id, entry.value.as[YMap]).parse()
        endpoint.fields.setWithoutId(
                            EndPointModel.Parameters,
                            AmfArray(parameters, Annotations(entry.value)),
                            Annotations(entry))
      }
    )

    map.regex(
      "subscribe|publish",
      entries => {
        val operations = mutable.ListBuffer[Operation]()
        entries.foreach { entry =>
          val operationParser = ctx.factory.operationParser(entry, (o: Operation) => o)
          operations += operationParser.parse()
        }
        endpoint.setWithoutId(EndPointModel.Operations, AmfArray(operations, Annotations(map)), Annotations(map))
      }
    )

    endpoint
  }
}
