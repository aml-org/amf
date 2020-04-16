package amf.plugins.document.webapi.parser.spec.async.parser

import amf.core.parser.{Annotations, ScalarNode, SearchScope, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorCorrelationId
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps, YMapEntryLike}
import amf.plugins.domain.webapi.metamodel.CorrelationIdModel
import amf.plugins.domain.webapi.models.CorrelationId
import amf.plugins.features.validation.CoreValidations
import org.yaml.model.{YMap, YNode}

case class AsyncCorrelationIdParser(entryLike: YMapEntryLike, parentId: String)(implicit val ctx: AsyncWebApiContext)
    extends SpecParserOps {
  def parse(): CorrelationId = {
    val map: YMap = entryLike.asMap
    ctx.link(map) match {
      case Left(fullRef) =>
        handleRef(map, fullRef)
      case Right(_) =>
        val correlationId = CorrelationId()
        nameAndAdopt(correlationId, entryLike.key)
        CorrelationIdPopulator(map, correlationId).populate()
    }
  }

  private def nameAndAdopt(correlationId: CorrelationId, key: Option[YNode]): CorrelationId = {
    key.foreach(
      k =>
        correlationId
          .set(CorrelationIdModel.Name, ScalarNode(k).string()))
    correlationId.adopted(parentId).add(Annotations(entryLike.asMap))
  }

  private def handleRef(map: YMap, fullRef: String) = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "correlationIds")
    ctx.declarations
      .findCorrelationId(label, SearchScope.Named)
      .map(correlationId => nameAndAdopt(correlationId.link(label), entryLike.key))
      .getOrElse(remote(fullRef, map))
  }

  private def remote(fullRef: String, map: YMap) = {
    ctx.obtainRemoteYNode(fullRef) match {
      case Some(correlationIdNode) =>
        val external = AsyncCorrelationIdParser(YMapEntryLike(correlationIdNode), parentId).parse()
        nameAndAdopt(external.link(fullRef), entryLike.key)
      case None =>
        ctx.eh.violation(CoreValidations.UnresolvedReference, "", s"Cannot find link reference $fullRef", map)
        val errorCorrelation = new ErrorCorrelationId(fullRef, map)
        nameAndAdopt(errorCorrelation.link(fullRef, errorCorrelation.annotations), entryLike.key)
    }
  }
}

object AsyncCorrelationIdParser {
  def apply(node: YNode, parentId: String)(implicit ctx: AsyncWebApiContext): AsyncCorrelationIdParser = {
    AsyncCorrelationIdParser(YMapEntryLike(node), parentId)
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
