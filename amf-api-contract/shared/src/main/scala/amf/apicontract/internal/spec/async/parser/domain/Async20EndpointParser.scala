package amf.apicontract.internal.spec.async.parser.domain

import amf.apicontract.client.scala.model.domain.{EndPoint, Operation, Server}
import amf.apicontract.internal.metamodel.domain.{EndPointModel, ServerModel}
import amf.apicontract.internal.spec.async.parser.bindings.AsyncChannelBindingsParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.WebApiDeclarations.ErrorChannel
import amf.apicontract.internal.spec.oas.parser.domain.OasLikeEndpointParser
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode, SearchScope}
import amf.core.internal.validation.CoreValidations
import amf.shapes.internal.spec.common.parser.{AnnotationParser, YMapEntryLike}
import org.yaml.model.{YMap, YMapEntry, YNode, YSequence}

import scala.collection.mutable

class Async20EndpointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint])(
    override implicit val ctx: AsyncWebApiContext
) extends OasLikeEndpointParser(entry, parentId, collector) {

  override type ConcreteContext = AsyncWebApiContext

  override def apply(entry: YMapEntry, parentId: String, collector: List[EndPoint])(
      ctx: ConcreteContext
  ): Async20EndpointParser = new Async20EndpointParser(entry, parentId, collector)(ctx)

  override protected def parseEndpointMap(endpoint: EndPoint, map: YMap): EndPoint = {

    super.parseEndpointMap(endpoint, map)

    map.key("bindings").foreach { entry =>
      val bindings = AsyncChannelBindingsParser(YMapEntryLike(entry.value)).parse()
      endpoint.setWithoutId(EndPointModel.Bindings, bindings, Annotations(entry))

      AnnotationParser(endpoint, map).parseOrphanNode("bindings")
    }

    map.key("description", EndPointModel.Description in endpoint)
    map.key(
      "parameters",
      entry => {
        val parameters = AsyncParametersParser(endpoint.id, entry.value.as[YMap]).parse()
        endpoint.fields
          .setWithoutId(EndPointModel.Parameters, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))
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

class Async22EndpointParser(
    entry: YMapEntry,
    parentId: String,
    collector: List[EndPoint]
)(
    override implicit val ctx: AsyncWebApiContext
) extends Async20EndpointParser(entry, parentId, collector) {

  override protected def parseEndpointMap(endpoint: EndPoint, map: YMap): EndPoint = {
    super.parseEndpointMap(endpoint, map)

    map.key(
      "servers",
      entry => {
        val nodes = entry.value.value.asInstanceOf[YSequence].nodes
        val servers = nodes.map { n =>
          val server = Server()
          server.setWithoutId(
            ServerModel.Name,
            AmfScalar(n.toString, Annotations(n.value)),
            Annotations(n)
          )
        }
        endpoint.setWithoutId(
          EndPointModel.Servers,
          AmfArray(servers, Annotations(entry.value)),
          Annotations(entry)
        )
      }
    )

    endpoint
  }
}

class Async23EndpointParser(
    entry: YMapEntry,
    parentId: String,
    collector: List[EndPoint]
)(
    override implicit val ctx: AsyncWebApiContext
) extends Async22EndpointParser(entry, parentId, collector) {

  override protected def parseEndpoint(endpoint: EndPoint): Option[EndPoint] = {
    Some(
      parseEndpointMap(endpoint, entry.value.as[YMap])
    ) // this is to avoid general ref detection of OasLikeParser. I want to use custom link detection for declarations for Async 2.3
  }

  override protected def parseEndpointMap(endpoint: EndPoint, map: YMap): EndPoint = {
    ctx.link(map) match {
      case Left(fullRef) => handleRef(fullRef, map, endpoint)
      case Right(_)      => super.parseEndpointMap(endpoint, map)
    }
  }

  private def handleRef(fullRef: String, map: YMap, endpoint: EndPoint): EndPoint = {
    val entryLike = YMapEntryLike(map)
    val label     = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "channels")
    ctx.declarations
      .findChannel(label, SearchScope.Named)
      .map(channel => {
        nameAndAdopt(generateLink(label, channel, entryLike), entryLike.key).withPath(endpoint.path.value())
      })
      .getOrElse(remote(fullRef, entryLike, endpoint))
  }

  private def remote(fullRef: String, entryLike: YMapEntryLike, endpoint: EndPoint)(implicit
      ctx: AsyncWebApiContext
  ): EndPoint = {
    ctx.navigateToRemoteYNode(fullRef) match {
      case Some(result) =>
        val serverNode = result.remoteNode
        val external   = parseEndpointMap(endpoint, serverNode.as[YMap])
        nameAndAdopt(
          external.link(AmfScalar(fullRef), entryLike.annotations, Annotations.synthesized()),
          entryLike.key
        ) // check if this link should be trimmed to just the label
      case None =>
        ctx.eh.violation(
          CoreValidations.UnresolvedReference,
          "",
          s"Cannot find link reference $fullRef",
          entryLike.asMap.location
        )
        val errorChannel = ErrorChannel(fullRef, entryLike.asMap)
        nameAndAdopt(errorChannel.link(fullRef, errorChannel.annotations), entryLike.key)
    }
  }

  private def generateLink(label: String, effectiveTarget: EndPoint, entryLike: YMapEntryLike): EndPoint = {
    val endPoint = EndPoint(entryLike.annotations)
    val hash     = s"${endPoint.id}$label".hashCode
    endPoint
      .withId(s"${endPoint.id}/link-$hash")
      .withLinkTarget(effectiveTarget)
      .withLinkLabel(label, Annotations(entryLike.value))
  }

  def nameAndAdopt(s: EndPoint, key: Option[YNode]): EndPoint = {
    key foreach { k =>
      s.setWithoutId(EndPointModel.Name, ScalarNode(k).string(), Annotations(k))
    }
    s
  }
}
