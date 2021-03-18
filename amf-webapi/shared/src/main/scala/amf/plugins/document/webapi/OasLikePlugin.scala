package amf.plugins.document.webapi
import amf.client.remod.amfcore.config.RenderOptions
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document._
import amf.core.parser.DefaultReferenceCollector
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext

import scala.collection.mutable

trait OasLikePlugin extends BaseWebApiPlugin {

  override def specContext(options: RenderOptions, errorHandler: ErrorHandler): OasLikeSpecEmitterContext

  // We might find $refs in the document pointing to actual shapes in external files in the
  // right positions of the AST.
  // We will try to promote these external fragments to data type fragments instead of just inlining them.
  def promoteFragments(unit: BaseUnit, ctx: OasLikeWebApiContext): BaseUnit = {
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
