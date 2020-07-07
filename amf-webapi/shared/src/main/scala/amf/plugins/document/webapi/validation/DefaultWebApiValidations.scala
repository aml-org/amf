package amf.plugins.document.webapi.validation

import amf._
import amf.core.validation.SeverityLevels
import amf.core.validation.core._
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.webapi.validation.AMFRawValidations.AMFValidation
import amf.plugins.features.validation.Validations

trait ImportUtils {
  protected def validationId(validation: AMFValidation): String =
    validation.uri match {
      case Some(s) => Namespace.expand(s.trim).iri()
      case None =>
        val classPostfix    = postfix(validation.owlClass, "domain")
        val propertyPostfix = postfix(validation.owlProperty, "property")
        val constraint      = postfix(validation.constraint, "constraint")
        Namespace.AmfParser.base + classPostfix + "-" + propertyPostfix.trim + "-" + constraint.trim
    }

  protected def postfix(s: String, default: String): String = s match {
    case p: String if p.nonEmpty =>
      if (p.indexOf("#") > -1) {
        p.split("#")(1).trim
      } else if (p.indexOf("/") == -1 && p.indexOf(":") != -1) {
        p.split(":")(1).trim
      } else {
        p.split("/").last.trim
      }
    case _ => default
  }

}

object DefaultAMFValidations extends ImportUtils {

  def profiles(): List[ValidationProfile] =
    AMFRawValidations.map.map {
      case (profile, validationsInGroup) =>
        val violationValidations = parseValidation(validationsInGroup.filter(_.severity == SeverityLevels.VIOLATION))
        val infoValidations      = parseValidation(validationsInGroup.filter(_.severity == SeverityLevels.INFO))
        val warningValidations   = parseValidation(validationsInGroup.filter(_.severity == SeverityLevels.WARNING))

        // sorting parser side validation for this profile
        val violationParserSideValidations = Validations.validations
          .filter { v =>
            Validations
              .level(v.id, profile) == SeverityLevels.VIOLATION
          }
          .map(_.name)
        val infoParserSideValidations = Validations.validations
          .filter { v =>
            Validations.level(v.id, profile) == SeverityLevels.INFO
          }
          .map(_.name)
        val warningParserSideValidations = Validations.validations
          .filter { v =>
            Validations.level(v.id, profile) == SeverityLevels.WARNING
          }
          .map(_.name)

        ValidationProfile(
          name = profile,
          baseProfile = if (profile == AmfProfile) None else Some(AmfProfile),
          infoLevel = infoParserSideValidations ++ infoValidations.map(_.name),
          warningLevel = warningParserSideValidations ++ warningValidations.map(_.name),
          violationLevel = violationParserSideValidations ++ violationValidations.map(_.name),
          validations = infoValidations ++ warningValidations ++ violationValidations ++ Validations.validations
        )
    }.toList

  private def parseValidation(validations: Seq[AMFValidation]): Seq[ValidationSpecification] = {
    validations.map { validation =>
      val uri = validation.uri match {
        case Some(s) => s.trim
        case _       => validationId(validation)
      }

      val spec = ValidationSpecification(
        name = uri,
        message = validation.message.getOrElse(""),
        ramlMessage = Some(validation.ramlErrorMessage),
        oasMessage = Some(validation.openApiErrorMessage),
        targetClass = Seq(validation.owlClass)
      )

      Namespace.expand(validation.target.trim).iri() match {
        case "http://www.w3.org/ns/shacl#path" =>
          val valueType = if (validation.constraint.trim.contains("#")) {
            val strings = validation.constraint.trim.split("#")
            ValueType(Namespace.find(strings.head).get, strings.last)
          } else Namespace.expand(validation.constraint)
          valueType match {
            case sh @ ValueType(Namespace.Shacl, _) =>
              spec.copy(propertyConstraints = Seq(parsePropertyConstraint(s"$uri/prop", validation, sh)))
            case sh @ ValueType(Namespace.Shapes, _) =>
              spec.copy(functionConstraint = Option(parseFunctionConstraint(validation, sh)))
            case _ => spec
          }

        case "http://www.w3.org/ns/shacl#targetObjectsOf" =>
          spec.copy(
            targetObject = Seq(validation.owlProperty),
            nodeConstraints = Seq(NodeConstraint(validation.constraint, validation.value))
          )
        case _ => throw new Exception(s"Unknown validation target ${validation.target}")
      }
    }
  }

