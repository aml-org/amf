package amf.cycle

import amf.client.remod.{AMFGraphConfiguration, ParseConfiguration}
import amf.core.CompilerContextBuilder
import amf.core.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.model.document.BaseUnit
import amf.core.parser.UnspecifiedReference
import amf.core.remote.{Amf, Oas30YamlHint}
import amf.core.services.RuntimeCompiler
import amf.facades.Validation
import amf.io.FunSuiteCycleTests

import scala.concurrent.{ExecutionContext, Future}

class OasRecursiveFilesCycleTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-client/shared/src/test/resources/references/oas/oas-references/"

  test("YAML OAS 2.0 with recursive file dependency doesn't output unresolved shape") {
    cycle("oas-2-root.yaml", "oas-2-root.jsonld", Oas30YamlHint, Amf)
  }

  test("YAML OAS 2.0 with recursive file dependency doesn't output unresolved shape starting from ref") {
    cycle("oas-2-ref.yaml", "oas-2-ref.jsonld", Oas30YamlHint, Amf)
  }

  test("YAML OAS 3.0 with recursive file dependency doesn't output unresolved shape") {
    cycle("oas-3-root.yaml", "oas-3-root.jsonld", Oas30YamlHint, Amf)
  }

  test("YAML OAS 3.0 with recursive file dependency doesn't output unresolved shape starting from ref") {
    cycle("oas-3-ref.yaml", "oas-3-ref.jsonld", Oas30YamlHint, Amf)
  }

  /** Method to parse unit. Override if necessary. */
  override def build(config: CycleConfig,
                     eh: Option[AMFErrorHandler],
                     useAmfJsonldSerialisation: Boolean): Future[BaseUnit] = {
    Validation(platform).flatMap { _ =>
      implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
      val finalPath =
        if (config.sourcePath.startsWith("file://")) config.sourcePath else s"file://${config.sourcePath}"
      val compilerContextBuilder =
        new CompilerContextBuilder(platform,
                                   ParseConfiguration(AMFGraphConfiguration.fromEH(UnhandledErrorHandler), finalPath))

      RuntimeCompiler
        .forContext(
          compilerContextBuilder.build(),
          None,
          UnspecifiedReference
        )
        .map(m => m)
    }
  }
}
