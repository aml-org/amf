package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.metamodel.document.DocumentModel
import amf.core.model.document.BaseUnit
import amf.core.parser.errorhandler.{ParserErrorHandler, UnhandledParserErrorHandler}
import amf.core.remote._
import amf.core.resolution.stages.ReferenceResolutionStage
import amf.facades.{AMFCompiler, Validation}
import org.scalatest.Assertion

import scala.concurrent.Future

/**
  * This unit tests run as one, we need several steps to check that the dumped json ld, after resolve types, it's correct.
  * That means, that not only the graph can be parsed, but also it's similar to the resolved model dumped as raml.
  * In order to check that, first dump a raml to jsonld to clean the annotations. Then, we parse that jsonld, resolve the model and dump it to raml.
  * We do that, to get a raml api generated from a resolved model without annotations
  * After check that, we parse the resolved jsonld model, and generates the raml, to check that the final raml it's equivalent to the raml resolved and dumped.
  * */
class ProductionServiceTest extends RamlResolutionTest {

  override def build(config: CycleConfig,
                     eh: Option[ParserErrorHandler],
                     useAmfJsonldSerialization: Boolean): Future[BaseUnit] = {
    Validation(platform).flatMap { _ =>
      AMFCompiler(s"file://${config.sourcePath}", platform, config.hint, eh = UnhandledParserErrorHandler).build()
    }
  }
  private def dummyFunc: (BaseUnit, CycleConfig) => BaseUnit =
    (u: BaseUnit, _: CycleConfig) => u

  override val basePath =
    "amf-client/shared/src/test/resources/production/resolution-dumpjsonld/"

  /* Generate the jsonld from a resolved raml */
  multiGoldenTest("Test step1: resolve and emit jsonld", "api.resolved.raml.%s") { config =>
    run("api.raml",
        config.golden,
        Raml10YamlHint,
        target = Amf,
        tFn = transform,
        renderOptions = Some(config.renderOptions))
  }

  /* Generate the api resolved directly, without serialize the jsonld */
  test("Test step2: resolve and emit raml") {
    run("api.raml", "api.resolved.raml", Raml10YamlHint, target = Raml10, tFn = transform)
  }

  /* Generate the jsonld without resolve (to clean the annotations) */
  multiGoldenTest("Test step3: emit jsonld without resolve", "api.raml.%s") { config =>
    run("api.raml",
        config.golden,
        Raml10YamlHint,
        target = Amf,
        tFn = dummyFunc,
        renderOptions = Some(config.renderOptions))
  }

  /* Generate the resolved raml after read the jsonld(without annotations) */
  multiSourceTest("Test step4: emit jsonld with resolve", "api.raml.%s") { config =>
    run(config.source, "api.raml.jsonld.resolved.raml", AmfJsonHint, target = Raml10, tFn = transform)
  }

  /* Now we really test the case, parse the json ld and compare to a similar jsonld (this should have the declarations) */
  multiTest("Test step5: parse resolved api and dump raml", "api.resolved.raml.%s", "api.resolved.%s.raml") { config =>
    run(config.source,
        config.golden,
        AmfJsonHint,
        target = Raml10,
        tFn = dummyFunc,
        renderOptions = Some(config.renderOptions))
  }

  /* Generate the raml from a json ld without resolve */
  multiTest("Test step6: parse resolved api and dump raml", "api.raml.%s", "api.%s.raml") { config =>
    run(config.source, config.golden, AmfJsonHint, target = Raml10, tFn = dummyFunc)
  }

  /* Generate the raml from a jsonld resolved raml */
  multiTest("Test step7: emit resolved jsonld and check against normal raml",
            "api.resolved.raml.%s",
            "api.resolved.%s.raml") { config =>
    run(config.source,
        config.golden,
        AmfJsonHint,
        target = Raml10,
        tFn = dummyFunc,
        renderOptions = Some(config.renderOptions))
  }

  /* Generate the raml api from a resolved raml to jsonld cleaning the declarations and refs stage */
  multiSourceTest("Test step8: emit resolved jsonld and check against normal raml", "api.resolved.raml.%s") { config =>
    run(
      config.source,
      "api.raml.jsonld.resolved.raml",
      AmfJsonHint,
      target = Raml10,
      tFn = (u: BaseUnit, _: CycleConfig) => {
        val resolved = new ReferenceResolutionStage(false)(UnhandledErrorHandler).resolve(u)
        resolved.fields.removeField(DocumentModel.Declares)
        resolved
      }
    )
  }

  def run(source: String,
          golden: String,
          hint: Hint,
          target: Vendor,
          tFn: (BaseUnit, CycleConfig) => BaseUnit,
          renderOptions: Option[RenderOptions] = None): Future[Assertion] = {

    val config = CycleConfig(source, golden, hint, target, basePath, None, None)

    build(config, None, renderOptions.forall(_.isAmfJsonLdSerilization))
      .map(tFn(_, config))
      .flatMap {
        renderOptions match {
          case Some(options) =>
            render(_, config, options)
          case None =>
            render(_, config, useAmfJsonldSerialization = true)
        }
      }
      .flatMap(writeTemporaryFile(golden))
      .flatMap(assertDifferences(_, config.goldenPath))
  }
}
