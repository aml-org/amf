package amf.plugins.document.webapi.parser.spec.declaration.emitters.schema.json

import amf.core.emitter.SpecOrdering
import amf.core.model.domain.RecursiveShape
import amf.plugins.document.webapi.contexts.emitter.oas.OasSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{
  AgnosticShapeEmitterContextAdapter,
  OasLikeShapeEmitterContextAdapter
}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.OasRecursiveShapeEmitter
import org.yaml.model.YDocument.EntryBuilder

class CompactOasRecursiveShapeEmitter(recursive: RecursiveShape,
                                      ordering: SpecOrdering,
                                      schemaPath: Seq[(String, String)])(implicit spec: OasSpecEmitterContext)
    extends OasRecursiveShapeEmitter(recursive, ordering, schemaPath)(OasLikeShapeEmitterContextAdapter(spec)) {

  override def emit(b: EntryBuilder): Unit = {
    val label = recursive.fixpoint
      .option()
      .flatMap { fixpoint =>
        spec.definitionsQueue.labelOfShape(fixpoint)
      }
      .orElse {
        recursive.fixpointTarget.map(spec.definitionsQueue.enqueue)
      }
    label match {
      case Some(name) => b.entry("$ref", s"#${spec.schemasDeclarationsPath}$name")
      case None       => super.emit(b) // default to normal behaviour
    }
  }
}
