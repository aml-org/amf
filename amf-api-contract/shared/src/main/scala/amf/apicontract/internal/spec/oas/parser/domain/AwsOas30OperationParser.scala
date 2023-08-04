package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.Operation
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.domain.ScalarNode
import org.yaml.model.YMapEntry

class AwsOas30OperationParser(entry: YMapEntry, adopt: Operation => Operation)(
    override implicit val ctx: OasWebApiContext
) extends Oas30OperationParser(entry, adopt) {
  override def entryKey: AmfScalar = {
    entry.key.toString match {
      case "x-amazon-apigateway-any-method" => ScalarNode("any").string()
      case _                                => ScalarNode(entry.key).string()
    }

  }

}
