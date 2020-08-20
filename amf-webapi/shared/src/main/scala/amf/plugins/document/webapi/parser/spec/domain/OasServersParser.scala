package amf.plugins.document.webapi.parser.spec.domain
import amf.core.metamodel.Field
import org.yaml.model.YMap
import amf.core.parser.Annotations
import amf.plugins.document.webapi.parser.spec.common.{SpecParserOps, YMapEntryLike}
import amf.core.model.domain.{AmfArray, DomainElement}
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.core.parser.YMapOps

abstract class OasServersParser(map: YMap, elem: DomainElement, field: Field)(implicit val ctx: OasLikeWebApiContext)
    extends SpecParserOps {
  def parse(): Unit

  protected def parseServers(key: String): Unit =
    map.key(key).foreach { entry =>
      val servers = entry.value.as[Seq[YMap]].map(m => new OasLikeServerParser(elem.id, YMapEntryLike(m)).parse())
      elem.set(field, AmfArray(servers, Annotations(entry)), Annotations(entry))
    }
}
