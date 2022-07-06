package amf.apicontract.internal.spec.async.parser.domain

import amf.apicontract.client.scala.model.domain.CorrelationId
import amf.apicontract.internal.metamodel.domain.CorrelationIdModel
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.WebApiDeclarations.ErrorCorrelationId
import amf.apicontract.internal.spec.common.parser.SpecParserOps
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode, SearchScope}
import amf.core.internal.validation.CoreValidations
import amf.shapes.internal.spec.common.parser.{AnnotationParser, YMapEntryLike}
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
    key.foreach(k =>
      correlationId
        .setWithoutId(CorrelationIdModel.Name, ScalarNode(k).string(), Annotations(k))
    )
    correlationId.add(entryLike.annotations)
  }

  private def handleRef(map: YMap, fullRef: String) = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "correlationIds")
    ctx.declarations
      .findCorrelationId(label, SearchScope.Named)
      .map(correlationId =>
        nameAndAdopt(
          correlationId.link(AmfScalar(label), Annotations(entryLike.value), Annotations.synthesized()),
          entryLike.key
        )
      )
      .getOrElse(remote(fullRef, map))
  }

  private def remote(fullRef: String, map: YMap) = {
    ctx.obtainRemoteYNode(fullRef) match {
      case Some(correlationIdNode) =>
        val external = AsyncCorrelationIdParser(YMapEntryLike(correlationIdNode), parentId).parse()
        nameAndAdopt(external.link(AmfScalar(fullRef), Annotations(map), Annotations.synthesized()), entryLike.key)
      case None =>
        ctx.eh.violation(CoreValidations.UnresolvedReference, "", s"Cannot find link reference $fullRef", map.location)
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
    ctx.closedShape(correlationId, map, "correlationId")
    correlationId
  }
}
