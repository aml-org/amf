package amf.shapes.internal.spec.jsonschema.emitter

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.model.document.Document
import amf.core.internal.remote.JsonSchema
import amf.core.internal.render.AMFSerializer
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.scala.annotations.{GeneratedJSONSchema, JSONSchemaRoot, ParsedJSONSchema}
import amf.shapes.client.scala.domain.models.AnyShape

trait JsonSchemaSerializer extends PlatformSecrets {
  // todo, check if its resolved?
  // todo lexical ordering?

  protected def toJsonSchema(element: AnyShape, config: AMFGraphConfiguration): String = {
    element.annotations.find(classOf[ParsedJSONSchema]) match {
      case Some(a) => a.rawText
      case _ =>
        element.annotations.find(classOf[GeneratedJSONSchema]) match {
          case Some(g) => g.rawText
          case _       => generateJsonSchema(element, config)
        }
    }
  }

  protected def generateJsonSchema(element: AnyShape, config: AMFGraphConfiguration): String = {

    // TODO: WE SHOULDN'T HAVE TO CREATE A DOCUMENT TO EMIT A SCHEMA!
    val originalId = element.id
    val document   = Document().withDeclares(Seq(fixNameIfNeeded(element)))
    val jsonSchema = new AMFSerializer(document, JsonSchema.mediaType, config.renderConfiguration).render()
    // TODO: why are we stripping annotations??
    element.withId(originalId)
    element.annotations.reject(a =>
      a.isInstanceOf[ParsedJSONSchema] || a.isInstanceOf[GeneratedJSONSchema] || a.isInstanceOf[JSONSchemaRoot])
    element.annotations += GeneratedJSONSchema(jsonSchema)
    jsonSchema
  }

  private def fixNameIfNeeded(element: AnyShape): AnyShape = {
    // Adding an annotation to identify the root shape of the JSON Schema
    element.annotations += JSONSchemaRoot()
    if (element.name.option().isEmpty)
      element.copyShape().withName("root")
    else {
      if (element.name.value().matches(".*/.*")) element.copyShape().withName("root")
      else element
    }
  }
}
