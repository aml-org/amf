package amf.plugins.document.webapi.validation

import amf.ProfileNames
import amf.core.validation.SeverityLevels
import amf.core.validation.core._
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.features.validation.ParserSideValidations

/**
  * Created by antoniogarrote on 17/07/2017.
  */
/**
  * Validation defined in a TSV file with AMF validations
  * @param uri URI of the validation, null to auto-generate
  * @param message Optional message for the validation
  * @param level Level: AMF, OpenAPI or RAML
  * @param owlClass Optional OWL class target of the validation
  * @param owlProperty Optional OWL property target of the validation
  * @param shape Type of SHACL shape for the validation
  * @param constraint URI of the constraint component
  * @param value Value for the contraint component
  */
case class AMFValidation(uri: Option[String],
                         message: Option[String],
                         spec: String,
                         level: String,
                         owlClass: Option[String],
                         owlProperty: Option[String],
                         shape: String,
                         target: String,
                         constraint: String,
                         value: String,
                         ramlErrorMessage: String,
                         openApiErrorMessage: String)

object AMFValidation {

  def fromLine(line: String): Option[AMFValidation] =
    line.split("\t") match {
      case Array(uri,
                 message,
                 spec: String,
                 level,
                 owlClass,
                 owlProperty,
                 shape,
                 target,
                 constraint,
                 value,
                 ramlError,
                 openApiError) =>
        val parsedValue =
          if (constraint.endsWith("pattern")) value
          else Namespace.uri(value).iri() // this might not be a URI, but trying to expand it is still safe
        Some(
          AMFValidation(
            nonNullString(Namespace.uri(uri).iri()),
            nonNullString(message),
            spec,
            level,
            nonNullString(Namespace.uri(owlClass).iri()),
            nonNullString(Namespace.uri(owlProperty).iri()),
            shape,
            Namespace.uri(target).iri(),
            Namespace.uri(constraint).iri(),
            parsedValue,
            ramlError,
            openApiError
          ))
      case _ => None
    }

  protected def nonNullString(s: String): Option[String] = if (s == "") { None } else { Some(s) }

}

trait ImportUtils {

  // def validationNS(postfix: String) = (Namespace.AmfParser + postfix).iri()

  protected def validationId(validation: AMFValidation): String =
    validation.uri match {
      case Some(s) => Namespace.expand(s.trim).iri()
      case None =>
        val classPostfix    = postfix(validation.owlClass, "domain")
        val propertyPostfix = postfix(validation.owlProperty, "property")
        val constraint      = postfix(Some(validation.constraint), "constraint")
        Namespace.AmfParser.base + classPostfix + "-" + propertyPostfix.trim + "-" + constraint.trim
    }

  protected def postfix(s: Option[String], default: String): String = s match {
    case Some(p) =>
      if (p.indexOf("#") > -1) {
        p.split("#")(1).trim
      } else if (p.indexOf("/") == -1 && p.indexOf(":") != -1) {
        p.split(":")(1).trim
      } else {
        p.split("/").last.trim
      }
    case None => default
  }

}

object DefaultAMFValidations extends ImportUtils {

  private def validations(): List[AMFValidation] =
    AMFRawValidations.raw
      .map(AMFValidation.fromLine)
      .filter(_.isDefined)
      .map(_.get)

  def profiles(): List[ValidationProfile] = {
    val groups = validations().groupBy(_.spec)
    groups.map {
      case (profile, validationsInGroup) =>
        val validations = parseValidation(validationsInGroup)

        // sorting parser side validation for this profile
        val violationParserSideValidations = ParserSideValidations.validations
          .filter { v =>
            ParserSideValidations
              .levels(v.id())
              .getOrElse(profile, SeverityLevels.VIOLATION) == SeverityLevels.VIOLATION
          }
          .map(_.name)
        val infoParserSideValidations = ParserSideValidations.validations
          .filter { v =>
            ParserSideValidations.levels(v.id()).getOrElse(profile, SeverityLevels.VIOLATION) == SeverityLevels.INFO
          }
          .map(_.name)
        val warningParserSideValidations = ParserSideValidations.validations
          .filter { v =>
            ParserSideValidations.levels(v.id()).getOrElse(profile, SeverityLevels.VIOLATION) == SeverityLevels.WARNING
          }
          .map(_.name)

        ValidationProfile(
          name = profile,
          baseProfileName = if (profile == ProfileNames.AMF) None else Some(ProfileNames.AMF),
          infoLevel = infoParserSideValidations,
          warningLevel = warningParserSideValidations,
          violationLevel = validations.map(_.name) ++ violationParserSideValidations,
          validations = validations ++ ParserSideValidations.validations
        )

    }.toList
  }

  private def parseValidation(validations: List[AMFValidation]): List[ValidationSpecification] = {
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
        targetClass = Seq(validation.owlClass.getOrElse((Namespace.Document + "DomainElement").iri()))
      )

      Namespace.expand(validation.target.trim).iri() match {
        case "http://www.w3.org/ns/shacl#path" =>
          val valueType = if (validation.constraint.trim.contains("#")) {
            val strings = validation.constraint.trim.split("#")
            ValueType(Namespace.find(strings.head), strings.last)
          } else Namespace.expand(validation.constraint)
          valueType match {
            case sh @ ValueType(Namespace.Shacl, _) =>
              spec.copy(propertyConstraints = Seq(parsePropertyConstraint(s"$uri/prop", validation, sh)))
            case sh @ ValueType(Namespace.Shapes, _) =>
              spec.copy(functionConstraint = Option(parseFunctionConstraint(s"$uri/prop", validation, sh)))
            case _ => spec
          }

        case "http://www.w3.org/ns/shacl#targetObjectsOf" if validation.owlProperty.isDefined =>
          spec.copy(
            targetObject = Seq(validation.owlProperty.get),
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
      ramlPropertyId = validation.owlProperty.get,
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

  private def parseFunctionConstraint(constraintUri: String,
                                      validation: AMFValidation,
                                      sh: ValueType): FunctionConstraint = {
    FunctionConstraint(
      message = validation.message,
      functionName = None, // i have to ignore the function name so it will be taken from the generated js library
      code = JsCustomValidations(sh.name)
    )
  }

}

object JsCustomValidations {
  val functions: Map[String, String] = Map(
    "multipleOfValidation" ->
      """|function(shape) {
         |  console.log(JSON.stringify(shape));
         |  var multipleOf = shape["raml-shapes:multipleOf"];
         |  console.log(multipleOf);
         |  console.log(parseFloat(multipleOf));
         |  if ( multipleOf == undefined) return true;
         |  else return parseFloat(multipleOf) > 0;
         |}
      """.stripMargin,
    "maxLengthValidation" ->
      """|function(shape) {
         |  var maxLength = shape["shacl:maxLength"];
         |  if ( maxLength == undefined) return true;
         |  else return parseFloat(maxLength) >= 0;
         |}
      """.stripMargin,
    "minLengthValidation" ->
      """|function(shape) {
         |  console.log(JSON.stringify(shape));
         |  var minLength = shape["shacl:minLength"];
         |  console.log(minLength);
         |  console.log(parseFloat(minLength));
         |  if ( minLength == undefined) return true;
         |  else return parseFloat(minLength) >= 0;
         |}
      """.stripMargin
  )

  def apply(name: String): Option[String] = functions.get(name)
}
