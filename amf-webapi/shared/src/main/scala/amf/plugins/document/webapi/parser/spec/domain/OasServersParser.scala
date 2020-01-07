package amf.plugins.document.webapi.parser.spec.domain
import amf.core.metamodel.Field
import org.yaml.model.YMap
import amf.core.parser.Annotations
import amf.plugins.document.webapi.parser.spec.common.SpecParserOps
import amf.core.model.domain.{DomainElement, AmfArray}
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.core.parser.YMapOps

abstract class OasServersParser(map: YMap, elem: DomainElement, field: Field)(implicit val ctx: OasLikeWebApiContext)
    extends SpecParserOps {
  def parse(): Unit

  protected def parseServers(key: String): Unit =
    map.key(key).foreach { entry =>
      val servers = entry.value.as[Seq[YMap]].map(new OasLikeServerParser(elem.id, _).parse())
      elem.set(field, AmfArray(servers, Annotations(entry)), Annotations(entry))
    }
}
