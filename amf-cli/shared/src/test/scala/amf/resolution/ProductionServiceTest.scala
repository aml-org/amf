package amf.resolution

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.transform.stages.ReferenceResolutionStage
import amf.core.internal.metamodel.document.DocumentModel
import amf.core.internal.remote._
import amf.testing.ConfigProvider.configFor
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

  override def build(config: CycleConfig, amfConfig: AMFGraphConfiguration): Future[BaseUnit] = {
    super.build(config, amfConfig.withErrorHandlerProvider(() => UnhandledErrorHandler))

  }
  private def dummyFunc: (BaseUnit, CycleConfig, AMFConfiguration) => BaseUnit =
    (u: BaseUnit, _: CycleConfig, _: AMFConfiguration) => u

  override val basePath =
    "amf-cli/shared/src/test/resources/production/resolution-dumpjsonld/"

  /* Generate the jsonld from a resolved raml */
  multiGoldenTest("Test step1: resolve and emit jsonld", "api.resolved.raml.%s") { config =>
    run("api.raml",
        config.golden,
        Raml10YamlHint,
        target = AmfJsonHint,
        tFn = transform,
        renderOptions = Some(config.renderOptions))
  }

  /* Generate the api resolved directly, without serialize the jsonld */
  test("Test step2: resolve and emit raml") {
    run("api.raml", "api.resolved.raml", Raml10YamlHint, target = Raml10YamlHint, tFn = transform)
  }

  /* Generate the jsonld without resolve (to clean the annotations) */
  multiGoldenTest("Test step3: emit jsonld without resolve", "api.raml.%s") { config =>
    run("api.raml",
        config.golden,
        Raml10YamlHint,
        target = AmfJsonHint,
        tFn = dummyFunc,
        renderOptions = Some(config.renderOptions))
  }

  /* Generate the resolved raml after read the jsonld(without annotations) */
  multiSourceTest("Test step4: emit jsonld with resolve", "api.raml.%s") { config =>
    run(config.source, "api.raml.jsonld.resolved.raml", AmfJsonHint, target = Raml10YamlHint, tFn = transform)
  }

  /* Now we really test the case, parse the json ld and compare to a similar jsonld (this should have the declarations) */
  multiTest("Test step5: parse resolved api and dump raml", "api.resolved.raml.%s", "api.resolved.%s.raml") { config =>
    run(config.source,
        config.golden,
        AmfJsonHint,
        target = Raml10YamlHint,
        tFn = dummyFunc,
        renderOptions = Some(config.renderOptions))
  }

  /* Generate the raml from a json ld without resolve */
  multiTest("Test step6: parse resolved api and dump raml", "api.raml.%s", "api.%s.raml") { config =>
    run(config.source, config.golden, AmfJsonHint, target = Raml10YamlHint, tFn = dummyFunc)
  }

  /* Generate the raml from a jsonld resolved raml */
  multiTest("Test step7: emit resolved jsonld and check against normal raml",
            "api.resolved.raml.%s",
            "api.resolved.%s.raml") { config =>
    run(config.source,
        config.golden,
        AmfJsonHint,
        target = Raml10YamlHint,
        tFn = dummyFunc,
        renderOptions = Some(config.renderOptions))
  }

  /* Generate the raml api from a resolved raml to jsonld cleaning the declarations and refs stage */
  multiSourceTest("Test step8: emit resolved jsonld and check against normal raml", "api.resolved.raml.%s") { config =>
    run(
      config.source,
      "api.raml.jsonld.resolved.raml",
      AmfJsonHint,
      target = Raml10YamlHint,
      tFn = (u: BaseUnit, _: CycleConfig, _: AMFConfiguration) => {
        val resolved = new ReferenceResolutionStage(false).transform(u, UnhandledErrorHandler)
        resolved.fields.removeField(DocumentModel.Declares)
        resolved
      }
    )
  }

  def run(source: String,
          golden: String,
          hint: Hint,
          target: Hint,
          tFn: (BaseUnit, CycleConfig, AMFConfiguration) => BaseUnit,
          renderOptions: Option[RenderOptions] = None): Future[Assertion] = {

    val config       = CycleConfig(source, golden, hint, target, basePath, None, None)
    val amfConfig    = buildConfig(renderOptions, None)
    val renderConfig = buildConfig(configFor(target.vendor), renderOptions, None)
    build(config, amfConfig)
      .map(tFn(_, config, amfConfig))
      .map { render(_, config, renderConfig) }
      .flatMap(writeTemporaryFile(golden))
      .flatMap(assertDifferences(_, config.goldenPath))
  }
}
