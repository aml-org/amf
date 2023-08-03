package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import org.yaml.model.{YMap, YMapEntry}

case class Oas20EndpointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint])(
    override implicit val ctx: OasWebApiContext
) extends OasEndpointParser(entry, parentId, collector) {

  override type ConcreteContext = OasWebApiContext

  override def apply(entry: YMapEntry, parentId: String, collector: List[EndPoint])(
      ctx: ConcreteContext
  ): Oas20EndpointParser = {
    Oas20EndpointParser(entry, parentId, collector)(ctx)
  }

  override protected def parseEndpointMap(endpoint: EndPoint, map: YMap): EndPoint = {
    super.parseEndpointMap(endpoint, map)
  }

}
