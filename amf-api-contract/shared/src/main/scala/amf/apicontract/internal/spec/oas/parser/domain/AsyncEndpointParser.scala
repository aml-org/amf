package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.apicontract.internal.metamodel.domain.EndPointModel
import amf.apicontract.internal.spec.async.parser.bindings.AsyncChannelBindingsParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.async.parser.domain.AsyncParametersParser
import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.internal.spec.common.parser.{AnnotationParser, YMapEntryLike}
import org.yaml.model.{YMap, YMapEntry}

import scala.collection.mutable

case class AsyncEndpointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint])(
    override implicit val ctx: AsyncWebApiContext
) extends OasLikeEndpointParser(entry, parentId, collector) {

  override type ConcreteContext = AsyncWebApiContext

  override def apply(entry: YMapEntry, parentId: String, collector: List[EndPoint])(
      ctx: ConcreteContext
  ): AsyncEndpointParser = {
    AsyncEndpointParser(entry, parentId, collector)(ctx)
  }

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
