package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.internal.spec.common.parser.SpecParserOps
import amf.apicontract.internal.spec.oas.parser.context.OasLikeWebApiContext
import amf.core.client.scala.model.domain.{AmfArray, DomainElement}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.YMap

abstract class OasServersParser(map: YMap, elem: DomainElement, field: Field)(implicit val ctx: OasLikeWebApiContext)
    extends SpecParserOps {
  def parse(): Unit

  protected def parseServers(key: String): Unit =
    map.key(key).foreach { entry =>
      val servers = entry.value.as[Seq[YMap]].map(m => new OasLikeServerParser(elem.id, YMapEntryLike(m)).parse())
      elem.set(field, AmfArray(servers, Annotations(entry)), Annotations(entry))
    }
}
