package amf.model

import amf.core.model.domain

import scala.collection.JavaConverters._
import amf.core.model.domain.{ParametrizedDeclaration, templates}
import amf.plugins.domain.webapi.models.templates.{ParametrizedResourceType => CoreParametrizedResourcetype, ParametrizedTrait => CoreParametrizedTrait}

/**
  * JVM AbstractDeclaration model class.
  */
abstract class ParametrizedDeclaration private[model] (
    private val declaration: templates.ParametrizedDeclaration)
    extends DomainElement {
  override private[amf] def element: templates.ParametrizedDeclaration

  val name: String                             = declaration.name
  val target: String                           = declaration.target
  val variables: java.util.List[VariableValue] = declaration.variables.map(VariableValue).asJava

  /** Set name property of this [[domain.ParametrizedDeclaration]]. */
  def withName(name: String): this.type = {
    declaration.withName(name)
    this
  }

  /** Set the target property of this [[domain.ParametrizedDeclaration]]. */
  def withTarget(target: String): this.type = {
    declaration.withTarget(target)
    this
  }

  /** Set variables property of this [[domain.ParametrizedDeclaration]]. */
  def withVariables(variables: java.util.List[VariableValue]): this.type = {
    declaration.withVariables(variables.asScala.map(_.element))
    this
  }
}

object ParametrizedDeclaration {
  def apply(declaration: templates.ParametrizedDeclaration): ParametrizedDeclaration = declaration match {
    case resourceType: CoreParametrizedResourcetype => ParametrizedResourceType(resourceType)
    case tr: CoreParametrizedTrait                  => ParametrizedTrait(tr)
    case _                                                 => throw new Exception("No wrapper for instance of ParametrizedDeclaration.")
  }
}