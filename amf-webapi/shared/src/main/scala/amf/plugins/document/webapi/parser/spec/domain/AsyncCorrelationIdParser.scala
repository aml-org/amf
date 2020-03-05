package amf.plugins.document.webapi.parser.spec.domain

import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.domain.webapi.metamodel.CorrelationIdModel
import amf.plugins.domain.webapi.models.CorrelationId
import org.yaml.model.{YMap, YMapEntry, YNode}
import amf.core.parser.{ScalarNode, SearchScope, YMapOps}
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorCorrelationId
import amf.plugins.features.validation.CoreValidations
import ConversionHelpers._

case class AsyncCorrelationIdParser(entryOrNode: Either[YMapEntry, YNode], parentId: String)(
    implicit val ctx: AsyncWebApiContext)
    extends SpecParserOps {
  def parse(): CorrelationId = {
    val map: YMap = entryOrNode
    ctx.link(map) match {
      case Left(fullRef) =>
        handleRef(map, fullRef)
      case Right(_) =>
        val correlationId = CorrelationId(map)
        nameAndAdopt(correlationId, entryOrNode)
        CorrelationIdPopulator(map, correlationId).populate()
    }
  }

  private def nameAndAdopt(correlationId: CorrelationId, entry: Either[YMapEntry, YNode]): CorrelationId = {
    entry.left.foreach(
      entry =>
        correlationId
          .set(CorrelationIdModel.Name, ScalarNode(entry.key).string()))
    correlationId.adopted(parentId)
  }

  private def handleRef(map: YMap, fullRef: String) = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "correlationIds")
    ctx.declarations
      .findCorrelationId(label, SearchScope.Named)
      .map(correlationId => nameAndAdopt(correlationId.link(label), entryOrNode))
      .getOrElse(remote(fullRef, map))
  }

  private def remote(fullRef: String, map: YMap) = {
    ctx.obtainRemoteYNode(fullRef) match {
      case Some(correlationIdNode) =>
        val external = AsyncCorrelationIdParser(Right(correlationIdNode), parentId).parse()
        nameAndAdopt(external.link(fullRef), entryOrNode)
      case None =>
        ctx.eh.violation(CoreValidations.UnresolvedReference, "", s"Cannot find link reference $fullRef", map)
        nameAndAdopt(new ErrorCorrelationId(fullRef, map).link(fullRef), entryOrNode)
    }
  }
}

object AsyncCorrelationIdParser {
  def apply(node: YNode, parentId: String)(implicit ctx: AsyncWebApiContext): AsyncCorrelationIdParser = {
    AsyncCorrelationIdParser(Right(node), parentId)
  }
}

sealed case class CorrelationIdPopulator(map: YMap, correlationId: CorrelationId)(implicit val ctx: AsyncWebApiContext)
    extends SpecParserOps {
  def populate(): CorrelationId = {
    map.key("description", CorrelationIdModel.Description in correlationId)
    map.key("location", CorrelationIdModel.Location in correlationId)

    AnnotationParser(correlationId, map).parse()
    ctx.closedShape(correlationId.id, map, "correlationId")
    correlationId
  }
}