  private def parsePropertyConstraint(constraintUri: String,
                                      validation: AMFValidation,
                                      sh: ValueType): PropertyConstraint = {
    val constraint = PropertyConstraint(
      ramlPropertyId = validation.owlProperty,
      name = constraintUri,
      message = validation.message
    )
    sh.iri() match {
      case "http://www.w3.org/ns/shacl#minCount"     => constraint.copy(minCount = Some(validation.value))
      case "http://www.w3.org/ns/shacl#maxCount"     => constraint.copy(maxCount = Some(validation.value))
      case "http://www.w3.org/ns/shacl#pattern"      => constraint.copy(pattern = Some(validation.value))
      case "http://www.w3.org/ns/shacl#minExclusive" => constraint.copy(minExclusive = Some(validation.value))
      case "http://www.w3.org/ns/shacl#maxExclusive" => constraint.copy(maxExclusive = Some(validation.value))
      case "http://www.w3.org/ns/shacl#minInclusive" => constraint.copy(minInclusive = Some(validation.value))
      case "http://www.w3.org/ns/shacl#maxInclusive" => constraint.copy(maxInclusive = Some(validation.value))
      case "http://www.w3.org/ns/shacl#minLength"    => constraint.copy(minLength = Some(validation.value))
      case "http://www.w3.org/ns/shacl#maxLength"    => constraint.copy(maxLength = Some(validation.value))
      case "http://www.w3.org/ns/shacl#in"           => constraint.copy(in = validation.value.split("\\s*,\\s*"))
      case "http://www.w3.org/ns/shacl#node"         => constraint.copy(node = Some(validation.value))
      case "http://www.w3.org/ns/shacl#datatype"     => constraint.copy(datatype = Some(validation.value))
      case "http://www.w3.org/ns/shacl#class"        => constraint.copy(`class` = Seq(validation.value))
      case _                                         => throw new Exception(s"Unsupported constraint ${validation.constraint}")
    }
  }

  private def parseFunctionConstraint(validation: AMFValidation, sh: ValueType): FunctionConstraint = {
    FunctionConstraint(
      message = validation.message,
      functionName = None, // i have to ignore the function name so it will be taken from the generated js library
      code = JsCustomValidations(sh.name),
      internalFunction = Some(sh.name)
    )
  }
}

