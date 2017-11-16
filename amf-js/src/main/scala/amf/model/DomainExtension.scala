package amf.model

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class DomainExtension(private[amf] val domainExtension: amf.domain.extensions.DomainExtension)
    extends DomainElement {

  def name: String                    = domainExtension.name
  def definedBy: CustomDomainProperty = CustomDomainProperty(domainExtension.definedBy)
  def extension: DataNode             = DataNode(domainExtension.extension)

  def withName(name: String): this.type = {
    domainExtension.withName(name)
    this
  }

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
}
