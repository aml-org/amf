package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import org.yaml.model.YMapEntry

class AwsOas30EndpointParser(
    override val entry: YMapEntry,
    override val parentId: String,
    override val collector: List[EndPoint]
)(
    override implicit val ctx: OasWebApiContext
) extends Oas30EndpointParser(entry, parentId, collector) {
  override protected val operationsRegex: String = "get|patch|put|post|delete|options|head|connect|trace|x-amazon-apigateway-any-method"
}
