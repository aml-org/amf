package amf.plugins.document.webapi.validation

import amf._
import amf.core.validation.{SeverityLevels => Severity}
import amf.core.vocabulary.Namespace.XsdTypes
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.webapi.validation.AMFRawValidations.AMFValidation
import amf.plugins.domain.webapi.metamodel._

object AMFRawValidations {

  /**
    * @param uri                 URI of the validation, null to auto-generate
    * @param message             Optional message for the validation, propagates to all spec-specific messages if they're all empty
    * @param owlClass            Optional OWL class target of the validation
    * @param owlProperty         Optional OWL property target of the validation
    * @param target              Default is "sh(path)"
    * @param constraint          URI of the constraint component
    * @param value               Value for the constraint component. Default is "0"
    * @param ramlErrorMessage    (optional) specify the validation message thrown in raml
    * @param openApiErrorMessage (optional) specify the validation message thrown in Oas
    * @param severity            The severity of the validation: VIOLATION | WARNING | INFO. Default is VIOLATION
    */
  class AMFValidation(val uri: Option[String],
                      val message: Option[String],
                      val owlClass: String,
                      val owlProperty: String,
                      val target: String,
                      val constraint: String,
                      val value: String,
                      val ramlErrorMessage: String,
                      val openApiErrorMessage: String,
                      val severity: String)
  object AMFValidation {
    def fromStrings(uri: String = "",
                    message: String = "",
                    owlClass: String,
                    owlProperty: String,
                    target: String = "sh:path",
                    constraint: String,
                    value: String = "0",
                    ramlErrorMessage: String = "",
                    openApiErrorMessage: String = "",
                    severity: String = Severity.VIOLATION): AMFValidation = {

      def iri(s: String) = Namespace.staticAliases.uri(s).iri()
      val sameMessage    = message.nonEmpty && ramlErrorMessage.isEmpty && openApiErrorMessage.isEmpty

      new AMFValidation(
        uri = optional(uri).map(Namespace.staticAliases.uri(_).iri()),
        message = optional(message),
        owlClass = iri(owlClass),
        owlProperty = iri(owlProperty),
        target = iri(target),
        constraint = iri(constraint),
        value = adaptValue(constraint, value),
        ramlErrorMessage = if (sameMessage) message else ramlErrorMessage,
        openApiErrorMessage = if (sameMessage) message else openApiErrorMessage,
        severity = severity
      )
    }

    def apply(uri: Option[ValueType] = None,
              message: String = "",
              owlClass: ValueType,
              owlProperty: ValueType,
              target: ValueType = sh("path"),
              constraint: ValueType,
              value: String = "0",
              ramlErrorMessage: String = "",
              openApiErrorMessage: String = "",
              severity: String = Severity.VIOLATION): AMFValidation = {

      val sameMessage = message.nonEmpty && ramlErrorMessage.isEmpty && openApiErrorMessage.isEmpty

      new AMFValidation(
        uri = uri.map(_.iri()),
        message = optional(message),
        owlClass = owlClass.iri(),
        owlProperty = owlProperty.iri(),
        target = target.iri(),
        constraint = constraint.iri(),
        value = adaptValue(constraint.iri(), value),
        ramlErrorMessage = if (sameMessage) message else ramlErrorMessage,
        openApiErrorMessage = if (sameMessage) message else openApiErrorMessage,
        severity = severity
      )
    }

    def adaptValue(constraint: String, value: String): String =
      if (constraint.endsWith("pattern")) value
      else Namespace.staticAliases.uri(value).iri() // this might not be a URI, but trying to expand it is still safe

    def optional(s: String): Option[String] = if (s.isEmpty) None else Some(s.trim)
  }

  // owl
  def apiContract(name: String): ValueType = ValueType(Namespace.ApiContract, name)

  def doc(name: String): ValueType = ValueType(Namespace.Document, name)

  def core(name: String): ValueType = ValueType(Namespace.Core, name)

  def shape(name: String): ValueType = ValueType(Namespace.Shapes, name)

  def security(name: String): ValueType = ValueType(Namespace.Security, name)

  def amfParser(name: String): Option[ValueType] = Some(ValueType(Namespace.AmfParser, name))

  def apiBinding(name: String): ValueType = ValueType(Namespace.ApiBinding, name)

  // constraints
  def sh(name: String): ValueType = ValueType(Namespace.Shacl, name)

  val dataType: ValueType = sh("datatype")
  val minCount: ValueType = sh("minCount")
  val maxCount: ValueType = sh("maxCount")

  // values
  val string: String  = XsdTypes.xsdString.iri()
  val boolean: String = XsdTypes.xsdBoolean.iri()
  val integer: String = XsdTypes.xsdInteger.iri()

  val schemaRequiredInParameter: AMFValidation = AMFValidation(
    owlClass = apiContract(ParameterModel.doc.displayName),
    owlProperty = shape("schema"),
    constraint = minCount,
    value = "1",
    ramlErrorMessage = "RAML Type information is mandatory for parameters",
    openApiErrorMessage = "Schema/type information required for Parameter objects",
  )

  // applying base api validations with specific WebAPI and AsyncAPI keys due to backwards compatibility
  def baseApiValidations(key: String): List[AMFValidation] = List(
    AMFValidation(
      owlClass = apiContract(key),
      owlProperty = core("name"),
      constraint = minCount,
      value = "1",
      ramlErrorMessage = "API title is mandatory",
      openApiErrorMessage = "Info object 'title' must be a single value"
    ),
    AMFValidation(
      owlClass = apiContract(key),
      owlProperty = apiContract("scheme"),
      constraint = dataType,
      value = string,
      ramlErrorMessage = "API BaseUri scheme information must be a string",
      openApiErrorMessage = "Swagger object 'schemes' must be a string"
    ),
    AMFValidation(
      owlClass = apiContract(key),
      owlProperty = apiContract("scheme"),
      constraint = dataType,
      value = string,
      ramlErrorMessage = "API BaseUri scheme information must be a string",
      openApiErrorMessage = "Swagger object 'schemes' must be a string"
    ),
    AMFValidation(
      owlClass = apiContract(key),
      owlProperty = apiContract("accepts"),
      constraint = dataType,
      value = string,
      ramlErrorMessage = "Default media types must contain strings",
      openApiErrorMessage = "Field 'consumes' must contain strings"
    ),
    AMFValidation(
      owlClass = apiContract(key),
      owlProperty = apiContract("accepts"),
      constraint = sh("pattern"),
      value = "^(([-\\w]+|[*]{1})\\/([-+.\\w]+|[*]{1}))(\\s*;\\s*\\w+=[-+\\w.]+)*$",
      ramlErrorMessage = "Default media types must be valid",
      openApiErrorMessage = "Field 'produces' must be valid"
    ),
    AMFValidation(
      owlClass = apiContract(key),
      owlProperty = core("mediaType"),
      constraint = dataType,
      value = string,
      ramlErrorMessage = "Default media types must be string",
      openApiErrorMessage = "Field 'produces' must contain strings"
    ),
    AMFValidation(
      owlClass = apiContract(key),
      owlProperty = core("version"),
      constraint = dataType,
      value = string,
      ramlErrorMessage = "API version must be a string",
      openApiErrorMessage = "Info object 'version' must be string"
    ),
    AMFValidation(
      owlClass = apiContract(key),
      owlProperty = core("termsOfService"),
      constraint = dataType,
      value = string,
      ramlErrorMessage = "API terms of service must be a string",
      openApiErrorMessage = "Info object 'termsOfService' must be string"
    )
  )

