package amf.cycle

import amf.client.remod.AMFGraphConfiguration
import amf.core.CompilerContextBuilder
import amf.core.model.document.BaseUnit
import amf.core.parser.UnspecifiedReference
import amf.core.parser.errorhandler.{ParserErrorHandler, UnhandledParserErrorHandler}
import amf.core.remote.{Amf, OasYamlHint}
import amf.core.services.RuntimeCompiler
import amf.facades.Validation
import amf.io.FunSuiteCycleTests

import scala.concurrent.{ExecutionContext, Future}

class OasRecursiveFilesCycleTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-client/shared/src/test/resources/references/oas/oas-references/"

  test("YAML OAS 2.0 with recursive file dependency doesn't output unresolved shape") {
    cycle("oas-2-root.yaml", "oas-2-root.jsonld", OasYamlHint, Amf)
  }

  test("YAML OAS 2.0 with recursive file dependency doesn't output unresolved shape starting from ref") {
    cycle("oas-2-ref.yaml", "oas-2-ref.jsonld", OasYamlHint, Amf)
  }

  test("YAML OAS 3.0 with recursive file dependency doesn't output unresolved shape") {
    cycle("oas-3-root.yaml", "oas-3-root.jsonld", OasYamlHint, Amf)
  }

  test("YAML OAS 3.0 with recursive file dependency doesn't output unresolved shape starting from ref") {
    cycle("oas-3-ref.yaml", "oas-3-ref.jsonld", OasYamlHint, Amf)
  }

  /** Method to parse unit. Override if necessary. */
  override def build(config: CycleConfig,
                     eh: Option[ParserErrorHandler],
                     useAmfJsonldSerialisation: Boolean): Future[BaseUnit] = {
    Validation(platform).flatMap { _ =>
      implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
      val finalPath =
        if (config.sourcePath.startsWith("file://")) config.sourcePath else s"file://${config.sourcePath}"
      val compilerContextBuilder = new CompilerContextBuilder(s"$finalPath", platform, UnhandledParserErrorHandler)

      RuntimeCompiler
        .forContext(
          compilerContextBuilder.build(),
          None,
          None,
          UnspecifiedReference
        )
        .map(m => m)
    }
  }
}
