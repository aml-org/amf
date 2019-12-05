package amf.dialects.oas.nodes
import amf.core.vocabulary.Namespace.XsdTypes.{xsdBoolean, xsdString}
import amf.dialects.{OAS30Dialect, OasBaseDialect}
import amf.plugins.document.vocabularies.model.domain.{NodeMapping, PropertyMapping}
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel
import amf.plugins.domain.webapi.metamodel.{EncodingModel, ParameterModel, RequestModel}

object AMLEncodingObject extends DialectNode {

  override def location: String        = OAS30Dialect.DialectLocation
  override def name: String            = "EncodingObject"
  override def nodeTypeMapping: String = EncodingModel.`type`.head.iri()
  override def properties: Seq[PropertyMapping] = Seq(
    PropertyMapping()
      .withId(location + "/name")
      .withName("name")
      .withNodePropertyMapping(EncodingModel.PropertyName.value.iri())
      .withLiteralRange(xsdString.iri()),
    PropertyMapping()
      .withId(location + "/contentType")
      .withName("contentType")
      .withNodePropertyMapping(EncodingModel.ContentType.value.iri())
      .withLiteralRange(xsdString.iri()),
    PropertyMapping()
      .withId(location + "/style")
      .withName("style")
      .withNodePropertyMapping(EncodingModel.Style.value.iri())
      .withLiteralRange(xsdString.iri()),
    PropertyMapping()
      .withId(location + "/explode")
      .withName("explode")
      .withNodePropertyMapping(EncodingModel.Explode.value.iri())
      .withLiteralRange(xsdBoolean.iri()),
    PropertyMapping()
      .withId(location + "/allowReserved")
      .withName("allowReserved")
      .withNodePropertyMapping(EncodingModel.AllowReserved.value.iri())
      .withLiteralRange(xsdBoolean.iri()),
    PropertyMapping()
      .withId(location + "/headers")
      .withName("headers")
      .withMapTermKeyProperty(ParameterModel.Name.value.iri())
      .withNodePropertyMapping(EncodingModel.Headers.value.iri())
      .withObjectRange(Seq(Oas30AMLHeaderObject.id))
  )
}
