package amf.tasks.validations

import amf.ProfileNames
import amf.core.validation.core.{ValidationProfile, ValidationSpecification}
import amf.core.vocabulary.Namespace

class ValidationDialectTextGenerator(profile: ValidationProfile) {

  def emit(): String = {
    val extension =
      if (profile.name == ProfileNames.RAML10 || profile.name == ProfileNames.RAML08 || profile.name == ProfileNames.OAS20) {
        "\nextends: AMF\n"
      } else {
        "\n"
      }

    val effectiveValidations = if (profile.name == ProfileNames.AMF) {
      profile.validations
    } else {
      profile.validations.filter(_.propertyConstraints.nonEmpty)
    }

    val violations = effectiveValidations
      .map { validation =>
        s"  - ${compact(validation.name)}"
      }
      .mkString("\n")

    val validations = effectiveValidations
      .map { validation =>
        emitValidation(validation)
      }
      .mkString("\n\n")

    s"""#%Validation Profile 1.0
      |
      |profile: ${profile.name}
      |$extension
      |violation:
      |$violations
      |
      |validations:
      |
      |$validations
    """.stripMargin
  }

  protected def emitValidation(validation: ValidationSpecification): String = {
    val propertyConstraintId = validation.propertyConstraints.headOption match {
      case Some(propertyConstraint) => compact(propertyConstraint.ramlPropertyId)
      case None                     => "unknown"
    }
    validation.propertyConstraints.headOption match {
      case Some(propertyConstraint) if propertyConstraint.node.isEmpty =>
        val constraintValue = if (propertyConstraint.minLength.isDefined) {
          s"minLength: ${propertyConstraint.minLength.get}"
        } else if (propertyConstraint.maxLength.isDefined) {
          s"maxLength: ${propertyConstraint.maxLength.get}"
        } else if (propertyConstraint.maxExclusive.isDefined) {
          s"maxExclusive: ${propertyConstraint.maxExclusive.get}"
        } else if (propertyConstraint.minExclusive.isDefined) {
          s"minExclusive: ${propertyConstraint.minExclusive.get}"
        } else if (propertyConstraint.maxInclusive.isDefined) {
          s"maxInclusive: ${propertyConstraint.maxInclusive.get}"
        } else if (propertyConstraint.minInclusive.isDefined) {
          s"minInclusive: ${propertyConstraint.minInclusive.get}"
        } else if (propertyConstraint.maxCount.isDefined) {
          s"maxCount: ${propertyConstraint.maxCount.get}"
        } else if (propertyConstraint.minCount.isDefined) {
          s"minCount: ${propertyConstraint.minCount.get}"
        } else if (propertyConstraint.maxCount.isDefined) {
          s"maxCount: ${propertyConstraint.maxCount.get}"
        } else if (propertyConstraint.pattern.isDefined) {
          s"pattern: ${propertyConstraint.pattern.get}"
        } else if (propertyConstraint.in.nonEmpty) {
          s"in: [ " + propertyConstraint.in.mkString(",") + " ]"
        } else if (propertyConstraint.datatype.isDefined) {
          s"datatype: ${propertyConstraint.datatype.get.split("#").last}"
        } else if (propertyConstraint.`class`.nonEmpty) {
          s"range: ${compact(propertyConstraint.`class`.head)}"
        }

        s"""  ${compact(validation.name)}:
           |    message: ${validation.ramlMessage.get}
           |    targetClass: ${compact(validation.targetClass.head)}
           |    propertyConstraint: ${compact(validation.propertyConstraints.head.ramlPropertyId)}
           |    $propertyConstraintId:
           |      $constraintValue
         """.stripMargin

      // non empty-list
      case Some(propertyConstraint) if propertyConstraint.node.isDefined =>
        s"""  ${compact(validation.name)}:g
           |    message: ${validation.message}
           |    targetClass: ${compact(validation.targetClass.head)}
           |    functionConstraint:
           |      functionName: nonEmptyList_${compact(validation.propertyConstraints.head.ramlPropertyId)
             .replace("-", "_")
             .replace(".", "_")}
        """.stripMargin
      case None =>
        s"""  ${compact(validation.name)}:
           |    message: ${validation.message}
           |    targetClass: schema-org.WebAPI
           |    functionConstraint:
           |      functionName: parserSide_${compact(validation.name.split("#").last).replace("-", "_")}
           |      libraries: http://a.ml/amf/validations/parser_side_validations.js
        """.stripMargin
    }
  }

  def compact(uri: String): String =
    Namespace.staticAliases
      .compact(uri)
      .replace(":", ".")
      .replace("raml-doc.", "doc.")
      .replace("raml-http.", "http.")
      .replace("schema-org.", "schema.")
}
