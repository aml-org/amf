package amf.core.metamodel.domain

import amf.core.metamodel.{DynamicObj, Field}
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.common.NameFieldSchema
import amf.core.model.domain._
import amf.core.vocabulary.Namespace.Data
import amf.core.vocabulary.{Namespace, ValueType}

/**
  * Data Model to parse any generic data structure defined
  * by recursive records with arrays and scalar values (think of JSON or RAML)
  * into a RDF graph.
  *
  * This can be used to parse value of annotations, payloads or
  * examples
  */
object DataNodeModel extends DomainElementModel with DynamicObj with NameFieldSchema {

  // We set this so it can be re-used in the definition of the dynamic types
  override def fields: List[Field]     = List(Name) ++ DomainElementModel.fields
  override val `type`: List[ValueType] = Data + "Node" :: DomainElementModel.`type`

  override def modelInstance =
    throw new Exception("DataNode is an abstract class and it cannot be instantiated directly")

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Data,
    "Data Node",
    "Base class for all data nodes parsed from the data structure"
  )
}

object ObjectNodeModel extends DomainElementModel {

  override def fields: List[Field]      = DataNodeModel.fields
  override val `type`: List[ValueType]  = Data + "Object" :: DataNodeModel.`type`
  override def modelInstance: AmfObject = ObjectNode()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Data,
    "Object Node",
    "Node that represents a dynamic object with records data structure"
  )
}

object ScalarNodeModel extends DomainElementModel with DynamicObj {

  val Value =
    Field(Str, Namespace.Data + "value", ModelDoc(ModelVocabularies.Data, "value", "value for an scalar dynamic node"))

  override def fields: List[Field]      = Value :: DataNodeModel.fields
  override val `type`: List[ValueType]  = Data + "Scalar" :: DataNodeModel.`type`
  override def modelInstance: AmfObject = ScalarNode()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Data,
    "Scalar Node",
    "Node that represents a dynamic scalar value data structure"
  )
}

object ArrayNodeModel extends DomainElementModel {

  val Member =
    Field(Array(DataNodeModel), Namespace.Rdf + "member", ModelDoc(ExternalModelVocabularies.Rdf, "member", ""))

  override def fields: List[Field]      = Member :: DataNodeModel.fields
  override val `type`: List[ValueType]  = Data + "Array" :: DataNodeModel.`type`
  override def modelInstance: AmfObject = ArrayNode()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Data,
    "Array Node",
    "Node that represents a dynamic array data structure"
  )
}

object LinkNodeModel extends DomainElementModel {

  override def fields: List[Field]      = DataNodeModel.fields
  override val `type`: List[ValueType]  = Data + "Link" :: DataNodeModel.`type`
  override def modelInstance: AmfObject = LinkNode()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Data,
    "Link Node",
    "Node that represents a dynamic link in a data structure"
  )
}
