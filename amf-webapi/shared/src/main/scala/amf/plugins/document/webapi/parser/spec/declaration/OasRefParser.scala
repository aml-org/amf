package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.annotations.ExternalFragmentRef
import amf.core.metamodel.domain.LinkableElementModel
import amf.core.model.domain.{AmfScalar, Linkable, Shape}
import amf.core.parser._
import amf.core.utils.UriUtils
import amf.plugins.document.webapi.annotations.ExternalJsonSchemaShape
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.contexts.parser.async.Async20WebApiContext
import amf.plugins.document.webapi.contexts.parser.oas.{Oas2WebApiContext, Oas3WebApiContext}
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.declaration.utils.JsonSchemaParsingHelper
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
                   version: SchemaVersion)(implicit val ctx: OasLikeWebApiContext) {

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
    val ref: String       = e.value
    val strippedPrefixRef = OasDefinitions.stripDefinitionsPrefix(ref)

    ctx.declarations
      .findType(strippedPrefixRef, SearchScope.All) match { // normal declaration to be used from raml or oas
      case Some(s) => createLinkToDeclaration(strippedPrefixRef, s)
      case _       => // Only enabled for JSON Schema, not OAS. In OAS local references can only point to the #/definitions (#/components in OAS 3) node
        // now we work with canonical JSON schema pointers, not local refs
        val referencedShape = ctx.findLocalJSONPath(ref) match {
          case Some(_) => searchLocalJsonSchema(ref, if (ctx.linkTypes) strippedPrefixRef else ref, e)
          case _       => searchRemoteJsonSchema(ref, if (ctx.linkTypes) strippedPrefixRef else ref, e)
        }
        referencedShape.foreach(safeAdoption)
        referencedShape
    }
  }

  private def createLinkToDeclaration(label: String, s: AnyShape) = {
    val link =
      s.link(AmfScalar(label), Annotations(ast), Annotations.synthesized())
        .asInstanceOf[AnyShape]
        .withName(name, nameAnnotations)
        .withSupportsRecursion(true)
    adopt(link)
    Some(link)
  }

  private def searchLocalJsonSchema(r: String, t: String, e: YMapEntry): Option[AnyShape] = {
    val (ref, text) =
      if (ctx.linkTypes) (r, t)
      else {
        val fullref = UriUtils.resolveRelativeTo(ctx.rootContextDocument, r)
        (fullref, fullref)
      }
    ctx.findJsonSchema(ref) match {
      case Some(s) =>
        val annots = Annotations(ast)
        val copied =
          s.link(AmfScalar(ref), annots, Annotations.synthesized())
            .asInstanceOf[AnyShape]
            .withName(name, Annotations())
            .withSupportsRecursion(true)
        Some(copied)
      // Local reference
      case None if isOasLikeContext && isDeclaration(ref) && ctx.isMainFileContext =>
        val shape = AnyShape(ast).withName(name, nameAnnotations)
        val tmpShape = UnresolvedShape(Fields(),
                                       Annotations(map),
                                       ref,
                                       None,
                                       Some((k: String) => shape.set(LinkableElementModel.TargetId, k)),
                                       shouldLink = false).withName(text, Annotations()).withSupportsRecursion(true)
        tmpShape.unresolved(text, e, "warning")(ctx)
        tmpShape.withContext(ctx)
        adopt(tmpShape)
        shape.withLinkTarget(tmpShape).withLinkLabel(text)
        adopt(shape)
        Some(shape)
      case None =>
        val tmpShape: UnresolvedShape = createAndRegisterUnresolvedShape(e, ref, text)
        ctx.registerJsonSchema(ref, tmpShape)
        ctx.findLocalJSONPath(r) match {
          case Some(entryLike) =>
            val definitiveName = entryLike.key.map(_.as[String]) getOrElse (name)
            OasTypeParser(entryLike, definitiveName, adopt, version)
              .parse()
              .map { shape =>
                ctx.futureDeclarations.resolveRef(text, shape)
                ctx.registerJsonSchema(ref, shape)
                if (ctx.linkTypes || ref.equals("#")) {
                  val link =
                    shape.link(AmfScalar(text), Annotations(ast), Annotations.synthesized()).asInstanceOf[AnyShape]
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
  }

  private def createAndRegisterUnresolvedShape(e: YMapEntry, ref: String, text: String) = {
    val tmpShape = UnresolvedShape(ref, map).withName(text, Annotations()).withSupportsRecursion(true)
    tmpShape.unresolved(text, e, "warning")(ctx)
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

  private def isOasLikeContext = ctx match {
    case _ @(_: OasLikeWebApiContext) => true
    case _                            => false
  }

  private val oas2DeclarationRegex = "^(\\#\\/definitions\\/){1}([^/\\n])+$"
  private val oas3DeclarationRegex =
    "^(\\#\\/components\\/){1}((schemas|parameters|securitySchemes|requestBodies|responses|headers|examples|links|callbacks){1}\\/){1}([^/\\n])+"

  private def isDeclaration(ref: String): Boolean =
    ctx match {
      case _: Oas2WebApiContext if ref.matches(oas2DeclarationRegex)                                => true
      case _ @(_: Oas3WebApiContext | _: Async20WebApiContext) if ref.matches(oas3DeclarationRegex) => true
      case _                                                                                        => false
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
            tmpShape.unresolved(text, e, "warning").withSupportsRecursion(true)
            Some(tmpShape)
          case Some(jsonSchemaShape) =>
            jsonSchemaShape.annotations.+=(ExternalJsonSchemaShape(e))
            if (ctx.declarations.fragments.contains(text)) {
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
    copied.unresolved(fullRef, entry, "warning")(ctx)
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
    val promotedShape = ctx.declarations.promoteExternaltoDataTypeFragment(text, fullRef, jsonSchemaShape)
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
