package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.TemplatedLink
import amf.apicontract.internal.metamodel.domain.TemplatedLinkModel
import amf.apicontract.internal.spec.common.WebApiDeclarations.ErrorLink
import amf.apicontract.internal.spec.common.parser.SpecParserOps
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.apicontract.internal.validation.definitions.ParserSideValidations.ExclusiveLinkTargetError
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode, SearchScope}
import amf.core.internal.validation.CoreValidations
import amf.shapes.client.scala.model.domain.IriTemplateMapping
import amf.shapes.internal.annotations.ExternalReferenceUrl
import amf.shapes.internal.domain.metamodel.IriTemplateMappingModel
import amf.shapes.internal.spec.common.parser.{AnnotationParser, YMapEntryLike}
import org.yaml.model.{YMap, YMapEntry, YScalar}

case class OasLinkParser(parentId: String, definitionEntry: YMapEntry)(implicit ctx: OasWebApiContext)
    extends SpecParserOps {

  private def nameAndAdopt(templateLink: TemplatedLink): TemplatedLink = {
    templateLink
      .setWithoutId(TemplatedLinkModel.Name, ScalarNode(definitionEntry.key).string(), Annotations(definitionEntry.key))
      .add(Annotations(definitionEntry))
  }
  def parse(): TemplatedLink = {
    val map = definitionEntry.value.as[YMap]

    ctx.link(map) match {
      case Left(fullRef) =>
        val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "links")
        val annotations = map
          .key("$ref")
          .flatMap(v => v.value.asOption[YScalar])
          .map(Annotations(_))
          .getOrElse(Annotations.synthesized())
        ctx.declarations
          .findTemplatedLink(label, SearchScope.Named)
          .map(templatedLink => {
            val linkedElement =
              ctx.link(templatedLink, map, AmfScalar(label), annotations, Annotations.synthesized())
            nameAndAdopt(linkedElement)
          })
          .getOrElse(remote(fullRef, map))

      case Right(_) => buildAndPopulate(map)
    }
  }

  private def remote(fullRef: String, map: YMap) = {
    ctx.obtainRemoteYNode(fullRef) match {
      case Some(requestNode) =>
        buildAndPopulate(requestNode.as[YMap]).add(ExternalReferenceUrl(fullRef))
      case None =>
        ctx.eh.violation(CoreValidations.UnresolvedReference, "", s"Cannot find link reference $fullRef", map.location)
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
      templatedLink.setWithoutId(TemplatedLinkModel.Server, server, Annotations(entry))
    }

    map.key(
      "parameters",
      entry => {
        val parameters: Seq[IriTemplateMapping] = entry.value.as[YMap].entries.map { entry =>
          val variable   = ScalarNode(entry.key).string()
          val expression = ScalarNode(entry.value).string()
          IriTemplateMapping(Annotations(entry))
            .setWithoutId(IriTemplateMappingModel.TemplateVariable, variable, Annotations(entry.key))
            .setWithoutId(IriTemplateMappingModel.LinkExpression, expression, Annotations(entry.value))
        }
        templatedLink.fields
          .setWithoutId(TemplatedLinkModel.Mapping, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))
      }
    )

    map.key("requestBody", TemplatedLinkModel.RequestBody in templatedLink)

    AnnotationParser(templatedLink, map).parse()

    ctx.closedShape(templatedLink, map, "link")

    templatedLink
  }
}
