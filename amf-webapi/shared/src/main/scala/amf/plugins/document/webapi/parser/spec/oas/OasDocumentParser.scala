package amf.plugins.document.webapi.parser.spec.oas

import amf.core.Root
import amf.core.annotations._
import amf.core.metamodel.Field
import amf.core.metamodel.document.{BaseUnitModel, ExtensionLikeModel}
import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.CustomDomainPropertyModel
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{Annotations, _}
import amf.core.utils.{AmfStrings, IdCounter}
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.model.{Extension, Overlay}
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps, WebApiBaseSpecParser}
import amf.plugins.document.webapi.parser.spec.declaration.{AbstractDeclarationsParser, OasTypeParser, _}
import amf.plugins.document.webapi.parser.spec.domain
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.vocabulary.VocabularyMappings
import amf.plugins.domain.shapes.models.ExampleTracking.tracking
import amf.plugins.domain.shapes.models.{CreativeWork, NodeShape}
import amf.plugins.domain.webapi.metamodel._
import amf.plugins.domain.webapi.metamodel.security.SecuritySchemeModel
import amf.plugins.domain.webapi.models._
import amf.plugins.domain.webapi.models.security.SecurityScheme
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import amf.plugins.features.validation.CoreValidations.DeclarationNotFound
import amf.validations.ParserSideValidations._
import org.yaml.model.{YMapEntry, YNode, _}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Oas spec parser
  */
