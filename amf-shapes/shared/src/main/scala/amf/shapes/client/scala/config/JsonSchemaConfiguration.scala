package amf.shapes.client.scala.config

import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.internal.spec.jsonschema.{JsonSchemaParsePlugin, JsonSchemaRenderPlugin}
import amf.shapes.internal.transformation.{
  JsonSchemaCachePipeline,
  JsonSchemaEditingPipeline,
  JsonSchemaTransformationPipeline
}

object JsonSchemaConfiguration {
  def JsonSchema(): ShapesConfiguration =
    ShapesConfiguration
      .predefined()
      .withPlugins(
        List(
          JsonSchemaParsePlugin,
          JsonSchemaRenderPlugin
        )
      )
      .withTransformationPipelines(
        List(
          JsonSchemaTransformationPipeline(),
          JsonSchemaEditingPipeline(),
          JsonSchemaCachePipeline()
        )
      )
}
