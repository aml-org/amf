package amf.apicontract.internal.spec.async.parser.bindings

import amf.apicontract.client.scala.model.domain.bindings.EmptyBinding
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations
import amf.core.client.scala.model.domain.AmfObject
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.{YMap, YMapEntry, YNode}

object EmptyBindingParser extends BindingParser[EmptyBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): EmptyBinding = {
    val binding = EmptyBinding(Annotations(entry))

    validateEmptyMap(entry.value, binding, entry.key.as[String])
    binding
  }

  private def validateEmptyMap(value: YNode, node: AmfObject, `type`: String)(implicit ctx: AsyncWebApiContext): Unit =
    if (value.as[YMap].entries.nonEmpty) {
      ctx.eh.violation(
        ParserSideValidations.NonEmptyBindingMap,
        node,
        s"Reserved name binding '${`type`}' must have an empty map",
        value.location
      )
    }
}
