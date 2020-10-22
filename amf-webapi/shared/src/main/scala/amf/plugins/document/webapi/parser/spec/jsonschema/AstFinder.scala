package amf.plugins.document.webapi.parser.spec.jsonschema

import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.model.document.{ExternalFragment, Fragment, RecursiveUnit}
import amf.core.parser.{EmptyFutureDeclarations, JsonParserFactory, ParsedReference, ParserContext, Reference, SchemaReference, SyamlParsedDocument}
import amf.core.parser.errorhandler.ParserErrorHandler
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.contexts.parser.oas.JsonSchemaWebApiContext
import amf.plugins.document.webapi.contexts.parser.raml.Raml08WebApiContext
import amf.plugins.document.webapi.parser.spec.toOasDeclarations
import amf.validations.ParserSideValidations.UnableToParseJsonSchema
import org.yaml.model.{YDocument, YMap, YMapEntry, YNode}
import org.yaml.parser.{YParser, YamlParser}

class AstFinder {

  def findAst(inputFragment: Fragment, pointer: Option[String])(implicit ctx: WebApiContext): Option[YNode] = {
    val encoded: YNode = getYNode(inputFragment, ctx)
    val doc: Root      = getRoot(inputFragment, pointer, encoded)

    doc.parsed match {
      case parsedDoc: SyamlParsedDocument =>
        val shapeId: String      = if (doc.location.contains("#")) doc.location else doc.location + "#/"
        val parts: Array[String] = doc.location.split("#")
        val url: String          = parts.head
        val hashFragment: Option[String] =
          parts.tail.headOption.map(t => if (t.startsWith("/")) t.stripPrefix("/") else t)

        val jsonSchemaContext = getJsonSchemaContext(doc, ctx, url, ctx.options)
        val rootAst = getRootAst(parsedDoc, shapeId, hashFragment, url, jsonSchemaContext) match {
          case Right(value) => value.value
          case Left(value)  => value
        }
        Some(rootAst)

      case _ => None
    }
  }

  def getYNode(inputFragment: Fragment, ctx: WebApiContext): YNode = {
    inputFragment match {
      case fragment: ExternalFragment                        => fragment.encodes.parsed.getOrElse(parsedFragment(inputFragment, ctx.eh))
      case fragment: RecursiveUnit if fragment.raw.isDefined => parsedFragment(inputFragment, ctx.eh)
      case _ =>
        ctx.eh.violation(UnableToParseJsonSchema,
          inputFragment,
          None,
          "Cannot parse JSON Schema from unit with missing syntax information")
        YNode(YMap(IndexedSeq(), ""))
    }
  }

  def parsedFragment(inputFragment: Fragment, eh: ParserErrorHandler) = JsonYamlParser(inputFragment)(eh).document().node

  def getRootAst(parsedDoc: SyamlParsedDocument,
                         shapeId: String,
                         hashFragment: Option[String],
                         url: String,
                         jsonSchemaContext: JsonSchemaWebApiContext): Either[YNode, YMapEntry] = {
    val documentRoot = parsedDoc.document.node
    val rootAst = findRootNode(documentRoot, jsonSchemaContext, hashFragment).getOrElse {
      // hashFragment is always defined when return is None
      jsonSchemaContext.eh.violation(UnableToParseJsonSchema,
        shapeId,
        s"Cannot find path ${hashFragment.getOrElse("")} in JSON schema $url",
        documentRoot)
      Left(documentRoot)
    }

    jsonSchemaContext.setJsonSchemaAST(documentRoot)
    rootAst
  }

  def getRoot(inputFragment: Fragment, pointer: Option[String], encoded: YNode): Root = {
    Root(
      SyamlParsedDocument(YDocument(encoded)),
      inputFragment.location().getOrElse(inputFragment.id) + (if (pointer.isDefined) s"#${pointer.get}" else ""),
      "application/json",
      inputFragment.references.map(ref => ParsedReference(ref, Reference(ref.location().getOrElse(""), Nil), None)),
      SchemaReference,
      inputFragment.raw.getOrElse("")
    )
  }

  def findRootNode(ast: YNode, ctx: JsonSchemaWebApiContext, path: Option[String]): Option[Either[YNode, YMapEntry]] =
    if (path.isDefined) {
      ctx.setJsonSchemaAST(ast)
      val res = ctx.findLocalJSONPath(path.get)
      ctx.localJSONSchemaContext = None
      res.map(_._2)
    } else Some(Left(ast))

  def getJsonSchemaContext(document: Root,
                                   parentContext: ParserContext,
                                   url: String,
                                   options: ParsingOptions): JsonSchemaWebApiContext = {

    val cleanNested = ParserContext(url, document.references, EmptyFutureDeclarations(), parentContext.eh)
    cleanNested.globalSpace = parentContext.globalSpace

    // Apparently, in a RAML 0.8 API spec the JSON Schema has a closure over the schemas declared in the spec...
    val inheritedDeclarations =
      if (parentContext.isInstanceOf[Raml08WebApiContext])
        Some(parentContext.asInstanceOf[WebApiContext].declarations)
      else None

    new JsonSchemaWebApiContext(url,
      document.references,
      cleanNested,
      inheritedDeclarations.map(d => toOasDeclarations(d)),
      options)
  }
}

object JsonYamlParser {
  def apply(fragment: Fragment)(implicit errorHandler: ParserErrorHandler): YParser = {
    val location = fragment.location().getOrElse("")
    if (isYaml(location)) YamlParser(getRaw(fragment), location)
    else JsonParserFactory.fromCharsWithSource(getRaw(fragment), fragment.location().getOrElse(""))(errorHandler)
  }

  private def isYaml(location: String) = location.endsWith(".yaml") || location.endsWith(".yml")

  private def getRaw(inputFragment: Fragment): String = inputFragment match {
    case fragment: ExternalFragment => fragment.encodes.raw.value()
    case fragment: RecursiveUnit    => fragment.raw.get
    case _                          => ""
  }
}