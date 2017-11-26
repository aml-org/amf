package amf.core.metamodel.document

import amf.core.metamodel.Field
import amf.core.vocabulary.Namespace.Document
import amf.core.vocabulary.ValueType

/**
  * Document metamodel
  */
trait DocumentModel extends FragmentModel with ModuleModel {

  override val `type`: List[ValueType] =
    Document + "Document" :: Document + "Fragment" :: Document + "Module" :: BaseUnitModel.`type`

  override def fields: List[Field] = Encodes :: Declares :: BaseUnitModel.fields
}

object DocumentModel extends DocumentModel {
  override def modelInstance = amf.core.model.document.Document()
}







