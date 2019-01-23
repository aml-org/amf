package amf.plugins.features.validation

import amf.{ProfileName, ProfileNames}
import amf.core.validation.SeverityLevels.VIOLATION
import amf.core.validation.core.ValidationSpecification
import amf.core.vocabulary.Namespace

/** Access parser, resolution and render validations together. */
object Validations {
  def level(id: String, profile: ProfileName): String =
    allLevels.getOrElse(id, default).getOrElse(profile, VIOLATION)

  lazy val validations: List[ValidationSpecification] =
    ParserSideValidations.validations ++ ResolutionSideValidations.validations ++ RenderSideValidations.validations

  lazy val levels: Map[String, Map[ProfileName, String]] =
    ParserSideValidations.levels ++ ResolutionSideValidations.levels ++ RenderSideValidations.levels

  lazy val allLevels: Map[String, Map[ProfileName, String]] = validations.foldLeft(levels) { (acc, validation) =>
    if (acc.contains(validation.id)) acc
    else acc + (validation.id -> default)
  }

  private lazy val default = all(VIOLATION)

  protected def all(lvl: String): Map[ProfileName, String] = ProfileNames.specProfiles.map(_ -> lvl).toMap
}

trait Validations {
  protected def validation(id: String,
                           message: String,
                           ramlMessage: Option[String] = None,
                           oasMessage: Option[String] = None): ValidationSpecification =
    ValidationSpecification(
      (namespace + id).iri(),
      message,
      ramlMessage,
      oasMessage,
      Seq(specification)
    )

  def level(id: String, profile: ProfileName): String =
    levels.getOrElse(id, default).getOrElse(profile, VIOLATION)

  def allLevels(): Map[String, Map[ProfileName, String]] = validations.foldLeft(levels) { (acc, validation) =>
    if (acc.contains(validation.id)) acc
    else acc + (validation.id -> default)
  }

  protected def all(lvl: String): Map[ProfileName, String] = ProfileNames.specProfiles.map(_ -> lvl).toMap

  private lazy val default = all(VIOLATION)

  val specification: String
  val namespace: Namespace
  val validations: List[ValidationSpecification]
  val levels: Map[String, Map[ProfileName, String]]
}
