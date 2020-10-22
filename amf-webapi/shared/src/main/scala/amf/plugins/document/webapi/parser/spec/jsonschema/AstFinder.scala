package amf.plugins.document.webapi.parser.spec.jsonschema

import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.model.document.{BaseUnit, ExternalFragment, Fragment, RecursiveUnit}
import amf.core.parser.errorhandler.ParserErrorHandler
import amf.core.parser.{EmptyFutureDeclarations, JsonParserFactory, ParsedReference, ParserContext, Reference, SchemaReference, SyamlParsedDocument}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.contexts.parser.oas.JsonSchemaWebApiContext
import amf.plugins.document.webapi.contexts.parser.raml.Raml08WebApiContext
import amf.plugins.document.webapi.parser.spec.common.YMapEntryLike
import amf.plugins.document.webapi.parser.spec.toOasDeclarations
import amf.validations.ParserSideValidations.UnableToParseJsonSchema
import org.yaml.model.{YDocument, YMap, YNode}
import org.yaml.parser.{YParser, YamlParser}

object AstFinder {

  def findSchemaAst(doc: Root, context: ParserContext, options: ParsingOptions): Option[YNode] = {
    findAst(doc, JsonSchemaUrlFragmentAdapter$, context, options)
  }

  def findAst(inputFragment: Fragment, pointer: Option[String])(implicit ctx: WebApiContext): Option[YNode] = {
    val doc = createRootFrom(inputFragment, pointer, ctx.eh)
    findAst(doc, DefaultUrlFragmentAdapter$, ctx, ctx.options)
  }

  def createRootFrom(inputFragment: Fragment, pointer: Option[String], errorHandler: ParserErrorHandler): Root = {
    val encoded: YNode = getYNodeFrom(inputFragment, errorHandler)
    createRoot(inputFragment, pointer, encoded)
  }

  private def findAst(doc: Root,
                      toolkit: UrlFragmentAdapter,
                      context: ParserContext,
                      options: ParsingOptions): Option[YNode] = {
    doc.parsed match {
      case parsedDoc: SyamlParsedDocument =>
        val shapeId: String                  = if (doc.location.contains("#")) doc.location else doc.location + "#/"
        val JsonReference(url, hashFragment) = JsonReference.buildReference(doc.location, toolkit)
        val jsonSchemaContext                = makeJsonSchemaContext(doc, context, url, options)
        val rootAst                          = getRootAst(parsedDoc, shapeId, hashFragment, url, jsonSchemaContext)
        Some(rootAst.value)

      case _ => None
    }
  }

  def deriveShapeIdFrom(doc: Root): String = if (doc.location.contains("#")) doc.location else doc.location + "#/"

  def getYNodeFrom(inputFragment: Fragment, errorHandler: ParserErrorHandler): YNode = {
    inputFragment match {
      case fragment: ExternalFragment                        => fragment.encodes.parsed.getOrElse(parsedFragment(inputFragment, errorHandler))
      case fragment: RecursiveUnit if fragment.raw.isDefined => parsedFragment(inputFragment, errorHandler)
      case _ =>
        errorHandler.violation(UnableToParseJsonSchema,
                               inputFragment,
                               None,
                               "Cannot parse JSON Schema from unit with missing syntax information")
        YNode(YMap(IndexedSeq(), ""))
    }
  }

  private def parsedFragment(inputFragment: Fragment, eh: ParserErrorHandler) =
    JsonYamlParser(inputFragment)(eh).document().node

  def getRootAst(parsedDoc: SyamlParsedDocument,
                 shapeId: String,
                 hashFragment: Option[String],
                 url: String,
                 ctx: WebApiContext): YMapEntryLike = {
    val documentRoot = parsedDoc.document.node
    ctx.setJsonSchemaAST(documentRoot)
    val rootAst = findRootNode(documentRoot, ctx, hashFragment).getOrElse {
      // hashFragment is always defined when return is None
      ctx.eh.violation(UnableToParseJsonSchema,
                       shapeId,
                       s"Cannot find path ${hashFragment.getOrElse("")} in JSON schema $url",
                       documentRoot)
      YMapEntryLike(documentRoot)
    }
    rootAst
  }

  private def createRoot(inputFragment: Fragment, pointer: Option[String], encoded: YNode): Root = {
    Root(
      SyamlParsedDocument(YDocument(encoded)),
      buildJsonReference(inputFragment, pointer),
      "application/json",
      toParsedReferences(inputFragment.references),
      SchemaReference,
      inputFragment.raw.getOrElse("")
    )
  }

  private def buildJsonReference(inputFragment: Fragment, pointer: Option[String]) = {
    inputFragment.location().getOrElse(inputFragment.id) + (if (pointer.isDefined) s"#${pointer.get}" else "")
  }

  private def toParsedReferences(references: Seq[BaseUnit]) = {
    references.map(ref => ParsedReference(ref, Reference(ref.location().getOrElse(""), Nil), None))
  }

  private def findRootNode(ast: YNode, ctx: WebApiContext, maybePath: Option[String]): Option[YMapEntryLike] = {
    maybePath.map { path =>
        val res = ctx.findLocalJSONPath(path)
        res.map(_._2).map {
          case Left(value)  => YMapEntryLike(value)
          case Right(value) => YMapEntryLike(value)
        }
      }
      .getOrElse(Some(YMapEntryLike(ast)))
  }

  def makeJsonSchemaContext(document: Root,
                            parentContext: ParserContext,
                            url: String,
                            options: ParsingOptions): JsonSchemaWebApiContext = {

    val cleanNested = ParserContext(url, document.references, EmptyFutureDeclarations(), parentContext.eh)
    cleanNested.globalSpace = parentContext.globalSpace

    // Apparently, in a RAML 0.8 API spec the JSON Schema has a closure over the schemas declared in the spec...
    val inheritedDeclarations = getInheritedDeclarations(parentContext)

    new JsonSchemaWebApiContext(url, document.references, cleanNested, inheritedDeclarations, options)
  }

  private def getInheritedDeclarations(parserContext: ParserContext) = {
    parserContext match {
      case ramlContext: Raml08WebApiContext => Some(toOasDeclarations(ramlContext.declarations))
      case _                                => None
    }
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
