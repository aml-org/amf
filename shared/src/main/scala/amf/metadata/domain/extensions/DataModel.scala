package amf.metadata.domain.extensions

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.Str
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.{Data, Schema}
import amf.vocabulary.ValueType

/**
  * Data Model to parse any generic data structure defined
  * by recursive records with arrays and scalar values (think of JSON or RAML)
  * into a RDF graph.
  *
  * This can be used to parse value of annotations, payloads or
  * examples
  */
object DataNodeModel extends DomainElementModel {

  val Name: Field = Field(Str, Schema + "name")

  // We set this so it can be re-used in the definition of the dynamic types
  override def fields: List[Field]     = List(Name) ++ DomainElementModel.fields
  override val `type`: List[ValueType] = Data + "Node" :: DomainElementModel.`type`

  // This is a dynamic class, the structure is not known before parsing
  override val dynamic = true
}
