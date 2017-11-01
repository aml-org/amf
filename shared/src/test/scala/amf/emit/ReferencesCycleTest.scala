package amf.emit

import amf.client.GenerationOptions
import amf.common.Diff
import amf.compiler.AMFCompiler
import amf.dumper.AMFDumper
import amf.io.BuildCycleTests
import amf.remote._
import amf.validation.Validation
import org.scalatest.{Assertion, Succeeded}

import scala.concurrent.Future

/**
  * Created by hernan.najles on 9/19/17.
  */
class ReferencesCycleTest extends BuildCycleTests {

  override val basePath = "file://shared/src/test/resources/references/"

  test("simple library raml") {
    assertReferences("libraries.raml", Seq("lib/lib.raml"), RamlYamlHint, Raml)
  }

  test("simple library oas") {
    assertReferences("libraries.json", Seq("lib/lib.json"), OasJsonHint, Oas)
  }

  test("library raml to oas") {
    assertReferences("libraries.raml", Seq("lib/lib.raml.json"), RamlJsonHint, Oas)
  }

  test("library oas to raml") {
    assertReferences("libraries.json", Seq("lib/lib.json.raml"), OasJsonHint, Raml)
  }

  test("library raml to amf") {
    assertReferences("libraries.raml", Seq("lib/lib.raml.jsonld"), RamlJsonHint, Amf)
  }

  test("library oas to amf") {
    assertReferences("libraries.json", Seq("lib/lib.json.jsonld"), OasJsonHint, Amf)
  }

  test("library amf to oas from include") {
    assertReferences("libraries.json.jsonld", Seq("lib/lib.json"), AmfJsonHint, Oas)
  }

  test("library amf to raml from include") {
    assertReferences("libraries.raml.jsonld", Seq("lib/lib.raml"), AmfJsonHint, Raml)
  }

  test("data type fragment raml to raml") {
    assertReferences("data-type-fragment.raml", Seq("fragments/person.raml"), RamlYamlHint, Raml)
  }

  test("data type fragment oas to oas") {
    assertReferences("data-type-fragment.json", Seq("fragments/person.json"), OasJsonHint, Oas)
  }

  test("data type fragment raml to oas") {
    assertReferences("data-type-fragment.raml", Seq("fragments/person.json"), RamlJsonHint, Oas)
  }

  test("data type fragment oas to raml") {
    assertReferences("data-type-fragment.json", Seq("fragments/person.raml"), OasJsonHint, Raml)
  }

  test("data type fragment amf to raml from include") {
    assertReferences("data-type-fragment.raml.jsonld", Seq("fragments/person.raml"), AmfJsonHint, Raml)
  }

  test("data type fragment amf to oas from include") {
    assertReferences("data-type-fragment.json.jsonld", Seq("fragments/person.json"), AmfJsonHint, Oas)
  }

  test("resource type fragment raml to raml") {
    assertReferences("resource-type-fragment.raml", Seq("fragments/resource-type.raml"), RamlYamlHint, Raml)
  }

  test("trait fragment raml to raml") {
    assertReferences("trait-fragment.raml", Seq("fragments/trait.raml"), RamlYamlHint, Raml)
  }

  test("Alias library reference raml test") {
    assertReferences("lib-alias-reference.raml", Seq("lib/lib-declaration.raml"), RamlYamlHint, Raml)
  }

  test("Security schemes fragment raml to raml") {
    assertReferences("security-scheme-fragment.raml", Seq("fragments/security-scheme.raml"), RamlYamlHint, Raml)
  }

  test("Security schemes fragment oas to oas") {
    assertReferences("security-scheme-fragment.json", Seq("fragments/security-scheme.json"), OasJsonHint, Oas)
  }

  test("Named Example fragment raml to raml") {
    assertReferences("named-example.raml", Seq("fragments/named-example.raml"), RamlYamlHint, Raml)
  }

  test("Named Example fragment oas to oas") {
    assertReferences("named-example.json", Seq("fragments/named-example.json"), OasJsonHint, Oas)
  }

  def assertReferences(documentRootPath: String, golden: Seq[String], hint: Hint, target: Vendor): Future[Assertion] = {

    val expected: Future[Seq[ModuleContent]] = Future.sequence(golden.map(g => {
      platform
        .resolve(basePath + g, None)
        .map(c => ModuleContent(c.url, c.stream.toString))
    }))

    val validation = Validation(platform)
    val actualModules: Future[Seq[ModuleContent]] = AMFCompiler(basePath + documentRootPath, platform, hint, validation)
      .build()
      .map(_.references)
      .flatMap(references => {
        Future.sequence(references.map(r => {
          val string = AMFDumper(r, target, target.defaultSyntax, GenerationOptions().withSourceMaps).dumpToString
          string.map(ModuleContent(r.location, _))
        }))
      })

    expected
      .zip(actualModules)
      .map(DiffModule.checkListDiff)

  }

  case class ModuleContent(url: String, content: String)

  object DiffModule {
    implicit object ModuleOrdering extends Ordering[ModuleContent] {
      override def compare(x: ModuleContent, y: ModuleContent): Int = x.url.compareTo(y.url)
    }

    def checkListDiff(tuple: (Seq[ModuleContent], Seq[ModuleContent])): Assertion = tuple match {
      case (actualList, expectedList) =>
        val actualSorted   = actualList.sorted
        val expectedSorted = expectedList.sorted
        if (actualSorted.size != expectedSorted.size) fail("Module content list have diff size")
        actualSorted.zipWithIndex.foreach {
          case (actual, index) =>
            checkDiff(actual, expectedSorted(index))
        }
        Succeeded
    }

    def checkDiff(tuple: (ModuleContent, ModuleContent)): Assertion = tuple match {
      case (actual, expected) =>
        checkDiff(actual, expected)
        Succeeded
    }

    /** Diff between 2 resolved contents. */
    def checkDiff(actual: ModuleContent, expected: ModuleContent): Unit = {
      val diffs: List[Diff.Delta[String]] = Diff.trimming.ignoreEmptyLines.diff(actual.content, expected.content)
      if (diffs.nonEmpty) {
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
        println(expected)
        println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
        println(actual)
        println("==============================================")
        println(s"====actual ${actual.url}")
        println(s"====expected ${expected.url}")

        fail("\n" + Diff.makeString(diffs))
      }
    }
  }

}
