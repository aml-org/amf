package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.Operation
import amf.apicontract.internal.metamodel.domain.OperationModel
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry, YScalar}

case class Oas30OperationParser(entry: YMapEntry, adopt: Operation => Operation)(
    override implicit val ctx: OasWebApiContext
) extends OasOperationParser(entry, adopt) {
  override def parse(): Operation = {
    val operation = super.parse()
    val map       = entry.value.as[YMap]

    map.key(
      "requestBody",
      entry => {
        operation.fields.setWithoutId(
          OperationModel.Request,
          AmfArray(Seq(Oas30RequestParser(entry.value.as[YMap], operation.id, entry).parse()), Annotations.virtual()),
          Annotations.inferred()
        )
      }
    )

    // parameters defined in endpoint are stored in the request
    Oas30ParametersParser(map, Option(operation.request).map(() => _).getOrElse(operation.withInferredRequest))
      .parseParameters()

    map.key(
      "callbacks",
      entry => {
        val callbacks = entry.value
          .as[YMap]
          .entries
          .flatMap { callbackEntry =>
            val name = callbackEntry.key.as[YScalar].text
            Oas30CallbackParser(callbackEntry.value.as[YMap], _.withName(name), name, callbackEntry)
              .parse()
          }
        operation.fields
          .setWithoutId(OperationModel.Callbacks, AmfArray(callbacks, Annotations(entry.value)), Annotations(entry))
      }
    )

    ctx.factory.serversParser(map, operation).parse()

    operation
  }
}
