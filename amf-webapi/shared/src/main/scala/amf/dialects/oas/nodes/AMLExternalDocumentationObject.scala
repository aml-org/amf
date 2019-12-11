package amf.dialects.oas.nodes
import amf.core.vocabulary.Namespace.XsdTypes.xsdString
import amf.dialects.OasBaseDialect
import amf.plugins.document.vocabularies.model.domain.{NodeMapping, PropertyMapping}
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel

object AMLExternalDocumentationObject extends DialectNode {

  override def name: String            = "ExternalDocumentationObject"
  override def nodeTypeMapping: String = CreativeWorkModel.`type`.head.iri()
  override def properties: Seq[PropertyMapping] = Seq(
    PropertyMapping()
      .withId(OasBaseDialect.DialectLocation + "#/declarations/ExternalDocumentationObject/description")
      .withName("description")
      .withNodePropertyMapping(CreativeWorkModel.Description.value.iri())
      .withLiteralRange(xsdString.iri()),
    PropertyMapping()
      .withId(OasBaseDialect.DialectLocation + "#/declarations/ExternalDocumentationObject/url")
      .withName("url")
      .withMinCount(1)
      .withNodePropertyMapping(CreativeWorkModel.Url.value.iri())
      .withLiteralRange(xsdString.iri())
  )
}
