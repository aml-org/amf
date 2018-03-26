package amf.spec

import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation._
import org.scalatest.{FunSuite, Inspectors, Matchers}

class AnnotationsTest extends FunSuite with Matchers with Inspectors {

  private val ramlFixture = Seq(
    ("title", None),
    ("(foo)", Some("foo")),
    ("(wadus)", Some("wadus")),
    ("()", None),
    ("(another", None),
    ("regex)", None),
    ("invalid(regex)", None),
    ("(invalid)regex", None),
    ("some", None)
  )

  private val oasFixture = Seq(
    ("title", None),
    ("x-foo", Some("foo")),
    ("x-wadus", Some("wadus")),
    ("x-", None),
    ("xanother", None),
    ("regex-x-", None),
    ("X-foo", Some("foo")),
    ("X-wadus", Some("wadus")),
    ("some", None)
  )

  private val knownFixture = Seq(
    ("x-title", None),
    ("x-facets", None),
    ("(examples)", None),
    ("x-examples", None),
    ("x-fileTypes", None),
    ("(license)", None),
    ("x-base-uri-parameters", None),
    ("(base-uri-parameters)", None),
    ("x-annotationTypes", None),
    ("(oas-deprecated)", None),
    ("(summary)", None)
  )

  test("Test is raml annotation") {
    forAll(ramlFixture) {
      case (test, expected) => isRamlAnnotation(test) should be(expected.isDefined)
    }
  }

  test("Test is oas annotation") {
    forAll(oasFixture) {
      case (test, expected) => isOasAnnotation(test) should be(expected.isDefined)
    }
  }

  test("Test resolve annotation") {
    forAll(ramlFixture ++ oasFixture ++ knownFixture) {
      case (test, expected) => resolveAnnotation(test) shouldBe expected
    }
  }
}
