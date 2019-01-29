package amf.plugins.document.vocabularies.model.domain
import amf.core.model.StrField
import amf.core.model.domain.{DomainElement, Linkable}
import amf.plugins.document.vocabularies.metamodel.domain.NodeMappableModel

trait NodeMappable extends DomainElement with Linkable with NodeMappableModel {
  def name: StrField                                = fields.field(Name)
  def withName(name: String)  = set(Name, name)
}
