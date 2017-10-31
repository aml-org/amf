package amf.dialects
import amf.compiler.AMFCompiler
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

  val basePath="file://shared/src/test/resources/vocabularies/"

  test("Basic Validation Test") {
    val dl=platform.dialectsRegistry.registerDialect(basePath + "mule_config_dialect3.raml")
    val cm=dl.flatMap(d=>
      AMFCompiler("file://shared/src/test/resources/vocabularies/muleconfig.raml", platform, RamlYamlHint, None, None,platform.dialectsRegistry).build()
    )
    cm.map(u=>DialectValidator.validate(u).size).map(s=>{
      s should be(0)
    })
  }

  test("another validation test") {
    val dl=platform.dialectsRegistry.registerDialect(basePath + "mule_config_dialect3.raml")
    val cm=dl.flatMap(d=>
      AMFCompiler("file://shared/src/test/resources/vocabularies/muleconfig2.raml", platform, RamlYamlHint, None, None,platform.dialectsRegistry).build()
    )
    cm.map(u=>DialectValidator.validate(u).size).map(s=>{
      s should be(1)
    })
  }
  test("missing required property") {
    val dl=platform.dialectsRegistry.registerDialect(basePath + "mule_config_dialect3.raml")
    val cm=dl.flatMap(d=>
      AMFCompiler("file://shared/src/test/resources/vocabularies/muleconfig3.raml", platform, RamlYamlHint, None, None,platform.dialectsRegistry).build()
    )
    cm.map(u=>DialectValidator.validate(u).size).map(s=>{
      s should be(1)
    })
  }

  test("Vocabulary can be validated") {
    val validator = Validation(platform)
    var dialect: Option[Dialect] = None
    validator.loadValidationDialect().flatMap { parsedDialect =>
      AMFCompiler("file://vocabularies/vocabularies/raml_doc.raml", platform, RamlYamlHint, None, None, platform.dialectsRegistry).build()
    } flatMap { unit =>
      validator.validate(unit, "RAML 1.0 Vocabulary")
    } flatMap { report =>
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  test("Vocabulary can be validated with closed nodes") {
    val validator = Validation(platform)
    var dialect: Option[Dialect] = None
    validator.loadValidationDialect().flatMap { parsedDialect =>
      AMFCompiler("file://shared/src/test/resources/vocabularies/vocabulary_closed_shape_invalid.raml", platform, RamlYamlHint, None, None, platform.dialectsRegistry).build()
    } flatMap { unit =>
      validator.validate(unit, "RAML 1.0 Vocabulary")
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
    val validator = Validation(platform)
    var dialect: Option[Dialect] = None
    platform.dialectsRegistry.registerDialect("file://shared/src/test/resources/dialects/mule_configuration/configuration_dialect.raml").flatMap { parsedDialect =>
      dialect = Some(parsedDialect)
      AMFCompiler("file://shared/src/test/resources/dialects/mule_configuration/example.raml", platform, RamlYamlHint, None, None, platform.dialectsRegistry).build()
    } flatMap { unit =>
      validator.loadDialectValidationProfile(dialect.get)
      validator.validate(unit, dialect.get.name)
    } flatMap { report =>
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }

  test("Vocabulary can be validated with closed nodes (k8)") {
    val validator = Validation(platform)
    var dialect: Option[Dialect] = None
    validator.loadValidationDialect().flatMap { parsedDialect =>
      AMFCompiler("file://shared/src/test/resources/vocabularies/k8/vocabulary/core.raml", platform, RamlYamlHint, None, None, platform.dialectsRegistry).build()
    } flatMap { unit =>
      validator.validate(unit, "RAML 1.0 Vocabulary")
    } flatMap { report =>
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }

  /*
  test("Dialect can be validated (k8)") {
    val validator = Validation(platform)
    var dialect: Option[Dialect] = None
    validator.loadValidationDialect().flatMap { parsedDialect =>
      AMFCompiler("file:///Users/antoniogarrote/Development/vocabularies/k8/dialects/pod.raml", platform, RamlYamlHint, None, None, platform.dialectsRegistry).build()
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
    val validator = Validation(platform)
    var dialect: Option[Dialect] = None
    platform.dialectsRegistry.registerDialect("file://shared/src/test/resources/vocabularies/k8/dialects/pod.raml").flatMap { parsedDialect =>
      dialect = Some(parsedDialect)
      AMFCompiler("file://shared/src/test/resources/vocabularies/k8/examples/pod.raml", platform, RamlYamlHint, None, None, platform.dialectsRegistry).build()
    } flatMap { unit =>
      validator.loadDialectValidationProfile(dialect.get)
      validator.validate(unit, dialect.get.name)
    } flatMap { report =>
      assert(!report.conforms)
      assert(report.results.length == 1)
    }
  }


  test("Custom dialect can be validated (amc2)") {
    val validator = Validation(platform)
    var dialect: Option[Dialect] = None
    platform.dialectsRegistry.registerDialect("file://shared/src/test/resources/vocabularies/amc2/dialect.raml").flatMap { parsedDialect =>
      dialect = Some(parsedDialect)
      AMFCompiler("file://shared/src/test/resources/vocabularies/amc2/example.raml", platform, RamlYamlHint, None, None, platform.dialectsRegistry).build()
    } flatMap { unit =>
      validator.loadDialectValidationProfile(dialect.get)
      validator.validate(unit, dialect.get.name)
    } flatMap { report =>
      println(report)
      assert(report.conforms)
      assert(report.results.isEmpty)
    }
  }
}
