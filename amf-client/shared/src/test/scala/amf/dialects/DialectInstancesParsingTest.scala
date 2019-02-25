package amf.dialects

import amf.core.remote._
import amf.facades.{AMFCompiler, Validation}
import amf.io.FunSuiteCycleTests
import org.scalatest.Assertion

import scala.concurrent.{ExecutionContext, Future}

trait DialectInstancesParsingTest extends FunSuiteCycleTests {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/vocabularies2/instances/"

  if (platform.name == "jvm") {
    test("parse 1 test") {
      withDialect("dialect1.raml",
                  "example1.raml",
                  "example1.json",
                  VocabularyYamlHint,
                  Amf,
                  useAmfJsonldSerialisation = false)
    }

    test("parse 1b test") {
      withDialect("dialect1.raml",
                  "example1b.raml",
                  "example1b.json",
                  VocabularyYamlHint,
                  Amf,
                  useAmfJsonldSerialisation = false)
    }
  }

  test("parse 1 (AMF) test") {
    withDialect("dialect1.raml", "example1.raml", "example1.amf.json", VocabularyYamlHint, Amf)
  }

  test("parse 1b (AMF) test") {
    withDialect("dialect1.raml", "example1b.raml", "example1b.amf.json", VocabularyYamlHint, Amf)
  }

  test("parse 1 with annotations test") {
    withDialect("dialect1.raml", "example1_annotations.yaml", "example1_annotations.json", VocabularyYamlHint, Amf)
  }

  test("parse 2 test") {
    withDialect("dialect2.raml", "example2.raml", "example2.json", VocabularyYamlHint, Amf)
  }

  test("parse 3 test") {
    withDialect("dialect3.raml", "example3.raml", "example3.json", VocabularyYamlHint, Amf)
  }

  test("parse 4 test") {
    withDialect("dialect4.raml", "example4.raml", "example4.json", VocabularyYamlHint, Amf)
  }

  test("parse 5 test") {
    withDialect("dialect5.raml", "example5.raml", "example5.json", VocabularyYamlHint, Amf)
  }

  test("parse 6 test") {
    withDialect("dialect6.raml", "example6.raml", "example6.json", VocabularyYamlHint, Amf)
  }

  test("parse 6b $ref test") {
    withDialect("dialect6.raml", "example6b.raml", "example6b.json", VocabularyYamlHint, Amf)
  }

  test("parse 7 test") {
    withDialect("dialect7.raml", "example7.raml", "example7.json", VocabularyYamlHint, Amf)
  }

  test("parse 8 test") {
    withDialect("dialect8.raml", "example8.raml", "example8.json", VocabularyYamlHint, Amf)
  }

  test("parse 8b $include test") {
    withDialect("dialect8.raml", "example8b.yaml", "example8b.json", VocabularyYamlHint, Amf)
  }

  test("parse 8c $ref test") {
    withDialect("dialect8.raml", "example8c.yaml", "example8c.json", VocabularyYamlHint, Amf)
  }

  test("parse 9 test") {
    withDialect("dialect9.raml", "example9.raml", "example9.json", VocabularyYamlHint, Amf)
  }

  test("parse 9b $ref test") {
    withDialect("dialect9.raml", "example9b.raml", "example9b.json", VocabularyYamlHint, Amf)
  }

  test("parse 10a test") {
    withDialect("dialect10.raml", "example10a.raml", "example10a.json", VocabularyYamlHint, Amf)
  }

  test("parse 10b test") {
    withDialect("dialect10.raml", "example10b.raml", "example10b.json", VocabularyYamlHint, Amf)
  }

  test("parse 10c test") {
    withDialect("dialect10.raml", "example10c.raml", "example10c.json", VocabularyYamlHint, Amf)
  }

  test("parse 11 test") {
    withDialect("dialect11.raml", "example11.raml", "example11.json", VocabularyYamlHint, Amf)
  }

  test("parse 12 test") {
    withInlineDialect("example12.raml", "example12.json", VocabularyYamlHint, Amf)
  }

  test("parse 13 test") {
    withDialect("dialect13.raml", "example13.raml", "example13.json", VocabularyYamlHint, Amf)
  }

  test("parse 14 test") {
    withDialect("dialect14.raml", "example14.raml", "example14.json", VocabularyYamlHint, Amf)
  }

  test("parse 15 test") {
    withDialect("dialect15a.raml", "example15.raml", "example15.json", VocabularyYamlHint, Amf)
  }