object JsCustomValidations {
  val functions: Map[String, String] = Map(
    "minimumMaximumValidation" ->
      """|function(shape) {
         |  //console.log(JSON.stringify(shape));
         |  var minimum = shape["shacl:minInclusive"];
         |  var maximum = shape["shacl:maxInclusive"];
         |  if (minimum == undefined || maximum == undefined) return true;
         |  else return (parseFloat(minimum) <= parseFloat(maximum));
         |}
      """.stripMargin,
    "pathParameterRequiredProperty" ->
      """|function(parameter) {
         |  var binding = parameter["apiContract:binding"];
         |  var requiredValue = parameter["apiContract:required"];
         |  if (binding == 'path' && requiredValue != 'true') {
         |    return false;
         |  }
         |  else {
         |    return true;
         |  }
         |}
      """.stripMargin,
    "fileParameterMustBeInFormData" ->
      """|function(parameter) {
         |  var binding = parameter["apiContract:binding"];
         |  var schema = parameter["raml-shapes:schema"];
         |  var typeList = schema[0]["@type"];
         |  if(Array.isArray(typeList) && typeList.indexOf("raml-shapes:FileShape") != -1){
         |    return binding == 'formData';
         |  } else {
         |    return true;
         |  }
         |}
      """.stripMargin,
    "minMaxItemsValidation" ->
      """|function(shape) {
         |  //console.log(JSON.stringify(shape));
         |  var minCount = shape["shacl:minCount"];
         |  var maxCount = shape["shacl:maxCount"];
         |  if (minCount == undefined || maxCount == undefined) return true;
         |  else return (parseInt(minCount) <= parseInt(maxCount));
         |}
      """.stripMargin,
    "minMaxLengthValidation" ->
      """|function(shape) {
         |  //console.log(JSON.stringify(shape));
         |  var minLength = shape["shacl:minLength"];
         |  var maxLength = shape["shacl:maxLength"];
         |  if (minLength == undefined || maxLength == undefined) return true;
         |  else return (parseInt(minLength) <= parseInt(maxLength));
         |}
      """.stripMargin,
    "minMaxPropertiesValidation" ->
      """|function(shape) {
         |  var minProperties = shape["raml-shapes:minProperties"];
         |  var maxProperties = shape["raml-shapes:maxProperties"];
         |  if (minProperties == undefined || maxProperties == undefined) return true;
         |  else return (parseInt(minProperties) <= parseInt(maxProperties));
         |}
      """.stripMargin,
    "patternValidation" ->
      """|function(shape) {
         |  var pattern = shape["shacl:pattern"];
         |  try {
         |    if(pattern) new RegExp(pattern);
         |    return true;
         |  } catch(e) {
         |    return false;
         |  }
         |}
      """.stripMargin,
    "xmlWrappedScalar" ->
      """
        |function(shape) {
        |  var xmlSerialization = shape["raml-shapes:xmlSerialization"];
        |  if (!xmlSerialization) return true;
        |  else {
        |    var wrapped_ = xmlSerialization[0]["raml-shapes:xmlWrapped"];
        |    var isWrapped = (wrapped_)? wrapped_[0] : false;
        |    var isScalar = shape["@type"].indexOf("raml-shapes:ScalarShape") !== -1;
        |    return !(isWrapped && isScalar);
        |  }
        |}
      """.stripMargin, // TODO
    "xmlNonScalarAttribute" ->
      """
        |function(shape) {
        |  var xmlSerialization = shape["raml-shapes:xmlSerialization"];
        |  if (!xmlSerialization) return true;
        |  else {
        |    var attribute_ = xmlSerialization[0]["raml-shapes:xmlAttribute"];
        |    var isAttribute = (attribute_)? attribute_[0] : false;
        |    var isNonScalar = shape["@type"].indexOf("raml-shapes:ScalarShape") === -1;
        |    return !(isAttribute && isNonScalar);
        |  }
        |}
      """.stripMargin, // TODO
    "nonEmptyListOfProtocols" ->
      """
        |function(shape) {
        |  var protocolsArray = shape["apiContract:scheme"];
        |  return !Array.isArray(protocolsArray) || protocolsArray.length > 0;
        |}
      """.stripMargin,
    "exampleMutuallyExclusiveFields" ->
      """
        |function(shape) {
        |  var externalValue = shape["doc:externalValue"];
        |  var value = shape["doc:structuredValue"];
        |  return !(externalValue != null && value != null);
        |}
      """.stripMargin,
    "requiredFlowsInOAuth2" ->
      """
        |function(shape) {
        |  return true;
        |}
      """.stripMargin, // TODO pending JS implementation
    "requiredOpenIdConnectUrl" ->
      """
        |function(shape) {
        |  return true;
        |}
      """.stripMargin,
    "validCallbackExpression" ->
      """
        |function(shape) {
        |  return true;
        |}
      """.stripMargin,
    "validLinkRequestBody" ->
      """
        |function(shape) {
        |  return true;
        |}
      """.stripMargin,
    "validLinkParameterExpressions" ->
      """
        |function(shape) {
        |  return true;
        |}
      """.stripMargin,
    "headerParamNameMustBeAscii" ->
      """
        |function(shape) {
        |  return true;
        |}
      """.stripMargin,
    "mandatoryHeadersObjectNode" ->
      """
        |function(shape) {
        |  return true;
        |}
      """.stripMargin,
    "mandatoryHeaderNamePattern" ->
      """
        |function(shape) {
        |  return true;
        |}
      """.stripMargin,
    "mandatoryHeaderBindingNamePattern" ->
      """
        |function(shape) {
        |  return true;
        |}
      """.stripMargin
  )

  def apply(name: String): Option[String] = functions.get(name)
}
