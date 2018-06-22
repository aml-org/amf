package amf.core.validation

import amf.core.validation.core.{ValidationProfile, ValidationSpecification}
import amf.core.vocabulary.Namespace

import scala.collection.mutable

class EffectiveValidations(val effective: mutable.HashMap[String, ValidationSpecification] = mutable.HashMap(),
                           val info: mutable.HashMap[String, ValidationSpecification] = mutable.HashMap(),
                           val warning: mutable.HashMap[String, ValidationSpecification] = mutable.HashMap(),
                           val violation: mutable.HashMap[String, ValidationSpecification] = mutable.HashMap(),
                           val all: mutable.HashMap[String, ValidationSpecification] = mutable.HashMap()) {

  def update(other: ValidationSpecification) = {
    all.get(other.name) match {
      case Some(added) => all.update(other.name, other withTargets added)
      case None        => all += other.name -> other
    }
  }

  def someEffective(profile: ValidationProfile): EffectiveValidations = {
    // we aggregate all of the validations to the total validations map
    profile.validations.foreach { update }

    profile.infoLevel.foreach(id => setLevel(id, SeverityLevels.INFO))
    profile.warningLevel.foreach(id => setLevel(id, SeverityLevels.WARNING))
    profile.violationLevel.foreach(id => setLevel(id, SeverityLevels.VIOLATION))

    profile.disabled foreach { id =>
      val validationName = if (!id.startsWith("http://") && !id.startsWith("https://") && !id.startsWith("file:/")) {
        Namespace.expand(id.replace(".", ":")).iri()
      } else { id }
      this.effective.remove(validationName)
    }

    this
  }

  def allEffective(specifications: Seq[ValidationSpecification]): EffectiveValidations = {
    specifications foreach { spec =>
      all += (spec.name       -> spec)
      effective += (spec.name -> spec)
      violation += (spec.name -> spec)
    }
    this
  }

  private def setLevel(id: String, targetLevel: String) = {
    val validationName = if (!id.startsWith("http://") && !id.startsWith("https://") && !id.startsWith("file:/")) {
      Namespace.expand(id.replace(".", ":")).iri()
    } else { id }
    all.get(validationName) match {
      case None => throw new Exception(s"Cannot enable with $targetLevel level unknown validation $validationName")
      case Some(validation) =>
        info.remove(validationName)
        warning.remove(validationName)
        violation.remove(validationName)
        targetLevel match {
          case SeverityLevels.INFO      => info += (validationName      -> validation)
          case SeverityLevels.WARNING   => warning += (validationName   -> validation)
          case SeverityLevels.VIOLATION => violation += (validationName -> validation)
        }
        effective += (validationName -> validation)
    }
  }

}

object EffectiveValidations {
  def apply() = new EffectiveValidations()
}
