package amf.plugins.document.webapi.parser.spec.common

import amf.core.AMFSerializer
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.Document
import amf.core.parser.Position
import amf.core.remote.JsonSchema
import amf.core.services.RuntimeSerializer
import amf.plugins.document.webapi.annotations.{GeneratedJSONSchema, ParsedJSONSchema}
import amf.plugins.document.webapi.contexts.JsonSchemaEmitterContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.oas.OasDeclarationsEmitter
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YDocument
import org.yaml.model.YDocument.EntryBuilder

trait JsonSchemaSerializer {
  // todo, check if its resolved?
  // todo lexical ordering?

  protected def toJsonSchema(element: AnyShape): String = {
    element.annotations.find(classOf[ParsedJSONSchema]) match {
      case Some(a) => a.rawText
      case _ =>
        element.annotations.find(classOf[GeneratedJSONSchema]) match {
          case Some(g) => g.rawText
          case _       => generateJsonSchema(element)
        }
    }
  }

  protected def generateJsonSchema(element: AnyShape): String = {
    AMFSerializer.init()
    val jsonSchema = RuntimeSerializer(Document().withDeclaredElement(fixNameIfNeeded(element)),
                                       "application/schema+json",
                                       JsonSchema.name)
    element.annotations.reject(_.isInstanceOf[ParsedJSONSchema])
    element.annotations.reject(_.isInstanceOf[GeneratedJSONSchema])
    element.annotations += GeneratedJSONSchema(jsonSchema)
    jsonSchema
  }

  private def fixNameIfNeeded(element: AnyShape): AnyShape = {
    if (element.name.option().isEmpty)
      element.copyShape().withName("root")
    else {
      if (element.name.value().matches(".*/.*")) element.copyShape().withName("root")
      else element
    }
  }
}

object JsonSchemaEntry extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = b.entry("$schema", "http://json-schema.org/draft-04/schema#")

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
    override def emit(b: EntryBuilder): Unit =
      b.entry("$ref", OasDefinitions.appendDefinitionsPrefix(shape.name.value()))

    override def position(): Position = Position.ZERO
  }

  private def sortedTypeEntries =
    ordering.sorted(
      OasDeclarationsEmitter(Seq(shape), SpecOrdering.Lexical, Seq())(JsonSchemaEmitterContext()).emitters) // spec 3 context? or 2? set from outside, from vendor?? support two versions of jsonSchema??

  private val emitters = Seq(JsonSchemaEntry, jsonSchemaRefEntry) ++ sortedTypeEntries
}
