package amf.model.domain

import amf.core.model.domain.templates

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import scala.scalajs.js.JSConverters._
import scala.scalajs.js

@JSExportAll
@JSExportTopLevel("model.domain.AbstractDeclaration")
class AbstractDeclaration(private[amf] val decl: templates.AbstractDeclaration) extends DomainElement with Linkable  {

  def name(): String = decl.name
  def withName(name: String): this.type = {
    decl.withName(name)
    this
  }

  def dataNode(): DataNode = platform.wrap[DataNode](decl.dataNode)
  def withDatanode(dataNode: DataNode) = {
    decl.withDataNode(dataNode.element)
    this
  }

  def variables(): js.Array[String] = decl.variables.toJSArray
  def withVariables(variables: js.Array[String]) = {
    decl.withVariables(variables.toSeq)
    this
  }

  override def linkTarget: Option[DomainElement with Linkable] = throw new Exception("AbstractDeclaration is abstract")

  override def linkCopy(): DomainElement with Linkable = throw new Exception("AbstractDeclaration is abstract")
}
