package amf.validation.model

import amf.vocabulary.Namespace

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
            Namespace.uri(value).iri(), // this might not be a URI, but trying to expand it is still safe
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


object ParserSideValidations {
  def validations: List[ValidationSpecification] = List(
    ValidationSpecification(
      (Namespace.AmfParser + "closed-shape").iri(),
      "invalid property for node",
      None,
      None,
      Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
    ),
    ValidationSpecification(
      (Namespace.AmfParser + "dialectAmbiguousRange").iri(),
      "Ambiguous entity range",
      None,
      None,
      Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
    )
  )
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

        ValidationProfile(
          name = profile,
          baseProfileName = if (profile == "AMF") { None } else { Some("AMF") },
          violationLevel = validations.map(_.name),
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
          spec.copy(propertyConstraints = Seq(parsePropertyConstraint(s"$uri/prop", validation)))
        case "http://www.w3.org/ns/shacl#targetObjectsOf" if validation.owlProperty.isDefined =>
          spec.copy(
            targetObject = Seq(validation.owlProperty.get),
            nodeConstraints = Seq(NodeConstraint(validation.constraint, validation.value))
          )
        case _ => throw new Exception(s"Unknown validation target ${validation.target}")
      }
    }
  }

  private def parsePropertyConstraint(constraintUri: String, validation: AMFValidation): PropertyConstraint = {
    val constraint = PropertyConstraint(
      ramlPropertyId = validation.owlProperty.get,
      name = constraintUri,
      message = validation.message
    )

    Namespace.expand(validation.constraint.trim).iri() match {
      case "http://www.w3.org/ns/shacl#minCount"     => constraint.copy(minCount = Some(validation.value))
      case "http://www.w3.org/ns/shacl#maxCount"     => constraint.copy(maxCount = Some(validation.value))
      case "http://www.w3.org/ns/shacl#pattern"      => constraint.copy(pattern = Some(validation.value))
      case "http://www.w3.org/ns/shacl#minExclusive" => constraint.copy(minExclusive = Some(validation.value))
      case "http://www.w3.org/ns/shacl#maxExclusive" => constraint.copy(maxExclusive = Some(validation.value))
      case "http://www.w3.org/ns/shacl#minInclusive" => constraint.copy(minInclusive = Some(validation.value))
      case "http://www.w3.org/ns/shacl#maxInclusive" => constraint.copy(maxInclusive = Some(validation.value))
      case "http://www.w3.org/ns/shacl#in"           => constraint.copy(in = validation.value.split("\\s*,\\s*"))
      case "http://www.w3.org/ns/shacl#node"         => constraint.copy(node = Some(validation.value))
      case "http://www.w3.org/ns/shacl#datatype"     => constraint.copy(datatype = Some(validation.value))
      case "http://www.w3.org/ns/shacl#class"        => constraint.copy(`class` = Seq(validation.value))
      case _                                         => throw new Exception(s"Unsupported constraint ${validation.constraint}")
    }
  }

}