  trait ProfileValidations {
    def validations(): Seq[AMFValidation]
  }

  trait AmfProfileValidations extends ProfileValidations {
    private lazy val result = Seq(
      AMFValidation(
        owlClass = doc("DomainElement"),
        owlProperty = core("name"),
        constraint = dataType,
        value = string,
        ramlErrorMessage = "Title and names must be string",
        openApiErrorMessage = "Names must be string"
      ),
      AMFValidation(
        owlClass = apiContract("Parameter"),
        owlProperty = apiContract("paramName"),
        uri = amfParser("mandatory-header-name-pattern"),
        constraint = shape("mandatoryHeaderNamePattern"),
        ramlErrorMessage = "Header name must comply RFC-7230",
        openApiErrorMessage = "Header name must comply RFC-7230",
        severity = Severity.WARNING
      ),
      AMFValidation(
        owlClass = doc("DomainElement"),
        owlProperty = core("description"),
        constraint = dataType,
        value = string,
        ramlErrorMessage = "Descriptions must be strings",
        openApiErrorMessage = "Description must be strings"
      ),
      AMFValidation(
        owlClass = core("Organization"),
        owlProperty = core("email"),
        constraint = dataType,
        value = string,
        ramlErrorMessage = "API provider email must be a string",
        openApiErrorMessage = "Contact object 'email' must be a string"
      ),
      AMFValidation(
        owlClass = apiContract("EndPoint"),
        owlProperty = apiContract("path"),
        constraint = dataType,
        value = string,
        ramlErrorMessage = "Resource path must be a string",
        openApiErrorMessage = "PathItem object path must be a string"
      ),
      AMFValidation(
        message = "Methods' summary information must be a string",
        owlClass = apiContract("Operation"),
        owlProperty = apiContract("guiSummary"),
        constraint = dataType,
        value = string
      ),
      AMFValidation(
        message = "Methods' deprecated must be a boolean",
        owlClass = apiContract("Operation"),
        owlProperty = doc("deprecated"),
        constraint = dataType,
        value = boolean
      ),
      AMFValidation(
        owlClass = apiContract("Operation"),
        owlProperty = apiContract("scheme"),
        constraint = dataType,
        value = string,
        ramlErrorMessage = "Protocols must contain strings",
        openApiErrorMessage = "Schemes must contain strings"
      ),
      AMFValidation(
        owlClass = apiContract("Operation"),
        owlProperty = apiContract("accepts"),
        constraint = dataType,
        value = string,
        ramlErrorMessage = "Method default media types consumed must be strings",
        openApiErrorMessage = "Operation object 'consumes' must be strings"
      ),
      AMFValidation(
        owlClass = apiContract("Response"),
        owlProperty = apiContract("statusCode"),
        constraint = dataType,
        value = string,
        ramlErrorMessage = "Status code for a Response must be a string",
        openApiErrorMessage = "Status code for a Response object must be a string"
      ),
      AMFValidation(
        owlClass = apiContract(ParameterModel.doc.displayName),
        owlProperty = core("name"),
        constraint = minCount,
        value = "1",
        ramlErrorMessage = "Parameter information must have a name",
        openApiErrorMessage = "Parameter object must have a name property"
      ),
      AMFValidation(
        owlClass = apiContract(ParameterModel.doc.displayName),
        owlProperty = apiContract("required"),
        constraint = dataType,
        value = boolean,
        ramlErrorMessage = "Information about required parameters must be a boolean value",
        openApiErrorMessage = "Required property of a Parameter object must be boolean"
      ),
      AMFValidation(
        owlClass = apiContract(ParameterModel.doc.displayName),
        owlProperty = apiContract("binding"),
        constraint = dataType,
        value = string,
        ramlErrorMessage = "Information about the binding of the parameter is mandatory",
        openApiErrorMessage = "'in' property of a Parameter object must be a string"
      ),
      AMFValidation(
        owlClass = apiContract(ParameterModel.doc.displayName),
        owlProperty = apiContract("binding"),
        constraint = minCount,
        value = "1",
        ramlErrorMessage = "Binding information for a parameter is mandatory",
        openApiErrorMessage = "'in' property of a Parameter object is mandatory"
      ),
      AMFValidation(
        owlClass = apiContract("Payload"),
        owlProperty = core("mediaType"),
        constraint = dataType,
        value = string,
        ramlErrorMessage = "Method default media types must be strings",
        openApiErrorMessage = "Operation object 'produces' must be strings"
      ),
      AMFValidation(
        uri = amfParser("xml-wrapped-scalar"),
        message = "XML property 'wrapped' must be false for scalar types",
        owlClass = shape("ScalarShape"),
        owlProperty = sh("xmlSerialization"),
        constraint = shape("xmlWrappedScalar")
      ),
      AMFValidation(
        uri = amfParser("xml-non-scalar-attribute"),
        message = "XML property 'attribute' must be false for non-scalar types",
        owlClass = shape("Shape"),
        owlProperty = sh("xmlSerialization"),
        constraint = shape("xmlNonScalarAttribute")
      ),
      AMFValidation(
        message = "XML attribute serialisation info must be boolean",
        owlClass = shape("XMLSerializer"),
        owlProperty = shape("xmlAtribute"),
        constraint = dataType,
        value = boolean
      ),
      AMFValidation(
        message = "XML wrapping serialisation info must be boolean",
        owlClass = shape("XMLSerializer"),
        owlProperty = shape("xmlWrapped"),
        constraint = dataType,
        value = boolean
      ),
      AMFValidation(
        message = "XML name serialisation info must be string",
        owlClass = shape("XMLSerializer"),
        owlProperty = shape("xmlName"),
        constraint = dataType,
        value = string
      ),
      AMFValidation(
        message = "XML namespace serialisation info must be string",
        owlClass = shape("XMLSerializer"),
        owlProperty = shape("xmlNamespace"),
        constraint = dataType,
        value = string
      ),
      AMFValidation(
        message = "XML prefix serialisation info must be string",
        owlClass = shape("XMLSerializer"),
        owlProperty = shape("xmlPrefix"),
        constraint = dataType,
        value = string
      ),
      AMFValidation(
        owlClass = shape("ObjectShape"),
        owlProperty = shape("minProperties"),
        constraint = sh("minInclusive"),
        ramlErrorMessage = "minProperties for a RAML Object type cannot be negative",
        openApiErrorMessage = "minProperties for a Schema object cannot be negative"
      ),
      AMFValidation(
        owlClass = shape("ObjectShape"),
        owlProperty = shape("minProperties"),
        constraint = dataType,
        value = integer,
        ramlErrorMessage = "minProperties for a RAML Object type must be an integer",
        openApiErrorMessage = "minProperties for a Schema object must be an integer"
      ),
      AMFValidation(
        owlClass = shape("ObjectShape"),
        owlProperty = shape("maxProperties"),
        constraint = sh("minInclusive"),
        ramlErrorMessage = "maxProperties for a RAML Object type cannot be negative",
        openApiErrorMessage = "maxProperties for a Schema object cannot be negative"
      ),
      AMFValidation(
        owlClass = shape("ObjectShape"),
        owlProperty = shape("maxProperties"),
        constraint = dataType,
        value = integer,
        ramlErrorMessage = "maxProperties for a RAML Object type must be an integer",
        openApiErrorMessage = "maxProperties for a Schema object must be an integer"
      ),
      AMFValidation(
        owlClass = shape("ObjectShape"),
        owlProperty = sh("closed"),
        constraint = dataType,
        value = boolean,
        ramlErrorMessage = "additionalProperties for a RAML Object type must be a boolean",
        openApiErrorMessage = "additionalProperties for a Schema object must be a boolean"
      ),
      AMFValidation(
        owlClass = shape("ObjectShape"),
        owlProperty = shape("discriminator"),
        constraint = dataType,
        value = string,
        ramlErrorMessage = "discriminator for RAML Object type must be a string value",
        openApiErrorMessage = "discriminator for a Schema object must be a string value"
      ),
      AMFValidation(
        owlClass = shape("ObjectShape"),
        owlProperty = shape("discriminatorValue"),
        constraint = dataType,
        value = string,
        ramlErrorMessage = "x-discriminatorValue for RAML Object type must be a string value",
        openApiErrorMessage = "discriminatorValue for a Schema object must be a string value"
      ),
      AMFValidation(
        owlClass = shape("ObjectShape"),
        owlProperty = shape("readOnly"),
        constraint = dataType,
        value = boolean,
        ramlErrorMessage = "(readOnly) for a RAML Object type must be a boolean",
        openApiErrorMessage = "readOnly for a Schema object must be a boolean"
      ),
      AMFValidation(
        owlClass = shape("ArrayShape"),
        owlProperty = minCount,
        constraint = dataType,
        value = integer,
        ramlErrorMessage = "minItems for a RAML Array type must be an integer",
        openApiErrorMessage = "minItems of a Schema object of type 'array' must be an integer"
      ),
      AMFValidation(
        owlClass = shape("ArrayShape"),
        owlProperty = minCount,
        constraint = sh("minInclusive"),
        ramlErrorMessage = "maxItems for a RAML Array type must be greater than 0",
        openApiErrorMessage = "maxItems of a Schema object of type 'array' must be greater than 0"
      ),
      AMFValidation(
        owlClass = shape("ArrayShape"),
        owlProperty = sh("maxCount"),
        constraint = dataType,
        value = integer,
        ramlErrorMessage = "maxItems for a RAML Array type must be an integer",
        openApiErrorMessage = "maxItems of a Schema object of type 'array' must be an integer"
      ),
      AMFValidation(
        owlClass = shape("ArrayShape"),
        owlProperty = minCount,
        constraint = sh("minInclusive"),
        ramlErrorMessage = "minItems for a RAML Array type must be greater than 0",
        openApiErrorMessage = "minItems of a Schema object of type 'array' must be greater than 0"
      ),
      AMFValidation(
        owlClass = shape("ArrayShape"),
        owlProperty = sh("maxCount"),
        constraint = sh("minInclusive"),
        ramlErrorMessage = "maxItems for a RAML Array type must be greater than 0",
        openApiErrorMessage = "maxItems of a Schema object of type 'array' must be greater than 0"
      ),
      AMFValidation(
        owlClass = shape("ArrayShape"),
        owlProperty = shape("uniqueItems"),
        constraint = dataType,
        value = boolean,
        ramlErrorMessage = "uniqueItems for a RAML Array type must be a boolean",
        openApiErrorMessage = "uniqueItems of a Schema object of type 'array' must be a boolean"
      ),
      AMFValidation(
        message = "minContains for an array type must be an integer",
        owlClass = shape("ArrayShape"),
        owlProperty = sh("qualifiedMinCount"),
        constraint = dataType,
        value = integer,
      ),
      AMFValidation(
        message = "maxContains for an array type must be an integer",
        owlClass = shape("ArrayShape"),
        owlProperty = sh("qualifiedMaxCount"),
        constraint = dataType,
        value = integer
      ),
      AMFValidation(
        message = "minContains facet should be greater or equal than 0",
        owlClass = shape("ArrayShape"),
        owlProperty = sh("qualifiedMinCount"),
        constraint = sh("minInclusive")
      ),
      AMFValidation(
        message = "maxContains facet should be greater or equal than 0",
        owlClass = shape("ArrayShape"),
        owlProperty = sh("qualifiedMaxCount"),
        constraint = sh("minInclusive")
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = sh("pattern"),
        constraint = dataType,
        value = string,
        ramlErrorMessage = "pattern facet for a RAML scalar type must be a string",
        openApiErrorMessage = "pattern for scalar Schema object of scalar type must be a string"
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = sh("minLength"),
        constraint = dataType,
        value = integer,
        ramlErrorMessage = "minLength facet for a RAML scalar type must be a integer",
        openApiErrorMessage = "minLength for scalar Schema object of scalar type must be a integer"
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = sh("maxLength"),
        constraint = dataType,
        value = integer,
        ramlErrorMessage = "maxLength facet for a RAML scalar type must be a integer",
        openApiErrorMessage = "maxLength for scalar Schema object of scalar type must be a integer"
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = sh("minInclusive"),
        constraint = dataType,
        value = "xsd:double",
        ramlErrorMessage = "minimum facet for a RAML scalar type must be a number",
        openApiErrorMessage = "minimum for scalar Schema object of scalar type must be a integer"
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = sh("maxInclusive"),
        constraint = dataType,
        value = "xsd:double",
        ramlErrorMessage = "maximum facet for a RAML scalar type must be a number",
        openApiErrorMessage = "maximum for scalar Schema object of scalar type must be a integer"
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = sh("minExclusive"),
        constraint = dataType,
        value = boolean,
        ramlErrorMessage = "x-exclusiveMinimum facet for a RAML scalar type must be a boolean",
        openApiErrorMessage = "exclusiveMinimum for scalar Schema object of scalar type must be a boolean"
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = sh("maxExclusive"),
        constraint = dataType,
        value = boolean,
        ramlErrorMessage = "x-exclusiveMaximum facet for a RAML scalar type must be a boolean",
        openApiErrorMessage = "exclusiveMaximum for scalar Schema object of scalar type must be a boolean"
      ),
      AMFValidation(
        message = "Min length facet should be greater or equal than 0",
        owlClass = shape("ScalarShape"),
        owlProperty = sh("minLength"),
        constraint = sh("minInclusive")
      ),
      AMFValidation(
        message = "Max length facet should be greater or equal than 0",
        owlClass = shape("ScalarShape"),
        owlProperty = sh("maxLength"),
        constraint = sh("minInclusive")
      ),
      AMFValidation(
        message = "Min length facet should be greater or equal than 0",
        owlClass = shape("FileShape"),
        owlProperty = sh("minLength"),
        constraint = sh("minInclusive")
      ),
      AMFValidation(
        message = "Max length facet should be greater or equal than 0",
        owlClass = shape("FileShape"),
        owlProperty = sh("maxLength"),
        constraint = sh("minInclusive")
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = shape("format"),
        constraint = dataType,
        value = string,
        ramlErrorMessage = "format facet for a RAML scalar type must be a string",
        openApiErrorMessage = "format for scalar Schema object of scalar type must be a string"
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = shape("multipleOf"),
        constraint = dataType,
        value = "xsd:double",
        ramlErrorMessage = "multipleOf facet for a RAML scalar type must be a number",
        openApiErrorMessage = "multipleOf for scalar Schema object of scalar type must be a number"
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = shape("multipleOf"),
        constraint = sh("minExclusive"),
        ramlErrorMessage = "multipleOf facet for a RAML scalar type must be greater than 0",
        openApiErrorMessage = "multipleOf for scalar Schema object of scalar type must be greater than 0"
      ),
      AMFValidation(
        owlClass = shape("ScalarShape"),
        owlProperty = dataType,
        constraint = minCount,
        value = "1",
        ramlErrorMessage = "type information for a RAML scalar is required",
        openApiErrorMessage = "type information fo a Schema object of scalar type is required"
      ),
      AMFValidation(
        owlClass = apiContract("Tag"),
        owlProperty = core("name"),
        constraint = minCount,
        value = "1",
        ramlErrorMessage = "Tag must have a name",
        openApiErrorMessage = "Tag object must have a name property"
      ),
      AMFValidation(
        owlClass = apiContract("Server"),
        owlProperty = core("urlTemplate"),
        constraint = dataType,
        value = string,
        ramlErrorMessage = "API baseUri host information must be a string",
        openApiErrorMessage = "Swagger object 'host' and 'basePath' must be a string"
      ),
      AMFValidation(
        message = "Server 'description' property must be a string",
        owlClass = apiContract("Server"),
        owlProperty = core("description"),
        constraint = dataType,
        value = string
      ),
      AMFValidation(
        message = "Server must have an 'url' property",
        owlClass = apiContract("Server"),
        owlProperty = core("urlTemplate"),
        constraint = minCount,
        value = "1"
      ),
      AMFValidation(
        message = "Security scheme type is mandatory",
        owlClass = security("SecurityScheme"),
        owlProperty = security("type"),
        constraint = minCount,
        value = "1"
      ),
      AMFValidation(
        message = "Type is mandatory in a Security Scheme Object",
        owlClass = security("SecurityScheme"),
        owlProperty = security("type"),
        constraint = minCount,
        value = "1"
      ),
      AMFValidation(
        uri = amfParser("strict-url-strinzgs"),
        message = "URLs in values mapped to core:url must be valid",
        owlClass = doc("DomainElement"),
        owlProperty = core("url"),
        target = sh("targetObjectsOf"),
        constraint = sh("nodeKind"),
        value = sh("IRI").iri(),
        ramlErrorMessage = "URLs must be valid",
        openApiErrorMessage = "URLs must be valid"
      ),
      AMFValidation(
        uri = amfParser("pattern-validation"),
        message = "Pattern is not valid",
        owlClass = shape("ScalarShape"),
        owlProperty = sh("pattern"),
        constraint = shape("patternValidation"),
      ),
      AMFValidation(
        owlClass = sh("NodeShape"),
        owlProperty = shape("unevaluatedPropertiesSchema"),
        constraint = maxCount,
        severity = Severity.WARNING,
        message = "Unevaluated properties facet won't be taken into account in validation"
      ),
      AMFValidation(
        owlClass = shape("ArrayShape"),
        owlProperty = shape("unevaluatedItemsSchema"),
        constraint = maxCount,
        severity = Severity.WARNING,
        message = "Unevaluated items facet won't be taken into account in validation"
      ),
      AMFValidation(
        owlClass = shape("ArrayShape"),
        owlProperty = sh("qualifiedMinCount"),
        constraint = maxCount,
        severity = Severity.WARNING,
        message = "minContains facet won't be taken into account in validation"
      ),
      AMFValidation(
        owlClass = shape("ArrayShape"),
        owlProperty = sh("qualifiedMaxCount"),
        constraint = maxCount,
        severity = Severity.WARNING,
        message = "maxContains facet won't be taken into account in validation"
      )
    )

