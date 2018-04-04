package amf.dialects

import amf.core.remote._
import amf.facades.{AMFCompiler, Validation}
import amf.io.BuildCycleTests

import scala.concurrent.ExecutionContext

class DialectProductionTest extends BuildCycleTests {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/vocabularies2/production/"

  test("Can parse validation dialect"){
    cycle("validation_dialect.raml", "validation_dialect.json", VocabularyYamlHint, Amf)
  }

  test("Can parse validation dialect instance"){
    withDialect("validation_dialect.raml", "validation_instance1.raml", "validation_instance1.raml.raml", VocabularyYamlHint, RamlVocabulary)
  }

  test("Can parse validation dialect cfg1 instance"){
    withDialect("example1.raml", "example1_instance.raml", "example1_instance.jsonld", VocabularyYamlHint, Amf, basePath + "cfg/")
  }

  test("Can parse validation dialect cfg2 instance"){
    withDialect("example2.raml", "example2_instance.raml", "example2_instance.jsonld", VocabularyYamlHint, Amf, basePath + "cfg/")
  }

  test("Can parse validation dialect cfg3 instance"){
    withDialect("example3.raml", "example3_instance.raml", "example3_instance.jsonld", VocabularyYamlHint, Amf, basePath + "cfg/")
  }


  protected def withDialect(dialect: String,
                            source: String,
                            golden: String,
                            hint: Hint,
                            target: Vendor,
                            directory: String = basePath) = {
    for {
      v   <- Validation(platform).map(_.withEnabledValidation(false))
      _   <- AMFCompiler(s"file://$directory/$dialect", platform, VocabularyYamlHint, v).build()
      res <- cycle(source, golden, hint, target, directory)
    } yield {
      res
    }
  }
}
