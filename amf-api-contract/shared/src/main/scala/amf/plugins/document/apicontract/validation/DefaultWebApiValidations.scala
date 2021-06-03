package amf.plugins.document.apicontract.validation

import amf._
import amf.core.validation.SeverityLevels
import amf.core.validation.SeverityLevels.VIOLATION
import amf.core.validation.core._
import amf.core.vocabulary.Namespace
import amf.plugins.document.vocabularies.validation.AMFDialectValidations
import amf.plugins.document.vocabularies.validation.AMFDialectValidations.ConstraintSeverityOverrides
import amf.plugins.document.apicontract.validation.AMFRawValidations.{AMFValidation, ProfileValidations}
import amf.plugins.features.validation.CoreValidations
import amf.validation.DialectValidations
import amf.validations.{
  ParserSideValidations,
  PayloadValidations,
  RenderSideValidations,
  ResolutionSideValidations,
  ShapeParserSideValidations,
  ShapePayloadValidations
}

trait ImportUtils {
  protected def validationId(validation: AMFValidation): String =
    validation.uri match {
      case Some(s) => Namespace.defaultAliases.expand(s.trim).iri()
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

  def buildProfileFrom(profile: ProfileName, profileValidations: ProfileValidations): ValidationProfile = {
    val validations          = profileValidations.validations()
    val violationValidations = parseRawValidations(validations.filter(_.severity == SeverityLevels.VIOLATION))
    val infoValidations      = parseRawValidations(validations.filter(_.severity == SeverityLevels.INFO))
    val warningValidations   = parseRawValidations(validations.filter(_.severity == SeverityLevels.WARNING))

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
      validations = infoValidations ++ warningValidations ++ violationValidations ++ staticValidations,
      severities = severityMapping
    )
  }

  val staticValidations: Seq[ValidationSpecification] = AMFDialectValidations.staticValidations ++
    ParserSideValidations.validations ++
    PayloadValidations.validations ++
    RenderSideValidations.validations ++
    ResolutionSideValidations.validations ++
    ShapePayloadValidations.validations ++
    ShapeParserSideValidations.validations

  private val levels: ConstraintSeverityOverrides = AMFDialectValidations.levels ++
    ParserSideValidations.levels ++
    PayloadValidations.levels ++
    RenderSideValidations.levels ++
    ResolutionSideValidations.levels ++
    ShapePayloadValidations.levels ++
    ShapeParserSideValidations.levels

  def severityLevelOf(id: String, profile: ProfileName): String =
    severityLevelsOfConstraints
      .getOrElse(id, default)
      .getOrElse(profile, VIOLATION)

  protected[amf] lazy val severityLevelsOfConstraints: ConstraintSeverityOverrides =
    staticValidations.foldLeft(levels) { (acc, validation) =>
      if (acc.contains(validation.id)) acc
      else acc + (validation.id -> default)
    }

  private lazy val default                                 = all(VIOLATION)
  protected def all(lvl: String): Map[ProfileName, String] = ProfileNames.specProfiles.map(_ -> lvl).toMap

  def profiles(): List[ValidationProfile] =
    AMFRawValidations.profileToValidationMap.map {
      case (profile, profileValidations) => buildProfileFrom(profile, profileValidations)
    }.toList

  private def getValidationsWithSeverity(profile: ProfileName, severity: String) = {
    staticValidations
      .filter { v =>
        severityLevelOf(v.id, profile) == severity
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
// TODO: erase this. This is kept for legacy reasons as we no longer use JS functions for shacl validations. JS function behaviour is hardcoded in
// several places and that change is out of the scope of this issue.
object JsCustomValidations {
  def apply(name: String): String = {
    """
      |function(shape) {
      |  return true;
      |}
      """.stripMargin
  }
}
