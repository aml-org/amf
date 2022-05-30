package amf.shapes.internal.spec.jsonschema.semanticjsonschema.transform

import amf.aml.client.scala.model.document.Dialect
import amf.aml.client.scala.model.domain.{DocumentMapping, DocumentsModel, External}

class DialectWrapper(transformed: TransformationResult, options: SchemaTransformerOptions, location: String) {

  def wrapTransformationResult(): Dialect = {
    val documentMapping: DocumentsModel = createDocumentMapping(location, transformed)
    createDialectWith(transformed, documentMapping)
  }

  private def createDialectWith(transformed: TransformationResult, documentMapping: DocumentsModel): Dialect = {
    val dialect = Dialect()
      .withId(location)
      .withName(options.dialectName)
      .withVersion(options.dialectVersion)
      .withDocuments(documentMapping)
      .withDeclares(transformed.declared)
      .withRoot(true)

    if (transformed.externals.nonEmpty) {
      val externals = extractExternals(transformed, location)
      dialect.withExternals(externals)
    }
    dialect
  }

  private def extractExternals(transformed: TransformationResult, location: String): Seq[External] = {
    transformed.externals.map { case (ns, prefix) =>
      External().withId(location + "/external/" + prefix).withBase(prefix).withAlias(ns)
    }.toList
  }

  private def createDocumentMapping(location: String, transformed: TransformationResult): DocumentsModel = {
    DocumentsModel()
      .withId(location + "/documents")
      .withRoot(DocumentMapping().withId("#/documents/root").withEncoded(transformed.encoded.id))
  }

}

object DialectWrapper {
  def apply(transformed: TransformationResult, options: SchemaTransformerOptions, location: String) =
    new DialectWrapper(transformed, options, location)
}
