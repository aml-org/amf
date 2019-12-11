package amf.plugins.document.webapi.parser.spec.domain

import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.domain.webapi.metamodel.TemplatedLinkModel
import amf.plugins.domain.webapi.models.{IriTemplateMapping, TemplatedLink}
import amf.plugins.features.validation.CoreValidations
import amf.validations.ParserSideValidations._
import org.yaml.model.{YMap, YNode}

case class OasLinkParser(node: YNode, name: String, adopt: TemplatedLink => Unit)(implicit ctx: OasWebApiContext)
    extends SpecParserOps {

  def parse(): Option[TemplatedLink] = {
    val map = node.as[YMap]

    ctx.link(map) match {
      case Left(fullRef) =>
        val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "links")
        ctx.declarations
          .findTemplatedLink(label, SearchScope.Named)
          .map(templatedLink => {
            val link: TemplatedLink = templatedLink.link(label, Annotations(map))
            link.withName(name)
            adopt(link)
            link
          })
          .orElse {
            ctx.obtainRemoteYNode(fullRef) match {
              case Some(requestNode) =>
                OasLinkParser(requestNode.as[YMap], name, adopt).parse()
              case None =>
                ctx.violation(CoreValidations.UnresolvedReference, "", s"Cannot find link reference $fullRef", map)
                None
            }
          }
      case Right(_) =>
        val templatedLink = TemplatedLink().withName(name).add(Annotations.valueNode(map))
        adopt(templatedLink)

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
          val m      = entry.value.as[YMap]
          val server = OasServerParser(templatedLink.id, m)(ctx).parse()
          templatedLink.withServer(server)
        }

        map.key(
          "parameters",
          entry => {
            val parameters: Seq[IriTemplateMapping] = entry.value.as[YMap].entries.map { entry =>
              val variable   = ScalarNode(entry.key).text().value.toString
              val expression = ScalarNode(entry.value).text().value.toString
              IriTemplateMapping(Annotations(entry)).withTemplateVariable(variable).withLinkExpression(expression)
            }
            templatedLink.setArray(TemplatedLinkModel.Mapping, parameters, Annotations(entry.value))
          }
        )

        map.key("requestBody", TemplatedLinkModel.RequestBody in templatedLink)

        AnnotationParser(templatedLink, map).parse()

        ctx.closedShape(templatedLink.id, map, "link")

        Some(templatedLink)
    }

  }

}
