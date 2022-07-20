package amf.shapes.client.scala.config

import amf.shapes.client.scala.ShapesConfiguration
import amf.shapes.internal.spec.jsonschema.{JsonSchemaParsePlugin, JsonSchemaRenderPlugin}
import amf.shapes.internal.transformation.{
  JsonSchemaCachePipeline,
  JsonSchemaEditingPipeline,
  JsonSchemaTransformationPipeline
}
import amf.shapes.internal.validation.model.ShapeEffectiveValidations.JsonSchemaEffectiveValidations
import amf.shapes.internal.validation.model.ShapeValidationProfiles.JsonSchemaValidationProfile

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
      .withValidationProfile(JsonSchemaValidationProfile, JsonSchemaEffectiveValidations)
      .withTransformationPipelines(
        List(
          JsonSchemaTransformationPipeline(),
          JsonSchemaEditingPipeline(),
          JsonSchemaCachePipeline()
        )
      )
}
