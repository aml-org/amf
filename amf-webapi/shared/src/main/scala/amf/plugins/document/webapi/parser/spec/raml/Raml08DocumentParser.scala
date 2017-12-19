package amf.plugins.document.webapi.parser.spec.raml

import amf.core.Root
import amf.core.annotations.SourceVendor
import amf.core.model.document.Document
import amf.core.model.domain.templates.AbstractDeclaration
import amf.core.parser.YMapOps
import amf.plugins.document.webapi.annotations.DeclaredElement
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.declaration.{
  AbstractDeclarationParser,
  Raml08TypeParser,
  SecuritySchemeParser
}
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.domain.webapi.models.{EndPoint, Parameter, Payload}
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import org.yaml.model.{YMap, YMapEntry, YScalar, YType}

import scala.collection.mutable.ListBuffer

/**
  * Raml 0.8 spec parser
  */
case class Raml08DocumentParser(root: Root)(implicit override val ctx: WebApiContext)
    extends RamlDocumentParser(root) {

  override protected def parseDeclarations(root: Root, map: YMap): Unit = {

    val parent = root.location + "#/declarations"
    parseSchemaDeclarations(map, parent)
//    parseAnnotationTypeDeclarations(map, parent)
    parseAbstractDeclarations("resourceTypes", entry => ResourceType(entry), map, parent)
    parseAbstractDeclarations("traits", entry => Trait(entry), map, parent)

    parseSecuritySchemeDeclarations(map, parent)
//    parseParameterDeclarations("(parameters)", map, root.location + "#/parameters")

  }

  private def parseAbstractDeclarations(key: String,
                                        producer: YMapEntry => AbstractDeclaration,
                                        map: YMap,
                                        parent: String) = {
    map.key(key).foreach { entry =>
      entry.value.as[Seq[YMap]].flatMap(m => m.entries).foreach { entry =>
        ctx.declarations += AbstractDeclarationParser(producer(entry), parent, entry).parse()
      }
    }
  }

  override protected def parseSecuritySchemeDeclarations(map: YMap, parent: String): Unit = {
    map.key(
      "securitySchemes",
      e => {
        e.value.tagType match {
          case YType.Seq =>
            e.value
              .as[Seq[YMap]]
              .foreach(m => {
                m.entries.foreach { entry =>
                  ctx.declarations += SecuritySchemeParser(entry, scheme => scheme.withName(entry.key).adopted(parent))
                    .parse()
                    .add(DeclaredElement())
                }
              })
          case YType.Null =>
          case t          => ctx.violation(parent, s"Invalid type $t for 'securitySchemes' node.", e.value)
        }
      }
    )
  }

  private def parseSchemaDeclarations(map: YMap, parent: String): Unit = {
    map.key("schemas").foreach { e =>
      e.value.tagType match {
        case YType.Map =>
          e.value.as[YMap].entries.foreach { entry =>
            Raml08TypeParser(entry,
                             entry.key.as[YScalar].toString(),
                             entry.value,
                             shape => shape.withName(entry.key).adopted(parent))
              .parse() match {
              case Some(shape) =>
                ctx.declarations += shape.add(DeclaredElement())
              case None => ctx.violation(parent, s"Error parsing shape '$entry'", entry)
            }
          }
        case YType.Null =>
        case YType.Seq =>
          e.value.as[Seq[YMap]].foreach(m => parseSchemaDeclarations(m, parent))
        case t => ctx.violation(parent, s"Invalid type $t for 'types' node.", e.value)
      }
    }
  }

  override def parseDocument[T <: Document](document: T): T = {
    document.adopted(root.location).withLocation(root.location)

    val map = root.parsed.document.as[YMap]

    parseDeclarations(root, map)

    val api = parseWebApi(map).add(SourceVendor(root.vendor))
    document.withEncodes(api)

    val declarables = ctx.declarations.declarables()
    if (declarables.nonEmpty) document.withDeclares(declarables)

    ctx.futureDeclarations.resolve()

    document
  }

  override protected def endpointParser
    : (YMapEntry, (String) => EndPoint, Option[EndPoint], ListBuffer[EndPoint], Boolean) => RamlEndpointParser =
    Raml08EndpointParser.apply

  override protected def parametersParser: (YMap, (String) => Parameter) => RamlParametersParser =
    Raml08ParametersParser.apply

  override def parseParameterDeclarations(key: String, map: YMap, parentPath: String): Unit = {
    map.key(
      key,
      entry => {
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val parameter =
              Raml08ParameterParser(e, (name) => Parameter().withId(parentPath + "/" + name).withName(name)).parse()
            if (Option(parameter.binding).isEmpty) {
              ctx.violation(parameter.id, "Missing binding information in declared parameter", entry.value)
            }
            ctx.declarations.registerParameter(parameter.add(DeclaredElement()),
                                               Payload().withSchema(parameter.schema))
          })
      }
    )
  }
}
