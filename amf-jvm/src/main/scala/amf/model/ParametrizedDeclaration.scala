package amf.model

import scala.collection.JavaConverters._
import amf.domain.`abstract`

/**
  * JVM AbstractDeclaration model class.
  */
abstract class ParametrizedDeclaration private[model] (
    private val declaration: amf.domain.`abstract`.ParametrizedDeclaration)
    extends DomainElement {
  override private[amf] def element: amf.domain.`abstract`.ParametrizedDeclaration

  val name: String                             = declaration.name
  val target: String                           = declaration.target
  val variables: java.util.List[VariableValue] = declaration.variables.map(VariableValue).asJava

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
  def withVariables(variables: java.util.List[VariableValue]): this.type = {
    declaration.withVariables(variables.asScala.map(_.element))
    this
  }
}

object ParametrizedDeclaration {
  def apply(declaration: amf.domain.`abstract`.ParametrizedDeclaration): ParametrizedDeclaration = declaration match {
    case resourceType: `abstract`.ParametrizedResourceType => ParametrizedResourceType(resourceType)
    case tr: `abstract`.ParametrizedTrait                  => ParametrizedTrait(tr)
    case _                                                 => throw new Exception("No wrapper for instance of ParametrizedDeclaration.")
  }
}

case class ParametrizedResourceType private[model] (
    private val resourceType: amf.domain.`abstract`.ParametrizedResourceType)
    extends ParametrizedDeclaration(resourceType) {
  def this() = this(amf.domain.`abstract`.ParametrizedResourceType())

  override private[amf] def element: amf.domain.`abstract`.ParametrizedResourceType = resourceType
}

case class ParametrizedTrait private[model] (private val tr: amf.domain.`abstract`.ParametrizedTrait)
    extends ParametrizedDeclaration(tr) {
  def this() = this(amf.domain.`abstract`.ParametrizedTrait())

  override private[amf] def element: amf.domain.`abstract`.ParametrizedTrait = tr
}
