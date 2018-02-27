package amf.core.model.domain.extensions

import amf.core.metamodel.domain.extensions.DomainExtensionModel.{DefinedBy, Extension, Name}
import amf.core.model.domain.{AmfObject, DataNode}

trait BaseDomainExtension extends AmfObject {

  def name: String = fields(Name)

  def definedBy: CustomDomainProperty = fields(DefinedBy)

  def extension: DataNode = fields(Extension)

  def withDefinedBy(customProperty: CustomDomainProperty): this.type = set(DefinedBy, customProperty)

  def withName(name: String): this.type = set(Name, name)

  def withExtension(extension: DataNode): this.type = set(Extension, extension)

  // This element will never be serialised in the JSON-LD graph, it is just a placeholder
  // for the extension point. ID is not required for serialisation
  override def adopted(parent: String): this.type = withId(parent + "/extension")
}
