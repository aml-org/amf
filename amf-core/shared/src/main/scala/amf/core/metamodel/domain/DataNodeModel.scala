package amf.core.metamodel.domain

import amf.core.metamodel.Field
import amf.core.metamodel.domain.common.NameFieldSchema
import amf.core.vocabulary.Namespace.Data
import amf.core.vocabulary.ValueType

/**
  * Data Model to parse any generic data structure defined
  * by recursive records with arrays and scalar values (think of JSON or RAML)
  * into a RDF graph.
  *
  * This can be used to parse value of annotations, payloads or
  * examples
  */
object DataNodeModel extends DomainElementModel with NameFieldSchema {

  // We set this so it can be re-used in the definition of the dynamic types
  override def fields: List[Field]     = List(Name) ++ DomainElementModel.fields
  override val `type`: List[ValueType] = Data + "Node" :: DomainElementModel.`type`

  // This is a dynamic class, the structure is not known before parsing
  override val dynamic = true

  override def modelInstance =
    throw new Exception("DataNode is an abstract class and it cannot be instantiated directly")
}
