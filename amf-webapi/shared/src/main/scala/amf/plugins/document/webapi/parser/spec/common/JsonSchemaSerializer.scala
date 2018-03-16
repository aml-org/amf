package amf.plugins.document.webapi.parser.spec.common

import amf.core.AMFSerializer
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.Document
import amf.core.parser.Position
import amf.core.services.RuntimeSerializer
import amf.plugins.document.webapi.contexts.Oas3SpecEmitterContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.oas.OasDeclarationsEmitter
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YDocument

trait JsonSchemaSerializer {
  // todo, check if its resolved?
  // todo lexical ordering?

  protected def toJsonSchema(element: AnyShape): String = {
    AMFSerializer.init()
    RuntimeSerializer(Document().withDeclaredElement(element), "application/schema+json", "JSON Schema")
  }
}

object JsonSchemaEntry extends EntryEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = b.entry("$schema", "http://json-schema.org/draft-04/schema#")

  override def position(): Position = Position.ZERO
}

case class JsonSchemaEmitter(shape: AnyShape, ordering: SpecOrdering = SpecOrdering.Lexical) {
  def emitDocument(): YDocument = {
    YDocument(b => {
      b.obj { b =>
        traverse(emitters, b)
      }
    })
  }

  private val jsonSchemaRefEntry = new EntryEmitter {
    override def emit(b: YDocument.EntryBuilder): Unit =
      b.entry("$ref", OasDefinitions.appendDefinitionsPrefix(shape.name))

    override def position(): Position = Position.ZERO
  }

  private def sortedTypeEntries =
    ordering.sorted(
      OasDeclarationsEmitter(Seq(shape), SpecOrdering.Lexical, Seq())(new Oas3SpecEmitterContext()).emitters) // spec 3 context? or 2? set from outside, from vendor?? support two versions of jsonSchema??

  private val emitters = Seq(JsonSchemaEntry, jsonSchemaRefEntry) ++ sortedTypeEntries
}
