package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.apicontract.internal.metamodel.domain.EndPointModel
import amf.apicontract.internal.spec.common.parser._
import amf.apicontract.internal.spec.oas.parser.context.{OasLikeWebApiContext, RemoteNodeNavigation}
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.apicontract.internal.validation.definitions.ParserSideValidations.{InvalidEndpointPath, InvalidEndpointType}
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.core.internal.utils.{AmfStrings, TemplateUri}
import amf.shapes.internal.spec.common.parser.AnnotationParser
import org.yaml.model._

import scala.collection.mutable

abstract class OasLikeEndpointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint])(implicit
    val ctx: OasLikeWebApiContext
) extends SpecParserOps {

  type ConcreteContext <: OasLikeWebApiContext
  def apply(entry: YMapEntry, parentId: String, collector: List[EndPoint])(ctx: ConcreteContext): OasLikeEndpointParser

  def parse(): Option[EndPoint] = {
    val path     = ScalarNode(entry.key).text()
    val pathText = path.toString
    val endpoint = EndPoint(Annotations(entry)).setWithoutId(EndPointModel.Path, path, Annotations.inferred())

    checkBalancedParams(pathText, entry.value, endpoint, EndPointModel.Path.value.iri(), ctx)

    if (!TemplateUri.isValid(pathText))
      ctx.eh.violation(InvalidEndpointPath, endpoint, TemplateUri.invalidMsg(pathText), entry.value.location)

    parseEndpoint(endpoint)
  }

  protected def parseEndpoint(endpoint: EndPoint): Option[EndPoint] =
    ctx.link(entry.value) match {
      case Left(value) =>
        val endpointName     = OasDefinitions.stripEndpointsDefinitionsPrefix(value)
        val declaredEndpoint = ctx.declarations.channels.get(endpointName)
        if (declaredEndpoint.isDefined) {
          val map         = entry.value.as[YMap]
          val annotations = getAnnotationsFromMap(map, "$ref")
          declaredEndpoint.map { declaredEndpoint =>
            val link: EndPoint =
              ctx.link(declaredEndpoint, map, AmfScalar(endpointName), annotations, Annotations.synthesized())
            link.withPath(endpoint.path.value())
          }
        } else {
          ctx.navigateToRemoteYNode(value) match {
            case Some(result) if isMap(result.remoteNode) =>
              val node = result.remoteNode.as[YMap]
              Some(buildNewParser(result).parseEndpointMap(endpoint, node))
            case Some(n) =>
              invalidNodeViolation(endpoint, n.remoteNode)
              None
            case None => getNodeFromDeclarations(endpoint, value)
          }
        }

      case Right(node) => Some(parseEndpointMap(endpoint, node.as[YMap]))
    }

  private def getNodeFromDeclarations(endpoint: EndPoint, value: String): Option[EndPoint] = {
    ctx.declarations.asts.get(value) match {
      case Some(node) if isMap(node) => Some(parseEndpointMap(endpoint, node.as[YMap]))
      case Some(notMapNode) =>
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
    ctx.eh.violation(InvalidEndpointPath, endpoint, s"Cannot find fragment path item ref $value", entry.value.location)
  }

  protected def parseEndpointMap(endpoint: EndPoint, map: YMap): EndPoint = {
    ctx.closedShape(endpoint, map, "pathItem")

    map.key("description".asOasExtension, EndPointModel.Description in endpoint)

    // TODO ASYNC parameter parser missing here. Is the same that OAS? Then need to extract to OasLikeParameter parser

    AnnotationParser(endpoint, map).parse()

    endpoint
  }

  protected def parseOperations(entries: Iterable[YMapEntry]): Seq[Operation] = {
    val operations = mutable.ListBuffer[Operation]()
    entries.foreach { entry =>
      val operationParser = ctx.factory.operationParser(entry, (o: Operation) => o)
      operations += operationParser.parse()
    }
    operations
  }
}
