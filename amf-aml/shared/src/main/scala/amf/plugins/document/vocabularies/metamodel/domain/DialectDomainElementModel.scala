package amf.plugins.document.vocabularies.metamodel.domain

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Bool
import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.vocabularies.model.domain.NodeMapping

class DialectDomainElementModel(val typeIri: String = (Namespace.Meta + "DialectDomainElement").iri(),
                                val typeFields: Seq[Field] = Nil,
                                val nodeMapping: Option[NodeMapping] = None)
    extends DomainElementModel
    with LinkableElementModel {

  val Abstract                         = Field(Bool, Namespace.Meta + "abstract")
  override def fields: List[Field]     = Abstract :: DomainElementModel.fields ++ LinkableElementModel.fields ++ typeFields
  override val `type`: List[ValueType] = Namespace.Meta + "DialectDomainElement" :: DomainElementModel.`type`

  override val dynamic = true
  override def modelInstance: AmfObject =
    throw new Exception("DialectDomainElement is an abstract class and it cannot be isntantiated directly")
}

object DialectDomainElementModel {
  def apply(): DialectDomainElementModel = new DialectDomainElementModel()
  def apply(typeIri: String)             = new DialectDomainElementModel(typeIri)
}
