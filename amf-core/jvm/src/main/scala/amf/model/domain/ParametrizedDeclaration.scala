package amf.model.domain

import amf.core.model.domain.templates
import amf.core.unsafe.PlatformSecrets

import scala.collection.JavaConverters._


/**
  * JVM AbstractDeclaration model class.
  */
abstract class ParametrizedDeclaration private[model] (
    private[amf] val declaration: templates.ParametrizedDeclaration)
    extends DomainElement {

  def name: String                             = declaration.name
  def target: AbstractDeclaration              = platform.wrap[AbstractDeclaration](declaration)
  def variables: java.util.List[VariableValue] = Option(declaration.variables).getOrElse(Nil).map(VariableValue).asJava

  /** Set name property of this [[ParametrizedDeclaration]]. */
  def withName(name: String): this.type = {
    declaration.withName(name)
    this
  }

  /** Set the target property of this [[ParametrizedDeclaration]]. */
  def withTarget(target: AbstractDeclaration): this.type = {
    declaration.withTarget(target.element)
    this
  }

  /** Set variables property of this [[ParametrizedDeclaration]]. */
  def withVariables(variables: java.util.List[VariableValue]): this.type = {
    declaration.withVariables(variables.asScala.map(_.element))
    this
  }
}

object ParametrizedDeclaration extends PlatformSecrets {
  def apply(declaration: templates.ParametrizedDeclaration): ParametrizedDeclaration = declaration match {
    case resourceType: templates.ParametrizedDeclaration => platform.wrap[ParametrizedDeclaration](resourceType)
    case _                                               => throw new Exception("No wrapper for instance of ParametrizedDeclaration.")
  }
}




