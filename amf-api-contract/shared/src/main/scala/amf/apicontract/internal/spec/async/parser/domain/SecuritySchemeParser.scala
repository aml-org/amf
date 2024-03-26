package amf.apicontract.internal.spec.async.parser.domain

import amf.apicontract.client.scala.model.domain.security.SecurityRequirement
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.parser.OasLikeSecurityRequirementParser
import amf.core.client.scala.model.domain.{AmfArray, AmfObject}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.utils.IdCounter
import org.yaml.model.{YMapEntry, YNode}

trait SecuritySchemeParser {
  def parseSecurityScheme(entry: YMapEntry, field: Field, parent: AmfObject)(implicit ctx: AsyncWebApiContext): Unit = {
    val idCounter = new IdCounter()
    val securedBy = entry.value
      .as[Seq[YNode]]
      .flatMap(s => OasLikeSecurityRequirementParser(s, (_: SecurityRequirement) => Unit, idCounter).parse())
    parent.setWithoutId(field, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
  }
}
