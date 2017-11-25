package amf.model

import amf.plugins.domain.webapi.models.templates

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll
import amf.core.model.domain.templates.{AbstractDeclaration => CoreAbstractDeclaration}

/**
  * JVM AbstractDeclaration model class.
  */
abstract class AbstractDeclaration private[model] (private val declaration: CoreAbstractDeclaration)
    extends DomainElement
    with Linkable {
  override private[amf] def element: CoreAbstractDeclaration

  val name: String                   = declaration.name
  val dataNode: DataNode             = DataNode(declaration.dataNode)
  val variables: js.Iterable[String] = declaration.variables.toJSArray

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
  def withVariables(variables: js.Iterable[String]): this.type = {
    declaration.withVariables(variables.toSeq)
    this
  }
}

@JSExportAll
case class ResourceType private[model] (private val resourceType: templates.ResourceType)
    extends AbstractDeclaration(resourceType) {
  def this() = this(amf.plugins.domain.webapi.models.templates.ResourceType())

  override private[amf] def element: templates.ResourceType = resourceType

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: templates.ResourceType => ResourceType(l) })

  override def linkCopy(): DomainElement with Linkable = ResourceType(element.linkCopy())
}

@JSExportAll
case class Trait private[model] (private val tr: templates.Trait) extends AbstractDeclaration(tr) {
  def this() = this(amf.plugins.domain.webapi.models.templates.Trait())

  override private[amf] def element: templates.Trait = tr

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: templates.Trait => Trait(l) })

  override def linkCopy(): DomainElement with Linkable = Trait(element.linkCopy())
}