abstract class OasDocumentParser(root: Root)(implicit val ctx: OasWebApiContext)
    extends OasSpecParser
    with OasLikeDeclarationsHelper {

  def parseExtension(): Extension = {
    val extension = parseDocument(Extension())

    parseExtension(extension, ExtensionLikeModel.Extends)

    extension
  }

  private def parseExtension(document: Document, field: Field): Unit = {
    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]
    UsageParser(map, document).parse()

    map
      .key("extends".asOasExtension)
      .foreach(e => {
        ctx.link(e.value) match {
          case Left(url) =>
            root.references
              .find(_.origin.url == url)
              .foreach(extend =>
                document
                  .set(field, AmfScalar(extend.unit.id, Annotations(e.value)), Annotations(e)))
          case _ =>
        }
      })
  }

  def parseOverlay(): Overlay = {
    val overlay = parseDocument(Overlay())

    parseExtension(overlay, ExtensionLikeModel.Extends)

    overlay
  }

  def parseDocument(): Document = parseDocument(Document())

  private def parseDocument[T <: Document](document: T): T = {
    document.adopted(root.location).withLocation(root.location)

    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]
    ctx.setJsonSchemaAST(map)

    val references = ReferencesParser(document, "uses".asOasExtension, map, root.references).parse(root.location)
    parseDeclarations(root, map)

    val api = parseWebApi(map).add(SourceVendor(ctx.vendor))
    document
      .withEncodes(api)
      .adopted(root.location)

    val declarable = ctx.declarations.declarables()
    if (declarable.nonEmpty) document.withDeclares(declarable)
    if (references.references.nonEmpty) document.withReferences(references.solvedReferences())

    ctx.futureDeclarations.resolve()
    document
  }

  def parseDeclarations(root: Root, map: YMap): Unit = {
    val parent = root.location + "#/declarations"
    parseTypeDeclarations(map, parent + "/types")
    parseAnnotationTypeDeclarations(map, parent)
    AbstractDeclarationsParser("resourceTypes".asOasExtension,
                               (entry: YMapEntry) => ResourceType(entry),
                               map,
                               parent + "/resourceTypes").parse()
    AbstractDeclarationsParser("traits".asOasExtension, (entry: YMapEntry) => Trait(entry), map, parent + "/traits")
      .parse()
    parseSecuritySchemeDeclarations(map, parent + "/securitySchemes")
    parseParameterDeclarations(map, parent + "/parameters")
    parseResponsesDeclarations("responses", map, parent + "/responses")
  }

  protected def parseAnnotationTypeDeclarations(map: YMap, customProperties: String): Unit = {

    map.key(
      "annotationTypes".asOasExtension,
      e => {
        e.value
          .as[YMap]
          .entries
          .map(entry => {
            val typeName = entry.key.as[YScalar].text
            val customProperty = AnnotationTypesParser(entry,
                                                       customProperty =>
                                                         customProperty
                                                           .withName(typeName)
                                                           .adopted(customProperties))
            ctx.declarations += customProperty.add(DeclaredElement())
          })
      }
    )
  }

  protected val definitionsKey: String
  protected val securityKey: String

  protected def parseSecuritySchemeDeclarations(map: YMap, parent: String): Unit = {
    parseSecuritySchemeDeclarationsFromKey(securityKey, map, parent)
    parseSecuritySchemeDeclarationsFromKey("securitySchemes".asOasExtension, map, parent)
  }

  protected def parseSecuritySchemeDeclarationsFromKey(key: String, map: YMap, parent: String): Unit = {

    def validateSchemeType(scheme: SecurityScheme): Unit = {
      val schemeType = scheme.`type`
      if (schemeType.nonEmpty && !OasLikeSecuritySchemeTypeMappings
            .validTypesFor(ctx.vendor)
            .contains(schemeType.value()))
        ctx.eh.violation(
          InvalidSecuritySchemeType,
          scheme.id,
          Some(SecuritySchemeModel.Type.value.iri()),
          s"'$schemeType' is not a valid security scheme type in ${ctx.vendor.name}",
          scheme.`type`.annotations().find(classOf[LexicalInformation]),
          Some(ctx.rootContextDocument)
        )
    }

    val isExtension = key.startsWith("x-amf-")

    map.key(
      key,
      e => {
        e.value.as[YMap].entries.foreach { entry =>
          val securityScheme: SecurityScheme = ctx.factory
            .securitySchemeParser(
              entry,
              (scheme) => {
                val name = entry.key.as[String]
                scheme.set(SecuritySchemeModel.Name,
                           AmfScalar(name, Annotations(entry.key.value)),
                           Annotations(entry.key))
                scheme.adopted(parent)
              }
            )
            .parse()
            .add(DeclaredElement())
          if (!isExtension) validateSchemeType(securityScheme)
          ctx.declarations += securityScheme
        }
      }
    )
  }

  protected def parseParameterDeclarations(map: YMap, parentPath: String): Unit = {
    map.key(
      "parameters",
      entry => {
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val typeName      = e.key
            val nameGenerator = new IdCounter()
            val oasParameter: domain.OasParameter = e.value.to[YMap] match {
              case Right(_) => ctx.factory.parameterParser(Left(e), parentPath, Some(typeName), nameGenerator).parse
              case _ =>
                val parameter =
                  ctx.factory.parameterParser(Right(YMap.empty), parentPath, Some(typeName), nameGenerator).parse
                ctx.eh.violation(InvalidParameterType,
                                 parameter.domainElement.id,
                                 "Map needed to parse a parameter declaration",
                                 e)
                parameter
            }
            ctx.declarations.registerOasParameter(oasParameter)

          })
      }
    )
  }

  protected def parseResponsesDeclarations(key: String, map: YMap, parentPath: String): Unit = {
    map.key(
      key,
      entry => {
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val node = ScalarNode(e.key).text()
            ctx.declarations += OasResponseParser(e.value.as[YMap], { r: Response =>
              r.set(ResponseModel.Name, node).adopted(parentPath).add(DeclaredElement())
              r.annotations ++= Annotations(e)
            }).parse()
          })
      }
    )
  }

  def parseWebApi(map: YMap): WebApi = {

    val api = WebApi(root.parsed.asInstanceOf[SyamlParsedDocument].document.node).adopted(root.location)

    map.key("info", entry => OasLikeInformationParser(entry, api, ctx).parse())

    ctx.factory.serversParser(map, api).parse()

    map.key("tags", entry => {
      val tags = OasLikeTagsParser(api.id, entry).parse()
      api.set(WebApiModel.Tags, AmfArray(tags, Annotations(entry.value)), Annotations(entry))
    })

    map.key("security", entry => {
      api.set(WebApiModel.Security, AmfArray(Seq(), Annotations(entry.value)), Annotations(entry))
      parseSecurity(entry, api)
    })
    map.key("security".asOasExtension, entry => { parseSecurity(entry, api) })

    val documentations: mutable.ListBuffer[(CreativeWork, YMapEntry)] = ListBuffer[(CreativeWork, YMapEntry)]()

    map.key(
      "externalDocs",
      entry => {
        documentations.append((OasLikeCreativeWorkParser(entry.value, api.id).parse(), entry))
      }
    )

    map.key(
      "userDocumentation".asOasExtension,
      entry => {
        documentations.appendAll(
          UserDocumentationParser(entry.value.as[Seq[YNode]])
            .parse()
            .map(c => (c, entry)))
      }
    )

    if (documentations.nonEmpty)
      api.setArray(WebApiModel.Documentations, documentations.map(_._1), Annotations(documentations.map(_._2).head))

    map.key("paths") match {
      case Some(entry) => parseEndpoints(api, entry)
      case None        => ctx.eh.violation(MandatoryPathsProperty, api.id, "'paths' is mandatory in OAS spec")
    }

    AnnotationParser(api, map).parse()
    AnnotationParser(api, map).parseOrphanNode("paths")

    ctx.closedShape(api.id, map, "webApi")

    api
  }

  def parseSecurity(entry: YMapEntry, api: WebApi) = {
    entry.value.tagType match {
      case YType.Seq =>
        val idCounter = new IdCounter()
        val securedBy =
          entry.value
            .as[Seq[YNode]]
            .map(s => OasLikeSecurityRequirementParser(s, api.withSecurity, idCounter).parse()) // todo when generating id for security requirements webapi id is null
            .collect { case Some(s) => s }
      //api.set(WebApiModel.Security, AmfArray(securedBy, Annotations(entry.value)))
      case _ =>
        ctx.eh.violation(InvalidSecurityRequirementsSeq,
                         entry.value,
                         "'security' must be an array of security requirement object")
    }
  }

  private def parseEndpoints(api: WebApi, entry: YMapEntry) = {
    val paths = entry.value.as[YMap]
    val endpoints =
      paths
        .regex("^/.*")
        .foldLeft(List[EndPoint]())((acc, curr) =>
          acc ++ ctx.factory.endPointParser(curr, api.withEndPoint, acc).parse())
    if (endpoints.nonEmpty) api.set(WebApiModel.EndPoints, AmfArray(endpoints), Annotations(entry.value))
    ctx.closedShape(api.id, paths, "paths")
  }
}

