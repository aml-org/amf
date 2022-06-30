package amf.shapes.internal.spec.jsonschema.semanticjsonschema.context

import amf.core.internal.utils.AliasCounter
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import amf.shapes.internal.spec.contexts.JsonSchemaRefGuide
import amf.shapes.internal.spec.jsonschema.ref.{AstIndex, AstIndexBuilder, JsonSchemaInference}
import org.yaml.model.{YMap, YNode, YScalar}

import scala.collection.mutable
import amf.core.internal.parser.YMapOps

trait JsonSchemaLikeContext extends JsonSchemaInference { this: ShapeParserContext =>
  var jsonSchemaIndex: Option[AstIndex]
  var globalSpace: mutable.Map[String, Any]
  var localJSONSchemaContext: Option[YNode]
  var indexCache: mutable.Map[String, AstIndex]
  var jsonSchemaRefGuide: JsonSchemaRefGuide = JsonSchemaRefGuide(loc, refs)(this)

  // TODO: several things in this trait are duplicated with WebApiContext. Need to DRY

  def findJsonPathIn(index: AstIndex, path: String): Option[YMapEntryLike] = index.getNode(normalizeJsonPath(path))

  private def normalizeJsonPath(path: String): String = {
    if (path == "#" || path == "" || path == "/") "/" // exception root cases
    else {
      val s = if (path.startsWith("#/")) path.replace("#/", "") else path
      if (s.startsWith("/")) s.stripPrefix("/") else s
    }
  }

  def findLocalJSONPath(path: String): Option[YMapEntryLike] = {
    jsonSchemaIndex.flatMap(index => findJsonPathIn(index, path))
  }

  protected def normalizedJsonPointer(url: String): String = if (url.endsWith("/")) url.dropRight(1) else url

  def link(node: YNode): Either[String, YNode] = {
    node.to[YMap] match {
      case Right(map) =>
        val ref: Option[String] = map.key("$ref").flatMap(v => v.value.asOption[YScalar]).map(_.text)
        ref match {
          case Some(url) => Left(url)
          case None      => Right(node)
        }
      case _ => Right(node)
    }
  }

  def findJsonSchema(url: String): Option[AnyShape] =
    globalSpace
      .get(normalizedJsonPointer(url))
      .collect { case shape: AnyShape => shape }

  def setJsonSchemaAST(value: YNode): Unit = {
    val location = value.sourceName
    localJSONSchemaContext = Some(value)
    val index = indexCache.getOrElse(
      location, {
        val result = AstIndexBuilder.buildAst(value, AliasCounter(), computeJsonSchemaVersion(value))(this)
        indexCache.put(location, result)
        result
      }
    )
    jsonSchemaIndex = Some(index)
  }

  def computeJsonSchemaVersion(ast: YNode): SchemaVersion = parseSchemaVersion(ast, eh)

}
