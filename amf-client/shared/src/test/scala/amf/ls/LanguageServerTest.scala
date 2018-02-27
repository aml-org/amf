package amf.ls

import amf.compiler.CompilerTestBuilder
import amf.core.model.document.Document
import amf.core.remote.RamlYamlHint
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import org.scalatest.AsyncFunSuite
import org.scalatest.Matchers._

import scala.concurrent.ExecutionContext

class LanguageServerTest extends AsyncFunSuite with CompilerTestBuilder {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val file = "file://amf-client/shared/src/test/resources/ls/resource-type-trait.raml"

  test("Parse resource type from model.") {
    build(file, RamlYamlHint)
      .map(_.asInstanceOf[Document])
      .map { model =>
        model.declares
          .collectFirst { case rt: ResourceType => rt }
          .map { rt =>
            val endPoint = rt.asEndpoint(model)
            endPoint.description should be("The collection of <<resourcePathName>>")
            succeed
          }
          .getOrElse(succeed)
      }
  }

  test("Parse trait from model.") {
    build(file, RamlYamlHint)
      .map(_.asInstanceOf[Document])
      .map { model =>
        model.declares
          .collectFirst { case tr: Trait => tr }
          .map { rt =>
            val op = rt.asOperation(model)
            op.description should be("Some requests require authentication.")
            succeed
          }
          .getOrElse(succeed)
      }
  }
}
