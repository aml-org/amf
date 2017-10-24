package amf.dialects
import amf.client.GenerationOptions
import amf.common.Tests.checkDiff
import amf.compiler.AMFCompiler
import amf.document.Document
import amf.dumper.AMFDumper
import amf.remote._
import amf.spec.dialects.Dialect
import amf.unsafe.PlatformSecrets
import amf.validation.Validation
import amf.validation.model.AMFDialectValidations
import org.scalatest.{Assertion, AsyncFunSuite}
import org.scalatest.Matchers._

import scala.concurrent.{ExecutionContext, Future}

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

  test("validation profile can be computed") {
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
}
