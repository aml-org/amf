package amf.dialects

import amf.client.GenerationOptions
import amf.common.Tests.checkDiff
import amf.compiler.AMFCompiler
import amf.framework.document.BaseUnit
import amf.dumper.AMFDumper
import amf.remote.Syntax.Yaml
import amf.remote.Syntax.Json
import amf.remote.{Amf, ExtensionYamlHint, Raml, RamlYamlHint}
import amf.unsafe.PlatformSecrets
import amf.validation.Validation
import amf.validation.emitters.ValidationReportJSONLDEmitter
import org.scalatest.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

case class ExpectedReport(conforms: Boolean, numErrors: Integer, profile: String)

class DialectFeatureTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath         = "file://shared/src/test/resources/vocabularies/"
  val vocabulariesPath = "file://shared/src/test/resources/vocabularies/"
  val examplesPath     = "file://shared/src/test/resources/validations/"

  test("Default values") {
    val validation = platform.dialectsRegistry.registerDialect(basePath + "validation_dialect_defaults.raml")
    val expected =
      platform.resolve(basePath + "validation_profile_with_default_example.json", None).map(_.stream.toString)
    val actual = validation
      .flatMap {
        unit =>
          AMFCompiler(basePath + "validation_profile_example.raml",
            platform,
            ExtensionYamlHint,
            Validation(platform),
            None,
            None)
            .build()
      }
    actual
      .map(AMFDumper(_, Amf, Json, GenerationOptions()).dumpToString)
      .map(v => {
        // platform.write(basePath + "validation_profile_with_default_example.json",v);
        v
      })
      .zip(expected)
      .map(checkDiff)
  }

  test("Uses (in dialect definition)") {
    val validation = platform.dialectsRegistry.registerDialect(basePath + "validation_dialect_uses.raml")
    val expected =
      platform.resolve(basePath + "validation_profile_with_uses_example.json", None).map(_.stream.toString)
    val actual = validation
      .flatMap(
        unit =>
          AMFCompiler(basePath + "validation_profile_example.raml",
                      platform,
                      ExtensionYamlHint,
                      Validation(platform),
                      None,
                      None)
            .build())
    actual
      .map(AMFDumper(_, Amf, Json, GenerationOptions()).dumpToString)
      .map(v => {
        platform.write(basePath + "validation_profile_with_uses_example.json", v)
        v
      })
      .zip(expected)
      .map(checkDiff)
  }

