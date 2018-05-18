package amf.plugins.document.webapi.parser.spec.declaration

import amf.ProfileNames
import amf.core.Root
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.{AmfScalar, Shape}
import amf.core.parser.{
  Annotations,
  InferredLinkReference,
  ParsedDocument,
  ParsedReference,
  Reference,
  ReferenceFragmentPartition,
  _
}
import amf.core.resolution.stages.ReferenceResolutionStage
import amf.core.utils.Strings
import amf.plugins.document.webapi.annotations.{ExternalFragmentRef, JSONSchemaId, ParsedJSONSchema}
import amf.plugins.document.webapi.contexts.{Oas2WebApiContext, OasWebApiContext, RamlWebApiContext, WebApiContext}
import amf.plugins.document.webapi.parser.spec.domain.NodeDataNodeParser
import amf.plugins.document.webapi.parser.spec.oas.Oas2DocumentParser
import amf.plugins.document.webapi.parser.spec.raml.RamlSpecParser
import amf.plugins.document.webapi.parser.spec.toOas
import amf.plugins.domain.shapes.models.{AnyShape, SchemaShape, UnresolvedShape}
import org.yaml.model.YNode.MutRef
import org.yaml.model._
import org.yaml.parser.YamlParser
import org.yaml.render.YamlRender

