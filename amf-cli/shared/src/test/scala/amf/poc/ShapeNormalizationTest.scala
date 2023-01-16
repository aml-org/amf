package amf.poc
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.IgnoringErrorHandler
import amf.core.internal.remote.{AmfJsonHint, Oas20YamlHint, Raml10, Raml10YamlHint}
import amf.resolution.ResolutionTest

class ShapeNormalizationTest extends ResolutionTest{
  val basePath = "amf-cli/shared/src/test/resources/poc/"
  override def renderOptions(): RenderOptions = RenderOptions().withPrettyPrint.withSourceMaps.withFlattenedJsonLd

  test("Simple inheritance") {
    cycle(
      "simple-inheritance.raml",
      "simple-inheritance.resolved.raml",
      Raml10YamlHint,
      Raml10YamlHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler)
    )
  }

  test("Property type inheritance") {
    cycle(
      "property-type-inheritance.raml",
      "property-type-inheritance.resolved.raml",
      Raml10YamlHint,
      Raml10YamlHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler)
    )
  }

  test("Complex inheritance") {
    cycle(
      "complex-inheritance-unions.raml",
      "complex-inheritance-unions.resolved.raml",
      Raml10YamlHint,
      Raml10YamlHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler)
    )
  }

  test("Recursion optional property") {
    cycle(
      "recursion-valid-optional-property.raml",
      "recursion-valid-optional-property.resolved.jsonld",
      Raml10YamlHint,
      AmfJsonHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler)
    )
  }

  test("Recursion minItems zero") {
    cycle(
      "recursion-valid-minitems-zero.raml",
      "recursion-valid-minitems-zero.resolved.jsonld",
      Raml10YamlHint,
      AmfJsonHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler)
    )
  }

  test("Recursion additionalProperties") {
    cycle(
      "recursion-valid-additionalproperties.yaml",
      "recursion-valid-additionalproperties.resolved.jsonld",
      Oas20YamlHint,
      AmfJsonHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler)
    )
  }

  test("Recursion optional type") {
    cycle(
      "recursion-valid-optional-type.raml",
      "recursion-valid-optional-type.resolved.jsonld",
      Raml10YamlHint,
      AmfJsonHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler)
    )
  }

  test("Recursion in inherited property") {
    cycle(
      "recursion-in-inherited-property.raml",
      "recursion-in-inherited-property.resolved.jsonld",
      Raml10YamlHint,
      AmfJsonHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler)
    )
  }

  test("Recursion optional property complex") {
    cycle(
      "recursion-invalid-optional-property-complex.raml",
      "recursion-invalid-optional-property-complex.resolved.jsonld",
      Raml10YamlHint,
      AmfJsonHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler),
      transformWith = Some(Raml10)
    )
  }

  test("Recursion mandatory property") {
    cycle(
      "recursion-invalid-mandatory-property.raml",
      "recursion-invalid-mandatory-property.resolved.jsonld",
      Raml10YamlHint,
      AmfJsonHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler),
      transformWith = Some(Raml10)
    )
  }

  test("Recursion minItems > zero") {
    cycle(
      "recursion-invalid-minitems-no-zero.raml",
      "recursion-invalid-minitems-no-zero.resolved.jsonld",
      Raml10YamlHint,
      AmfJsonHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler),
      transformWith = Some(Raml10)
    )
  }

  test("Recursion mandatory type") {
    cycle(
      "recursion-invalid-mandatory-type.raml",
      "recursion-invalid-mandatory-type.resolved.jsonld",
      Raml10YamlHint,
      AmfJsonHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler),
      transformWith = Some(Raml10)
    )
  }

  test("Recursion inheritance cycle") {
    cycle(
      "recursion-inheritance-cycle.raml",
      "recursion-inheritance-cycle.resolved.jsonld",
      Raml10YamlHint,
      AmfJsonHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler),
      transformWith = Some(Raml10)
    )
  }

  test("Recursion valid union") {
    cycle(
      "recursion-valid-union.raml",
      "recursion-valid-union.resolved.jsonld",
      Raml10YamlHint,
      AmfJsonHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler),
      transformWith = Some(Raml10)
    )
  }

  test("Recursion invalid union") {
    cycle(
      "recursion-invalid-union.raml",
      "recursion-invalid-union.resolved.jsonld",
      Raml10YamlHint,
      AmfJsonHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler),
      transformWith = Some(Raml10)
    )
  }

  test("Recursion invalid nested union") {
    cycle(
      "recursion-invalid-union-nested.raml",
      "recursion-invalid-union-nested.resolved.jsonld",
      Raml10YamlHint,
      AmfJsonHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler),
      transformWith = Some(Raml10)
    )
  }

  test("Recursion valid nested union (exit at first union)") {
    cycle(
      "recursion-valid-union-nested-exit-at-first.raml",
      "recursion-valid-union-nested-exit-at-first.resolved.jsonld",
      Raml10YamlHint,
      AmfJsonHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler),
      transformWith = Some(Raml10)
    )
  }

  test("Recursion valid nested union (exit at second union)") {
    cycle(
      "recursion-valid-union-nested-exit-at-second.raml",
      "recursion-valid-union-nested-exit-at-second.resolved.jsonld",
      Raml10YamlHint,
      AmfJsonHint,
      pipeline = Option(PipelineId.Editing),
      eh = Some(IgnoringErrorHandler),
      transformWith = Some(Raml10)
    )
  }
}
