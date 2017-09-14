package amf.model

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class DomainExtension(private[amf] val domainExtension: amf.domain.extensions.DomainExtension) extends DomainElement {

  def definedBy: CustomDomainProperty = CustomDomainProperty(domainExtension.definedBy)
  def extension: DataNode             = DataNode(domainExtension.extension)

  def withDefinedBy(customDomainProperty: CustomDomainProperty): this.type = {
    domainExtension.withDefinedBy(customDomainProperty.customDomainProperty)
    this
  }

  def withExtension(dataNode: DataNode): this.type = {
    domainExtension.withExtension(dataNode.dataNode)
    this
  }

  override private[amf] def element = domainExtension

  def this() = this(amf.domain.extensions.DomainExtension())

  override def equals(other: Any): Boolean = other match {
    case that: DomainExtension =>
      (that canEqual this) &&
        extension == that.extension
    case _ => false
  }

  override def canEqual(other: Any): Boolean = other.isInstanceOf[DomainExtension]
}
