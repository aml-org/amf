package amf.metadata.document

import amf.vocabulary.Namespace.Document
import amf.vocabulary.ValueType

/**
  * Document metamodel
  */
object DocumentModel extends FragmentModel with ModuleModel {
  override val `type`: List[ValueType] =
    List(Document + "Document", Document + "Fragment", Document + "Module") ++ BaseUnitModel.`type`
}
