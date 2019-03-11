package amf.plugins.domain.webapi.resolution

import amf.core.services.ValidationsMerger
import amf.core.validation.AMFValidationResult

case class ResourceTypeAndTraitValidationsMerger(override val parserRun: Int) extends ValidationsMerger {
  val blackListRules: List[BlackListRule] =
//    UnresolvedReferenceFromParameterNameRule ::
    EmptyTargetRule ::
      ClosedShapeViolationsFromNestedEndpointsRule ::
      Nil

  override def merge(validationResult: AMFValidationResult): Boolean = {
    val bool = !blackListRules.exists(_.applies(validationResult))
    bool
  }
}

trait BlackListRule {
  def applies(result: AMFValidationResult): Boolean
}

object UnresolvedReferenceFromParameterNameRule extends BlackListRule {
  val regex = "Unresolved reference '<<.+>>' from root context "

  override def applies(result: AMFValidationResult): Boolean = {
    result.message.matches(regex)
  }
}

object EmptyTargetRule extends BlackListRule {
  override def applies(result: AMFValidationResult): Boolean = {
    result.targetNode == ""
  }
}

object ClosedShapeViolationsFromNestedEndpointsRule extends BlackListRule {
  val invalidValidations: List[String] =
    "Property /.+ not supported in a .+ resourceType node" ::
      "Property /.+ not supported in a .+ trait node" ::
      Nil

  override def applies(result: AMFValidationResult): Boolean = {
    invalidValidations.exists(result.message.matches)
  }
}
