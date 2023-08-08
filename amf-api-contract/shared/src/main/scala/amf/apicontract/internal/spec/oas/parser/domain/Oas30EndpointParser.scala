package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.internal.metamodel.domain.EndPointModel
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.core.internal.parser.YMapOps
import org.yaml.model.{YMap, YMapEntry}

case class Oas30EndpointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint])(
    override implicit val ctx: OasWebApiContext
) extends OasEndpointParser(entry, parentId, collector) {

  override type ConcreteContext = OasWebApiContext

  override def apply(entry: YMapEntry, parentId: String, collector: List[EndPoint])(
      ctx: ConcreteContext
  ): Oas30EndpointParser = {
    Oas30EndpointParser(entry, parentId, collector)(ctx)
  }

  /** Verify if two paths are identical. In the case of OAS 3.0, paths with the same hierarchy but different templated
    * names are considered identical.
    */

  override protected def parseEndpointMap(endpoint: EndPoint, map: YMap): EndPoint = {
    super.parseEndpointMap(endpoint, map)
    map.key("summary", EndPointModel.Summary in endpoint)
    map.key("description", EndPointModel.Description in endpoint)
    endpoint
  }
}
