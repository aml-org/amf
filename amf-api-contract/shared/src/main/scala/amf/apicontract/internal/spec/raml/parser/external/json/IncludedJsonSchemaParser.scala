package amf.apicontract.internal.spec.raml.parser.external.json

import amf.apicontract.internal.spec.common.parser.WebApiShapeParserContextAdapter
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.spec.raml.parser.external.RamlJsonSchemaParser.{errorShape, withScopedContext}
import amf.apicontract.internal.validation.definitions.ParserSideValidations.JsonSchemaFragmentNotFound
import amf.core.client.scala.parse.document.ReferenceFragmentPartition
import amf.core.internal.annotations.ExternalFragmentRef
import amf.core.internal.parser.domain.JsonParserFactory
import amf.core.internal.utils.UriUtils
import amf.shapes.client.scala.model.domain.{AnyShape, UnresolvedShape}
import amf.shapes.internal.annotations.ParsedJSONSchema
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.parser.ExternalFragmentHelper.searchForAlreadyParsedNodeInFragments
import amf.shapes.internal.spec.jsonschema.parser.JsonSchemaParsingHelper
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.spec.raml.parser.external.ValueAndOrigin
import org.mulesoft.lexer.Position
import org.yaml.model.{YMapEntry, YNode}
import org.yaml.parser.JsonParser

case class IncludedJsonSchemaParser(key: YNode, ast: YNode)(implicit ctx: RamlWebApiContext) {

  def parse(origin: ValueAndOrigin, url: String) = {
    val (basePath, localPath) = ReferenceFragmentPartition(url)
    val normalizedLocalPath   = localPath.map(_.stripPrefix("/definitions/")) // assumes draft 4 definitions
    findInExternals(basePath, normalizedLocalPath) match {
      case Some(s) =>
        copyExternalShape(basePath, s, localPath)
      case _ if isInnerSchema(normalizedLocalPath) =>
        JsonSchemaDefinitionsParser.parse(key, origin, basePath, localPath, normalizedLocalPath)
      case _ =>
        new LegacyRootJsonSchemaParser(key, ast).parse(origin, basePath)
    }
  }

  private def findInExternals(basePath: String, normalizedLocalPath: Option[String]) = {
    normalizedLocalPath
      .flatMap(ctx.declarations.findInExternalsLibs(basePath, _))
      .orElse(ctx.declarations.findInExternals(basePath))
  }

  private def isInnerSchema(normalizedLocalPath: Option[String]) = normalizedLocalPath.isDefined

  private def copyExternalShape(basePath: String, s: AnyShape, localPath: Option[String]) = {
    val shape = s.copyShape().withName(key.as[String])
    ctx.declarations.fragments
      .get(basePath)
      .foreach(e =>
        shape.callAfterAdoption { () =>
          shape.withReference(e.encoded.id + localPath.getOrElse(""))
        }
      )
    if (shape.examples.nonEmpty) { // top level inlined shape, we don't want to reuse the ID, this must be an included JSON schema => EDGE CASE!
      // We remove the examples declared in the previous endpoint for this inlined shape , see previous comment about the edge case
      shape.fields.remove(AnyShapeModel.Examples.value.iri())
    }
    shape
  }
}
