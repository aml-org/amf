package amf.plugins.document.webapi.validation

import amf._
import amf.core.validation.SeverityLevels
import amf.core.validation.core._
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.validation.AMFRawValidations.AMFValidation
import amf.plugins.features.validation.Validations

trait ImportUtils {
  protected def validationId(validation: AMFValidation): String =
    validation.uri match {
      case Some(s) => Namespace.staticAliases.expand(s.trim).iri()
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
    AMFRawValidations.profileToValidationMap.map {
      case (profile, validationsInGroup) =>
        val violationValidations =
          parseRawValidations(validationsInGroup.filter(_.severity == SeverityLevels.VIOLATION))
        val infoValidations    = parseRawValidations(validationsInGroup.filter(_.severity == SeverityLevels.INFO))
        val warningValidations = parseRawValidations(validationsInGroup.filter(_.severity == SeverityLevels.WARNING))

        // sorting parser side validation for this profile
        val violationParserSideValidations = getValidationsWithSeverity(profile, SeverityLevels.VIOLATION)
        val infoParserSideValidations      = getValidationsWithSeverity(profile, SeverityLevels.INFO)
        val warningParserSideValidations   = getValidationsWithSeverity(profile, SeverityLevels.WARNING)

        val severityMapping = SeverityMapping()
          .set(infoParserSideValidations ++ infoValidations.map(_.name), SeverityLevels.INFO)
          .set(warningParserSideValidations ++ warningValidations.map(_.name), SeverityLevels.WARNING)
          .set(violationParserSideValidations ++ violationValidations.map(_.name), SeverityLevels.VIOLATION)

        ValidationProfile(
          name = profile,
          baseProfile = if (profile == AmfProfile) None else Some(AmfProfile),
          validations = infoValidations ++ warningValidations ++ violationValidations ++ Validations.validations,
          severities = severityMapping
        )
    }.toList

  private def getValidationsWithSeverity(profile: ProfileName, severity: String) = {
    Validations.validations
      .filter { v =>
        Validations.severityLevelOf(v.id, profile) == severity
      }
      .map {
        _.copy(severity = ShaclSeverityUris.amfToShaclSeverity(severity))
      }
      .map(_.name)
  }

  private def parseRawValidations(validations: Seq[AMFValidation]): Seq[ValidationSpecification] = {
    validations.flatMap { RawValidationAdapter(_) }
  }
}

object JsCustomValidations {
  val functions: Map[String, String] = Map(
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
    "nonEmptyListOfProtocols" ->
      """
        |function(shape) {
        |  var protocolsArray = shape["apiContract:scheme"];
        |  return !Array.isArray(protocolsArray) || protocolsArray.length > 0;
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
