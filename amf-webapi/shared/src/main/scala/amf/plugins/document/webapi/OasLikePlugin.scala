package amf.plugins.document.webapi

import amf.core.emitter.{RenderOptions, ShapeRenderOptions}
import amf.core.model.document._
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext

trait OasLikePlugin extends BaseWebApiPlugin {

  override def specContext(options: RenderOptions, shapeRenderOptions: ShapeRenderOptions): OasLikeSpecEmitterContext

  // We might find $refs in the document pointing to actual shapes in external files in the
  // right positions of the AST.
  // We will try to promote these external fragments to data type fragments instead of just inlining them.
  def promoteFragments(unit: BaseUnit, ctx: OasLikeWebApiContext): BaseUnit = {
    var oldReferences = unit.references.foldLeft(Map[String, BaseUnit]()) {
      case (acc: Map[String, BaseUnit], e: BaseUnit) =>
        acc + (e.location().getOrElse(e.id) -> e)
    }
    ctx.declarations.promotedFragments.foreach { promoted =>
      val key = promoted.location().getOrElse(promoted.id)
      oldReferences = oldReferences + (key -> promoted)
    }

    if (oldReferences.values.nonEmpty)
      unit.withReferences(oldReferences.values.toSeq)
    else
      unit
  }

}
