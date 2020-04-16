package amf.plugins.document.webapi.parser.spec.declaration.emitters.oas

import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.domain.RecursiveShape
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.oas.JsonSchemaEmitterContext
import org.yaml.model.YDocument.EntryBuilder

case class OasRecursiveShapeEmitter(recursive: RecursiveShape,
                                    ordering: SpecOrdering,
                                    schemaPath: Seq[(String, String)])(implicit spec: OasLikeSpecEmitterContext)
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
      case Some((_, pointer)) if pointer.equals("#") && !spec.isInstanceOf[JsonSchemaEmitterContext] => None
      case Some((_, pointer)) if !extraneousChars.forall(pointer.contains(_))                        => Some(pointer)
      case _                                                                                         => None
    }
  }

  override def position(): Position = ZERO
}
