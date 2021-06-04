package amf.plugins.document.apicontract.validation.remote

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

object Draft4MetaSchema {
  val text =
    "{\"id\":\"http://json-schema.org/draft-04/schema#\",\"$schema\":\"http://json-schema.org/draft-04/schema#\",\"description\":\"Core schema meta-schema\",\"definitions\":{\"schemaArray\":{\"type\":\"array\",\"minItems\":1,\"items\":{\"$ref\":\"#\"}},\"positiveInteger\":{\"type\":\"integer\",\"minimum\":0},\"positiveIntegerDefault0\":{\"allOf\":[{\"$ref\":\"#/definitions/positiveInteger\"},{\"default\":0}]},\"simpleTypes\":{\"enum\":[\"array\",\"boolean\",\"integer\",\"null\",\"number\",\"object\",\"string\"]},\"stringArray\":{\"type\":\"array\",\"items\":{\"type\":\"string\"},\"minItems\":1,\"uniqueItems\":true}},\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"string\",\"format\":\"uri\"},\"$schema\":{\"type\":\"string\",\"format\":\"uri\"},\"title\":{\"type\":\"string\"},\"description\":{\"type\":\"string\"},\"default\":{},\"multipleOf\":{\"type\":\"number\",\"minimum\":0,\"exclusiveMinimum\":true},\"maximum\":{\"type\":\"number\"},\"exclusiveMaximum\":{\"type\":\"boolean\",\"default\":false},\"minimum\":{\"type\":\"number\"},\"exclusiveMinimum\":{\"type\":\"boolean\",\"default\":false},\"maxLength\":{\"$ref\":\"#/definitions/positiveInteger\"},\"minLength\":{\"$ref\":\"#/definitions/positiveIntegerDefault0\"},\"pattern\":{\"type\":\"string\",\"format\":\"regex\"},\"additionalItems\":{\"anyOf\":[{\"type\":\"boolean\"},{\"$ref\":\"#\"}],\"default\":{}},\"items\":{\"anyOf\":[{\"$ref\":\"#\"},{\"$ref\":\"#/definitions/schemaArray\"}],\"default\":{}},\"maxItems\":{\"$ref\":\"#/definitions/positiveInteger\"},\"minItems\":{\"$ref\":\"#/definitions/positiveIntegerDefault0\"},\"uniqueItems\":{\"type\":\"boolean\",\"default\":false},\"maxProperties\":{\"$ref\":\"#/definitions/positiveInteger\"},\"minProperties\":{\"$ref\":\"#/definitions/positiveIntegerDefault0\"},\"required\":{\"$ref\":\"#/definitions/stringArray\"},\"additionalProperties\":{\"anyOf\":[{\"type\":\"boolean\"},{\"$ref\":\"#\"}],\"default\":{}},\"definitions\":{\"type\":\"object\",\"additionalProperties\":{\"$ref\":\"#\"},\"default\":{}},\"properties\":{\"type\":\"object\",\"additionalProperties\":{\"$ref\":\"#\"},\"default\":{}},\"patternProperties\":{\"type\":\"object\",\"additionalProperties\":{\"$ref\":\"#\"},\"default\":{}},\"dependencies\":{\"type\":\"object\",\"additionalProperties\":{\"anyOf\":[{\"$ref\":\"#\"},{\"$ref\":\"#/definitions/stringArray\"}]}},\"enum\":{\"type\":\"array\",\"minItems\":1,\"uniqueItems\":true},\"type\":{\"anyOf\":[{\"$ref\":\"#/definitions/simpleTypes\"},{\"type\":\"array\",\"items\":{\"$ref\":\"#/definitions/simpleTypes\"},\"minItems\":1,\"uniqueItems\":true}]},\"allOf\":{\"$ref\":\"#/definitions/schemaArray\"},\"anyOf\":{\"$ref\":\"#/definitions/schemaArray\"},\"oneOf\":{\"$ref\":\"#/definitions/schemaArray\"},\"not\":{\"$ref\":\"#\"}},\"dependencies\":{\"exclusiveMaximum\":[\"maximum\"],\"exclusiveMinimum\":[\"minimum\"]},\"default\":{}}"
  lazy val instance: js.Object = js.JSON.parse(text).asInstanceOf[js.Object]
}

@js.native
trait ValidationResult extends js.Any {
  val keyword: String    = js.native
  val dataPath: String   = js.native
  val schemaPath: String = js.native
  val params: js.Object  = js.native
  val message: String    = js.native
}

@JSGlobal
@js.native
class Ajv(options: js.Object) extends js.Object {
  def validate(schema: js.Object, data: js.Dynamic): Boolean = js.native
  def addMetaSchema(metaSchema: js.Object): Ajv              = js.native
  def addFormat(name: String, formatValidator: Any): Ajv     = js.native
  val errors: js.UndefOr[js.Array[ValidationResult]]         = js.native
}

object AjvValidator {
  private lazy val options = js.JSON
    .parse(
      "{\"schemaId\":\"auto\", \"unknownFormats\": \"ignore\", \"allErrors\": true, \"validateSchema\": false, \"multipleOfPrecision\": 6}")
    .asInstanceOf[js.Object]
  private lazy val fastOptions = js.JSON
    .parse(
      "{\"schemaId\":\"auto\", \"unknownFormats\": \"ignore\", \"allErrors\": false, \"validateSchema\": false, \"multipleOfPrecision\": 6}")
    .asInstanceOf[js.Object]

  def apply(): Ajv = {
    val ajv = js.Dynamic
      .newInstance(nativeAjv)(options)
      .asInstanceOf[Ajv]
      .addMetaSchema(Draft4MetaSchema.instance)
    setValidators(ajv)
  }

  def fast(): Ajv = {
    val ajv = js.Dynamic
      .newInstance(nativeAjv)(fastOptions)
      .asInstanceOf[Ajv]
      .addMetaSchema(Draft4MetaSchema.instance)
    setValidators(ajv)
  }

  private def setValidators(ajv: Ajv) = {
    ajv
      .addFormat(
        "RFC2616",
        "^(Mon|Tue|Wed|Thu|Fri|Sat|Sun), (0[1-9]|[12][0-9]|3[01]) (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) ([0-9]{4}) ([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]|60) (GMT)$"
      )
      .addFormat(
        "rfc2616",
        "^(Mon|Tue|Wed|Thu|Fri|Sat|Sun), (0[1-9]|[12][0-9]|3[01]) (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) ([0-9]{4}) ([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]|60) (GMT)$"
      )
      .addFormat(
        "date-time-only",
        "^([0-9]{4})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])[Tt]([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]|60)(\\.[0-9]+)?$")
      .addFormat("date", "^([0-9]+)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$")
  }

  def nativeAjv: js.Dynamic =
    if (js.isUndefined(js.Dynamic.global.Ajv)) {
      throw new Exception("Cannot find global Ajv object")
    } else {
      js.Dynamic.global.Ajv
    }
}

object LazyAjv {
  lazy val default   = AjvValidator()
  lazy val fast: Ajv = AjvValidator.fast()
}
