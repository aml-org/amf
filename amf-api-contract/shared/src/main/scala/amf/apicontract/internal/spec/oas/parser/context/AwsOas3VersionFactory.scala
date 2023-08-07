package amf.apicontract.internal.spec.oas.parser.context

import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.apicontract.internal.spec.oas.parser.domain._
import org.yaml.model.YMapEntry

class AwsOas3VersionFactory()(implicit override val ctx: AwsOas3WebApiContext) extends Oas3VersionFactory {
  override def endPointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint]): OasLikeEndpointParser =
    new AwsOas30EndpointParser(entry, parentId, collector)(ctx)

  override def operationParser(entry: YMapEntry, adopt: Operation => Operation): OasLikeOperationParser =
    new AwsOas30OperationParser(entry, adopt)(ctx)
}
