package amf.plugins.document.apicontract.parser.spec.common

import amf.client.remod.AMFGraphConfiguration
import amf.core.AMFSerializer
import amf.core.model.document.Document
import amf.core.remote.JsonSchema
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.apicontract.annotations.{GeneratedJSONSchema, JSONSchemaRoot, ParsedJSONSchema}
import amf.plugins.domain.shapes.models.AnyShape

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