    override def validations(): Seq[AMFValidation] = result
  }

  object AmfValidations extends AmfProfileValidations

  trait WebApiValidations extends AmfProfileValidations {
    private lazy val result = super.validations() ++ Seq(
      AMFValidation(
        owlClass = apiContract(ParameterModel.doc.displayName),
        owlProperty = apiContract("binding"),
        constraint = sh("in"),
        value = "query,path,header,uri,cookie",
        ramlErrorMessage = "Binding information for a parameter with an invalid value",
        openApiErrorMessage = "'in' property of a parameter with an invalid value"
      ),
      AMFValidation(
        owlClass = apiContract("EndPoint"),
        owlProperty = apiContract("path"),
        constraint = sh("pattern"),
        value = "^/",
        ramlErrorMessage = "Resource path must start with a '/'",
        openApiErrorMessage = "PathItem path must start with a '/'"
      ),
      AMFValidation(
        owlClass = apiContract("Operation"),
        owlProperty = apiContract("method"),
        constraint = sh("in"),
        value = "get,put,post,delete,options,head,patch,connect,trace",
        ramlErrorMessage = "Unknown method type",
        openApiErrorMessage = "Unknown Operation method"
      ),
      AMFValidation(
        message = "Security scheme type should be one of the supported ones",
        owlClass = security("SecurityScheme"),
        owlProperty = security("type"),
        constraint = sh("pattern"),
        // Includes all types due to compatibility reasons.
        value =
          "^OAuth\\s1.0|OAuth\\s2.0|Basic\\sAuthentication|Digest\\sAuthentication|Pass\\sThrough|Api\\sKey|http|openIdConnect|userPassword|X509|symmetricEncryption|asymmetricEncryption|x-.+$"
      )
    ) ++ baseApiValidations("WebAPI")

