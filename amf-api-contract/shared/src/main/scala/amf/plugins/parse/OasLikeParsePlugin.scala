package amf.plugins.parse

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.DefaultReferenceCollector
import amf.plugins.document.apicontract.contexts.parser.OasLikeWebApiContext

trait OasLikeParsePlugin extends ApiParsePlugin {

  protected def promoteFragments(unit: BaseUnit, ctx: OasLikeWebApiContext): BaseUnit = {
    val collector = DefaultReferenceCollector[BaseUnit]()
    unit.references.foreach(baseUnit => collector += (baseUnit.location().getOrElse(baseUnit.id), baseUnit))
    ctx.declarations.promotedFragments.foreach { promoted =>
      val key = promoted.location().getOrElse(promoted.id)
      collector += (key, promoted)
    }
    if (collector.nonEmpty) unit.withReferences(collector.references())
    else unit
  }
}