//  test("Uses (in dialect)") {
//    val validation = platform.dialectsRegistry.registerDialect(basePath + "validation_dialect_unions.raml")
//    val expected =
//      platform.resolve(basePath + "validation_profile_example_uses_gold.raml", None).map(_.stream.toString)
//    val actual = validation
//      .flatMap(unit => {
//        val dl = new DialectRegistry();
//        dl.add(unit)
//        AMFCompiler(basePath + "validation_profile_example_uses.raml", platform, ExtensionYamlHint, None, None, dl)
//          .build()
//      })
//    actual
//      .flatMap({ unit =>
//        AMFDumper(unit, Raml, Yaml, GenerationOptions()).dumpToString
//
//      })
//      .map(v => {
//        // platform.write(basePath + "validation_profile_example_uses_gold.raml",v);
//        v
//      })
//      .zip(expected)
//      .map(checkDiff)
//  }

  test("Uses (in dialect (real library))") {
    val expected: Future[String] =
      platform.resolve(basePath + "validation_profile_example_uses_gold2.raml", None).map(_.stream.toString)
    val actual
      : Future[String] = platform.dialectsRegistry.registerDialect(basePath + "validation_dialect_unions.raml") flatMap [BaseUnit] {
      _ =>
        AMFCompiler(basePath + "validation_profile_example_uses2.raml",
                    platform,
                    ExtensionYamlHint,
                    Validation(platform),
                    None,
                    None).build()
    } map { AMFDumper(_, Raml, Yaml, GenerationOptions()).dumpToString }
    actual.zip(expected).map(checkDiff)
  }

  test("Fragments ") {
    val expectedFile             = "validationFragment.raml"
    val dialectFile              = "validation_dialect_with_fragments.raml"
    val exampleFile              = "validationFragment.raml"
    val expected: Future[String] = platform.resolve(basePath + expectedFile, None).map(_.stream.toString)
    val actual: Future[String] = platform.dialectsRegistry.registerDialect(basePath + dialectFile) flatMap [BaseUnit] {
      _ =>
        AMFCompiler(basePath + exampleFile,
                    platform,
                    ExtensionYamlHint,
                    Validation(platform),
                    None,
                    None).build()
    } map { AMFDumper(_, Raml, Yaml, GenerationOptions()).dumpToString }
    actual.zip(expected).map(checkDiff)
  }

  test("Fragments in Dialects ") {
    val expectedFile             = "validationFragment_simple.raml"
    val dialectFile              = "validation_dialect_using_fragments.raml"
    val exampleFile              = "validationFragment_simple.raml"
    val expected: Future[String] = platform.resolve(basePath + expectedFile, None).map(_.stream.toString)
    val actual: Future[String] = platform.dialectsRegistry.registerDialect(basePath + dialectFile) flatMap [BaseUnit] {
      _ =>
        AMFCompiler(basePath + exampleFile,
                    platform,
                    ExtensionYamlHint,
                    Validation(platform),
                    None,
                    None).build()
    } map { AMFDumper(_, Raml, Yaml, GenerationOptions()).dumpToString }
    actual.zip(expected).map(checkDiff)
  }
  test("Fragments in Dialects + inplace") {
    val expectedFile             = "validationFragment_simple.raml"
    val dialectFile              = "validation_dialect_using_fragments2.raml"
    val exampleFile              = "validationFragment_simple.raml"
    val expected: Future[String] = platform.resolve(basePath + expectedFile, None).map(_.stream.toString)
    val actual: Future[String] = platform.dialectsRegistry.registerDialect(basePath + dialectFile) flatMap [BaseUnit] {
      _ =>
        AMFCompiler(basePath + exampleFile,
                    platform,
                    ExtensionYamlHint,
                    Validation(platform),
                    None,
                    None).build()
    } map { AMFDumper(_, Raml, Yaml, GenerationOptions()).dumpToString }
    actual.zip(expected).map(checkDiff)
  }

  test("Using library in dialect definition") {
    val expectedFile             = "validationFragment.raml"
    val dialectFile              = "validation_dialect_uses(dialect_lib).raml"
    val exampleFile              = "validationFragment.raml"
    val expected: Future[String] = platform.resolve(basePath + expectedFile, None).map(_.stream.toString)
    val actual: Future[String] = platform.dialectsRegistry.registerDialect(basePath + dialectFile) flatMap [BaseUnit] {
      _ =>
        AMFCompiler(basePath + exampleFile,
                    platform,
                    ExtensionYamlHint,
                    Validation(platform),
                    None,
                    None).build()
    } map { AMFDumper(_, Raml, Yaml, GenerationOptions()).dumpToString }
    actual.zip(expected).map(checkDiff)
  }

  test("Bruno problems in sequences") {
    val validation = platform.dialectsRegistry.registerDialect(basePath + "bruno/Dialects/EventDialect.raml")
    val expected =
      platform.resolve(basePath + "bruno/EventedAPI_Banking.json", None).map(_.stream.toString)
    val actual = validation
      .flatMap(
        unit =>
          AMFCompiler(basePath + "bruno/examples/EventedAPI_Banking.raml",
            platform,
            RamlYamlHint,
            Validation(platform),
            None,
            None,
            platform.dialectsRegistry)
            .build())
    actual
      .map(AMFDumper(_, Amf, Json, GenerationOptions()).dumpToString)
      .map(v => {
        platform.write(basePath + "bruno/EventedAPI_Banking.json", v)
        v
      })
      .zip(expected)
      .map(checkDiff)
  }

  test("Bruno problems in sequences 2") {
    val validation = platform.dialectsRegistry.registerDialect(basePath + "bruno/Dialects/EventDialect.raml")
    val expected =
      platform.resolve(basePath + "bruno/EventedAPI_Banking1.json", None).map(_.stream.toString)
    val actual = validation
      .flatMap(
        unit =>
          AMFCompiler(basePath + "bruno/examples/EventedAPI_Banking1.raml",
            platform,
            RamlYamlHint,
            Validation(platform),
            None,
            None,
            platform.dialectsRegistry)
            .build())
    actual
      .map(AMFDumper(_, Amf, Json, GenerationOptions()).dumpToString)
      .map(v => {
        platform.write(basePath + "bruno/EventedAPI_Banking1.json", v)
        v
      })
      .zip(expected)
      .map(checkDiff)
  }

  test("Bruno problems in sequences 3") {
    val validation = platform.dialectsRegistry.registerDialect(basePath + "bruno/Dialects/EventDialect.raml")
    val expected =
      platform.resolve(basePath + "bruno/EventedAPI_Banking2.json", None).map(_.stream.toString)
    val actual = validation
      .flatMap(
        unit =>
          AMFCompiler(basePath + "bruno/examples/EventedAPI_Banking1.raml",
            platform,
            RamlYamlHint,
            Validation(platform),
            None,
            None,
            platform.dialectsRegistry)
            .build())
    actual
      .map(AMFDumper(_, Amf, Json, GenerationOptions()).dumpToString)
      .map(v => {
        platform.write(basePath + "bruno/EventedAPI_Banking2.json", v)
        v
      })
      .zip(expected)
      .map(checkDiff)
  }
}
