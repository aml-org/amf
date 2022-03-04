package amf.jsonldschema.internal.scala

import amf.aml.client.scala.model.document.Dialect
import amf.aml.client.scala.model.domain.{NodeMapping, PropertyMapping}
import amf.core.client.scala.vocabulary.Namespace
import amf.jsonldschema.internal.scala.model.metamodel.JsonLdSchemaEncodesModel.base

object JsonLDDialectWrapper {
  def unWrap(wrappedDialect: WrappedDialect) = {
    wrappedDialect.dialect.withDeclares(wrappedDialect.dialect.declares.filterNot(_ == rootNode))
    wrappedDialect.dialect.documents().root().withEncoded(wrappedDialect.originalRoot)
    wrappedDialect.dialect
  }

  val dataPropertyTerm = base + "dataPropertyMapping"
  val propertyMapping  = PropertyMapping().withName("$data").withNodePropertyMapping(dataPropertyTerm)

  val rootNode = NodeMapping()
    .withNodeTypeMapping(base + "jsonldSchemaRootNode")
    .withId(base + "jsonSchemaRootNodeId")
    .withName("JsonSchemaRootNode")
    .withPropertiesMapping(Seq(propertyMapping))

  def wrap(dialect: Dialect, isArray: Boolean): WrappedDialect = {
    val originalEncoded = dialect.documents().root().encoded().value()
    dialect.documents().root().withEncoded(rootNode.id)
    propertyMapping.withObjectRange(Seq(originalEncoded))
    if (isArray) propertyMapping.withAllowMultiple(true) else propertyMapping.withAllowMultiple(false)
    WrappedDialect(dialect.withDeclares(dialect.declares :+ rootNode), originalEncoded, rootNode, dataPropertyTerm)
  }
}

case class WrappedDialect(dialect: Dialect, originalRoot: String, rootNode: NodeMapping, dataPropertyTerm: String)
