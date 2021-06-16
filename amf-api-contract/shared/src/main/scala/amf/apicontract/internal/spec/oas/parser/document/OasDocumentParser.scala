package amf.apicontract.internal.spec.oas.parser.document

import amf.apicontract.client.scala.model.document.{Extension, Overlay}
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.client.scala.model.domain.security.{SecurityRequirement, SecurityScheme}
import amf.apicontract.client.scala.model.domain.templates.{ResourceType, Trait}
import amf.apicontract.client.scala.model.domain.{EndPoint, Response}
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.metamodel.domain.security.SecuritySchemeModel
import amf.apicontract.internal.metamodel.domain.templates.{ResourceTypeModel, TraitModel}
import amf.apicontract.internal.spec.common.OasParameter
import amf.apicontract.internal.spec.common.parser._
import amf.apicontract.internal.spec.oas.OasLikeSecuritySchemeTypeMappings
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.apicontract.internal.spec.oas.parser.OasResponseParser
import amf.apicontract.internal.spec.oas.parser.domain.{OasLikeInformationParser, OasLikeTagsParser, OasResponseParser}
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.annotations.{DeclaredElement, LexicalInformation, SingleValueArray, SourceVendor}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.document.{BaseUnitModel, DocumentModel, ExtensionLikeModel}
import amf.core.internal.metamodel.domain.extensions.CustomDomainPropertyModel
import amf.core.internal.parser.domain.{Annotations, ArrayNode, ScalarNode, SearchScope}
import amf.core.internal.parser.{Root, YMapOps}
import amf.core.internal.utils.{AmfStrings, IdCounter}
import amf.core.internal.validation.CoreValidations.DeclarationNotFound
import amf.plugins.document.vocabularies.parser.common.DeclarationKey
import org.yaml.model.{YMapEntry, YNode, _}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Oas spec parser
  */