    override def validations(): Seq[AMFValidation] = result
  }

  trait RamlValidations extends WebApiValidations {
    private lazy val result = super.validations() ++ Seq(
      AMFValidation(
        owlClass = apiContract("WebAPI"),
        owlProperty = core("name"),
        constraint = sh("minLength"),
        value = "1",
        ramlErrorMessage = "Info object 'title' must not be empty",
        openApiErrorMessage = "API name must not be an empty string"
      ),
      AMFValidation(
        owlClass = core("CreativeWork"),
        owlProperty = core("title"),
        constraint = minCount,
        value = "1",
        ramlErrorMessage = "API documentation title is mandatory",
        openApiErrorMessage = "Documentation object 'x-title' is mandatory"
      ),
      AMFValidation(
        owlClass = core("CreativeWork"),
        owlProperty = core("description"),
        constraint = minCount,
        value = "1",
        ramlErrorMessage = "API documentation content is mandatory",
        openApiErrorMessage = "Documentation object 'description' is mandatory"
      ),
      AMFValidation(
        owlClass = core("CreativeWork"),
        owlProperty = core("title"),
        message = "Documentation title MUST be a non-empty string",
        constraint = sh("minLength"),
        value = "1",
        severity = Severity.WARNING // TODO: should be violation
      ),
      AMFValidation(
        owlClass = core("CreativeWork"),
        owlProperty = core("description"),
        message = "Documentation content MUST be a non-empty string",
        constraint = sh("minLength"),
        value = "1",
        severity = Severity.WARNING // TODO: should be violation
      ),
      AMFValidation(
        owlClass = doc("DomainProperty"),
        owlProperty = shape("schema"),
        constraint = minCount,
        value = "1",
        ramlErrorMessage = "type is mandatory for a RAML annotationType",
        openApiErrorMessage = "schema is mandatory for an extension type"
      ),
      AMFValidation(
        message =
          "Invalid authorization grant. The options are: authorization_code, password, client_credentials, implicit or any valid absolute URI",
        owlClass = security("Settings"),
        owlProperty = security("authorizationGrant"),
        constraint = sh("pattern"),
        value = "^authorization_code|password|client_credentials|implicit|(\\w+:(\\/?\\/?)[^\\s]+)$"
      ),
      AMFValidation(
        message = "requestTokenUri is required when security type is OAuth 1.0",
        owlClass = security("OAuth1Settings"),
        owlProperty = security("requestTokenUri"),
        constraint = minCount,
        value = "1",
        severity = Severity.WARNING
      ),
      AMFValidation(
        message = "authorizationUri is required when security type is OAuth 1.0",
        owlClass = security("OAuth1Settings"),
        owlProperty = security("authorizationUri"),
        constraint = minCount,
        value = "1",
        severity = Severity.WARNING
      ),
      AMFValidation(
        message = "tokenCredentialsUri is required when security type is OAuth 1.0",
        owlClass = security("OAuth1Settings"),
        owlProperty = security("tokenCredentialsUri"),
        constraint = minCount,
        value = "1",
        severity = Severity.WARNING
      ),
      AMFValidation(
        uri = amfParser("raml-root-schemes-values"),
        message = "Protocols property must be http or https",
        owlClass = apiContract("WebAPI"),
        owlProperty = apiContract("scheme"),
        constraint = sh("pattern"),
        value = "^(H|h)(T|t)(T|t)(P|p)(S|s)?$",
        ramlErrorMessage = "Protocols must have a case insensitive value matching http or https",
        openApiErrorMessage =
          "Swagger object 'schemes' property must have a case insensitive value matching http or https"
      ),
      AMFValidation(
        uri = amfParser("raml-operation-schemes-values"),
        message = "Protocols property must be http or https",
        owlClass = apiContract("Operation"),
        owlProperty = apiContract("scheme"),
        constraint = sh("pattern"),
        value = "^(H|h)(T|t)(T|t)(P|p)(S|s)?$",
        ramlErrorMessage = "Protocols must have a case insensitive value matching http or https",
        openApiErrorMessage =
          "Swagger object 'schemes' property must have a case insensitive value matching http or https"
      ),
      AMFValidation(
        uri = amfParser("raml-root-schemes-non-empty-array"),
        message = "Protocols must be a non-empty array of case-insensitive strings with values 'http' and/or 'https'",
        owlClass = apiContract("WebAPI"),
        owlProperty = apiContract("scheme"),
        constraint = shape("nonEmptyListOfProtocols")
      ),
      AMFValidation(
        uri = amfParser("raml-operation-schemes-non-empty-array"),
        message = "Protocols must be a non-empty array of case-insensitive strings with values 'http' and/or 'https'",
        owlClass = apiContract("Operation"),
        owlProperty = apiContract("scheme"),
        constraint = shape("nonEmptyListOfProtocols")
      ),
      AMFValidation(
        uri = amfParser("min-max-inclusive"),
        message = "Maximum must be greater than or equal to minimum",
        owlClass = shape("ScalarShape"),
        owlProperty = sh("minInclusive"),
        constraint = shape("minimumMaximumValidation")
      ),
      AMFValidation(
        uri = amfParser("min-max-items"),
        message = "MaxItems must be greater than or equal to minItems",
        owlClass = shape("ArrayShape"),
        owlProperty = minCount,
        constraint = shape("minMaxItemsValidation")
      ),
      AMFValidation(
        uri = amfParser("min-max-length"),
        message = "MaxLength must be greater than or equal to minLength",
        owlClass = shape("ScalarShape"),
        owlProperty = sh("minLength"),
        constraint = shape("minMaxLengthValidation")
      ),
      AMFValidation(
        uri = amfParser("min-max-length"),
        message = "MaxLength must be greater than or equal to minLength",
        owlClass = shape("FileShape"),
        owlProperty = sh("minLength"),
        constraint = shape("minMaxLengthValidation")
      ),
      AMFValidation(
        uri = amfParser("min-max-properties"),
        message = "MaxProperties must be greater than or equal to minProperties",
        owlClass = sh("NodeShape"),
        owlProperty = shape("minProperties"),
        constraint = shape("minMaxPropertiesValidation")
      ),
      AMFValidation(
        owlClass = apiContract("Payload"),
        message = "Payload media type is mandatory",
        owlProperty = core("mediaType"),
        constraint = minCount,
        value = "1"
      ),
      AMFValidation(
        message = "Invalid OAuth 1.0 signature. The options are: HMAC-SHA1, RSA-SHA1, or PLAINTEXT",
        owlClass = security("Settings"),
        owlProperty = security("signature"),
        constraint = sh("pattern"),
        value = "^HMAC-SHA1|RSA-SHA1|PLAINTEXT$"
      ),
      AMFValidation(
        owlClass = apiContract("Response"),
        owlProperty = apiContract("statusCode"),
        constraint = sh("pattern"),
        value = "^([1-5]{1}[0-9]{2})$|^(default)$",
        ramlErrorMessage = "Status code for a Response must be a value between 100 and 599",
        openApiErrorMessage = "Status code for a Response must be a value between 100 and 599 or 'default'"
      ),
      schemaRequiredInParameter
    )