  test("parse 16 test") {
    withDialect("dialect16a.raml", "example16a.raml", "example16a.json", VocabularyYamlHint, Amf)
  }

  test("parse 16 $include test") {
    withDialect("dialect16a.raml", "example16c.yaml", "example16c.json", VocabularyYamlHint, Amf)
  }

  test("parse 17 test") {
    withDialect("dialect17.input.json", "example17.input.json", "example17.output.json", VocabularyJsonHint, Amf)
  }

  test("parse 18 test") {
    withDialect("dialect18.raml", "example18.raml", "example18.json", VocabularyYamlHint, Amf)
  }

  test("parse 19 test") {
    withDialect("dialect19.raml", "example19.raml", "example19.json", VocabularyYamlHint, Amf)
  }

  test("parse 20 test") {
    withDialect("dialect20.raml", "example20.raml", "example20.json", VocabularyYamlHint, Amf)
  }

  test("parse 21a test") {
    withDialect("dialect21.raml", "patch21.raml", "patch21.json", VocabularyYamlHint, Amf)
  }

  test("parse 21b test") {
    withDialect("dialect21.raml", "patch21b.raml", "patch21b.json", VocabularyYamlHint, Amf)
  }

  test("parse 23 test") {
    withDialect("dialect23.raml", "example23.raml", "example23.json", VocabularyYamlHint, Amf)
  }

  if (platform.name == "jvm") {
    test("parse 23 (non-amf) test") {
      withDialect("dialect23.raml",
                  "example23.raml",
                  "example23.rdf.json",
                  VocabularyYamlHint,
                  Amf,
                  useAmfJsonldSerialisation = false)
    }
  }

  test("parse 24 test") {
    withDialect("dialect24.raml", "example24.raml", "example24.json", VocabularyYamlHint, Amf)
  }

  test("parse 24b test") {
    withDialect("dialect24.raml", "example24b.raml", "example24b.json", VocabularyYamlHint, Amf)
  }

  test("parse 24c test") {
    withDialect("dialect24.raml", "example24c.raml", "example24c.json", VocabularyYamlHint, Amf)
  }

  test("parse 27a test") {
    withDialect("dialect27.raml", "example27a.raml", "example27a.json", VocabularyYamlHint, Amf)
  }

  if (platform.name == "jvm") {
    test("generate 1 test") {
      withDialect("dialect1.raml",
                  "example1.json",
                  "example1.raml",
                  AmfJsonHint,
                  Aml,
                  useAmfJsonldSerialisation = false)
    }
  }

  test("generate 1 (AMF) test") {
    withDialect("dialect1.raml", "example1.amf.json", "example1.raml", AmfJsonHint, Aml)
  }

  test("generate 1b (AMF) test") {
    withDialect("dialect1.raml", "example1b.amf.json", "example1b.raml", AmfJsonHint, Aml)
  }

  test("generate 1b test") {
    withDialect("dialect1.raml",
                "example1b.json",
                "example1b.raml",
                AmfJsonHint,
                Aml,
                useAmfJsonldSerialisation = false)
  }

  test("generate 2 test") {
    withDialect("dialect2.raml", "example2.json", "example2.raml", AmfJsonHint, Aml)
  }

  test("generate 3 test") {
    withDialect("dialect3.raml", "example3.json", "example3.raml", AmfJsonHint, Aml)
  }

  test("generate 4 test") {
    withDialect("dialect4.raml", "example4.json", "example4.raml", AmfJsonHint, Aml)
  }

  test("generate 5 test") {
    withDialect("dialect5.raml", "example5.json", "example5.raml", AmfJsonHint, Aml)
  }

  test("generate 6 test") {
    withDialect("dialect6.raml", "example6.json", "example6.raml", AmfJsonHint, Aml)
  }

  test("generate 6b $ref test") {
    withDialect("dialect6.raml", "example6b.json", "example6b.raml", AmfJsonHint, Aml)
  }

  test("generate 7 test") {
    withDialect("dialect7.raml", "example7.json", "example7.raml", AmfJsonHint, Aml)
  }

  test("generate 8 test") {
    withDialect("dialect8.raml", "example8.json", "example8.raml", AmfJsonHint, Aml)
  }

  test("generate 8b $include test") {
    withDialect("dialect8.raml", "example8b.json", "example8b.yaml", AmfJsonHint, Aml)
  }

