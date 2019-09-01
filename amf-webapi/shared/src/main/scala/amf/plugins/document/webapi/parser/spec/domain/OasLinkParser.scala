package amf.plugins.document.webapi.parser.spec.domain

import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.domain.webapi.metamodel.TemplatedLinkModel
import amf.plugins.domain.webapi.models.{Server, TemplatedLink}
import amf.validations.ParserSideValidations._
import org.yaml.model.{YMap, YNode}

case class OasLinkParser(node: YNode, name: String, producer: String => TemplatedLink)(implicit ctx: OasWebApiContext)
  extends SpecParserOps {

  def parse(): TemplatedLink = {
    val map = node.as[YMap]
    val templatedLink = producer(name).add(Annotations.valueNode(map))

    map.key("operationRef", TemplatedLinkModel.OperationRef in templatedLink)
    map.key("operationId", TemplatedLinkModel.OperationId in templatedLink)

    if (templatedLink.operationRef.option().isDefined && templatedLink.operationId.option().isDefined) {
      ctx.violation(
        ExclusiveLinkTargetError,
        templatedLink.id,
        ExclusiveLinkTargetError.message,
        templatedLink.annotations
      )
    }

    map.key("description", TemplatedLinkModel.Description in templatedLink)

    map.key("server").foreach { entry =>
      val m = entry.value.as[YMap]
      val server = OasServerParser(templatedLink.id, m)(ctx).parse()
      templatedLink.withServer(server)
    }

    map.key("parameters", entry => {
      entry.value.as[YMap].entries.map { entry =>
        val variable = ScalarNode(entry.key).text().value.toString
        val expression = ScalarNode(entry.value).text().value.toString
        templatedLink.withIriMapping(variable, Some(Annotations(entry.key))).withLinkExpression(expression)
      }
    })

    map.key("requestBody", TemplatedLinkModel.RequestBody in templatedLink)

    AnnotationParser(templatedLink, map).parse()

    ctx.closedShape(templatedLink.id, map, "link")

    templatedLink
  }

}