abstract class OasSpecParser(implicit ctx: OasLikeWebApiContext) extends WebApiBaseSpecParser with SpecParserOps {

  case class UsageParser(map: YMap, baseUnit: BaseUnit) {
    def parse(): Unit = {
      map.key("usage".asOasExtension, entry => {
        val value = ScalarNode(entry.value)
        baseUnit.set(BaseUnitModel.Usage, value.string(), Annotations(entry))
      })
    }
  }

  object AnnotationTypesParser {
    def apply(ast: YMapEntry, adopt: CustomDomainProperty => Unit): CustomDomainProperty =
      ast.value.tagType match {
        case YType.Map =>
          ast.value.as[YMap].key("$ref") match {
            case Some(reference) =>
              LinkedAnnotationTypeParser(ast, reference.value.as[YScalar].text, reference.value.as[YScalar], adopt)
                .parse()
            case _ => AnnotationTypesParser(ast, ast.key.as[YScalar].text, ast.value.as[YMap], adopt).parse()
          }
        case YType.Seq =>
          val customDomainProperty = CustomDomainProperty().withName(ast.key.as[YScalar].text)
          adopt(customDomainProperty)
          ctx.eh.violation(
            InvalidAnnotationType,
            customDomainProperty.id,
            "Invalid value node type for annotation types parser, expected map or scalar reference",
            ast.value
          )
          customDomainProperty
        case _ =>
          LinkedAnnotationTypeParser(ast, ast.key.as[YScalar].text, ast.value.as[YScalar], adopt).parse()
      }

  }