  test("generate 8c $ref test") {
    withDialect("dialect8.raml", "example8c.json", "example8c.yaml", AmfJsonHint, Aml)
  }

  test("generate 9 test") {
    withDialect("dialect9.raml", "example9.json", "example9.raml", AmfJsonHint, Aml)
  }

  test("generate 9b $ref test") {
    withDialect("dialect9.raml", "example9b.json", "example9b.json.raml", AmfJsonHint, Aml)
  }

  test("generate 10a test") {
    withDialect("dialect10.raml", "example10a.json", "example10a.raml", AmfJsonHint, Aml)
  }

  test("generate 10b test") {
    withDialect("dialect10.raml", "example10b.json", "example10b.raml", AmfJsonHint, Aml)
  }

  test("generate 10c test") {
    withDialect("dialect10.raml", "example10c.json", "example10c.raml", AmfJsonHint, Aml)
  }

  test("generate 11 test") {
    withDialect("dialect11.raml", "example11.json", "example11.raml", AmfJsonHint, Aml)
  }

  test("generate 13 test") {
    withDialect("dialect13.raml", "example13.json", "example13.raml", AmfJsonHint, Aml)
  }

  test("generate 14 test") {
    withDialect("dialect14.raml", "example14.json", "example14.raml", AmfJsonHint, Aml)
  }

  test("generate 15 test") {
    withDialect("dialect15a.raml", "example15.json", "example15.raml", AmfJsonHint, Aml)
  }

  test("generate 16 test") {
    withDialect("dialect16a.raml", "example16a.json", "example16a.raml", AmfJsonHint, Aml)
  }

  test("generate 16c test") {
    withDialect("dialect16a.raml", "example16c.json", "example16c.yaml", AmfJsonHint, Aml)
  }

  test("generate 18 test") {
    withDialect("dialect18.raml", "example18.json", "example18.raml", AmfJsonHint, Aml)
  }

  test("generate 19 test") {
    withDialect("dialect19.raml", "example19.json", "example19.raml", AmfJsonHint, Aml)
  }

  test("generate 23 test") {
    withDialect("dialect23.raml", "example23.json", "example23.raml", AmfJsonHint, Aml)
  }

  if (platform.name == "jvm") {
    test("generate 23 (non-amf) test") {
      withDialect("dialect23.raml",
                  "example23.rdf.json",
                  "example23.raml",
                  AmfJsonHint,
                  Aml,
                  useAmfJsonldSerialisation = false)
    }
  }

  test("generate 24 test") {
    withDialect("dialect24.raml", "example24.json", "example24.raml", AmfJsonHint, Aml)
  }

  test("generate 24 b test") {
    withDialect("dialect24.raml", "example24b.json", "example24b.raml", AmfJsonHint, Aml)
  }

  test("generate 24 c test") {
    withDialect("dialect24.raml", "example24c.json", "example24c.raml", AmfJsonHint, Aml)
  }

  test("generate 27a test") {
    withDialect("dialect27.raml", "example27a.json", "example27a.raml", AmfJsonHint, Aml)
  }

  test("Generate instance with invalid property terms") {
    withDialect(
      "/invalids/schema-uri/dialect.yaml",
      "/invalids/schema-uri/instance.yaml",
      "/invalids/schema-uri/instance.json",
      VocabularyYamlHint,
      Amf,
      enableValidation = true
    )
  }

  protected def withDialect(dialect: String,
                            source: String,
                            golden: String,
                            hint: Hint,
                            target: Vendor,
                            directory: String = basePath,
                            useAmfJsonldSerialisation: Boolean = true,
                            enableValidation: Boolean = false): Future[Assertion] = {
    for {
      v   <- Validation(platform).map(_.withEnabledValidation(enableValidation))
      _   <- AMFCompiler(s"file://$directory/$dialect", platform, VocabularyYamlHint, v).build()
      res <- cycle(source, golden, hint, target, basePath, None, useAmfJsonldSerialisation)
    } yield {
      res
    }
  }

  protected def withInlineDialect(source: String,
                                  golden: String,
                                  hint: Hint,
                                  target: Vendor,
                                  directory: String = basePath): Future[Assertion] = {
    for {
      _   <- Validation(platform).map(_.withEnabledValidation(false))
      res <- cycle(source, golden, hint, target)
    } yield {
      res
    }
  }
}
