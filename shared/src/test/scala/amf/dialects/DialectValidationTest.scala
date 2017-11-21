package amf.dialects
import amf.client.GenerationOptions
import amf.compiler.AMFCompiler
import amf.dumper.AMFDumper
import amf.remote.Syntax.Json
import amf.remote._
import amf.spec.dialects.Dialect
import amf.unsafe.PlatformSecrets
import amf.validation.Validation
import org.scalatest.AsyncFunSuite
import org.scalatest.Matchers._

import scala.concurrent.ExecutionContext

/**
  * Created by Pavel Petrochenko on 22/09/17.
  */
class DialectValidationTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://shared/src/test/resources/vocabularies/"

  test("Basic Validation Test") {
    val dl = platform.dialectsRegistry.registerDialect(basePath + "mule_config_dialect3.raml")
    val cm = dl.flatMap(
      d =>
        AMFCompiler("file://shared/src/test/resources/vocabularies/muleconfig.raml",
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
    val dl = platform.dialectsRegistry.registerDialect(basePath + "mule_config_dialect3.raml")
    val cm = dl.flatMap(
      d =>
        AMFCompiler("file://shared/src/test/resources/vocabularies/muleconfig2.raml",
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
    val dl = platform.dialectsRegistry.registerDialect(basePath + "mule_config_dialect3.raml")
    val cm = dl.flatMap(
      d =>
        AMFCompiler("file://shared/src/test/resources/vocabularies/muleconfig3.raml",
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

  test("Empty Vocabulary") {
    val validation = Validation(platform)
    AMFCompiler(
      "file://shared/src/test/resources/vocabularies/empty.raml",
      platform,
      RamlYamlHint,
      validation,
      None,
      None
    ).build() flatMap  { model =>
      validation.validate(model, "RAML 1.0 Vocabulary")
    } flatMap { report =>
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  test("Vocabulary can be validated with closed nodes") {
    val validation = Validation(platform)

    AMFCompiler(
      "file://shared/src/test/resources/vocabularies/vocabulary_closed_shape_invalid.raml",
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
    val dialectFile              = "file://shared/src/test/resources/dialects/mule_configuration/configuration_dialect.raml"
    val dialectExampleFile       = "file://shared/src/test/resources/dialects/mule_configuration/example.raml"

    platform.dialectsRegistry.registerDialect(dialectFile) flatMap { parsedDialect =>
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
      "file://shared/src/test/resources/vocabularies/k8/vocabulary/core.raml",
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
      "file://shared/src/test/resources/vocabularies/broken_core.raml",
      platform,
      RamlYamlHint,
      validation,
      None,
      None
    ).build() flatMap { model =>
      validation.validate(model, "RAML 1.0 Vocabulary")
    } flatMap { report =>
      assert(!report.conforms)
      assert(report.results.head.targetNode=="http://mulesoft.com/vocabularies/k8-core#priority")
    }
  }

  /*
  test("Dialect can be validated (k8)") {
    val validator = Validation(platform)
    var dialect: Option[Dialect] = None
    validator.loadValidationDialect().flatMap { parsedDialect =>
      AMFCompiler("file://shared/src/test/resources/vocabularies/k8/dialects/pod.raml", platform, RamlYamlHint, validator, None, None, platform.dialectsRegistry).build()
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
    val dialectFile              = "file://shared/src/test/resources/vocabularies/k8/dialects/pod.raml"
    val dialectExampleFile       = "file://shared/src/test/resources/vocabularies/k8/examples/pod.raml"

    platform.dialectsRegistry.registerDialect(dialectFile) flatMap { parsedDialect =>
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
    val dialectFile              = "file://shared/src/test/resources/vocabularies/amc2/dialect.raml"
    val dialectExampleFile       = "file://shared/src/test/resources/vocabularies/amc2/example.raml"

    platform.dialectsRegistry.registerDialect(dialectFile) flatMap { parsedDialect =>
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
    val dialectFile              = "file://shared/src/test/resources/vocabularies/eng_demos/dialect.raml"
    val dialectExampleFile       = "file://shared/src/test/resources/vocabularies/eng_demos/demo.raml"

    platform.dialectsRegistry.registerDialect(dialectFile) flatMap { parsedDialect =>
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
    val validation = Validation(platform)
    var dialect: Option[Dialect] = None
    val dialectFile              = "file://shared/src/test/resources/vocabularies/eng_demos/dialect.raml"
    val dialectExampleFile       = "file://shared/src/test/resources/vocabularies/eng_demos/demo.raml"
    val dialectValidationProfileFile =
      "file://shared/src/test/resources/vocabularies/eng_demos/validation_profile.raml"

    platform.dialectsRegistry.registerDialect(dialectFile) flatMap { parsedDialect =>
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
    val dialectFile              = "file://shared/src/test/resources/vocabularies/evented_apis/dialect.raml"
    val dialectExampleFile       = "file://shared/src/test/resources/vocabularies/evented_apis/example/example.raml"

    platform.dialectsRegistry.registerDialect(dialectFile) flatMap { parsedDialect =>
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
    platform.dialectsRegistry.registerDialect("file://shared/src/test/resources/vocabularies/dialect_lib/main_dialect.raml").flatMap { parsedDialect =>
      dialect = Some(parsedDialect)
      AMFCompiler("file://shared/src/test/resources/vocabularies/dialect_lib/example.raml", platform, RamlYamlHint, None, None, platform.dialectsRegistry).build()
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
