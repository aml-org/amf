package amf.plugins.document.apicontract.parser.spec.declaration

import amf.core.annotations.ExternalFragmentRef
import amf.core.metamodel.domain.LinkableElementModel
import amf.core.model.domain.{AmfScalar, Linkable, Shape}
import amf.core.parser._
import amf.core.utils.UriUtils
import amf.plugins.document.apicontract.annotations.ExternalJsonSchemaShape
import amf.plugins.document.apicontract.parser.ShapeParserContext
import amf.plugins.document.apicontract.parser.spec.OasShapeDefinitions
import amf.plugins.document.apicontract.parser.spec.declaration.utils.JsonSchemaParsingHelper
import amf.plugins.domain.shapes.models.{AnyShape, UnresolvedShape}
import org.yaml.model.{YMap, YMapEntry, YPart, YType}

/*
  TODO: Refactor. Right now this implements refs for RAML and OAS json schemas as well proper json schemas. It should be modular. This refactor should be made alongside the OasTypeParser refactor.
 */
class OasRefParser(map: YMap,
                   name: String,
                   nameAnnotations: Annotations,
                   ast: YPart,
                   adopt: Shape => Unit,
                   version: SchemaVersion)(implicit val ctx: ShapeParserContext) {

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

  private def findDeclarationAndParse(e: YMapEntry) = {
    val rawRef: String = e.value
    val definitionName = OasShapeDefinitions.stripDefinitionsPrefix(rawRef)
    ctx.findType(definitionName, SearchScope.All) match {
      case Some(s) =>
        // normal declaration to be used from raml or oas
        val link = createLinkToDeclaration(definitionName, s)
        adopt(link)
        Some(link)
      case _ =>
        /**
          *  Only enabled for JSON Schema, not OAS. In OAS local references can only point to the #/definitions (#/components in OAS 3) node
          *  now we work with canonical JSON schema pointers, not local refs
          */
        val referencedShape = ctx.findLocalJSONPath(rawRef) match {
          case Some(_) => searchLocalJsonSchema(rawRef, if (ctx.linkTypes) definitionName else rawRef, e)
          case _       => searchRemoteJsonSchema(rawRef, if (ctx.linkTypes) definitionName else rawRef, e)
        }
        referencedShape.foreach(safeAdoption)
        referencedShape
    }
  }

  private def createLinkToDeclaration(label: String, s: AnyShape) = {
    s.link(AmfScalar(label), Annotations(ast), Annotations.synthesized())
      .asInstanceOf[AnyShape]
      .withName(name, nameAnnotations)
      .withSupportsRecursion(true)
  }

  private def searchLocalJsonSchema(r: String, t: String, e: YMapEntry): Option[AnyShape] = {
    val (rawOrResolvedRef, declarationNameOrResolvedRef) =
      if (ctx.linkTypes) (r, t)
      else {
        val resolvedRef = UriUtils.resolveRelativeTo(ctx.rootContextDocument, r)
        (resolvedRef, resolvedRef)
      }
    ctx.findJsonSchema(rawOrResolvedRef) match {
      case Some(s) =>
        Some(createLinkToDeclaration(rawOrResolvedRef, s))
      case None if isOasLikeContext && isDeclaration(rawOrResolvedRef) && ctx.isMainFileContext =>
        handleNotFoundOas(e, rawOrResolvedRef, declarationNameOrResolvedRef)
      case None =>
        handleNotFoundJsonSchema(r, e, rawOrResolvedRef, declarationNameOrResolvedRef)
    }
  }

  private def handleNotFoundJsonSchema(r: String,
                                       e: YMapEntry,
                                       rawOrResolvedRef: String,
                                       declarationNameOrResolvedRef: String) = {
    val tmpShape: UnresolvedShape =
      createAndRegisterUnresolvedShape(e, rawOrResolvedRef, declarationNameOrResolvedRef)
    ctx.registerJsonSchema(rawOrResolvedRef, tmpShape)
    ctx.findLocalJSONPath(r) match {
      case Some(entryLike) =>
        val definitiveName = entryLike.key.map(_.as[String]) getOrElse (name)
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

  private def handleNotFoundOas(e: YMapEntry, rawOrResolvedRef: String, declarationNameOrResolvedRef: String) = {
    val shape = AnyShape(ast).withName(name, nameAnnotations)
    val tmpShape = UnresolvedShape(
      Fields(),
      Annotations(map),
      rawOrResolvedRef,
      None,
      Some((k: String) => shape.set(LinkableElementModel.TargetId, k)),
      shouldLink = false).withName(declarationNameOrResolvedRef, Annotations()).withSupportsRecursion(true)
    tmpShape.unresolved(declarationNameOrResolvedRef, e)(ctx)
    tmpShape.withContext(ctx)
    adopt(tmpShape)
    shape.withLinkTarget(tmpShape).withLinkLabel(declarationNameOrResolvedRef)
    adopt(shape)
    Some(shape)
  }

  private def createAndRegisterUnresolvedShape(e: YMapEntry, ref: String, text: String) = {
    val tmpShape = UnresolvedShape(ref, map).withName(text, Annotations()).withSupportsRecursion(true)
    tmpShape.unresolved(text, e)(ctx)
    tmpShape.withContext(ctx)
    adopt(tmpShape)
    tmpShape
  }

  def safeAdoption(s: AnyShape): Unit = {
    val oldId = Option(s.id)
    adopt(s)
    s match {
      case l: Linkable if l.isLink && s.id == l.effectiveLinkTarget().id =>
        oldId.foreach(s.id = _)
      case _ =>
    }
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
    ctx.findJsonSchema(fullUrl) match {
      case Some(u: UnresolvedShape) => copyUnresolvedShape(ref, fullUrl, e, u)
      case Some(shape)              => createLinkToParsedShape(ref, shape)
      case _                        =>
        // TODO: parsed json schema is registered with ref but searched with fullRef, leads to repeated parsing
        parseRemoteSchema(ref) match {
          case None =>
            val tmpShape = JsonSchemaParsingHelper.createTemporaryShape(shape => adopt(shape), e, ctx, fullUrl)
            // it might still be resolvable at the RAML (not JSON Schema) level
            tmpShape.unresolved(text, e).withSupportsRecursion(true)
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
    copied.unresolved(fullRef, entry)(ctx)
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

  private def promoteParsedShape(ref: String,
                                 text: String,
                                 fullRef: String,
                                 jsonSchemaShape: AnyShape): Option[AnyShape] = {
    val promotedShape = ctx.promoteExternaltoDataTypeFragment(text, fullRef, jsonSchemaShape)
    Some(
      promotedShape
        .link(AmfScalar(text), Annotations(ast) += ExternalFragmentRef(ref), Annotations.synthesized())
        .asInstanceOf[AnyShape]
        .withName(name, nameAnnotations)
        .withSupportsRecursion(true))
  }

  private def parseRemoteSchema(fullRef: String): Option[AnyShape] = {
    ctx.parseRemoteJSONPath(fullRef).map { shape =>
      ctx.registerJsonSchema(fullRef, shape)
      ctx.futureDeclarations.resolveRef(fullRef, shape)
      shape
    }
  }
}