import scala.collection.mutable
case class RamlJsonSchemaExpression(name: String, value: YNode, adopt: Shape => Shape, parseExample: Boolean = false)(
    override implicit val ctx: RamlWebApiContext)
    extends RamlExternalTypesParser {
  def parse(): AnyShape = {

    val origin = buildTextAndOrigin(value, "JSON", adopt)

    val parsed: AnyShape = origin.oriUrl match {
      case Some(url) =>
        val (path, extFrament) = ReferenceFragmentPartition(url)
        val fragment           = extFrament.map(_.stripPrefix("/definitions/"))
        fragment
          .flatMap(ctx.declarations.findInExternalsLibs(path, _))
          .orElse(ctx.declarations.findInExternals(path)) match {
          case Some(s) =>
            val shape = s.copyShape().withName(name)
            ctx.declarations.fragments
              .get(path)
              .foreach(e => shape.withReference(e.id + extFrament.getOrElse("")))
            shape
          case _ if fragment.isDefined => // oas lib
            RamlExternalOasLibParser(ctx, origin.text, origin.valueAST, path).parse()
            val shape = ctx.declarations.findInExternalsLibs(path, fragment.get) match {
              case Some(s) =>
                s.copyShape().withName(name)
              case _ =>
                ctx.violation(s"could not find json schema fragment ${extFrament.get} in file $path", origin.valueAST)
                UnresolvedShape(url)
            }
            ctx.declarations.fragments
              .get(path)
              .foreach(e => shape.withReference(e.id + extFrament.get))

            shape.annotations += ExternalFragmentRef(extFrament.get)
            shape
          case _ =>
            val shape = parseJsonShape(origin.text, name, origin.valueAST, adopt, value)
            ctx.declarations.fragments
              .get(path)
              .foreach(e => shape.withReference(e.id))
            ctx.declarations.registerExternalRef(path, shape)
            shape
        }
      case None =>
        val shape = parseJsonShape(origin.text, name, origin.valueAST, adopt, value)
        shape.annotations += ParsedJSONSchema(origin.text)
        shape
    }

    // parsing the potential example
    if (parseExample && value.tagType == YType.Map) {
      val map = value.as[YMap]

      map.key("displayName", (ShapeModel.DisplayName in parsed).allowingAnnotations)
      map.key("description", (ShapeModel.Description in parsed).allowingAnnotations)
      map.key(
        "default",
        entry => {
          val dataNodeResult = NodeDataNodeParser(entry.value, parsed.id, quiet = false).parse()
          val str            = YamlRender.render(entry.value)
          parsed.set(ShapeModel.DefaultValueString, AmfScalar(str), Annotations(entry))
          dataNodeResult.dataNode.foreach { dataNode =>
            parsed.set(ShapeModel.Default, dataNode, Annotations(entry))
          }
        }
      )
      parseExamples(parsed, value.as[YMap])
    }
    parsed

  }

  case class RamlExternalOasLibParser(ctx: RamlWebApiContext, text: String, valueAST: YNode, path: String) {
    def parse(): Unit = {
      val url       = path.normalizeUrl + (if (!path.endsWith("/")) "/" else "") // alwarys add / to avoid ask if there is any one before add #
      val schemaAst = YamlParser(text)(ctx).withIncludeTag("!include").parse(keepTokens = true)
      val schemaEntry = schemaAst.collectFirst({ case d: YDocument => d }) match {
        case Some(d) => d
        case _       =>
          // TODO get parent id
          ctx.violation("invalid json schema expression", valueAST)
          YDocument(YMap.empty)
      }
      val context = new Oas2WebApiContext(url, Nil, ctx, None)
      context.localJSONSchemaContext = Some(schemaEntry.node)

      Oas2DocumentParser(
        Root(ParsedDocument(None, schemaEntry), url, "application/json", Nil, InferredLinkReference, "OAS", text))(
        context)
        .parseTypeDeclarations(schemaEntry.node.as[YMap], url + "#/definitions/")
      val libraryShapes = context.declarations.shapes
      val resolvedShapes = new ReferenceResolutionStage(ProfileNames.RAML, false)
        .resolveDomainElementSet[Shape](libraryShapes.values.toSeq)

      val shapesMap = mutable.Map[String, AnyShape]()
      resolvedShapes.map(s => (s, s.annotations.find(classOf[JSONSchemaId]))).foreach {
        case (s: AnyShape, Some(a)) if a.id.equals(s.name.value()) =>
          shapesMap += s.name.value -> s
        case (s: AnyShape, Some(a)) =>
          shapesMap += s.name.value() -> s
          shapesMap += a.id           -> s
        case (s: AnyShape, None) => shapesMap += s.name.value -> s
      }

      ctx.declarations.registerExternalLib(path, shapesMap.toMap)
    }
  }

  private def parseJsonShape(text: String, name: String, valueAST: YNode, adopt: Shape => Shape, value: YNode) = {
    val schemaAst = YamlParser(text)(ctx).withIncludeTag("!include").parse(keepTokens = true)
    val schemaEntry = schemaAst.head match {
      case d: YDocument => YMapEntry(name, d.node)
      case _            =>
        // TODO get parent id
        ctx.violation("invalid json schema expression", valueAST)
        YMapEntry(name, YNode.Null)
    }

    // we set the local schema entry to be able to resolve local $refs
    ctx.localJSONSchemaContext = Some(schemaEntry.value)

    val s =
      OasTypeParser(schemaEntry, shape => adopt(shape), JSONSchemaVersion)(toSchemaContext(ctx, valueAST))
        .parse() match {
        case Some(sh) => sh
        case None =>
          val shape = SchemaShape()
          adopt(shape)
          ctx.violation(shape.id, "Cannot parse JSON Schema", value)
          shape
      }
    ctx.localJSONSchemaContext = None // we reset the JSON schema context after parsing
    s
  }

  protected def toSchemaContext(ctx: WebApiContext, ast: YNode): OasWebApiContext = {
    ast match {
      case inlined: MutRef =>
        if (inlined.origTag.tagType == YType.Include) {
          // JSON schema file we need to update the context
          val fileHint = inlined.origValue.asInstanceOf[YScalar].text.split("/").last.split("#").head
          ctx.refs.filter(r => Option(r.unit.location).isDefined).find(_.unit.location.endsWith(fileHint)) match {
            case Some(ref) =>
              toOas(ref.unit.location,
                    ref.unit.references.map(r => ParsedReference(r, Reference(ref.unit.location, Nil), None)),
                    ctx)
            case _ => toOas(ctx)
          }
        } else {
          // Inlined we don't need to update the context for ths JSON schema file
          toOas(ctx)
        }
      case _ =>
        toOas(ctx)
    }
  }

}

