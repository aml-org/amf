package amf.shapes.internal.spec.oas.parser

import amf.core.client.scala.model.domain.{AmfScalar, Shape}
import amf.core.internal.annotations.ExternalFragmentRef
import amf.core.internal.metamodel.domain.LinkableElementModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain._
import amf.core.internal.utils.UriUtils
import amf.shapes.client.scala.model.domain.{AnyShape, UnresolvedShape}
import amf.shapes.internal.annotations.ExternalJsonSchemaShape
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.common.parser.ShapeParserContext
import amf.shapes.internal.spec.jsonschema.parser.{JsonSchemaParsingHelper, RemoteJsonSchemaParser}
import amf.shapes.internal.spec.oas.OasShapeDefinitions
import org.yaml.model.{YMap, YMapEntry, YPart, YType}

/*
  TODO: Refactor. Right now this implements refs for RAML and OAS json schemas as well proper json schemas. It should be modular. This refactor should be made alongside the OasTypeParser refactor.
 */
class OasRefParser(
    map: YMap,
    name: String,
    nameAnnotations: Annotations,
    ast: YPart,
    adopt: Shape => Unit,
    version: SchemaVersion
)(implicit val ctx: ShapeParserContext) {

  private val REF_KEY = "$ref"

  def parse(): Option[AnyShape] = {
    map
      .key(REF_KEY)
      .flatMap { e =>
        e.value.tagType match {
          case YType.Null => Some(AnyShape(e))
          case _ =>
            findDeclarationAndParse(e)
        }
      }
  }

  private def findDeclarationAndParse(entry: YMapEntry): Option[AnyShape] = {
    val rawRef: String = entry.value
    val definitionName = OasShapeDefinitions.stripDefinitionsPrefix(rawRef)
    ctx.findType(definitionName, SearchScope.All) match {
      case Some(s) =>
        // normal declaration to be used from raml or oas
        val link = createLinkToDeclaration(definitionName, s)
        adopt(link)
        Some(link)
      case _ =>
        /** Only enabled for JSON Schema, not OAS. In OAS local references can only point to the #/definitions
          * (#/components in OAS 3) node now we work with canonical JSON schema pointers, not local refs
          */
        val referencedShape = ctx.findLocalJSONPath(rawRef) match {
          case Some(_) => searchLocalJsonSchema(rawRef, if (ctx.linkTypes) definitionName else rawRef, entry)
          case _       => searchRemoteJsonSchema(rawRef, if (ctx.linkTypes) definitionName else rawRef, entry)
        }
        referencedShape.foreach(adopt)
        referencedShape
    }
  }

  private def createLinkToDeclaration(label: String, s: AnyShape) = {
    s.link(AmfScalar(label), Annotations(ast), Annotations.synthesized())
      .asInstanceOf[AnyShape]
      .withName(name, nameAnnotations)
      .withSupportsRecursion(true)
  }

  private def searchLocalJsonSchema(raw: String, term: String, entry: YMapEntry): Option[AnyShape] = {
    val (rawOrResolvedRef, declarationNameOrResolvedRef) =
      if (ctx.linkTypes) (raw, term)
      else {
        val resolvedRef = UriUtils.resolveRelativeTo(ctx.rootContextDocument, raw)
        (resolvedRef, resolvedRef)
      }
    ctx.findCachedJsonSchema(rawOrResolvedRef) match {
      case Some(s) =>
        Some(createLinkToDeclaration(rawOrResolvedRef, s))
      case None if isOasLikeContext && isDeclaration(rawOrResolvedRef) && ctx.isMainFileContext =>
        handleNotFoundOas(entry, rawOrResolvedRef, declarationNameOrResolvedRef)
      case None =>
        handleNotFoundJsonSchema(raw, entry, rawOrResolvedRef, declarationNameOrResolvedRef)
    }
  }

  private def handleNotFoundJsonSchema(
      raw: String,
      entry: YMapEntry,
      rawOrResolvedRef: String,
      declarationNameOrResolvedRef: String
  ) = {
    val tmpShape: UnresolvedShape =
      createAndRegisterUnresolvedShape(entry, rawOrResolvedRef, declarationNameOrResolvedRef)
    ctx.registerJsonSchema(rawOrResolvedRef, tmpShape)
    ctx.findLocalJSONPath(raw) match {
      case Some(entryLike) =>
        val definitiveName = entryLike.key.map(_.as[String]) getOrElse name
        OasTypeParser(entryLike, definitiveName, adopt, version)
          .parse()
          .map { shape =>
            ctx.futureDeclarations.resolveRef(declarationNameOrResolvedRef, shape)
            ctx.registerJsonSchema(rawOrResolvedRef, shape)
            if (ctx.linkTypes || rawOrResolvedRef.equals("#")) {
              val link = shape
                .link(AmfScalar(declarationNameOrResolvedRef), Annotations(ast), Annotations.synthesized())
                .asInstanceOf[AnyShape]
              val (nextName, annotations) = entryLike.key match {
                case Some(keyNode) =>
                  val key = keyNode.asScalar.map(_.text).getOrElse(name)
                  (key, Annotations(keyNode))
                case None => (name, Annotations())
              }
              link.withName(nextName, annotations)
            } else shape
          } orElse { Some(tmpShape) }

      case None => Some(tmpShape)
    }
  }

  private def handleNotFoundOas(entry: YMapEntry, rawOrResolvedRef: String, declarationNameOrResolvedRef: String) = {
    val shape = AnyShape(ast).withName(name, nameAnnotations)
    val tmpShape = UnresolvedShape(
      Fields(),
      Annotations(map),
      rawOrResolvedRef,
      None,
      Some((k: String) => shape.set(LinkableElementModel.TargetId, k)),
      shouldLink = false
    ).withName(declarationNameOrResolvedRef, Annotations()).withSupportsRecursion(true)
    tmpShape.unresolved(declarationNameOrResolvedRef, Nil, Some(entry.location))(ctx)
    tmpShape.withContext(ctx)
    adopt(tmpShape)
    shape.withLinkTarget(tmpShape).withLinkLabel(declarationNameOrResolvedRef)
    adopt(shape)
    Some(shape)
  }

  private def createAndRegisterUnresolvedShape(e: YMapEntry, ref: String, text: String) = {
    val tmpShape = UnresolvedShape(ref, map).withName(text, Annotations()).withSupportsRecursion(true)
    tmpShape.unresolved(text, Nil, Some(e.location))(ctx)
    tmpShape.withContext(ctx)
    adopt(tmpShape)
    tmpShape
  }

  private def isOasLikeContext = ctx.isOasLikeContext

  private val oas2DeclarationRegex = "^(\\#\\/definitions\\/){1}([^/\\n])+$"
  private val oas3DeclarationRegex =
    "^(\\#\\/components\\/){1}((schemas|parameters|securitySchemes|requestBodies|responses|headers|examples|links|callbacks){1}\\/){1}([^/\\n])+"

  private def isDeclaration(ref: String): Boolean =
    ctx match {
      case _ if ctx.isOas2Context && ref.matches(oas2DeclarationRegex)                         => true
      case _ if (ctx.isOas3Context || ctx.isAsyncContext) && ref.matches(oas3DeclarationRegex) => true
      case _                                                                                   => false
    }

  private def searchRemoteJsonSchema(ref: String, text: String, e: YMapEntry) = {
    val fullUrl = UriUtils.resolveRelativeTo(ctx.rootContextDocument, ref)
    ctx.findCachedJsonSchema(fullUrl) match {
      case Some(u: UnresolvedShape) => copyUnresolvedShape(ref, fullUrl, e, u)
      case Some(shape)              => createLinkToParsedShape(ref, shape)
      case _ =>
        parseRemoteSchema(ref, fullUrl) match {
          case None =>
            val tmpShape = JsonSchemaParsingHelper.createTemporaryShape(shape => adopt(shape), e, ctx, fullUrl)
            // it might still be resolvable at the RAML (not JSON Schema) level
            tmpShape.unresolved(text, Nil, Some(e.location)).withSupportsRecursion(true)
            Some(tmpShape)
          case Some(jsonSchemaShape) =>
            jsonSchemaShape.annotations += ExternalJsonSchemaShape(e)
            if (ctx.fragments.contains(text)) {
              // case when in an OAS spec we point with a regular $ref to something that is external
              // and holds a JSON schema we need to promote an external fragment to data type fragment
              promoteParsedShape(ref, text, fullUrl, jsonSchemaShape)
            } else Some(jsonSchemaShape)
        }
    }
  }

  private def copyUnresolvedShape(ref: String, fullRef: String, entry: YMapEntry, unresolved: UnresolvedShape) = {
    val annots = Annotations(ast)
    val copied = unresolved.copyShape(annots ++= unresolved.annotations.copy()).withLinkLabel(ref)
    copied.unresolved(fullRef, Nil, Some(entry.location))(ctx)
    adopt(copied)
    Some(copied)
  }

  private def createLinkToParsedShape(ref: String, shape: AnyShape) = {
    val annots = Annotations(ast)
    val copied =
      shape
        .link(AmfScalar(ref), annots, Annotations.synthesized())
        .asInstanceOf[AnyShape]
        .withName(name, nameAnnotations)
        .withSupportsRecursion(true)
    adopt(copied)
    Some(copied)
  }

  private def promoteParsedShape(
      ref: String,
      text: String,
      fullRef: String,
      jsonSchemaShape: AnyShape
  ): Option[AnyShape] = {
    ctx.promoteExternalToDataTypeFragment(text, fullRef, jsonSchemaShape)
    Some(
      jsonSchemaShape
        .link(AmfScalar(text), Annotations(ast) += ExternalFragmentRef(ref), Annotations.synthesized())
        .asInstanceOf[AnyShape]
        .withName(name, nameAnnotations)
        .withSupportsRecursion(true)
    )
  }

  private def parseRemoteSchema(ref: String, fullUrl: String): Option[AnyShape] = {
    RemoteJsonSchemaParser.parse(ref, fullUrl)
  }
}
