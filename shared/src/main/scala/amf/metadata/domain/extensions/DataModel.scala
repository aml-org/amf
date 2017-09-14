package amf.metadata.domain.extensions

import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.Data


/**
  * Data Model to parse any generic data structure defined
  * by recursive records with arrays and scalar values (think of JSON or RAML)
  * into a RDF graph.
  *
  * This can be used to parse value of annotations, payloads or
  * examples
  */

trait DataNode extends DomainElementModel {
  override val fields = Seq.empty ++ DomainElementModel.fields
  override val `type` = Data + "Node" :: DomainElementModel.`type`
  // This is a dynamic class, the structure is not known before parsing
  override val dynamic = true
}

object DataNode extends  DataNode {}


case class ObjectNode(subjectValue: String, properties: List[PropertyNode]) extends DataNode

case class PropertyNode(predicateValue: String, objectValue: List[DataNode])

case class ScalarNode(id: Option[String], objectValue: String, scalarType: Option[String])

case class ArrayNode(id: Option[String], objectValue: List[DataNode])