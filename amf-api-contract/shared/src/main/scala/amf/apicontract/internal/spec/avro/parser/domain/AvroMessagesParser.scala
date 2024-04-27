package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.apicontract.internal.metamodel.domain.{EndPointModel, OperationModel}
import amf.apicontract.internal.spec.avro.parser.context.AvroWebAPIContext
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry, YScalar}
import amf.core.internal.parser.{Root, YMapOps, YScalarYRead}
import amf.shapes.internal.spec.common.parser.QuickFieldParserOps
import amf.apicontract.client.scala.model.domain.{Payload, Request, Response}
import amf.core.internal.remote.Mimes

class AvroMessagesParser(map: YMap)(implicit ctx: AvroWebAPIContext) {

  def parse(): List[EndPoint] = map.entries.map(AvroMessageParser(_).parse()).toList

}

case class AvroMessageParser(entry: YMapEntry)(implicit ctx: AvroWebAPIContext) extends QuickFieldParserOps {
  val endPoint: EndPoint   = EndPoint()
  val operation: Operation = endPoint.withOperation("post") // TODO: check operation

  def parse() = {
    val nameNode = entry.key
    val path     = nameNode.as[YScalar].text
    endPoint.withName(nameNode)
    endPoint.set(EndPointModel.Path, path, Annotations(nameNode))
    parseMap(entry.value.as[YMap])
    endPoint
  }

  def parseMap(map: YMap) = {
    map.key(
      "doc", {
        (EndPointModel.Description in endPoint).allowingAnnotations
        (OperationModel.Description in operation).allowingAnnotations
      }
    )
    map.key(
      "request",
      entry => {
        val request = AvroRequestParser().parse(entry)
        operation.withRequest(request)
      }
    )
    map.key("response", parseResponse)
    // TODO: parse errors and one-way (one-way is a push? async?)

  }

  def parseResponse(entry: YMapEntry): Unit = {
    val response  = operation.withResponse().withStatusCode("200")
    val payload   = response.withPayload().withMediaType(Mimes.`application/json`)
    val reference = entry.value.as[YScalar].text
    val shape     = AvroReferenceParser(reference, entry.value).parse()
    payload.withSchema(shape)
  }
}

case class AvroRequestParser()(implicit ctx: AvroWebAPIContext) {
  def parse(entry: YMapEntry) = {
    val request = Request(Annotations.virtual())
    request.withPayloads(entry.value.as[Seq[YMap]].map(parsePayload))
    request
  }

  def parsePayload(map: YMap) = {
    val payload = Payload().withMediaType(Mimes.`application/json`)
    AvroRecordFieldParser(map).parse().foreach(payload.withSchema)
    payload
  }
}