    override def validations(): Seq[AMFValidation] = result
  }

  object Raml10Validations extends RamlValidations {
    private lazy val result = super.validations() ++ Seq(
      )

    override def validations(): Seq[AMFValidation] = result
  }

  object Raml08Validations extends RamlValidations {
    private lazy val result = super.validations() ++ Seq(
      AMFValidation(
        message = "Invalid authorization grant. The options are: code, token, owner or credentials",
        owlClass = security("Settings"),
        owlProperty = security("authorizationGrant"),
        constraint = sh("pattern"),
        value = "^code|token|owner|credentials$"
      ),
      AMFValidation(
        uri = amfParser("raml-schemes"),
        message = "Protocols property must be http or https",
        owlClass = apiContract("WebAPI"),
        owlProperty = apiContract("scheme"),
        constraint = sh("in"),
        value = "http,https,HTTP,HTTPS",
        ramlErrorMessage = "Protocols must have a case insensitive value matching http or https",
        openApiErrorMessage =
          "Swagger object 'schemes' property must have a case insensitive value matching http or https"
      ),
      AMFValidation(
        uri = amfParser("min-max-properties"),
        message = "MaxProperties must be greater than or equal to minProperties",
        owlClass = sh("NodeShape"),
        owlProperty = shape("minProperties"),
        constraint = shape("minMaxPropertiesValidation"),
      )
    )

    override def validations(): Seq[AMFValidation] = result
  }

