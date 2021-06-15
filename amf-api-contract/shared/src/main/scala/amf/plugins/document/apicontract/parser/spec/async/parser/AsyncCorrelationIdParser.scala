package amf.plugins.document.apicontract.parser.spec.async.parser

import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode, SearchScope}
import amf.core.internal.validation.CoreValidations
import amf.plugins.document.apicontract.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec.OasDefinitions
import amf.plugins.document.apicontract.parser.spec.WebApiDeclarations.ErrorCorrelationId
import amf.plugins.document.apicontract.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.apicontract.parser.spec.declaration.common.YMapEntryLike
import amf.plugins.domain.apicontract.metamodel.CorrelationIdModel
import amf.plugins.domain.apicontract.models.CorrelationId
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
          .set(CorrelationIdModel.Name, ScalarNode(k).string(), Annotations(k)))
    correlationId.adopted(parentId).add(entryLike.annotations)
  }

  private def handleRef(map: YMap, fullRef: String) = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "correlationIds")
    ctx.declarations
      .findCorrelationId(label, SearchScope.Named)
      .map(correlationId =>
        nameAndAdopt(correlationId.link(AmfScalar(label), Annotations(entryLike.value), Annotations.synthesized()),
                     entryLike.key))
      .getOrElse(remote(fullRef, map))
  }

  private def remote(fullRef: String, map: YMap) = {
    ctx.obtainRemoteYNode(fullRef) match {
      case Some(correlationIdNode) =>
        val external = AsyncCorrelationIdParser(YMapEntryLike(correlationIdNode), parentId).parse()
        nameAndAdopt(external.link(AmfScalar(fullRef), Annotations(map), Annotations.synthesized()), entryLike.key)
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

    AnnotationParser(correlationId, map)(WebApiShapeParserContextAdapter(ctx)).parse()
    ctx.closedShape(correlationId.id, map, "correlationId")
    correlationId
  }
}
