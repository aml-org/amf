package amf.error

import amf.core.model.document.BaseUnit
import amf.core.parser.Range
import amf.core.remote.RamlYamlHint
import amf.facades.{AMFCompiler, Validation}
import amf.plugins.features.validation.ParserSideValidations

import scala.concurrent.Future

class RamlParserErrorTest extends ParserErrorTest {

  test("Test unexpected node types") {
    validate(
      "unexpected-nodes.raml",
      invalid => {
        invalid.level should be("Violation")
        invalid.message should be("Unexpected key 'invalid'. Options are 'value' or annotations \\(.+\\)")
        invalid.position.map(_.range) should be(Some(Range((3, 4), (3, 11))))
      },
      description => {
        description.level should be("Violation")
        description.message should be("Expected scalar but found: [invalid]")
        description.position.map(_.range) should be(Some(Range((4, 13), (4, 24))))
      },
      protocols => {
        protocols.level should be("Violation")
        protocols.message should be("Expected scalar but found: {invalid: http}")
        protocols.position.map(_.range) should be(Some(Range((5, 10), (7, 0))))
      },
      securedBy => { // todo should not be an error after APIMF-483!
        securedBy.level should be("Violation")
        securedBy.message should be("Security scheme 'oauth' not found in declarations.")
        securedBy.position.map(_.range) should be(Some(Range((7, 11), (7, 16))))
      }
    )
  }

  test("Custom facets work correctly with the closed node detection mechanism") {
    validate(
      "custom-facets.raml",
      erroneousTypeShape => {
        erroneousTypeShape.level should be("Violation")
        erroneousTypeShape.targetNode should be(
          "file://amf-client/shared/src/test/resources/error/custom-facets.raml#/declarations/scalar/ErroneousType")
        erroneousTypeShape.validationId should be(ParserSideValidations.ClosedShapeSpecification.id)
      },
      incorrect1 => {
        incorrect1.level should be("Violation")
        incorrect1.targetNode should be(
          "file://amf-client/shared/src/test/resources/error/custom-facets.raml#/declarations/union/Incorrect1")
        incorrect1.validationId should be(ParserSideValidations.ClosedShapeSpecification.id)
      },
      incorrect2 => {
        incorrect2.level should be("Violation")
        incorrect2.targetNode should be(
          "file://amf-client/shared/src/test/resources/error/custom-facets.raml#/declarations/union/Incorrect2")
        incorrect2.validationId should be(ParserSideValidations.ClosedShapeSpecification.id)
      },
      incorrect3 => {
        incorrect3.level should be("Violation")
        incorrect3.targetNode should be(
          "file://amf-client/shared/src/test/resources/error/custom-facets.raml#/declarations/union/Incorrect3")
        incorrect3.validationId should be(ParserSideValidations.ClosedShapeSpecification.id)
      }
    )
  }

  test("Invalid node parsing type") {
    validate(
      "invalid-type.raml",
      artist => {
        artist.level should be("Violation")
        artist.message should be("Expecting !!str and !!seq provided")
        artist.position.map(_.range) should be(Some(Range((115, 10), (115, 12))))
      },
      tracks => {
        tracks.level should be("Violation")
        tracks.message should be("Expecting !!str and !!seq provided")
        tracks.position.map(_.range) should be(Some(Range((120, 10), (120, 12))))
      }
    )
  }

  test("Inline external fragment from non mutable ref") {
    validate(
      "/inline-non-mutable-ref/api.raml",
      invalidRef => {
        invalidRef.level should be("Violation")
        invalidRef.message should be("Cannot inline a fragment in a not mutable node")
        invalidRef.position.map(_.range) should be(Some(Range((3, 8), (3, 17))))
      },
      invalidModule => {
        invalidModule.level should be("Violation")
        invalidModule.message should startWith("Expected module but found: ExternalFragment(")
        invalidModule.position.map(_.range) should be(Some(Range((3, 2), (5, 0))))
      },
      unresolvedRef => {
        unresolvedRef.level should be("Violation")
        unresolvedRef.message should startWith("Unresolved reference 'lib1.B' from root context")
        unresolvedRef.position.map(_.range) should be(Some(Range((9, 9), (9, 15))))
      }
    )
  }

  // todo: json schema parser test? should expose json schema parser?
  test("Not seq in dependencies entry at json schema type def") {
    validate(
      "/not-seq-dependency-def-jsonchema.raml",
      invalidSeq => {
        invalidSeq.level should be("Violation")
        invalidSeq.message should startWith("Expected scalar but found:")
        invalidSeq.position.map(_.range) should be(Some(Range((34, 16), (42, 4))))
      }
    )
  }

  override protected val basePath: String = "file://amf-client/shared/src/test/resources/error/"

  override protected def build(validation: Validation, file: String): Future[BaseUnit] =
    AMFCompiler(file, platform, RamlYamlHint, validation).build()
}
