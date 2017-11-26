package amf.model.domain

import amf.core.model.domain.templates
import amf.core.unsafe.PlatformSecrets

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

/**
  * JVM AbstractDeclaration model class.
  */
@JSExportAll
abstract class ParametrizedDeclaration private[model] (
    private val declaration: templates.ParametrizedDeclaration)
    extends DomainElement {
  val name: String                          = declaration.name
  val target: String                        = declaration.target
  val variables: js.Iterable[VariableValue] = declaration.variables.map(VariableValue).toJSArray

  /** Set name property of this [[ParametrizedDeclaration]]. */
  def withName(name: String): this.type = {
    declaration.withName(name)
    this
  }

  /** Set the target property of this [[ParametrizedDeclaration]]. */
  def withTarget(target: String): this.type = {
    declaration.withTarget(target)
    this
  }

  /** Set variables property of this [[ParametrizedDeclaration]]. */
  def withVariables(variables: js.Iterable[VariableValue]): this.type = {
    declaration.withVariables(variables.map(_.element).toSeq)
    this
  }
}

object ParametrizedDeclaration extends PlatformSecrets {
  def apply(declaration: templates.ParametrizedDeclaration): ParametrizedDeclaration = declaration match {
    case resourceType: templates.ParametrizedDeclaration => platform.wrap(resourceType)
    case _                                               => throw new Exception("No wrapper for instance of ParametrizedDeclaration.")
  }
}




