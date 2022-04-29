package amf.shapes.internal.spec.oas.emitter

import amf.core.client.common.position.Position
import amf.core.client.common.position.Position.ZERO
import amf.core.client.scala.model.domain.RecursiveShape
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.common.emitter.OasLikeShapeEmitterContext
import org.yaml.model.YDocument.EntryBuilder

case class OasRecursiveShapeEmitter(
    recursive: RecursiveShape,
    ordering: SpecOrdering,
    schemaPath: Seq[(String, String)]
)(implicit spec: OasLikeShapeEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val pointer = recursive.fixpoint.option() match {
      case Some(id) =>
        findInPath(id).orElse(recursive.fixpointTarget match {
          case Some(shape) =>
            findInPath(shape.id).orElse {
              // If the fixpoint is not in the schemaPath I will assume that it is a declaration and this declaration will be present
              recursive.fixpointTarget
                .flatMap(_.name.option().map(s"#${spec.schemasDeclarationsPath}" + _))
            }
          case None => None
        })
      case _ => None
    }
    for { p <- pointer } b.entry("$ref", p)
  }

  private def findInPath(id: String): Option[String] = {
    // List of chars that generates an URISyntaxException in Java but works in JS
    // Pointers with these keys must be ignored
    val extraneousChars = Seq('^')
    schemaPath.reverse.find(_._1 == id) match {
      case Some((_, pointer)) if pointer.equals("#") && !spec.isJsonSchema    => None
      case Some((_, pointer)) if !extraneousChars.forall(pointer.contains(_)) => Some(pointer)
      case _                                                                  => None
    }
  }

  override def position(): Position = ZERO
}
