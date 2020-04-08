package amf.dialects.oas.nodes
import amf.dialects.OasBaseDialect
import amf.plugins.document.vocabularies.model.domain.{NodeMapping, PropertyMapping}

trait DialectNode {

  def location: String = OasBaseDialect.DialectLocation

  def name: String
  def id: String = location + "/#declarations/" + name
  def nodeTypeMapping: String
  def isAbstract = false

  def properties: Seq[PropertyMapping]

  private def getTypeMappingUri: String = if (isAbstract) nodeTypeMapping + "Abstract" else nodeTypeMapping

  lazy val Obj: NodeMapping = NodeMapping()
    .withId(id)
    .withName(name)
    .withNodeTypeMapping(getTypeMappingUri)
    .withPropertiesMapping(properties)

}
