package amf.model

import scala.collection.JavaConverters._

/**
  * JVM AbstractDeclaration model class.
  */
abstract class AbstractDeclaration private[model] (private val declaration: amf.domain.`abstract`.AbstractDeclaration)
    extends DomainElement {
  override private[amf] def element: amf.domain.`abstract`.AbstractDeclaration

  val name: String                        = declaration.name
  val dataNode: DataNode                  = DataNode(declaration.dataNode)
  val variables: java.util.List[Variable] = declaration.variables.map(Variable).asJava

  /** Set name property of this [[AbstractDeclaration]]. */
  def withName(name: String): this.type = {
    declaration.withName(name)
    this
  }

  /** Set the dataNode property of this [[AbstractDeclaration]]. */
  def withDataNode(dataNode: DataNode): this.type = {
    declaration.withDataNode(dataNode.dataNode)
    this
  }

  /** Set variables property of this [[AbstractDeclaration]]. */
  def withVariables(variables: java.util.List[Variable]): this.type = {
    declaration.withVariables(variables.asScala.map(_.element))
    this
  }

  def withVariable(name: String): Variable = Variable(declaration.withVariable(name))
}

case class ResourceType private[model] (private val resourceType: amf.domain.`abstract`.ResourceType)
    extends AbstractDeclaration(resourceType) {
  def this() = this(amf.domain.`abstract`.ResourceType())

  override private[amf] def element: amf.domain.`abstract`.ResourceType = resourceType
}

case class Trait private[model] (private val tr: amf.domain.`abstract`.Trait) extends AbstractDeclaration(tr) {
  def this() = this(amf.domain.`abstract`.Trait())

  override private[amf] def element: amf.domain.`abstract`.Trait = tr
}
