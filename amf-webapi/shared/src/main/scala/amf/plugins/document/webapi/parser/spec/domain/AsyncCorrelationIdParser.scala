package amf.plugins.document.webapi.parser.spec.domain

import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.domain.webapi.metamodel.CorrelationIdModel
import amf.plugins.domain.webapi.models.CorrelationId
import org.yaml.model.{YMap, YMapEntry, YNode}
import amf.core.parser.{Annotations, ScalarNode, YMapOps}

case class AsyncCorrelationIdParser(entryOrNode: Either[YMapEntry, YNode], parentId: String)(
    implicit val ctx: AsyncWebApiContext)
    extends SpecParserOps {
  def parse(): CorrelationId = {
    // missing handling of refs
    entryOrNode match {
      case Left(entry) =>
        val map           = entry.value.as[YMap]
        val correlationId = nameAndAdopt(CorrelationId(map), entry)
        CorrelationIdPopulator(map, correlationId).populate()
      case Right(node) =>
        val map           = node.as[YMap]
        val correlationId = CorrelationId(map).adopted(parentId)
        CorrelationIdPopulator(map, correlationId).populate()
    }
  }

  private def nameAndAdopt(correlationId: CorrelationId, entry: YMapEntry): CorrelationId = {
    correlationId
      .set(CorrelationIdModel.Name, ScalarNode(entry.key).string())
      .adopted(parentId)
      .add(Annotations(entry))
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
