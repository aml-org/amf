package amf.plugins.document.webapi.parser.spec.oas

import amf.core.client.GenerationOptions
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.parser.Position
import amf.core.plugins.AMFDocumentPlugin
import amf.core.remote.Oas
import amf.core.{AMFSerializer, ASTMaker}
import amf.plugins.document.webapi.contexts.Oas3SpecEmitterContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YDocument

trait JsonSchemaSerializer {
  private val ordering: SpecOrdering = SpecOrdering.Lexical
  // todo, check if its resolved?
  // todo lexical ordering?

  protected def toJsonSchema(element: AnyShape): String = new AMFSerializer(JsonSchemaAstMaker(element)).dumpToString

  case class JsonSchemaAstMaker(override val element: AnyShape) extends ASTMaker[AnyShape] {
    override val domainPlugin: Option[AMFDocumentPlugin] = None
    override val mediaType: String                       = "application/json"
    override val vendor: String                          = Oas.name
    override val options: GenerationOptions              = GenerationOptions()

    override def make(): YDocument = {
      YDocument(b => {
        b.obj { b =>
          traverse(emitters, b)
        }
      })
    }

    private val jsonSchemaRefEntry = new EntryEmitter {
      override def emit(b: YDocument.EntryBuilder): Unit =
        b.entry("$ref", OasDefinitions.appendDefinitionsPrefix(element.name))

      override def position(): Position = Position.ZERO
    }

    private def sortedTypeEntries =
      ordering.sorted(
        OasDeclarationsEmitter(Seq(element), SpecOrdering.Lexical, Seq())(new Oas3SpecEmitterContext()).emitters) // spec 3 context? or 2? set from outside, from vendor?? support two versions of jsonSchema??

    private val emitters = Seq(jsonSchemaEntry, jsonSchemaRefEntry) ++ sortedTypeEntries

  }
}

object jsonSchemaEntry extends EntryEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = b.entry("$schema", "http://json-schema.org/draft-04/schema#")

  override def position(): Position = Position.ZERO
}
