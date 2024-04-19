package amf.apicontract.internal.spec.async.parser.bindings.message

import amf.apicontract.client.scala.model.domain.bindings.anypointmq.AnypointMQMessageBinding
import amf.apicontract.internal.metamodel.domain.bindings.AnypointMQMessageBindingModel
import amf.apicontract.internal.spec.async.parser.bindings.BindingParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.apicontract.internal.validation.definitions.ParserSideValidations.UnsupportedBindingVersion
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, SearchScope}
import amf.core.internal.validation.CoreValidations
import org.yaml.model.{YMap, YMapEntry}

object AnypointMQMessageBindingParser extends BindingParser[AnypointMQMessageBinding] {
  override def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): AnypointMQMessageBinding = {
    val map            = entry.value.as[YMap]
    val bindingVersion = getBindingVersion(entry.value.as[YMap], "AnypointMQMessageBinding", ctx.specSettings.spec)

    val binding: AnypointMQMessageBinding = bindingVersion match {
      case "0.1.0" | "latest" => AnypointMQMessageBinding(Annotations(entry))
      case "0.0.1"            => AnypointMQMessageBinding(Annotations(entry))
      case invalidVersion =>
        val defaultBinding = AnypointMQMessageBinding(Annotations(entry))
        ctx.violation(
          UnsupportedBindingVersion,
          "AnypointMQ",
          s"Version $invalidVersion is not supported in an AnypointMQ",
          entry.value.location
        )
        defaultBinding
    }

    map.key("headers").foreach { entry =>
      ctx.link(entry.value) match {
        case Left(fullRef) => handleRef(fullRef, entry, AnypointMQMessageBindingModel.Headers, binding)
        case Right(_)      => parseSchema(AnypointMQMessageBindingModel.Headers, binding, entry)
      }
    }

    parseBindingVersion(binding, AnypointMQMessageBindingModel.BindingVersion, map)
    ctx.closedShape(binding, map, "AnypointMQMessageBinding")
    binding
  }

  protected def handleRef(fullRef: String, entry: YMapEntry, field: Field, binding: DomainElement)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "schemas")
    ctx.declarations
      .findType(label, SearchScope.Named)
      .map(shape => binding.setWithoutId(field, shape, Annotations(entry)))
      .getOrElse {
        remote(fullRef, entry, field, binding)
      }
  }

  private def remote(fullRef: String, entry: YMapEntry, field: Field, binding: DomainElement)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    ctx.navigateToRemoteYNode(fullRef) match {
      case Some(remoteResult) =>
        parseSchema(field, binding, remoteResult.remoteNode)
      case None =>
        ctx.eh.violation(
          CoreValidations.UnresolvedReference,
          binding,
          s"Cannot find link reference $fullRef",
          entry.location
        )
    }
  }
}
