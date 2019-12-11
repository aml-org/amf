package amf.dialects.oas.nodes
import amf.dialects.OasBaseDialect
import amf.plugins.document.vocabularies.model.domain.{NodeMapping, PropertyMapping}

trait DialectNode {

  def location: String = OasBaseDialect.DialectLocation

  def name: String
  def id: String = location + "/#declarations/" + name
  def nodeTypeMapping: String

  def properties: Seq[PropertyMapping]

  lazy val Obj: NodeMapping = NodeMapping()
    .withId(id)
    .withName(name)
    .withNodeTypeMapping(nodeTypeMapping)
    .withPropertiesMapping(properties)

}