  trait OasValidations extends WebApiValidations with GenericValidations {
    private lazy val result = super.validations() ++ Seq(
      AMFValidation(
        uri = amfParser("mandatory-api-version"),
        message = "Missing madatory Swagger / info / version",
        owlClass = apiContract("WebAPI"),
        owlProperty = core("version"),
        constraint = minCount,
        value = "1",
        ramlErrorMessage = "API Version is Mandatory",
        openApiErrorMessage = "Version is mandatory in Info object"
      ),
      AMFValidation(
        uri = amfParser("openapi-schemes"),
        message = "Protocols property must be http,https,ws,wss",
        owlClass = apiContract("WebAPI"),
        owlProperty = apiContract("scheme"),
        constraint = sh("in"),
        value = "http,https,ws,wss",
        ramlErrorMessage = "Protocols must match a value http, https, ws or wss",
        openApiErrorMessage = "Swagger object 'schemes' property must have a value matching http, https, ws or wss"
      ),
      AMFValidation(
        uri = amfParser("mandatory-external-doc-url"),
        message = "Swagger external-doc element without URL",
        owlClass = core("CreativeWork"),
        owlProperty = core("url"),
        constraint = minCount,
        value = "1",
        ramlErrorMessage = "Documentation URL is mandatory in API external documentation",
        openApiErrorMessage = "URL is mandatory in External Documentation object"
      ),
      AMFValidation(
        uri = amfParser("mandatory-license-name"),
        message = "Swagger License node without name",
        owlClass = core("License"),
        owlProperty = core("name"),
        constraint = minCount,
        value = "1",
        ramlErrorMessage = "License name is mandatory if license information is included",
        openApiErrorMessage = "Name is mandatory in License object"
      ),
      AMFValidation(
        uri = amfParser("empty-responses"),
        message = "No responses declared",
        owlClass = apiContract("Operation"),
        owlProperty = apiContract("returns"),
        constraint = minCount,
        value = "1",
        ramlErrorMessage = "Responses array cannot be empty",
        openApiErrorMessage = "Responses cannot be empty"
      ),
      AMFValidation(
        uri = amfParser("empty-enum"),
        message = "Enum in types cannot be empty",
        owlClass = shape("Shape"),
        owlProperty = sh("in"),
        constraint = sh("node"),
        value = amfParser("NonEmptyList").get.iri(),
        ramlErrorMessage = "Property 'enum' must have at least one value",
        openApiErrorMessage = "Property 'enum' for a Schema object must have at least one value"
      ),
      AMFValidation(
        uri = amfParser("array-shape-items-mandatory"),
        message = "Declaration of the type of the items for an array is required",
        owlClass = shape("ArrayShape"),
        owlProperty = shape("items"),
        constraint = minCount,
        value = "1",
        ramlErrorMessage = "items facet of RAML Array type is required",
        openApiErrorMessage = "items property of Schema objects of type 'array' is required"
      ),
      AMFValidation(
        uri = amfParser("path-parameter-required"),
        message = "Path parameters must have the required property set to true",
        owlClass = apiContract(ParameterModel.doc.displayName),
        owlProperty = apiContract("binding"),
        constraint = shape("pathParameterRequiredProperty")
      ),
      AMFValidation(
        uri = amfParser("file-parameter-in-form-data"),
        message = "Parameter of type file must set property 'in' to formData",
        owlClass = apiContract(ParameterModel.doc.displayName),
        owlProperty = shape("schema"),
        constraint = shape("fileParameterMustBeInFormData")
      ),
      AMFValidation(
        uri = amfParser("description-is-required-in-response"),
        message = "Description must be defined in a response",
        owlClass = apiContract("Response"),
        owlProperty = core("description"),
        constraint = minCount,
        value = "1",
        openApiErrorMessage = "Response must have a 'description' field"
      ),
      emailValidation(core("Organization"), core("email")),
      urlValidation(core("License"), core("url")),
      urlValidation(core("Organization"), core("url")),
      urlValidation(core("CreativeWork"), core("url"))
    )

    override def validations(): Seq[AMFValidation] = result
  }

  object Oas20Validations extends OasValidations {
    private lazy val result = super.validations() ++ Seq(
      AMFValidation(
        owlClass = apiContract("Response"),
        owlProperty = apiContract("statusCode"),
        constraint = sh("pattern"),
        value = "^([1-5]{1}[0-9]{2})$|^(default)$",
        openApiErrorMessage = "Status code for a Response must be a value between 100 and 599 or 'default'"
      ),
      AMFValidation(
        message = "Invalid flow. The options are: implicit, password, application or accessCode",
        owlClass = security("OAuth2Flow"),
        owlProperty = security("flow"),
        constraint = sh("pattern"),
        value = "^(implicit|password|application|accessCode)$"
      ),
      AMFValidation(
        message = "Invalid 'in' value. The options are: query or header",
        owlClass = security("Settings"),
        owlProperty = security("in"),
        constraint = sh("pattern"),
        value = "^(query|header)$"
      ),
      schemaRequiredInParameter
    )

    override def validations(): Seq[AMFValidation] = result
  }

