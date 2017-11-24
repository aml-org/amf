package amf.framework.metamodel.document

import amf.framework.metamodel.Field
import amf.framework.vocabulary.Namespace.Document
import amf.framework.vocabulary.ValueType

/**
  * Document metamodel
  */
trait DocumentModel extends FragmentModel with ModuleModel {

  override val `type`: List[ValueType] =
    Document + "Document" :: Document + "Fragment" :: Document + "Module" :: BaseUnitModel.`type`

  override def fields: List[Field] = Encodes :: Declares :: BaseUnitModel.fields
}

object DocumentModel extends DocumentModel {
  override def modelInstance = amf.framework.model.document.Document()
}







