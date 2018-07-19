package amf.plugins.document.vocabularies.model.document

import amf.core.model.domain.AmfObject
import amf.plugins.document.vocabularies.metamodel.document.ExternalContextModelFields
import amf.plugins.document.vocabularies.model.domain.External

trait ExternalContext[T <: AmfObject] { this: T =>
  def externals: Seq[External]     = fields.field(ExternalContextModelFields.Externals)
  def withExternals(externals: Seq[External]): T = setArray(ExternalContextModelFields.Externals, externals).asInstanceOf[T]
}
