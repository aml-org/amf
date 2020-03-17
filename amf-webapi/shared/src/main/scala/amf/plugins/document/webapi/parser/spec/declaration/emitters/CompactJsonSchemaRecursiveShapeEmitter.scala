package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.SpecOrdering
import amf.core.model.domain.RecursiveShape
import amf.plugins.document.webapi.contexts.emitter.oas.CompactJsonSchemaEmitterContext
import org.yaml.model.YDocument.EntryBuilder

class CompactJsonSchemaRecursiveShapeEmitter(
    recursive: RecursiveShape,
    ordering: SpecOrdering,
    schemaPath: Seq[(String, String)])(implicit spec: CompactJsonSchemaEmitterContext)
    extends OasRecursiveShapeEmitter(recursive, ordering, schemaPath) {

  override def emit(b: EntryBuilder): Unit = {
    val label = recursive.fixpoint.option().flatMap { fixpoint =>
      spec.definitionsQueue.labelOfShape(fixpoint)
    }
    label match {
      case Some(name) => b.entry("$ref", s"#${spec.schemasDeclarationsPath}$name")
      case None       => super.emit(b) // default to normal behaviour
    }
  }
}
