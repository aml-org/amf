package amf.dialects
import amf.core.AMF
import amf.core.remote.RamlYamlHint
import amf.core.unsafe.PlatformSecrets
import amf.facades.{AMFCompiler, Validation}
import amf.plugins.document.vocabularies.RAMLVocabulariesPlugin
import amf.plugins.document.vocabularies.core.DialectValidator
import amf.plugins.document.vocabularies.registries.PlatformDialectRegistry
import amf.plugins.document.vocabularies.spec.Dialect
import amf.plugins.document.webapi.{OAS20Plugin, RAML08Plugin, RAML10Plugin}
import amf.plugins.features.validation.AMFValidatorPlugin
import org.scalatest.AsyncFunSuite
import org.scalatest.Matchers._

import scala.concurrent.ExecutionContext

/**
  * Created by Pavel Petrochenko on 22/09/17.
  */
class DialectValidationTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://amf-client/shared/src/test/resources/vocabularies/"

  // todo review with mgutierrez and pcolunga.
  amf.core.AMF.registerPlugin(RAMLVocabulariesPlugin)
  amf.core.AMF.registerPlugin(RAML10Plugin)
  amf.core.AMF.registerPlugin(RAML08Plugin)
  amf.core.AMF.registerPlugin(OAS20Plugin)
  amf.core.AMF.registerPlugin(AMFValidatorPlugin)
  AMF.init()

  test("Basic Validation Test") {
    val dl = PlatformDialectRegistry.registerDialect(basePath + "mule_config_dialect3.raml")
    val cm = dl.flatMap(
      d =>
        AMFCompiler("file://amf-client/shared/src/test/resources/vocabularies/muleconfig.raml",
                    platform,
                    RamlYamlHint,
                    Validation(platform),
                    None,
                    None).build())
    cm.map(u => DialectValidator.validate(u).size)
      .map(s => {
        s should be(0)
      })
  }

  test("another validation test") {
    val dl = PlatformDialectRegistry.registerDialect(basePath + "mule_config_dialect3.raml")
    val cm = dl.flatMap(
      d =>
        AMFCompiler("file://amf-client/shared/src/test/resources/vocabularies/muleconfig2.raml",
                    platform,
                    RamlYamlHint,
                    Validation(platform),
                    None,
                    None).build())
    cm.map(u => DialectValidator.validate(u).size)
      .map(s => {
        s should be(1)
      })
  }
  test("missing required property") {
    val dl = PlatformDialectRegistry.registerDialect(basePath + "mule_config_dialect3.raml")
    val cm = dl.flatMap(
      d =>
        AMFCompiler("file://amf-client/shared/src/test/resources/vocabularies/muleconfig3.raml",
                    platform,
                    RamlYamlHint,
                    Validation(platform),
                    None,
                    None).build())
    cm.map(u => DialectValidator.validate(u).size)
      .map(s => {
        s should be(1)
      })
  }

  test("Vocabulary can be validated") {
    val validation = Validation(platform)
    AMFCompiler(
      "file://vocabularies/vocabularies/raml_doc.raml",
      platform,
      RamlYamlHint,
      validation,
      None,
      None
    ).build() flatMap { model =>
      validation.validate(model, "RAML 1.0 Vocabulary")
    } flatMap { report =>
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  val vocabularies = Seq(
    "file://vocabularies/vocabularies/raml_doc.raml",
    "file://vocabularies/vocabularies/raml_http.raml",
    "file://vocabularies/vocabularies/raml_shapes.raml",
    "file://vocabularies/vocabularies/data_model.raml"
  )
  vocabularies.foreach { vocab =>
    test(s"Standard vocabularies ${vocab} validates") {
      val validation = Validation(platform)
      AMFCompiler(
        vocab,
        platform,
        RamlYamlHint,
        validation,
        None,
        None
      ).build() flatMap { model =>
        validation.validate(model, "RAML 1.0 Vocabulary")
      } flatMap { report =>
        assert(report.conforms)
        assert(report.results.isEmpty)
      }
    }
  }

  test("Empty Vocabulary") {
    val validation = Validation(platform)
    AMFCompiler(
      "file://amf-client/shared/src/test/resources/vocabularies/empty.raml",
      platform,
      RamlYamlHint,
      validation,
      None,
      None
    ).build() flatMap { model =>
      validation.validate(model, "RAML 1.0 Vocabulary")
    } flatMap { report =>
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  test("Vocabulary can be validated with closed nodes") {
    val validation = Validation(platform)

    AMFCompiler(
      "file://amf-client/shared/src/test/resources/vocabularies/vocabulary_closed_shape_invalid.raml",
      platform,
      RamlYamlHint,
      validation,
      None,
      None
    ).build() flatMap { model =>
      validation.validate(model, "RAML 1.0 Vocabulary")
    } flatMap { report =>
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  /*
  test("Dialect can be validated") {
    val validator = Validation(platform)
    var dialect: Option[Dialect] = None
    validator.loadValidationDialect().flatMap { parsedDialect =>
      AMFCompiler("file://vocabularies/dialects/validation.raml", platform, RamlYamlHint, None, None, platform.dialectsRegistry).build()
    } flatMap { unit =>
      validator.validate(unit, "RAML 1.0 Dialect")
    } flatMap { report =>
      println(report)
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }
   */

  test("Custom dialect can be validated") {
    val validation               = Validation(platform)
    var dialect: Option[Dialect] = None
    val dialectFile =
      "file://amf-client/shared/src/test/resources/dialects/mule_configuration/configuration_dialect.raml"
    val dialectExampleFile = "file://amf-client/shared/src/test/resources/dialects/mule_configuration/example.raml"

    PlatformDialectRegistry.registerDialect(dialectFile) flatMap { parsedDialect =>
      dialect = Some(parsedDialect)
      AMFCompiler(
        dialectExampleFile,
        platform,
        RamlYamlHint,
        validation,
        None,
        None
      ).build()
    } flatMap { model =>
      validation.loadDialectValidationProfile(dialect.get)
      validation.validate(model, dialect.get.name)
    } flatMap { report =>
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("Vocabulary can be validated with closed nodes (k8)") {
    val validation = Validation(platform)

    AMFCompiler(
      "file://amf-client/shared/src/test/resources/vocabularies/k8/vocabulary/core.raml",
      platform,
      RamlYamlHint,
      validation,
      None,
      None
    ).build() flatMap { model =>
      validation.validate(model, "RAML 1.0 Vocabulary")
    } flatMap { report =>
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  test("Vocabulary can be validated with references") {
    val validation = Validation(platform)

    AMFCompiler(
      "file://amf-client/shared/src/test/resources/vocabularies/broken_core.raml",
      platform,
      RamlYamlHint,
      validation,
      None,
      None
    ).build() flatMap { model =>
      validation.validate(model, "RAML 1.0 Vocabulary")
    } flatMap { report =>
      assert(!report.conforms)
      assert(report.results.head.targetNode == "http://mulesoft.com/vocabularies/k8-core#priority")
    }
  }

  /*
  test("Dialect can be validated (k8)") {
    val validator = Validation(platform)
    var dialect: Option[Dialect] = None
    validator.loadValidationDialect().flatMap { parsedDialect =>
      AMFCompiler("file://amf-client/shared/src/test/resources/vocabularies/k8/dialects/pod.raml", platform, RamlYamlHint, validator, None, None, platform.dialectsRegistry).build()
    } flatMap { unit =>
      validator.validate(unit, "RAML 1.0 Dialect")
    } flatMap { report =>
      println(report)
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }
   */

  test("Custom dialect can be validated (k8)") {
    val validation               = Validation(platform)
    var dialect: Option[Dialect] = None
    val dialectFile              = "file://amf-client/shared/src/test/resources/vocabularies/k8/dialects/pod.raml"
    val dialectExampleFile       = "file://amf-client/shared/src/test/resources/vocabularies/k8/examples/pod.raml"

    PlatformDialectRegistry.registerDialect(dialectFile) flatMap { parsedDialect =>
      dialect = Some(parsedDialect)
      AMFCompiler(
        dialectExampleFile,
        platform,
        RamlYamlHint,
        validation,
        None,
        None
      ).build()
    } flatMap { model =>
      validation.loadDialectValidationProfile(dialect.get)
      validation.validate(model, dialect.get.name)
    } flatMap { report =>
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("Custom dialect can be validated (amc2)") {
    val validation               = Validation(platform)
    var dialect: Option[Dialect] = None
    val dialectFile              = "file://amf-client/shared/src/test/resources/vocabularies/amc2/dialect.raml"
    val dialectExampleFile       = "file://amf-client/shared/src/test/resources/vocabularies/amc2/example.raml"

    PlatformDialectRegistry.registerDialect(dialectFile) flatMap { parsedDialect =>
      dialect = Some(parsedDialect)
      AMFCompiler(
        dialectExampleFile,
        platform,
        RamlYamlHint,
        validation,
        None,
        None
      ).build()
    } flatMap { model =>
      validation.loadDialectValidationProfile(dialect.get)
      validation.validate(model, dialect.get.name)
    } flatMap { report =>
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  test("Custom dialect can be validated (amf-eng-demos)") {
    val validation               = Validation(platform)
    var dialect: Option[Dialect] = None
    val dialectFile              = "file://amf-client/shared/src/test/resources/vocabularies/eng_demos/dialect.raml"
    val dialectExampleFile       = "file://amf-client/shared/src/test/resources/vocabularies/eng_demos/demo.raml"

    PlatformDialectRegistry.registerDialect(dialectFile) flatMap { parsedDialect =>
      dialect = Some(parsedDialect)
      AMFCompiler(
        dialectExampleFile,
        platform,
        RamlYamlHint,
        validation,
        None,
        None
      ).build()
    } flatMap { model =>
      // validation.loadDialectValidationProfile(dialect.get)
      validation.validate(model, dialect.get.name + " " + dialect.get.version)
    } flatMap { report =>
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  test("Custom dialect with custom validation can be validated (amf-eng-demos)") {
    val validation               = Validation(platform)
    var dialect: Option[Dialect] = None
    val dialectFile              = "file://amf-client/shared/src/test/resources/vocabularies/eng_demos/dialect.raml"
    val dialectExampleFile       = "file://amf-client/shared/src/test/resources/vocabularies/eng_demos/demo.raml"
    val dialectValidationProfileFile =
      "file://amf-client/shared/src/test/resources/vocabularies/eng_demos/validation_profile.raml"

    PlatformDialectRegistry.registerDialect(dialectFile) flatMap { parsedDialect =>
      dialect = Some(parsedDialect)
      AMFCompiler(
        dialectExampleFile,
        platform,
        RamlYamlHint,
        validation,
        None,
        None
      ).build()
    } flatMap { model =>
      validation.loadValidationDialect() flatMap { _ =>
        validation.loadValidationProfile(dialectValidationProfileFile) flatMap { _ =>
          validation.validate(model, "Custom Eng-Demos Validation")
        }
      }
    } flatMap { report =>
      assert(!report.conforms)
      assert(report.results.length == 6)
    }
  }

  test("Custom dialect can be validated (evented_apis)") {
    val validation               = Validation(platform)
    var dialect: Option[Dialect] = None
    val dialectFile              = "file://amf-client/shared/src/test/resources/vocabularies/evented_apis/dialect.raml"
    val dialectExampleFile =
      "file://amf-client/shared/src/test/resources/vocabularies/evented_apis/example/example.raml"

    PlatformDialectRegistry.registerDialect(dialectFile) flatMap { parsedDialect =>
      dialect = Some(parsedDialect)
      AMFCompiler(
        dialectFile,
        platform,
        RamlYamlHint,
        validation,
        None,
        None
      ).build()
    } flatMap { model =>
      /*
      AMFDumper(model, Amf, Json, GenerationOptions()).dumpToString map { json =>
        println("GENERATED")
        println(json)
      }
       */
      validation.loadDialectValidationProfile(dialect.get)
      validation.validate(model, "RAML 1.0 Dialect")
    } flatMap { report =>
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  /*
  test("Custom dialect using lib in dialect") {
    val validator = Validation(platform)
    var dialect: Option[Dialect] = None
    platform.dialectsRegistry.registerDialect("file://amf-client/shared/src/test/resources/vocabularies/dialect_lib/main_dialect.raml").flatMap { parsedDialect =>
      dialect = Some(parsedDialect)
      AMFCompiler("file://amf-client/shared/src/test/resources/vocabularies/dialect_lib/example.raml", platform, RamlYamlHint, None, None, platform.dialectsRegistry).build()
    } flatMap { unit =>
      validator.loadDialectValidationProfile(dialect.get)
      validator.validate(unit, dialect.get.name)
    } flatMap { report =>
      println(report)
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }
 */
}
