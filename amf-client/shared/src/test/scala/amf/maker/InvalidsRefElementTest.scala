package amf.maker

import amf.compiler.CompilerTestBuilder
import amf.core.annotations.SourceAST
import amf.core.model.document.Document
import amf.core.remote.{Oas20YamlHint, Raml10YamlHint}
import amf.facades.Validation
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorResponse
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.webapi.models.api.WebApi
import amf.plugins.domain.webapi.models.security.SecurityScheme
import amf.plugins.domain.webapi.models.templates.{ParametrizedResourceType, ParametrizedTrait}
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class InvalidsRefElementTest extends AsyncFunSuite with CompilerTestBuilder {

  override val executionContext: ExecutionContext = global

  test("Invalid link to Response with ast") {
    Validation(platform)
      .flatMap(v => {

        build("file://amf-client/shared/src/test/resources/invalids/error-response.oas",
              Oas20YamlHint,
              validation = Some(v))
      })
      .map(unit => {
        val api      = unit.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
        val response = api.endPoints.head.operations.head.responses.head
        assert(response.linkTarget.isDefined)
        assert(response.linkTarget.get.isInstanceOf[ErrorResponse])
        assert(response.linkTarget.get.annotations.contains(classOf[SourceAST]))
        assert(response.linkTarget.get.id.startsWith("http://amferror.com/#errorResponse/"))
      })
  }

  test("Invalid link to Trait with ast") {
    Validation(platform)
      .flatMap(v => {

        build("file://amf-client/shared/src/test/resources/invalids/error-trait.raml",
              Raml10YamlHint,
              validation = Some(v))
      })
      .map(unit => {
        val api      = unit.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
        val badTrait = api.endPoints.head.operations.head.extend.head
        assert(badTrait.asInstanceOf[ParametrizedTrait].target.linkTarget.isDefined)
        assert(badTrait.asInstanceOf[ParametrizedTrait].target.linkTarget.get.annotations.contains(classOf[SourceAST]))
        assert(
          badTrait
            .asInstanceOf[ParametrizedTrait]
            .target
            .linkTarget
            .get
            .id
            .startsWith("http://amferror.com/#errorTrait/"))
      })
  }

  test("Invalid link to resource type with ast") {
    Validation(platform)
      .flatMap(v => {

        build("file://amf-client/shared/src/test/resources/invalids/error-resource-type.raml",
              Raml10YamlHint,
              validation = Some(v))
      })
      .map(unit => {
        val api         = unit.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
        val badResource = api.endPoints.head.extend.head
        assert(badResource.asInstanceOf[ParametrizedResourceType].target.linkTarget.isDefined)
        assert(
          badResource
            .asInstanceOf[ParametrizedResourceType]
            .target
            .linkTarget
            .get
            .annotations
            .contains(classOf[SourceAST]))
        assert(
          badResource
            .asInstanceOf[ParametrizedResourceType]
            .target
            .linkTarget
            .get
            .id
            .startsWith("http://amferror.com/#errorResourceType/"))
      })
  }

  test("Invalid link to security scheme type with ast") {
    Validation(platform)
      .flatMap(v => {

        build("file://amf-client/shared/src/test/resources/invalids/error-security-scheme.raml",
              Raml10YamlHint,
              validation = Some(v))
      })
      .map(unit => {
        val declaration = unit.asInstanceOf[Document].declares.head
        assert(declaration.isInstanceOf[SecurityScheme])
        val scheme = declaration.asInstanceOf[SecurityScheme]
        assert(scheme.linkTarget.isDefined)
        assert(scheme.linkTarget.get.annotations.contains(classOf[SourceAST]))
        assert(scheme.linkTarget.get.id.startsWith("http://amferror.com/#errorSecurityScheme/"))
      })
  }
  test("Invalid link to named example with ast") {
    Validation(platform)
      .flatMap(v => {

        build("file://amf-client/shared/src/test/resources/invalids/error-named-example.raml",
              Raml10YamlHint,
              validation = Some(v))
      })
      .map(unit => {
        val declaration = unit.asInstanceOf[Document].declares.head
        assert(declaration.isInstanceOf[AnyShape])
        val shape   = declaration.asInstanceOf[AnyShape]
        val example = shape.examples.head
        assert(example.linkTarget.isDefined)
        assert(example.linkTarget.get.annotations.contains(classOf[SourceAST]))
        assert(example.linkTarget.get.id.startsWith("http://amferror.com/#errorNamedExample/"))
      })
  }

  // add creative work error and parameter error when starts to be used.

}