abstract class OasDocumentParser(root: Root)(implicit val ctx: OasWebApiContext)
    extends OasSpecParser()(WebApiShapeParserContextAdapter(ctx))
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

    val references = ReferencesParser(document, root.location, "uses".asOasExtension, map, root.references).parse()
    parseDeclarations(root, map)

    val api = parseWebApi(map).add(SourceVendor(ctx.vendor))
    document.set(DocumentModel.Encodes, api, Annotations.inferred())

    addDeclarationsToModel(document)
    if (references.nonEmpty)
      document.setWithoutId(DocumentModel.References,
                            AmfArray(references.baseUnitReferences()),
                            Annotations.synthesized())

    ctx.futureDeclarations.resolve()
    document
  }

  def parseDeclarations(root: Root, map: YMap): Unit = {
    val parent = root.location + "#/declarations"
    parseTypeDeclarations(map, parent + "/types", Some(this))
    parseAnnotationTypeDeclarations(map, parent)
    AbstractDeclarationsParser("resourceTypes".asOasExtension,
                               (entry: YMapEntry) => ResourceType(entry),
                               map,
                               parent + "/resourceTypes",
                               ResourceTypeModel,
                               this).parse()
    AbstractDeclarationsParser("traits".asOasExtension,
                               (entry: YMapEntry) => Trait(entry),
                               map,
                               parent + "/traits",
                               TraitModel,
                               this)
      .parse()
    parseSecuritySchemeDeclarations(map, parent + "/securitySchemes")
    parseParameterDeclarations(map, parent + "/parameters")
    parseResponsesDeclarations("responses", map, parent + "/responses")
  }

  protected def parseAnnotationTypeDeclarations(map: YMap, customProperties: String): Unit = {

    map.key(
      "annotationTypes".asOasExtension,
      e => {
        addDeclarationKey(DeclarationKey(e, isAbstract = true))
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
        addDeclarationKey(DeclarationKey(e))
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
        addDeclarationKey(DeclarationKey(entry))
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val typeName      = e.key
            val nameGenerator = new IdCounter()
            val oasParameter: OasParameter = e.value.to[YMap] match {
              case Right(_) =>
                ctx.factory.parameterParser(YMapEntryLike(e), parentPath, Some(typeName), nameGenerator).parse
              case _ =>
                val parameter =
                  ctx.factory.parameterParser(YMapEntryLike(e), parentPath, Some(typeName), nameGenerator).parse
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
        addDeclarationKey(DeclarationKey(entry))
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val node = ScalarNode(e.key)
            ctx.declarations += OasResponseParser(
              e.value.as[YMap], { r: Response =>
                r.withName(node).adopted(parentPath).add(DeclaredElement())
                r.annotations ++= Annotations(e)
              }
            ).parse()
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

    map.key("security".asOasExtension, entry => { parseSecurity(entry, api) }) // extension needs to go first, so normal security key lexical info will be used if present

    map.key("security", entry => { parseSecurity(entry, api) })

    val documentations: mutable.ListBuffer[(CreativeWork, YMapEntry)] = ListBuffer[(CreativeWork, YMapEntry)]()

    map.key(
      "externalDocs",
      entry => {
        documentations.append(
          (OasLikeCreativeWorkParser(entry.value, api.id)(WebApiShapeParserContextAdapter(ctx)).parse(), entry))
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
      api.fields.set(api.id,
                     WebApiModel.Documentations,
                     AmfArray(documentations.map(_._1), Annotations.virtual()),
                     Annotations.virtual())

    map.key("paths") match {
      case Some(entry) => parseEndpoints(api, entry)
      case None        => ctx.eh.violation(MandatoryPathsProperty, api.id, "'paths' is mandatory in OAS spec")
    }

    AnnotationParser(api, map)(WebApiShapeParserContextAdapter(ctx)).parse()
    AnnotationParser(api, map)(WebApiShapeParserContextAdapter(ctx)).parseOrphanNode("paths")

    ctx.closedShape(api.id, map, "webApi")

    api
  }

  def parseSecurity(entry: YMapEntry, api: WebApi): Unit = {
    val requirements: Seq[SecurityRequirement] = entry.value.tagType match {
      case YType.Seq =>
        val idCounter = new IdCounter()
        entry.value
          .as[Seq[YNode]]
          .flatMap(s =>
            OasLikeSecurityRequirementParser(s, (se: SecurityRequirement) => se.adopted(api.id), idCounter)
              .parse()) // todo when generating id for security requirements webapi id is null
      case _ =>
        ctx.eh.violation(InvalidSecurityRequirementsSeq,
                         entry.value,
                         "'security' must be an array of security requirement object")
        Nil
    }
    val extension: Seq[SecurityRequirement] = api.security
    api.set(WebApiModel.Security, AmfArray(requirements ++ extension, Annotations(entry.value)), Annotations(entry))
  }

  private def parseEndpoints(api: WebApi, entry: YMapEntry) = {
    val paths = entry.value.as[YMap]
    val endpoints =
      paths
        .regex("^/.*")
        .foldLeft(List[EndPoint]())((acc, curr) => acc ++ ctx.factory.endPointParser(curr, api.id, acc).parse())
    api.set(WebApiModel.EndPoints, AmfArray(endpoints, Annotations(entry.value)), Annotations(entry))
    ctx.closedShape(api.id, paths, "paths")
  }
}

abstract class OasSpecParser(implicit ctx: ShapeParserContext) extends WebApiBaseSpecParser with SpecParserOps {

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
      ctx
        .findAnnotation(scalar.text, SearchScope.All)
        .map { a =>
          val copied: CustomDomainProperty = a.link(AmfScalar(scalar.text), Annotations(ast), Annotations(scalar))
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
            ctx.findDocumentations(text, SearchScope.All) match {
              case Some(doc) =>
                doc.link(AmfScalar(text), Annotations(n), Annotations.synthesized()).asInstanceOf[CreativeWork]
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
