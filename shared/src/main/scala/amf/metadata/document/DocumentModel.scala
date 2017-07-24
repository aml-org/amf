package amf.metadata.document

import amf.metadata.Field
import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

/**
  * Document metamodel
  */
object DocumentModel extends FragmentModel with ModuleModel {

  override val `type`: List[ValueType] =
    Document + "Document" :: Document + "Fragment" :: Document + "Module" :: BaseUnitModel.`type`

  override val fields: List[Field] = Encodes :: Declares :: BaseUnitModel.fields
}
