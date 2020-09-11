package amf.plugins.document.webapi.parser.spec.domain

import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorLink
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps, YMapEntryLike}
import amf.plugins.domain.webapi.metamodel.{IriTemplateMappingModel, TemplatedLinkModel}
import amf.plugins.domain.webapi.models.{IriTemplateMapping, TemplatedLink}
import amf.plugins.features.validation.CoreValidations
import amf.validations.ParserSideValidations._
import org.yaml.model.{YMap, YMapEntry}

case class OasLinkParser(parentId: String, definitionEntry: YMapEntry)(implicit ctx: OasWebApiContext)
    extends SpecParserOps {

  private def nameAndAdopt(templateLink: TemplatedLink): TemplatedLink = {
    templateLink
      .set(TemplatedLinkModel.Name, ScalarNode(definitionEntry.key).string())
      .adopted(parentId)
      .add(Annotations(definitionEntry))
  }
  def parse(): TemplatedLink = {
    val map = definitionEntry.value.as[YMap]

    ctx.link(map) match {
      case Left(fullRef) =>
        val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "links")
        ctx.declarations
          .findTemplatedLink(label, SearchScope.Named)
          .map(templatedLink => nameAndAdopt(templatedLink.link(label)))
          .getOrElse(remote(fullRef, map))

      case Right(_) => buildAndPopulate(map)
    }
  }

  private def remote(fullRef: String, map: YMap) = {
    ctx.obtainRemoteYNode(fullRef) match {
      case Some(requestNode) =>
        buildAndPopulate(requestNode.as[YMap])
      case None =>
        ctx.eh.violation(CoreValidations.UnresolvedReference, "", s"Cannot find link reference $fullRef", map)
        nameAndAdopt(new ErrorLink(fullRef, map).link(fullRef))

    }
  }

  private def buildAndPopulate(map: YMap) = OasLinkPopulator(map, nameAndAdopt(TemplatedLink())).populate()

}

sealed case class OasLinkPopulator(map: YMap, templatedLink: TemplatedLink)(implicit ctx: OasWebApiContext)
    extends SpecParserOps {
  def populate(): TemplatedLink = {
    map.key("operationRef", TemplatedLinkModel.OperationRef in templatedLink)
    map.key("operationId", TemplatedLinkModel.OperationId in templatedLink)

    if (templatedLink.operationRef.option().isDefined && templatedLink.operationId.option().isDefined) {
      ctx.eh.violation(
        ExclusiveLinkTargetError,
        templatedLink.id,
        ExclusiveLinkTargetError.message,
        templatedLink.annotations
      )
    }

    map.key("description", TemplatedLinkModel.Description in templatedLink)

    map.key("server").foreach { entry =>
      val m      = entry.value.as[YMap]
      val server = new OasLikeServerParser(templatedLink.id, YMapEntryLike(m))(ctx).parse()
      templatedLink.set(TemplatedLinkModel.Server, server, Annotations(entry))
    }

    map.key(
      "parameters",
      entry => {
        val parameters: Seq[IriTemplateMapping] = entry.value.as[YMap].entries.map { entry =>
          val variable   = ScalarNode(entry.key).string()
          val expression = ScalarNode(entry.value).string()
          IriTemplateMapping(Annotations(entry))
            .set(IriTemplateMappingModel.TemplateVariable, variable)
            .set(IriTemplateMappingModel.LinkExpression, expression)
        }
        templatedLink.setArray(TemplatedLinkModel.Mapping, parameters, Annotations(entry.value))

      }
    )

    map.key("requestBody", TemplatedLinkModel.RequestBody in templatedLink)

    AnnotationParser(templatedLink, map).parse()

    ctx.closedShape(templatedLink.id, map, "link")

    templatedLink
  }
}