  object Async20Validations extends AmfProfileValidations with GenericValidations {
    private lazy val result = super.validations() ++ Seq(
      AMFValidation(
        owlClass = apiContract("AsyncAPI"),
        owlProperty = core("version"),
        constraint = minCount,
        value = "1",
        ramlErrorMessage = "API version is mandatory",
        openApiErrorMessage = "Info object 'version' is mandatory"
      ),
      AMFValidation(
        owlClass = apiBinding("HttpMessageBinding"),
        owlProperty = apiBinding("headers"),
        uri = amfParser("mandatory-header-name-pattern"),
        constraint = shape("mandatoryHeaderBindingNamePattern"),
        ramlErrorMessage = "Header name must comply RFC-7230",
        openApiErrorMessage = "Header name must comply RFC-7230",
        severity = Severity.WARNING
      ),
      AMFValidation(
        message = "Documentation 'url' field is mandatory",
        owlClass = core("CreativeWork"),
        owlProperty = core("url"),
        constraint = minCount,
        value = "1"
      ),
      AMFValidation(
        message = "License 'name' is mandatory",
        owlClass = core("License"),
        owlProperty = core("name"),
        constraint = minCount,
        value = "1"
      ),
      AMFValidation(
        message = """Parameter name must comply with regex '^[A-Za-z0-9_\-]+$'""",
        owlClass = apiContract(ParameterModel.doc.displayName),
        owlProperty = core("name"),
        constraint = sh("pattern"),
        value = """^[A-Za-z0-9_\-]+$""".stripMargin
      ),
      AMFValidation(
        message = """Server name must comply with regex '^[A-Za-z0-9_\-]+$'""",
        owlClass = apiContract("Server"),
        owlProperty = core("name"),
        constraint = sh("pattern"),
        value = """^[A-Za-z0-9_\-]+$""".stripMargin
      ),
      AMFValidation(
        message = "Server 'protocol' field is mandatory",
        owlClass = apiContract("Server"),
        owlProperty = apiContract("protocol"),
        constraint = minCount,
        value = "1"
      ),
      AMFValidation(
        message = "'name' field is mandatory in httpApiKey security scheme",
        owlClass = security("HttpApiKeySettings"),
        owlProperty = core("name"),
        constraint = minCount,
        value = "1"
      ),
      AMFValidation(
        message = "'location' field is mandatory in CorrelationId",
        owlClass = core("CorrelationId"),
        owlProperty = core("location"),
        constraint = minCount,
        value = "1"
      ),
      AMFValidation(
        message = "'in' field is mandatory in ApiKey scheme",
        owlClass = security("HttpApiKeySettings"),
        owlProperty = security("in"),
        constraint = minCount,
        value = "1"
      ),
      AMFValidation(
        message = "'in' field is mandatory in HttpApiKey scheme",
        owlClass = security("ApiKeySettings"),
        owlProperty = security("in"),
        constraint = minCount,
        value = "1"
      ),
      AMFValidation(
        message = "Invalid 'in' value. The options are: query, header or cookie",
        owlClass = security("HttpApiKeySettings"),
        owlProperty = security("in"),
        constraint = sh("pattern"),
        value = "^(query|header|cookie)$"
      ),
      AMFValidation(
        message = "Invalid 'in' value. The options are: user or password",
        owlClass = security("ApiKeySettings"),
        owlProperty = security("in"),
        constraint = sh("pattern"),
        value = "^(user|password)$"
      ),
      AMFValidation(
        message = "'scheme' field is mandatory in http security scheme",
        owlClass = security("HttpSettings"),
        owlProperty = security("scheme"),
        constraint = minCount,
        value = "1"
      ),
      AMFValidation(
        owlClass = apiBinding("WebSocketsChannelBinding"),
        owlProperty = apiBinding("method"),
        constraint = sh("in"),
        value = "GET,POST",
        openApiErrorMessage = "'method' for channel binding object must be one of 'GET' or 'POST'"
      ),
      AMFValidation(
        owlClass = apiBinding("MqttOperationBinding"),
        owlProperty = apiBinding("qos"),
        constraint = sh("pattern"),
        value = "^[0-2]$",
        openApiErrorMessage = "'qos' for mqtt operation binding object must be one of 0, 1 or 2"
      ),
      AMFValidation(
        owlClass = apiBinding("Amqp091ChannelBinding"),
        owlProperty = apiBinding("is"),
        constraint = sh("in"),
        value = "routingKey,queue",
        openApiErrorMessage = "'is' for amqp 0.9.1 channel binding object must be one of 'queue' or 'routingKey'"
      ),
      AMFValidation(
        owlClass = apiBinding("Amqp091ChannelExchange"),
        owlProperty = apiBinding("type"),
        constraint = sh("in"),
        value = "topic,direct,fanout,default,headers",
        openApiErrorMessage =
          "'type' for amqp 0.9.1 channel exchange object must be one of 'topic', 'direct', 'fanout', 'default' or 'headers'"
      ),
      AMFValidation(
        owlClass = apiBinding("HttpOperationBinding"),
        owlProperty = apiBinding("method"),
        constraint = sh("in"),
        value = "GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS,CONNECT,TRACE",
        openApiErrorMessage =
          "'method' for http operation binding object must be one of 'GET','POST','PUT','PATCH','DELETE','HEAD','OPTIONS','CONNECT','TRACE'"
      ),
      AMFValidation(
        owlClass = apiBinding("HttpOperationBinding"),
        owlProperty = apiBinding("operationType"),
        constraint = minCount,
        value = "1",
        openApiErrorMessage = "'type' for http operation binding is required"
      ),
      AMFValidation(
        owlClass = apiBinding("Amqp091ChannelExchange"),
        owlProperty = apiBinding("name"),
        constraint = sh("maxLength"),
        value = "255",
        openApiErrorMessage = "'type' for http operation binding is required"
      ),
      AMFValidation(
        owlClass = apiBinding("Amqp091ChannelExchange"),
        owlProperty = core("name"),
        constraint = sh("maxLength"),
        value = "255",
        openApiErrorMessage = "Amqp channel binding name can't be longer than 255 characters"
      ),
      AMFValidation(
        owlClass = apiBinding("Amqp091ChannelQueue"),
        owlProperty = core("name"),
        constraint = sh("maxLength"),
        value = "255",
        openApiErrorMessage = "Amqp channel binding name can't be longer than 255 characters"
      ),
      AMFValidation(
        owlClass = apiBinding("HttpOperationBinding"),
        owlProperty = apiBinding("operationType"),
        constraint = sh("pattern"),
        value = """^(request|response)$""".stripMargin,
        openApiErrorMessage = "Http operation binding must be either 'request' or 'response'"
      ),
      AMFValidation(
        owlClass = apiBinding("Amqp091ChannelExchange"),
        owlProperty = apiBinding("name"),
        constraint = sh("pattern"),
        value = """^(request|response)$""".stripMargin,
        openApiErrorMessage = "Http operation binding must be either 'request' or 'response'"
      ),
      AMFValidation(
        owlClass = apiBinding("MqttServerBinding"),
        owlProperty = apiBinding("keepAlive"),
        constraint = sh("minInclusive"),
        openApiErrorMessage = "'keepAlive' must be greater than 0"
      ),
      AMFValidation(
        owlClass = apiBinding("MqttServerLastWill"),
        owlProperty = apiBinding("qos"),
        constraint = sh("pattern"),
        value = "^[0-2]$",
        openApiErrorMessage = "'qos' for mqtt server binding last will object must be one of 0, 1 or 2"
      ),
      AMFValidation(
        owlClass = apiBinding("Amqp091OperationBinding"),
        owlProperty = apiBinding("deliveryMode"),
        constraint = sh("pattern"),
        value = "^[1-2]$",
        openApiErrorMessage = "'deliveryMode' for amqp 0.9.1 operation binding object must be one of 1 or 2"
      ),
      AMFValidation(
        owlClass = apiBinding("Amqp091OperationBinding"),
        owlProperty = apiBinding("expiration"),
        constraint = sh("pattern"),
        value = "^[0-9]+(.[0-9]+)?$",
        openApiErrorMessage = "'expiration' for amqp 0.9.1 operation binding object must greather than or equal to 0"
      ),
      AMFValidation(
        owlClass = apiBinding("MqttServerLastWill"),
        owlProperty = apiBinding("qos"),
        constraint = sh("pattern"),
        value = "^[0-2]$",
        openApiErrorMessage = "'qos' for mqtt server last will binding object must be one 0, 1 or 2"
      ),
      AMFValidation(
        owlClass = apiBinding("MqttServerBinding"),
        owlProperty = apiBinding("expiration"),
        constraint = sh("minInclusive"),
        openApiErrorMessage = "'expiration' must be greater than 0"
      ),
      AMFValidation(
        message = "'flows' field is mandatory in OAuth2 security scheme",
        owlClass = security("SecurityScheme"),
        owlProperty = security("settings"),
        constraint = shape("requiredFlowsInOAuth2")
      ),
      AMFValidation(
        message = "'openIdConnectUrl' field is mandatory in openIdConnect security scheme",
        owlClass = security("SecurityScheme"),
        owlProperty = security("settings"),
        constraint = shape("requiredOpenIdConnectUrl")
      ),
      AMFValidation(
        message = "'query' property of ws channel binding must be of type object and have properties",
        owlClass = apiBinding("WebSocketsChannelBinding"),
        owlProperty = apiBinding("query"),
        constraint = shape("mandatoryQueryObjectNodeWithPropertiesFacet")
      ),
      AMFValidation(
        message = "'headers' property of ws channel binding must be of type object and have properties",
        owlClass = apiBinding("WebSocketsChannelBinding"),
        owlProperty = apiBinding("headers"),
        constraint = shape("mandatoryHeadersObjectNodeWithPropertiesFacet")
      ),
      AMFValidation(
        message = "'headers' property of ws channel binding must be of type object and have properties",
        owlClass = apiBinding("HttpMessageBinding"),
        owlProperty = apiBinding("headers"),
        constraint = shape("mandatoryHeadersObjectNodeWithPropertiesFacet")
      ),
      AMFValidation(
        message = "'headers' property of ws channel binding must be of type object and have properties",
        owlClass = apiBinding("HttpOperationBinding"),
        owlProperty = apiBinding("query"),
        constraint = shape("mandatoryQueryObjectNodeWithPropertiesFacet")
      ),
      AMFValidation(
        message = "Does not comply with runtime expression ABNF syntax",
        owlClass = core("CorrelationId"),
        owlProperty = core("location"),
        constraint = shape("validCorrelationIdLocation")
      ),
      AMFValidation(
        message = "Does not comply with runtime expression ABNF syntax",
        owlClass = apiContract(ParameterModel.doc.displayName),
        owlProperty = apiContract("binding"),
        constraint = shape("validParameterLocation")
      ),
      AMFValidation(
        uri = amfParser("uri-query-param"),
        message = "Query parameters must not be defined in uri, use bindings instead",
        owlClass = apiContract("EndPoint"),
        owlProperty = apiContract("path"),
        constraint = sh("pattern"),
        value = """^(?!(.*\?.*=.*)$).*$""".stripMargin // negates the regex for defining a query param
      ),
      AMFValidation(
        uri = amfParser("uri-fragment"),
        message = "Fragments must not be defined in uri",
        owlClass = apiContract("EndPoint"),
        owlProperty = apiContract("path"),
        constraint = sh("pattern"),
        value = """^(?!(.*#.+)$).*$""".stripMargin
      ),
      emailValidation(core("Organization"), core("email")),
      urlValidation(core("Organization"), core("url")),
      urlValidation(core("License"), core("url")),
      urlValidation(apiContract("AsyncAPI"), core("termsOfService")),
      uriValidation(apiContract("AsyncAPI"), core("identifier")),
      urlValidation(core("CreativeWork"), core("url")),
      urlValidation(security("OAuth2Flow"), security("authorizationUri")),
      urlValidation(security("OAuth2Flow"), security("accessTokenUri")),
      urlValidation(security("OAuth2Flow"), security("refreshUri")),
      AMFValidation(
        message = "Discriminator must be in the objects required properties",
        constraint = shape("discriminatorInRequiredProperties"),
        owlClass = sh("NodeShape"),
        owlProperty = shape("discriminator"),
      ),
      AMFValidation(
        message = "Security scheme type should be one of the supported ones",
        owlClass = security("SecurityScheme"),
        owlProperty = security("type"),
        constraint = sh("pattern"),
        value =
          "^(Api\\sKey|OAuth\\s2.0|http|httpApiKey|openIdConnect|userPassword|X509|symmetricEncryption|asymmetricEncryption|x-.+)$"
      )
    ) ++ baseApiValidations("AsyncAPI")