  case class LinkedAnnotationTypeParser(ast: YPart,
                                        annotationName: String,
                                        scalar: YScalar,
                                        adopt: CustomDomainProperty => Unit) {
    def parse(): CustomDomainProperty = {
      ctx.declarations
        .findAnnotation(scalar.text, SearchScope.All)
        .map { a =>
          val copied: CustomDomainProperty = a.link(scalar.text, Annotations(ast))
          copied.id = null // we reset the ID so ti can be adopted, there's an extra rule where the id is not set
          // because the way they are inserted in the mode later in the parsing
          adopt(copied.withName(annotationName))
          copied
        }
        .getOrElse {
          val customDomainProperty = CustomDomainProperty().withName(annotationName)
          adopt(customDomainProperty)
          ctx.eh.violation(DeclarationNotFound,
                           customDomainProperty.id,
                           "Could not find declared annotation link in references",
                           scalar)
          customDomainProperty
        }
    }
  }

  case class AnnotationTypesParser(ast: YPart, annotationName: String, map: YMap, adopt: CustomDomainProperty => Unit) {
    def parse(): CustomDomainProperty = {
      val custom = CustomDomainProperty(ast)
      custom.withName(annotationName)
      adopt(custom)

      map.key(
        "allowedTargets",
        entry => {
          val annotations = Annotations(entry)
          val targets: AmfArray = entry.value.value match {
            case _: YScalar =>
              annotations += SingleValueArray()
              AmfArray(Seq(ScalarNode(entry.value).text()))
            case sequence: YSequence =>
              ArrayNode(sequence).text()
          }

          val targetUris = targets.values.map({
            case s: AmfScalar =>
              VocabularyMappings.ramlToUri.get(s.toString) match {
                case Some(uri) => AmfScalar(uri, s.annotations)
                case None      => s
              }
            case nodeType => AmfScalar(nodeType.toString, nodeType.annotations)
          })

          custom.set(CustomDomainPropertyModel.Domain, AmfArray(targetUris), annotations)
        }
      )

      map.key("displayName", entry => {
        val value = ScalarNode(entry.value)
        custom.set(CustomDomainPropertyModel.DisplayName, value.string(), Annotations(entry))
      })

      map.key("description", entry => {
        val value = ScalarNode(entry.value)
        custom.set(CustomDomainPropertyModel.Description, value.string(), Annotations(entry))
      })

      map.key(
        "schema",
        entry => {
          OasTypeParser(entry, shape => shape.adopted(custom.id))
            .parse()
            .foreach({ shape =>
              tracking(shape, custom.id)
              custom.set(CustomDomainPropertyModel.Schema, shape, Annotations(entry))
            })
        }
      )

      AnnotationParser(custom, map).parse()

      custom
    }
  }

  case class UserDocumentationParser(seq: Seq[YNode]) {
    def parse(): Seq[CreativeWork] =
      seq.map(n =>
        n.tagType match {
          case YType.Map => RamlCreativeWorkParser(n).parse()
          case YType.Str =>
            val text = n.as[YScalar].text
            ctx.declarations.findDocumentations(text, SearchScope.All) match {
              case Some(doc) => doc.link(text, Annotations(n)).asInstanceOf[CreativeWork]
              case _ =>
                val documentation = RamlCreativeWorkParser(YNode(YMap.empty)).parse()
                ctx.eh.violation(DeclarationNotFound,
                                 documentation.id,
                                 s"not supported scalar $n.text for documentation item",
                                 n)
                documentation
            }
      })
  }
}
