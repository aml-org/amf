package amf.shapes.internal.validation.model

import amf.aml.internal.validate.AMFDialectValidations.ConstraintSeverityOverrides
import amf.core.client.common.validation.SeverityLevels.VIOLATION
import amf.core.client.common.validation.{AmfProfile, ProfileName, ProfileNames, SeverityLevels}
import amf.core.internal.validation.core.{
  SeverityMapping,
  ShaclSeverityUris,
  ValidationProfile,
  ValidationSpecification
}
import amf.shapes.internal.validation.model.AMFRawValidations.AMFValidation

trait ValidationProfileBuilder extends ImportUtils {

  def buildProfileFrom(
      profile: ProfileName,
      profileValidations: ProfileValidations,
      withStaticValidations: Boolean = true
  ): ValidationProfile = {
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

    var finalValidations = infoValidations ++ warningValidations ++ violationValidations
    if (withStaticValidations) finalValidations ++= staticValidations

    ValidationProfile(
      name = profile,
      baseProfile = if (profile == AmfProfile) None else Some(AmfProfile),
      validations = finalValidations,
      severities = severityMapping
    )
  }

  protected def all(lvl: String): Map[ProfileName, String] = ProfileNames.specProfiles.map(_ -> lvl).toMap

  protected[amf] lazy val severityLevelsOfConstraints: ConstraintSeverityOverrides =
    staticValidations.foldLeft(levels) { (acc, validation) =>
      if (acc.contains(validation.id)) acc
      else acc + (validation.id -> default)
    }

  private def severityLevelOf(id: String, profile: ProfileName): String =
    severityLevelsOfConstraints
      .getOrElse(id, default)
      .getOrElse(profile, VIOLATION)
  private lazy val default                                 = all(VIOLATION)

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

  def profiles(): List[ValidationProfile]
  val staticValidations: Seq[ValidationSpecification]
  protected[amf] val levels: ConstraintSeverityOverrides

}
