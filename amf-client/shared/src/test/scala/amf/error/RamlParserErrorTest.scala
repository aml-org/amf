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
      "error/unexpected-nodes.raml",
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
      "error/custom-facets.raml",
      erroneousTypeShape => {
        erroneousTypeShape.level should be("Violation")
        erroneousTypeShape.targetNode should be(
          "file://amf-client/shared/src/test/resources/parser-results/raml/error/custom-facets.raml#/declarations/types/scalar/ErroneousType")
        erroneousTypeShape.validationId should be(ParserSideValidations.ClosedShapeSpecification.id)
      },
      incorrect1 => {
        incorrect1.level should be("Violation")
        incorrect1.targetNode should be(
          "file://amf-client/shared/src/test/resources/parser-results/raml/error/custom-facets.raml#/declarations/types/union/Incorrect1")
        incorrect1.validationId should be(ParserSideValidations.ClosedShapeSpecification.id)
      },
      incorrect2 => {
        incorrect2.level should be("Violation")
        incorrect2.targetNode should be(
          "file://amf-client/shared/src/test/resources/parser-results/raml/error/custom-facets.raml#/declarations/types/union/Incorrect2")
        incorrect2.validationId should be(ParserSideValidations.ClosedShapeSpecification.id)
      },
      incorrect3 => {
        incorrect3.level should be("Violation")
        incorrect3.targetNode should be(
          "file://amf-client/shared/src/test/resources/parser-results/raml/error/custom-facets.raml#/declarations/types/union/Incorrect3")
        incorrect3.validationId should be(ParserSideValidations.ClosedShapeSpecification.id)
      }
    )
  }

  test("Invalid node parsing type") {
    validate(
      "error/invalid-type.raml",
      artist => {
        artist.level should be("Violation")
        artist.message should be("Expecting !!str and !!seq provided")
        artist.position.map(_.range) should be(Some(Range((44, 10), (44, 12))))
      },
      tracks => {
        tracks.level should be("Violation")
        tracks.message should be("Expecting !!str and !!seq provided")
        tracks.position.map(_.range) should be(Some(Range((49, 10), (49, 12))))
      }
    )
  }

  test("Inline external fragment from non mutable ref") {
    validate(
      "error/inline-non-mutable-ref/api.raml",
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
      "error/not-seq-dependency-def-jsonchema.raml",
      invalidSeq => {
        invalidSeq.level should be("Violation")
        invalidSeq.message should startWith("Expected scalar but found:")
        invalidSeq.position.map(_.range) should be(Some(Range((42, 33), (50, 21))))
      }
    )
  }

  test("Overflow number") {
    validate(
      "error/overflow-number.raml",
      numberViolation => {
        numberViolation.level should be("Violation")
        numberViolation.message should startWith("Cannot parse '9223372036854776000' with tag '?'")
      }
    )
  }

  test("Duplicated title property test") {
    validate(
      "/error/dup_title.raml",
      numberViolation => {
        numberViolation.level should be("Violation")
        numberViolation.message should startWith("Property 'title' is duplicated")
      }
    )
  }

  test("Duplicated endpoints validations test") {
    validate(
      "error/dup_endpoint.raml",
      numberViolation => {
        numberViolation.level should be("Violation")
        numberViolation.message should startWith("Duplicated resource path /users/foo")
      }
    )
  }

  test("Invalid baseUri validations test") {
    validate(
      "error/invalid_baseuri.raml",
      numberViolation => {
        numberViolation.level should be("Violation")
        numberViolation.message should startWith("'http://{myapi.com' is not a valid template uri")
      }
    )
  }

  ignore("Invalid mediaType validations test") {
    validate(
      "/invalid_media_type.raml",
      numberViolation => {
        numberViolation.level should be("Violation")
        numberViolation.message should startWith("")
      }
    )
  }

  test("Mutually exclusive 'type' and 'schema' facets validations test") {
    validate(
      "error/type-exclusive-facets.raml",
      exclusive1 => {
        exclusive1.level should be("Violation")
        exclusive1.message should startWith("'schema' and 'type' properties are mutually exclusive")
        exclusive1.position.map(_.range) should be(Some(Range((8, 4), (8, 10))))
      },
      exclusive1Warning => {
        exclusive1Warning.level should be("Warning")
        exclusive1Warning.message should startWith(
          "'schema' keyword it's deprecated for 1.0 version, should use 'type' instead")
        exclusive1Warning.position.map(_.range) should be(Some(Range((8, 4), (8, 10))))
      },
      exclusive2 => {
        exclusive2.level should be("Violation")
        exclusive2.message should startWith("'schema' and 'type' properties are mutually exclusive")
        exclusive2.position.map(_.range) should be(Some(Range((17, 12), (17, 18))))
      },
      exclusive2Warning => {
        exclusive2Warning.level should be("Warning")
        exclusive2Warning.message should startWith(
          "'schema' keyword it's deprecated for 1.0 version, should use 'type' instead")
        exclusive2Warning.position.map(_.range) should be(Some(Range((17, 12), (17, 18))))

      }
    )
  }

  test("Mutually exclusive 'types' and 'schemas' facets validations test") {
    validate(
      "error/webapi-exclusive-facets.raml",
      exclusive1 => {
        exclusive1.level should be("Violation")
        exclusive1.message should startWith("'schemas' and 'types' properties are mutually exclusive")
      },
      warning => {
        warning.level should be("Warning")
        warning.message should startWith(
          "'schemas' keyword it's deprecated for 1.0 version, should use 'types' instead")
      }
    )
  }

  test("Null trait API") {
    validate(
      "warning/null_trait.raml",
      warning => {
        warning.level should be("Warning")
        warning.message should startWith("Generating abstract declaration (resource type / trait)  with null value")
      }
    )
  }

  test("Invalid example in any shape") {
    validate(
      "/warning/any-shape-invalid-example.raml",
      warning => {
        warning.level should be("Warning")
        warning.message should startWith("Error node '{")
      }
    )
  }

  test("Chained references violation test") {
    validate(
      "/error/chained/api.raml",
      chained => {
        chained.level should be("Violation")
        chained.message should startWith("Chained reference")
      }
    )
  }

  test("Invalid path template syntax text") {
    validate(
      "/error/unbalanced_paths.raml",
      baseUri => {
        baseUri.level should be("Violation")
        baseUri.message should startWith("Invalid path template syntax")
        baseUri.position.map(_.range) should be(Some(Range((4, 23), (6, 0))))

      },
      param => {
        param.level should be("Violation")
        param.message should startWith("Invalid path template syntax")
        param.position.map(_.range) should be(Some(Range((3, 9), (3, 38))))

      }
    )
  }

  test("Empty library entry") {
    validate(
      "/valid/empty-library-entry.raml",
      violation => {
        violation.level should be("Violation")
        violation.message should be("Missing library location")
        violation.position.map(_.range) should be(Some(Range((4, 2), (4, 14))))
      }
    )
  }

  test("Bad ident flow array and map") {
    validate("/valid/bad-ident-flow.raml")
  }

  test("Bad ident flow map") {
    validate("/valid/bad-ident-flow-map.raml")
  }

  test("Invalid custom facets name format") {
    validate(
      "/error/invalid-facet-format.raml",
      violation => {
        violation.level should be("Violation")
        violation.message should be("Default type name cannot be used to name a custom type")
        violation.position.map(_.range) should be(Some(Range((4, 2), (4, 10))))
      }
    )
  }

  test("Invalid closed shape media type 08") {
    validate(
      "/error/invalid-mediatype.raml",
      violation => {
        violation.level should be("Violation")
        violation.message should be("Property applicationjson not supported in a raml 0.8 shape node")
        violation.position.map(_.range) should be(Some(Range((8, 10), (8, 26))))
      }
    )
  }

  test("Invalid payload key - close shape") {
    validate(
      "/error/invalid-payload-facet.raml",
      violation => {
        violation.level should be("Violation")
        violation.message should be("Properties typically not supported in a raml 1.0 anyShape node")
        violation.position.map(_.range) should be(Some(Range((10, 12), (15, 30))))
      }
    )
  }

  override protected val basePath: String = "file://amf-client/shared/src/test/resources/parser-results/raml/"

  override protected def build(validation: Validation, file: String): Future[BaseUnit] =
    AMFCompiler(file, platform, RamlYamlHint, validation).build()
}
