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
import amf.shapes.internal.validation.payload.JsonSchemaPayloadValidationPlugin
import amf.shapes.internal.validation.shacl.JsonSchemaShaclModelValidationPlugin

object JsonSchemaConfiguration {
  def JsonSchema(): ShapesConfiguration =
    ShapesConfiguration
      .predefined()
      .withPlugins(
        List(
          JsonSchemaParsePlugin,
          JsonSchemaRenderPlugin,
          JsonSchemaPayloadValidationPlugin(),
          JsonSchemaShaclModelValidationPlugin()
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
