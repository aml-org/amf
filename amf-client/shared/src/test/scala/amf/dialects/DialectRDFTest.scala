package amf.dialects
import amf.core.emitter.RenderOptions
import amf.core.remote.{Amf, Aml, VocabularyYamlHint}
import amf.core.unsafe.PlatformSecrets
import amf.io.{FunSuiteCycleTests, FunSuiteRdfCycleTests}

import scala.concurrent.ExecutionContext

class DialectRDFTest extends FunSuiteRdfCycleTests with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  val productionPath                                       = "amf-client/shared/src/test/resources/vocabularies2/production/"

  override def basePath: String = "amf-client/shared/src/test/resources/vocabularies2/dialects/"

  test("RDF 1 test") {
    cycleFullRdf("example1.raml", "example1.raml", VocabularyYamlHint, Aml, basePath)
  }

  test("RDF 2 test") {
    cycleFullRdf("example2.raml", "example2.raml", VocabularyYamlHint, Aml, basePath)
  }

  multiGoldenTest("RDF 3 test", "example3.%s") { config =>
    cycleFullRdf("example3.raml",
                 config.golden,
                 VocabularyYamlHint,
                 target = Amf,
                 directory = basePath,
                 renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("RDF 13 test", "example13.%s") { config =>
    cycleFullRdf("example13.raml",
                 config.golden,
                 VocabularyYamlHint,
                 target = Amf,
                 directory = basePath,
                 renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("RDF Production system2 dialect ex1  test", "dialectex1.%s") { config =>
    cycleFullRdf("dialectex1.raml",
                 config.golden,
                 VocabularyYamlHint,
                 target = Amf,
                 directory = s"${productionPath}system2/",
                 renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("RDF Production system2 dialect ex2  test", "dialectex2.%s") { config =>
    cycleFullRdf("dialectex2.raml",
                 config.golden,
                 VocabularyYamlHint,
                 target = Amf,
                 directory = s"${productionPath}system2/",
                 renderOptions = Some(config.renderOptions))
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps
}
