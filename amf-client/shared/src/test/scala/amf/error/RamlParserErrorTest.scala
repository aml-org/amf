package amf.error

import amf.core.model.document.{BaseUnit, Document}
import amf.core.parser.Range
import amf.core.parser.errorhandler.{ParserErrorHandler, UnhandledParserErrorHandler}
import amf.core.remote.RamlYamlHint
import amf.facades.{AMFCompiler, Validation}
import amf.plugins.domain.shapes.models.{ScalarShape, UnresolvedShape}
import amf.validation.DialectValidations.ClosedShapeSpecification
import amf.validations.ParserSideValidations.MissingRequiredUserDefinedFacet

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
        erroneousTypeShape.validationId should be(MissingRequiredUserDefinedFacet.id)
      },
      erroneousTypeShape => {
        erroneousTypeShape.level should be("Violation")
        erroneousTypeShape.targetNode should be(
          "file://amf-client/shared/src/test/resources/parser-results/raml/error/custom-facets.raml#/declarations/types/scalar/ErroneousType")
        erroneousTypeShape.validationId should be(ClosedShapeSpecification.id)
      },
      incorrect1 => {
        incorrect1.level should be("Violation")
        incorrect1.targetNode should be(
          "file://amf-client/shared/src/test/resources/parser-results/raml/error/custom-facets.raml#/declarations/types/union/Incorrect1")
        incorrect1.validationId should be(ClosedShapeSpecification.id)
      },
      incorrect2 => {
        incorrect2.level should be("Violation")
        incorrect2.targetNode should be(
          "file://amf-client/shared/src/test/resources/parser-results/raml/error/custom-facets.raml#/declarations/types/union/Incorrect2")
        incorrect2.validationId should be(ClosedShapeSpecification.id)
      },
      incorrect3 => {
        incorrect3.level should be("Violation")
        incorrect3.targetNode should be(
          "file://amf-client/shared/src/test/resources/parser-results/raml/error/custom-facets.raml#/declarations/types/union/Incorrect3")
        incorrect3.validationId should be(ClosedShapeSpecification.id)
      }
    )
  }

  test("Invalid node parsing type") {
    validate(
      "error/invalid-type.raml",
      artist => {
        artist.level should be("Violation")
        artist.message should be("Invalid property name")
        artist.position.map(_.range) should be(Some(Range((44, 10), (44, 12))))
      },
      tracks => {
        tracks.level should be("Violation")
        tracks.message should be("Invalid property name")
        tracks.position.map(_.range) should be(Some(Range((49, 10), (49, 12))))
      },
      anotherTrack => {
        anotherTrack.level should be("Violation")
        anotherTrack.message should be("Invalid property name")
        anotherTrack.position.map(_.range) should be(Some(Range((54, 10), (54, 12))))
      }
    )
  }

  test("Inline external fragment from non mutable ref") {
    validate(
      "error/inline-non-mutable-ref/api.raml",
      badInclude1 => {
        badInclude1.level should be("Violation")
        badInclude1.message should startWith("Fragments must be imported by using '!include'")
      },
      badInclude2 => {
        badInclude2.level should be("Violation")
        badInclude2.message should startWith("Fragments must be imported by using '!include'")
      },
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
        unresolvedRef.message should startWith("Unresolved reference 'lib1.B'")
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

  ignore("Invalid example in any shape") {
    validate(
      "/warning/any-shape-invalid-example.raml",
      warning => {
        warning.level should be("Warning")
        warning.message should startWith("Error node ',")
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
        violation.message should be("'datetime' cannot be used to name a custom type")
        violation.position.map(_.range) should be(Some(Range((4, 2), (4, 10))))
      }
    )
  }

  test("Invalid closed shape media type 08") {
    validate(
      "/error/invalid-mediatype.raml",
      violation => {
        violation.level should be("Violation")
        violation.message should be("Property 'applicationjson' not supported in a RAML 0.8 shape node")
        violation.position.map(_.range) should be(Some(Range((8, 10), (8, 26))))
      }
    )
  }

  test("Invalid payload key - close shape") {
    validate(
      "/error/invalid-payload-facet.raml",
      violation => {
        violation.level should be("Violation")
        violation.message should be("Property 'typically' not supported in a RAML 1.0 any node")
        violation.position.map(_.range) should be(Some(Range((10, 12), (15, 30))))
      }
    )
  }

  test("Invalid scope at secured by") {
    validate(
      "/error/invalid-scope-secured-by.raml",
      violation => {
        violation.level should be("Violation")
        violation.message should be("Scope 'USER' not found in settings of declared secured by oauth_2_0.")
        violation.position.map(_.range) should be(Some(Range((15, 45), (15, 49))))
      }
    )
  }

  test("Invalid scope at secured by defined in fragment") {
    validate(
      "/error/invalid-scope-secured-by-in-fragment.raml",
      violation => {
        violation.level should be("Violation")
        violation.message should be("Scope 'USER' not found in settings of declared secured by oauth_2_0.")
        violation.position.map(_.range) should be(Some(Range((9, 45), (9, 49))))
      }
    )
  }

  test("Test invalid map in resource type use") {
    validate("/valid/invalid-map-resource-type.raml")
  }

  test("baseUriParameters without baseUri") {
    validate(
      "/error/no-base-uri.raml",
      violation => {
        violation.level should be("Violation")
        violation.message should be("'baseUri' not defined and 'baseUriParameters' defined.")
        violation.position.map(_.range) should be(Some(Range((4, 0), (5, 14))))
      },
      warning => {
        warning.level should be("Warning")
        warning.message should be("Unused base uri parameter some")
        warning.position.map(_.range) should be(Some(Range((5, 2), (5, 14))))
      }
    )
  }

  test("Invalid type def with json schemas includes") {
    validate(
      "/error/invalid-jsonschema-includes/cloudhub-api.raml",
      violation => {
        violation.level should be("Violation")
        violation.message should be("Cannot parse JSON Schema expression out of a non string value")
        violation.position.map(_.range) should be(Some(Range((7, 23), (10, 62))))
      }
    )
  }

  test("Numeric key in external fragment root entry") {
    validate("/valid/numeric-key-in-external-fragment/api.raml")
  }

  test("Invalid library and type def in 08") {
    validate(
      "/error/invalid-lib-and-type-08/api.raml",
      first => {
        first.level should be("Violation")
        first.message should be("Property 'uses' not supported in a RAML 0.8 webApi node")
        first.position.map(_.range) should be(Some(Range((4, 0), (8, 0))))
      },
      second => {
        second.level should be("Violation")
        second.message should be("Invalid type def duTypes.storyCollection for RAML 0.8")
        second.position.map(_.range) should be(Some(Range((14, 18), (14, 41))))
      }
    )
  }

  test("Invalid library tag type def") {
    validate(
      "/error/invalid-lib-tagtype/api.raml",
      first => {
        first.level should be("Violation")
        first.message should be("Missing library location")
        first.position.map(_.range) should be(Some(Range((5, 2), (14, 12))))
      }
    )
  }

  test("Invalid map key") {
    validate(
      "/error/map-key.raml",
      first => {
        first.level should be("Violation")
        first.message should be("Property '{alpha2code: }' not supported in a RAML 1.0 webApi node")
        first.position.map(_.range) should be(Some(Range((7, 0), (9, 14))))
      }
    )
  }

  test("Json example external that starts with space") {
    validate("/valid/json-example-space-start/api.raml")
  }

  test("Discriminator in union definition") {
    validate(
      "/error/discriminator_union.raml",
      error => {
        error.level should be("Violation")
        error.message should be("Property discriminator forbidden in a node extending a unionShape")
        error.position.map(_.range) should be(Some(Range((20, 3), (20, 25))))
      }
    )
  }

  test("Connect and trace methods") {
    validate("/valid/connect-trace.raml")
  }

  test("Cycle in references handled exception") {
    validateWithUnit(
      "/error/cycle-references/api.raml",
      (u: BaseUnit) => {
        val shape = u.asInstanceOf[Document].declares.head.asInstanceOf[ScalarShape]
        shape.linkTarget.get.isInstanceOf[UnresolvedShape] should be(false)
      },
      Seq(
        error => {
          error.level should be("Violation")
          error.message should startWith(
            "Cyclic found following references file://amf-client/shared/src/test/resources/parser-results/raml/error/cycle-references/api.raml ->")
          error.message should endWith(
            "-> file://amf-client/shared/src/test/resources/parser-results/raml/error/cycle-references/yaKassa.raml")
        },
        unresolve1 => {
          unresolve1.level should be("Violation")
          unresolve1.message should startWith("Unresolved reference 'lib.InvoiceId'")
        },
        badInclude => {
          badInclude.level should be("Violation")
          badInclude.message should startWith("Libraries must be applied by using 'uses'")
        },
        unresolve2 => {
          unresolve2.level should be("Violation")
          unresolve2.message should startWith("Unresolved reference 'typeFragment.raml'")
        }
      )
    )
  }

  test("Forward reference with facets in base") {
    validate("/valid/forward-ref-with-facets.raml")
  }

  test("Forward reference with type son") {
    validate("/valid/future-ref-withType.raml")
  }

  test("Items keyword at node shape") {
    validate(
      "/error/items-key-in-object.raml",
      error => {
        error.level should be("Violation")
        error.message should be("Property 'items' not supported in a RAML 1.0 object node")
        error.position.map(_.range) should be(Some(Range((14, 4), (14, 16))))
      }
    )
  }

  test("Uses keyword at node shape fragment") {
    validate(
      "/error/uses-in-object.raml"
    )
  }

  test("test non used base uri and uri params") {
    validate(
      "/warning/unused-uri-params.raml",
      endPoint => {
        endPoint.level should be("Warning")
        endPoint.message should be("Unused uri parameter unusedUriParam")
        endPoint.position.map(_.range) should be(Some(Range((18, 4), (19, 18))))
      },
      baseUri => {
        baseUri.level should be("Warning")
        baseUri.message should be("Unused base uri parameter unusedParam")
        baseUri.position.map(_.range) should be(Some(Range((9, 2), (11, 0))))
      }
    )
  }

  test("test non used operation uri param 08") {
    validate(
      "/warning/unused-uri-params-operation08.raml",
      operation => {
        operation.level should be("Warning")
        operation.message should be("Unused operation uri parameter unusedUriParam")
        operation.position.map(_.range) should be(Some(Range((9, 6), (10, 20))))
      }
    )
  }

  ignore("Invalid json example - unquoted key") {
    validate(
      "/error/unquoted-json-key-example.raml",
      error => {
        error.level should be("Violation")
        error.message should be("Error node 'x'")
        error.position.map(_.range) should be(Some(Range((16, 20), (16, 21))))
      }
    )
  }

  test("Invalid ints formats") {
    validate(
      "warning/invalid-number-formats.raml",
      doubleWarning => {
        doubleWarning.level should be("Warning")
        doubleWarning.message should startWith(
          "Format double is not valid for type http://www.w3.org/2001/XMLSchema#integer")
      },
      floatWarning => {
        floatWarning.level should be("Warning")
        floatWarning.message should startWith(
          "Format float is not valid for type http://www.w3.org/2001/XMLSchema#integer")
      }
    )
  }

  test("Test location error in fragment") {
    validate(
      "error/error-fragment/api.raml",
      notYmap => {
        notYmap.level should be("Violation")
        notYmap.location.get should endWith("error-fragment/fragment.raml")
      }
    )
  }

  test("Test reference by id at json schema") {
    validate("valid/reference-by-id/api.raml")
  }

  test("Test invalid string format in jsonschema number") {
    validate(
      "error/invalid-number-format/api.raml",
      duplicateWarning => {
        duplicateWarning.level should be("Warning")
        duplicateWarning.message should endWith("Duplicate key : 'type'")
      },
      formatWarning => {
        formatWarning.level should be("Warning")
        formatWarning.message should endWith(
          "Format UTC_MILLISEC is not valid for type http://a.ml/vocabularies/shapes#number")
      }
    )
  }

  test("Test swap between referenced schema and example") {
    validate(
      "error/swap-schema-example/api.raml",
      notYmap => {
        notYmap.level should be("Violation")
        notYmap.message should be("YAML map expected")
      }
    )
  }

  test("Test invalid fragment (distinct type)") {
    validate(
      "error/invalid-fragment/api.raml",
      fragmentError => {
        fragmentError.level should be("Violation")
        fragmentError.message should be("Fragment of type ResourceType does not conform to the expected type DataType")
      },
      resourceType => {
        resourceType.level should be("Violation")
        resourceType.message should be("ResourceType Resource not found")
      },
      unresolved => {
        unresolved.level should be("Violation")
        unresolved.message should be("Unresolved reference 'fragment.raml'")
        unresolved.location should be(
          Some("file://amf-client/shared/src/test/resources/parser-results/raml/error/invalid-fragment/api.raml"))
      }
    )
  }

  test("Test that chain ref in valid paths works ok") {
    validate(
      "valid/points-in-path/api.raml",
      fileNotFound => {
        fileNotFound.level should be("Violation")
        fileNotFound.message should startWith("File Not Found")
      },
      unresolved => {
        unresolved.level should be("Violation")
        unresolved.message should startWith("Unresolved reference ")
      }
    )
  }

  test("Test empty flow map in resource type reference") {
    validate(
      "error/empty-map-resource-type.raml",
      resourceType => {
        resourceType.level should be("Violation")
        resourceType.message should be("Invalid model extension.")
      }
    )
  }

  override protected val basePath: String = "file://amf-client/shared/src/test/resources/parser-results/raml/"

  override protected def build(eh: ParserErrorHandler, file: String): Future[BaseUnit] =
    AMFCompiler(file, platform, RamlYamlHint, eh = eh).build()
}
