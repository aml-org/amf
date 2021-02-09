package amf
import amf.client.AMF
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.validation.AMFRawValidations.AMFValidation
import amf.plugins.document.webapi.validation.{AMFRawValidations, ImportUtils}
import amf.plugins.features.validation.Validations

object ValidationsExporter extends ImportUtils {

  def main(args: Array[String]): Unit = {

    AMF.init() // Needed to load all the validations in the platform

    val parserSideVals = Validations.allLevels.foldLeft(Map[String, Map[String, String]]()) {
      case (accMap, (id, levels)) =>
        accMap.updated(id, levels.foldLeft(Map[String, String]()) {
          case (acc, (p, v)) =>
            acc.updated(p.profile, v)
        })
    }

    Validations.validations.foreach { validation =>
      val severity: Map[String, String] = parserSideVals(validation.id)
      val levelsString = severity.keys.toSeq.sorted
        .map(severity)
        .mkString("\t")
      println(s"${uriModel(Some(validation.id))}\t\t\t${validation.message}\t\t\t$levelsString")
    }
    var validationsAcc                           = Map[String, AMFValidation]()
    var levels: Map[String, Map[String, String]] = Map()

    AMFRawValidations.map.foreach {
      case (profile, validations) =>
        validations.foreach { validation =>
          val id = uri(validation)
          if (!validations.contains(id)) {
            validationsAcc = validationsAcc + (id -> validation)
          }
          var thisLevel = levels.getOrElse(id, Map())
          thisLevel = thisLevel + (profile.profile -> validation.severity)
          levels = levels.updated(id, thisLevel)
        }
    }

    validationsAcc.foreach {
      case (id, validation) =>
        val severity: Map[String, String] = levels.getOrElse(id, parserSideVals(id))
        val levelsString = severity.keys.toSeq.sorted
          .map(severity)
          .mkString("\t")

        println(s"${uri(validation)}\t${validation.owlClass}\t${validation.owlProperty}\t${validation.message
          .getOrElse("")}\t${validation.ramlErrorMessage}\t${validation.openApiErrorMessage}\t$levelsString")
    }

  }

  def uri(validation: AMFValidation): String = {
    val id = validation.uri match {
      case Some(id) => id
      case _        => validationId(validation)
    }
    Namespace.staticAliases.compact(id)
  }

  def uriModel(s: Option[String]): String = {
    s match {
      case Some(uri) => Namespace.staticAliases.compact(uri)
      case _         => ""
    }
  }
}
