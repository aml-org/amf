package amf.ls

import amf.client.environment.AMFConfiguration
import amf.compiler.CompilerTestBuilder
import amf.core.client.scala.errorhandling.DefaultErrorHandler
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.templates.ParametrizedDeclaration
import amf.core.internal.remote.Raml10YamlHint
import amf.plugins.domain.apicontract.models.api.WebApi
import amf.plugins.domain.apicontract.models.templates.{ResourceType, Trait}
import amf.plugins.domain.shapes.models.NodeShape
import org.scalatest.AsyncFunSuite
import org.scalatest.Matchers._

import scala.concurrent.ExecutionContext

class LanguageServerTest extends AsyncFunSuite with CompilerTestBuilder {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val file = "file://amf-cli/shared/src/test/resources/ls/resource-type-trait.raml"

  override def defaultConfig: AMFConfiguration =
    super.defaultConfig.withErrorHandlerProvider(() => DefaultErrorHandler())
  test("Parse resource type from model.") {
    build(file, Raml10YamlHint)
      .map(_.asInstanceOf[Document])
      .map { model =>
        model.declares
          .collectFirst { case rt: ResourceType => rt }
          .map { rt =>
            val endPoint = rt.asEndpoint(model)
            endPoint.description.value() should be("The collection of <<resourcePathName>>")
            succeed
          }
          .getOrElse(succeed)
      }
  }

  test("Parse trait from model.") {
    build(file, Raml10YamlHint)
      .map(_.asInstanceOf[Document])
      .map { model =>
        model.declares
          .collectFirst { case tr: Trait => tr }
          .map { rt =>
            val op = rt.asOperation(model)
            op.description.value() should be("Some requests require authentication.")
            succeed
          }
          .getOrElse(succeed)
      }
  }

  test("Parse variable in query param") {
    val file = "file://amf-cli/shared/src/test/resources/ls/trait_error1.raml"
    build(file, Raml10YamlHint)
      .map(_.asInstanceOf[Document])
      .map { model =>
        model.declares
          .collectFirst { case tr: Trait => tr }
          .map { rt =>
            val op = rt.asOperation(model)
            op.request.queryParameters shouldNot be(empty)
            succeed
          }
          .getOrElse(succeed)
      }
  }

  test("Parse variable in nested type") {
    val file = "file://amf-cli/shared/src/test/resources/ls/trait_error2.raml"
    build(file, Raml10YamlHint)
      .map(_.asInstanceOf[Document])
      .map { model =>
        model.declares
          .collectFirst { case rt: ResourceType => rt }
          .map { rt =>
            val op    = rt.asEndpoint(model)
            val props = op.operations.last.responses.head.payloads.head.schema.asInstanceOf[NodeShape].properties
            assert(Option(props.find(_.name.is("p2")).get.range).isEmpty)
            succeed
          }
          .getOrElse(succeed)
      }
  }

  test("Error in trait 3") {
    val file = "file://amf-cli/shared/src/test/resources/ls/trait_error3.raml"
    build(file, Raml10YamlHint)
      .map(_.asInstanceOf[Document])
      .map { model =>
        val res = model.encodes
          .asInstanceOf[WebApi]
          .endPoints
          .head
          .operations
          .head
          .extend
          .head
          .asInstanceOf[ParametrizedDeclaration]
          .target
          .asInstanceOf[Trait]
          .asOperation(model)
        assert(Option(res).isDefined)
        succeed
      }
  }

  test("Trait with unresolved reference") {
    val file = "file://amf-cli/shared/src/test/resources/ls/trait-unresolved.raml"
    build(file, Raml10YamlHint)
      .map(_.asInstanceOf[Document])
      .map { model =>
        val res = model.encodes
          .asInstanceOf[WebApi]
          .endPoints
          .head
          .operations
          .head
          .extend
          .head
          .asInstanceOf[ParametrizedDeclaration]
          .target
          .asInstanceOf[Trait]
          .asOperation(model)
        assert(res.fields.fields().isEmpty)
      }
  }
}
