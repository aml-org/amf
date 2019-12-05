package amf.dialects.oas.nodes
import amf.core.vocabulary.Namespace.XsdTypes.xsdString
import amf.dialects.OAS20Dialect.DialectLocation
import amf.dialects.OAS30Dialect
import amf.plugins.document.vocabularies.model.domain.{NodeMapping, PropertyMapping}
import amf.plugins.domain.webapi.metamodel.{OperationModel, PayloadModel, RequestModel}

object AMLRequestBodyObject extends DialectNode {

  override def location: String = OAS30Dialect.DialectLocation
  override def name: String     = "RequestBodyObject"

  override def nodeTypeMapping: String = RequestModel.`type`.head.iri()
  override def properties: Seq[PropertyMapping] = Seq(
    PropertyMapping()
      .withId(DialectLocation + "#/declarations/RequestBodyObject/description")
      .withName("description")
      .withNodePropertyMapping(RequestModel.Description.value.iri())
      .withLiteralRange(xsdString.iri()),
    PropertyMapping()
      .withId(DialectLocation + "#/declarations/RequestBodyObject/content")
      .withName("content")
      .withNodePropertyMapping(RequestModel.Payloads.value.iri())
      .withMapTermKeyProperty(PayloadModel.MediaType.value.iri())
      .withObjectRange(Seq(AMLContentObject.id))
  )
}
