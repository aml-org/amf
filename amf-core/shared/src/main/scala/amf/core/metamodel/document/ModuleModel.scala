package amf.core.metamodel.document

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Array
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.model.document.Module
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.Document
import amf.core.vocabulary.ValueType

/**
  * Module metamodel
  *
  * A Module is a parsing Unit that declares DomainElements that can be referenced from the DomainElements in other parsing Units.
  * It main purpose is to expose the declared references so they can be re-used
  */
trait ModuleModel extends BaseUnitModel {

  /**
    * The declares relationship exposes a DomainElement as a re-usable unit that can be referenced from other units.
    * URIs for the declared DomainElement are considered to be stable and safe to reference from other DomainElements.
    */
  val Declares = Field(Array(DomainElementModel), Document + "declares", ModelDoc(ModelVocabularies.AmlDoc, "declares", "The declares relationship exposes a DomainElement as a re-usable unit that can be referenced from other units.\nURIs for the declared DomainElement are considered to be stable and safe to reference from other DomainElements."))

  override def modelInstance: AmfObject = Module()
}

object ModuleModel extends ModuleModel {

  override val `type`: List[ValueType] = List(Document + "Module") ++ BaseUnitModel.`type`

  override def fields: List[Field] = Declares :: BaseUnitModel.fields

  override  val doc: ModelDoc = ModelDoc(
    ModelVocabularies.AmlDoc,
    "Module",
    "A Module is a parsing Unit that declares DomainElements that can be referenced from the DomainElements in other parsing Units.\nIt main purpose is to expose the declared references so they can be re-used"
  )
}
