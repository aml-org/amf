package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.{Operation, Request}
import amf.apicontract.internal.metamodel.domain.OperationModel
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry}

case class Oas20OperationParser(entry: YMapEntry, adopt: Operation => Operation)(
    override implicit val ctx: OasWebApiContext
) extends OasOperationParser(entry, adopt) {
  override def parse(): Operation = {
    val operation = super.parse()
    val map       = entry.value.as[YMap]
    Oas20RequestParser(map, (r: Request) => Unit)
      .parse()
      .map(r =>
        operation.setWithoutId(OperationModel.Request, AmfArray(Seq(r), Annotations.virtual()), Annotations(map))
      )

    map.key("schemes", OperationModel.Schemes in operation)
    map.key("consumes", OperationModel.Accepts in operation)
    map.key("produces", OperationModel.ContentType in operation)

    operation
  }
}
