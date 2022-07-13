package amf.shapes.internal.spec.jsonschema.emitter

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.internal.plugins.render.DefaultRenderConfiguration
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.annotations.{GeneratedJSONSchema, JSONSchemaRoot, ParsedJSONSchema}
import org.yaml.render.JsonRender

trait JsonSchemaSerializer extends PlatformSecrets {
  // todo, check if its resolved?
  // todo lexical ordering?

  protected def toJsonSchema(element: AnyShape, config: AMFGraphConfiguration): String = {

    element.annotations
      .find(classOf[ParsedJSONSchema])
      .map(_.rawText)
      .orElse(element.annotations.find(classOf[GeneratedJSONSchema]).map(_.rawText))
      .getOrElse(generateJsonSchema(element, config))
  }

  protected def generateJsonSchema(element: AnyShape, config: AMFGraphConfiguration): String = {

    val originalId = element.id
    fixNameIfNeeded(element)
    val renderConfig = DefaultRenderConfiguration(config)
    val yamlDoc      = JsonSchemaEmitter(renderConfig = renderConfig).emit(element)
    val jsonSchema   = JsonRender.render(yamlDoc)
    element.withId(originalId)
    element.annotations.reject(a =>
      a.isInstanceOf[ParsedJSONSchema] || a.isInstanceOf[GeneratedJSONSchema] || a.isInstanceOf[JSONSchemaRoot]
    )
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
