package amf.validation

import amf.core.remote.{Hint, OasYamlHint}

// TODO: Add test to check for Japanese characters in OAS 3 Component schema name. @author: Tomás Fernández
class OasJapaneseCharsetValidationTest extends MultiPlatformReportGenTest {

  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/japanese/oas/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/japanese/oas/"
  override val hint: Hint          = OasYamlHint

  test("Documentation server params") {
    validate("documentation-server-params.json")
  }

  test("Documentation path and response description") {
    validate("documentation-path-and-response-description.json")
  }

  test("Documentation path item description") {
    validate("documentation-path-item-description.json")
  }

  test("Documentation security schemes") {
    validate("documentation-security-schemes.json")
  }

  test("Facet string pattern valid") {
    validate("facet-string-pattern-valid.yaml")
  }

  test("Facet string pattern invalid") {
    validate("facet-string-pattern-invalid.yaml", Some("facet-string-pattern-invalid.report"))
  }

  test("Facet string length valid") {
    validate("facet-string-length-valid.yaml")
  }

  test("Extensions") {
    validate("extensions.yaml")
  }

  test("Security definitions") {
    validate("security-definitions.yaml")
  }

  test("Full API check") {
    validate("full-check.json")
  }

  /*
  Note: Runs fine locally but doesnt run in remote Jenkins due to filename being in Japanese.
  As Jenkins coverage broke while parsing the japanese name, the file was deleted and its contents were dumped below
  test("JSON Schema include") {
    validate("json-schema-include.yaml")
  }

  {
  "$id": "https://example.com/person.schema.json",
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "人",
  "type": "object",
  "properties": {
    "ファーストネーム": {
      "type": "string",
      "description": "人の名。"
    },
    "苗字": {
      "type": "string",
      "description": "その人の姓"
    },
    "年齢": {
      "description": "ゼロ以上でなければならない年数",
      "type": "integer",
      "minimum": 0
    },
    "住所": {
      "$ref": "#/definitions/住所"
    }
  },
  "definitions": {
    "住所": {
      "type": "object",
      "properties": {
        "住所": {
          "type": "string"
        },
        "シティ": {
          "type": "string"
        },
        "状態": {
          "type": "string"
        }
      },
      "required": [
        "street_address",
        "city",
        "state"
      ]
    }
  }
}
   */

  test("Documentation info") {
    validate("documentation-info.json")
  }
}