case class RamlXmlSchemaExpression(name: String, value: YNode, adopt: Shape => Shape, parseExample: Boolean = false)(
    override implicit val ctx: RamlWebApiContext)
    extends RamlExternalTypesParser {
  def parse(): AnyShape = {
    val origin = buildTextAndOrigin(value, "XML", adopt)

    val (maybeReferenceId, maybeFragmentLabel): (Option[String], Option[String]) = origin.oriUrl
      .map(ReferenceFragmentPartition.apply) match {
      case Some((path, uri)) =>
        (ctx.declarations.fragments
           .get(path)
           .map(_.id + uri.map(u => if (u.startsWith("/")) u else "/" + u).getOrElse("")),
         uri)
      case None => (None, None)
    }

    val parsed = value.tagType match {
      case YType.Map =>
        val map = value.as[YMap]
        val parsedSchema = typeOrSchema(map) match {
          case Some(typeEntry: YMapEntry) if typeEntry.value.toOption[YScalar].isDefined =>
            val shape =
              SchemaShape().withRaw(typeEntry.value.as[YScalar].text).withMediaType("application/xml")
            shape.withName(name)
            adopt(shape)
            shape
          case _ =>
            val shape = SchemaShape()
            adopt(shape)
            ctx.violation(shape.id, "Cannot parse XML Schema expression out of a non string value", value)
            shape
        }
        map.key("displayName", (ShapeModel.DisplayName in parsedSchema).allowingAnnotations)
        map.key("description", (ShapeModel.Description in parsedSchema).allowingAnnotations)
        map.key(
          "default",
          entry => {
            val dataNodeResult = NodeDataNodeParser(entry.value, parsedSchema.id, quiet = false).parse()
            val str            = YamlRender.render(entry.value)
            parsedSchema.set(ShapeModel.DefaultValueString, AmfScalar(str), Annotations(entry))
            dataNodeResult.dataNode.foreach { dataNode =>
              parsedSchema.set(ShapeModel.Default, dataNode, Annotations(entry))
            }
          }
        )
        parseExamples(parsedSchema, value.as[YMap])

        parsedSchema
      case YType.Seq =>
        val shape = SchemaShape()
        adopt(shape)
        ctx.violation(shape.id, "Cannot parse XML Schema expression out of a non string value", value)
        shape
      case _ =>
        val raw   = value.as[YScalar].text
        val shape = SchemaShape().withRaw(raw).withMediaType("application/xml")
        shape.withName(name)
        adopt(shape)
        shape
    }
    maybeReferenceId.foreach { parsed.withReference }
    maybeFragmentLabel.foreach { parsed.annotations += ExternalFragmentRef(_) }
    parsed
  }
}

trait RamlExternalTypesParser extends RamlSpecParser with ExampleParser with RamlTypeSyntax {

  protected def getOrigi(node: YNode): Option[String] = node match {
    case ref: MutRef => Some(ref.origValue.toString)
    case _           => None
  }

  protected case class ValueAndOrigin(text: String, valueAST: YNode, oriUrl: Option[String])

  protected def buildTextAndOrigin(value: YNode, mode: String, adopt: Shape => Shape): ValueAndOrigin = {
    value.tagType match {
      case YType.Map =>
        val map = value.as[YMap]
        typeOrSchema(map) match {
          case Some(typeEntry: YMapEntry) if typeEntry.value.toOption[YScalar].isDefined =>
            ValueAndOrigin(typeEntry.value.as[YScalar].text, typeEntry.value, getOrigi(typeEntry.value))
          case _ =>
            val shape = SchemaShape()
            adopt(shape)
            ctx.violation(shape.id, s"Cannot parse $mode Schema expression out of a non string value", value)
            ValueAndOrigin("", value, None)
        }
      case YType.Seq =>
        val shape = SchemaShape()
        adopt(shape)
        ctx.violation(shape.id, s"Cannot parse $mode Schema expression out of a non string value", value)
        ValueAndOrigin("", value, None)
      case _ => ValueAndOrigin(value.as[YScalar].text, value, getOrigi(value))
    }
  }
}