    override def validations(): Seq[AMFValidation] = result
  }

  object Oas30Validations extends OasValidations {
    private lazy val result = super.validations() ++ Seq(
      AMFValidation(
        owlClass = apiContract("Response"),
        owlProperty = apiContract("statusCode"),
        constraint = sh("pattern"),
        value = "^([1-5]{1}(([0-9]{2})|XX))$|^(default)$",
        openApiErrorMessage =
          "Status code for a Response must be a value between 100 and 599, a [1-5]XX wildcard, or 'default'"
      ),
      AMFValidation(
        message = "Invalid flow. The options are: implicit, password, clientCredentials or authorizationCode",
        owlClass = security("OAuth2Flow"),
        owlProperty = security("flow"),
        constraint = sh("pattern"),
        value = "^(implicit|password|clientCredentials|authorizationCode)$"
      ),
      AMFValidation(
        message = "Invalid 'in' value. The options are: query, header or cookie",
        owlClass = security("Settings"),
        owlProperty = security("in"),
        constraint = sh("pattern"),
        value = "^(query|header|cookie)$"
      ),
      AMFValidation(
        uri = amfParser("example-mutually-exclusive-fields"),
        message = "Example 'value' and 'externalValue' fields are mutually exclusive",
        owlClass = apiContract("Example"),
        owlProperty = doc("externalValue"),
        constraint = shape("exampleMutuallyExclusiveFields"),
        openApiErrorMessage = "Example 'value' and 'externalValue' fields are mutually exclusive"
      ),
      AMFValidation(
        owlClass = apiContract(ParameterModel.doc.displayName),
        owlProperty = apiContract("payload"),
        constraint = sh("maxCount"),
        value = "1",
        openApiErrorMessage = "Parameters 'content' field must only have one entry"
      ),
      urlValidation(apiContract("WebAPI"), core("termsOfService")),
      AMFValidation(
        message = "'scheme' field is mandatory in http security scheme",
        owlClass = security("HttpSettings"),
        owlProperty = security("scheme"),
        constraint = minCount,
        value = "1"
      ),
      AMFValidation(
        message = "'name' field is mandatory in apiKey security scheme",
        owlClass = security("ApiKeySettings"),
        owlProperty = core("name"),
        constraint = minCount,
        value = "1"
      ),
      AMFValidation(
        message = "'in' field is mandatory in apiKey security scheme",
        owlClass = security("ApiKeySettings"),
        owlProperty = security("in"),
        constraint = minCount,
        value = "1"
      ),
      AMFValidation(
        message = "'openIdConnectUrl' field is mandatory in openIdConnect security scheme",
        owlClass = security("SecurityScheme"),
        owlProperty = security("settings"),
        constraint = shape("requiredOpenIdConnectUrl")
      ),
      AMFValidation(
        message = "'flows' field is mandatory in OAuth2 security scheme",
        owlClass = security("SecurityScheme"),
        owlProperty = security("settings"),
        constraint = shape("requiredFlowsInOAuth2")
      ),
      AMFValidation(
        message = "Does not comply with runtime expression ABNF syntax",
        owlClass = apiContract("Callback"),
        owlProperty = apiContract("expression"),
        constraint = shape("validCallbackExpression")
      ),
      AMFValidation(
        message = "Does not comply with runtime expression ABNF syntax",
        owlClass = apiContract("TemplatedLink"),
        owlProperty = apiContract("requestBody"),
        constraint = shape("validLinkRequestBody")
      ),
      AMFValidation(
        message = "Does not comply with runtime expression ABNF syntax",
        owlClass = apiContract("TemplatedLink"),
        owlProperty = apiContract("mapping"),
        constraint = shape("validLinkParameterExpressions")
      ),
      AMFValidation(
        message = "Property 'name' in Tag object cannot be empty",
        owlClass = apiContract("Tag"),
        owlProperty = core("name"),
        constraint = sh("minLength"),
        value = "1",
      )
    )

    override def validations(): Seq[AMFValidation] = result
  }

  trait GenericValidations {
    def urlValidation(owlClass: ValueType, owlProperty: ValueType): AMFValidation =
      AMFValidation(
        message = "Must be in the format of a URL",
        owlClass = owlClass,
        owlProperty = owlProperty,
        constraint = sh("pattern"),
        value = """^((https?|ftp|file)://)?[-a-zA-Z0-9()+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9()+&@#/%=~_|]$""".stripMargin
      )

    def uriValidation(owlClass: ValueType, owlProperty: ValueType): AMFValidation =
      AMFValidation(
        message = "Must be in the format of a URI",
        owlClass = owlClass,
        owlProperty = owlProperty,
        constraint = sh("pattern"),
        value = """^\w+:(\/?\/?)[^\s]+$""".stripMargin
      )

    def emailValidation(owlClass: ValueType, owlProperty: ValueType): AMFValidation =
      AMFValidation(
        owlClass = owlClass,
        owlProperty = owlProperty,
        constraint = sh("pattern"),
        value =
          """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".stripMargin,
        openApiErrorMessage = "Field 'email' must be in the format of an email address",
      )
  }

  val profileToValidationMap: Map[ProfileName, Seq[AMFValidation]] = Map(
    AmfProfile     -> forProfile(AmfProfile),
    Raml10Profile  -> forProfile(Raml10Profile),
    Raml08Profile  -> forProfile(Raml08Profile),
    RamlProfile    -> forProfile(RamlProfile), // ???
    Oas20Profile   -> forProfile(Oas20Profile),
    Oas30Profile   -> forProfile(Oas30Profile),
    OasProfile     -> forProfile(OasProfile),
    Async20Profile -> forProfile(Async20Profile)
  )

  private def forProfile(p: ProfileName): Seq[AMFValidation] = {
    p match {
      case Raml10Profile | RamlProfile => Raml10Validations.validations()
      case Raml08Profile               => Raml08Validations.validations()
      case OasProfile | Oas20Profile   => Oas20Validations.validations()
      case Oas30Profile                => Oas30Validations.validations()
      case Async20Profile              => Async20Validations.validations()
      case AmfProfile                  => AmfValidations.validations()
      case _                           => Nil
    }
  }
}
