package amf.dialects.oas.nodes
import amf.dialects.{OAS20Dialect, OasBaseDialect}
import amf.plugins.document.vocabularies.model.domain.{NodeMapping, PropertyMapping}
import amf.plugins.domain.webapi.metamodel.TagModel
import amf.core.vocabulary.Namespace.XsdTypes._

object AMLTagObject extends DialectNode {

  override def name: String            = "TagObject"
  override def nodeTypeMapping: String = TagModel.`type`.head.iri()
  override def properties: Seq[PropertyMapping] = Seq(
    PropertyMapping()
      .withId(OasBaseDialect.DialectLocation + "#/declarations/TagObject/name")
      .withNodePropertyMapping(TagModel.Name.value.iri())
      .withName("name")
      .withMinCount(1)
      .withLiteralRange(xsdString.iri()),
    PropertyMapping()
      .withId(OasBaseDialect.DialectLocation + "#/declarations/TagObject/description")
      .withName("description")
      .withNodePropertyMapping(TagModel.Description.value.iri())
      .withLiteralRange(xsdString.iri()),
    PropertyMapping()
      .withId(OasBaseDialect.DialectLocation + "#/declarations/TagObject/externalDocs")
      .withName("externalDocs")
      .withNodePropertyMapping(TagModel.Documentation.value.iri())
      .withObjectRange(
        Seq(
          AMLExternalDocumentationObject.Obj.id
        ))
  )
}
