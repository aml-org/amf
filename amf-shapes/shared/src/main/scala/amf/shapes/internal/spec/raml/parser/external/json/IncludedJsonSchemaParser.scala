package amf.shapes.internal.spec.raml.parser.external.json

import amf.core.client.scala.parse.document.ReferenceFragmentPartition
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.spec.common.parser.ShapeParserContext
import amf.shapes.internal.spec.contexts.ReferenceFinder
import amf.shapes.internal.spec.jsonschema.parser.document.JsonSchemaLinker
import amf.shapes.internal.spec.raml.parser.external.ValueAndOrigin
import org.yaml.model.YNode

case class IncludedJsonSchemaParser(key: YNode, ast: YNode)(implicit ctx: ShapeParserContext) {

  def parse(origin: ValueAndOrigin, url: String) = {
    val (basePath, localPath) = ReferenceFragmentPartition(url)
    val normalizedLocalPath   = localPath.map(_.stripPrefix("/definitions/")) // assumes draft 4 definitions
    findInExternals(basePath, normalizedLocalPath) match {
      case Some(s) =>
        copyExternalShape(basePath, s, localPath)
      case _ if isIncludeToJsonSchemaDoc(url) =>
        JsonSchemaLinker.linkShapeIn(url, origin.valueAST).getOrElse(AnyShape())
      case _ if isInnerSchema(normalizedLocalPath) =>
        JsonSchemaDefinitionsParser.parse(key, origin, basePath, localPath, normalizedLocalPath)
      case _ =>
        new LegacyRootJsonSchemaParser(key, ast).parse(origin, basePath)
    }
  }

  private def isIncludeToJsonSchemaDoc(include: String): Boolean = {
    ReferenceFinder
      .findJsonReferencedUnit(include, ctx)
      .collect { case unit: JsonSchemaDocument =>
        unit
      }
      .isDefined
  }

  private def findInExternals(basePath: String, normalizedLocalPath: Option[String]) = {
    normalizedLocalPath
      .flatMap(ctx.findInExternalsLibs(basePath, _))
      .orElse(ctx.findInExternals(basePath))
  }

  private def isInnerSchema(normalizedLocalPath: Option[String]) = normalizedLocalPath.isDefined

  private def copyExternalShape(basePath: String, s: AnyShape, localPath: Option[String]) = {
    val shape = s.copyShape().withName(key.as[String])
    ctx.fragments
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
