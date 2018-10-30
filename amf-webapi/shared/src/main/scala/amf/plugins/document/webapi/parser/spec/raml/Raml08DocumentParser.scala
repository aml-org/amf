package amf.plugins.document.webapi.parser.spec.raml

import amf.core.Root
import amf.core.annotations.DeclaredElement
import amf.core.model.domain.templates.AbstractDeclaration
import amf.core.parser.YMapOps
import amf.core.utils._
import amf.plugins.document.webapi.contexts.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.declaration.{
  AbstractDeclarationParser,
  Raml08TypeParser,
  SecuritySchemeParser
}
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import amf.plugins.domain.webapi.models.{Parameter, Payload}
import org.yaml.model.{YMap, YMapEntry, YType}

/**
  * Raml 0.8 spec parser
  */
case class Raml08DocumentParser(root: Root)(implicit override val ctx: RamlWebApiContext)
    extends RamlDocumentParser(root) {

  override protected def parseDeclarations(root: Root, map: YMap): Unit = {

    val parent = root.location + "#/declarations"
    parseSchemaDeclarations(map, parent + "/schemas")
    parseAbstractDeclarations(
      "resourceTypes",
      entry =>
        ResourceType(entry)
          .withName(entry.key.as[String])
          .withId(parent + s"/resourceTypes/${entry.key.as[String].urlComponentEncoded}"),
      map,
      parent + "/resourceTypes"
    )
    parseAbstractDeclarations(
      "traits",
      entry =>
        Trait(entry)
          .withName(entry.key.as[String])
          .withId(parent + s"/traits/${entry.key.as[String].urlComponentEncoded}"),
      map,
      parent + "/traits"
    )

    parseSecuritySchemeDeclarations(map, parent + "/securitySchemes")

  }

  private def parseAbstractDeclarations(key: String,
                                        producer: YMapEntry => AbstractDeclaration,
                                        map: YMap,
                                        parent: String): Unit = {

    map.key(key).foreach { entry =>
      val entries = entry.value.tagType match {
        case YType.Seq => entry.value.as[Seq[YMap]].flatMap(m => m.entries)
        case YType.Map => entry.value.as[YMap].entries
        case t =>
          ctx.violation(parent, s"Invalid node $t in abstract declaration", entry.value)
          Nil
      }
      entries.foreach { entry =>
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
            e.value.as[Seq[YMap]].foreach(map => parseEntries(map.entries, parent))
          case YType.Map  => parseEntries(e.value.as[YMap].entries, parent)
          case YType.Null =>
          case t          => ctx.violation(parent, s"Invalid type $t for 'securitySchemes' node.", e.value)
        }
      }
    )
  }

  private def parseEntries(entries: Seq[YMapEntry], parent: String): Unit = entries.foreach { entry =>
    ctx.declarations += SecuritySchemeParser(entry, (scheme, name) => scheme.withName(name).adopted(parent))
      .parse()
      .add(DeclaredElement())
  }

  private def parseSchemaDeclarations(map: YMap, parent: String): Unit = {
    map.key("schemas").foreach { e =>
      e.value.tagType match {
        case YType.Map =>
          parseSchemaEntries(e.value.as[YMap].entries, parent)
        case YType.Null =>
        case YType.Seq =>
          parseSchemaEntries(e.value.as[Seq[YMap]].flatMap(_.entries), parent)
        case t => ctx.violation(parent, s"Invalid type $t for 'types' node.", e.value)
      }
    }
  }

  private def parseSchemaEntries(entries: Seq[YMapEntry], parent: String): Unit = {
    entries.foreach { entry =>
      Raml08TypeParser(entry,
                       shape => shape.withName(entry.key).adopted(parent),
                       isAnnotation = false,
                       StringDefaultType)
        .parse() match {
        case Some(shape) =>
          ctx.declarations += shape.add(DeclaredElement())
        case None => ctx.violation(parent, s"Error parsing shape '$entry'", entry)
      }
    }
  }
}
