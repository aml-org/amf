package amf.model

import amf.core.model.domain.templates

import scala.collection.JavaConverters._

/**
  * JVM AbstractDeclaration model class.
  */
abstract class AbstractDeclaration private[model] (private val declaration: templates.AbstractDeclaration)
    extends DomainElement
    with Linkable {
  override private[amf] def element: templates.AbstractDeclaration

  val name: String                      = declaration.name
  val dataNode: DataNode                = DataNode(declaration.dataNode)
  val variables: java.util.List[String] = declaration.variables.asJava

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
  def withVariables(variables: java.util.List[String]): this.type = {
    declaration.withVariables(variables.asScala)
    this
  }
}




