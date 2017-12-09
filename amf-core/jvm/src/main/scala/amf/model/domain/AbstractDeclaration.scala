package amf.model.domain

import amf.core.model.domain.templates

import scala.collection.JavaConverters._

class AbstractDeclaration(private[amf] val decl: templates.AbstractDeclaration) extends DomainElement with Linkable  {

  def name(): String = decl.name
  def withName(name: String): this.type = {
    decl.withName(name)
    this
  }

  def dataNode(): DataNode = platform.wrap[DataNode](decl.dataNode)
  def withDatanode(dataNode: DataNode) = {
    decl.withDataNode(dataNode.element.asInstanceOf[amf.core.model.domain.DataNode])
    this
  }

  def variables(): java.util.List[String] = decl.variables.asJava
  def withVariables(variables: java.util.List[String]) = {
    decl.withVariables(variables.asScala)
    this
  }

  override private[amf] def element: templates.AbstractDeclaration = decl

  override def linkTarget: Option[DomainElement with Linkable] = throw new Exception("AbstractDeclaration is abstract")

  override def linkCopy(): DomainElement with Linkable = throw new Exception("AbstractDeclaration is abstract")
}
