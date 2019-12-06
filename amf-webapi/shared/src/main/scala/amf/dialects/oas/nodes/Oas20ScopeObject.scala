package amf.dialects.oas.nodes
import amf.core.vocabulary.Namespace.XsdTypes.xsdString
import amf.dialects.OAS20Dialect
import amf.plugins.document.vocabularies.model.domain.PropertyMapping
import amf.plugins.domain.webapi.metamodel.security.ScopeModel

object Oas20ScopeObject extends DialectNode {
  override def name: String            = "ScopeObject"
  override def nodeTypeMapping: String = ScopeModel.`type`.head.iri()
  override def properties: Seq[PropertyMapping] = Seq(
    PropertyMapping()
      .withId(OAS20Dialect.DialectLocation + "#/declarations/ScopeObject/name")
      .withName("name")
      .withMinCount(1)
      .withNodePropertyMapping(ScopeModel.Name.value.iri())
      .withLiteralRange(xsdString.iri()),
    PropertyMapping()
      .withId(OAS20Dialect.DialectLocation + "#/declarations/ScopeObject/description")
      .withName("description")
      .withNodePropertyMapping(ScopeModel.Description.value.iri())
      .withLiteralRange(xsdString.iri())
  )
}
