package amf.plugins.document.webapi.parser.spec.raml

import amf.core.Root
import amf.core.annotations.DeclaredElement
import amf.core.model.domain.templates.AbstractDeclaration
import amf.core.parser.YMapOps
import amf.core.remote.Context
import amf.core.unsafe.PlatformSecrets
import amf.core.utils._
import amf.plugins.document.webapi.annotations.DeclarationKey
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.parser.RamlTypeDefMatcher
import amf.plugins.document.webapi.parser.spec.declaration.{AbstractDeclarationParser, Raml08TypeParser, _}
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import amf.validations.ParserSideValidations.{
  InvalidAbstractDeclarationType,
  InvalidSecuredByType,
  InvalidTypeDefinition,
  InvalidTypesType
}
import org.yaml.model.{YMap, YMapEntry, YScalar, YType}

/**
  * Raml 0.8 spec parser
  */
case class Raml08DocumentParser(root: Root)(implicit override val ctx: RamlWebApiContext)
    extends RamlDocumentParser(root)
    with PlatformSecrets {

  override protected def parseDeclarations(root: Root, map: YMap): Unit = {

    val parent = root.location + "#/declarations"
    parseSchemaDeclarations(map, parent + "/schemas")
    parseAbstractDeclarations(
      "resourceTypes",
      entry => {
        ResourceType(entry)
          .withName(entry.key.as[YScalar].text)
          .withId(parent + s"/resourceTypes/${entry.key.as[YScalar].text.urlComponentEncoded}")
      },
      map,
      parent + "/resourceTypes"
    )
    parseAbstractDeclarations(
      "traits",
      entry => {
        Trait(entry)
          .withName(entry.key.as[YScalar].text)
          .withId(parent + s"/traits/${entry.key.as[YScalar].text.urlComponentEncoded}")
      },
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
      {
        addDeclarationKey(DeclarationKey(entry))
        val entries = entry.value.tagType match {
          case YType.Seq => entry.value.as[Seq[YMap]].flatMap(m => m.entries)
          case YType.Map => entry.value.as[YMap].entries
          case t =>
            ctx.eh
              .violation(InvalidAbstractDeclarationType,
                         parent,
                         s"Invalid node $t in abstract declaration",
                         entry.value)
            Nil
        }
        entries.foreach { entry =>
          ctx.declarations += AbstractDeclarationParser(producer(entry), parent, entry).parse()
        }
      }
    }
  }

  override protected def parseSecuritySchemeDeclarations(map: YMap, parent: String): Unit = {
    map.key(
      "securitySchemes",
      e => {
        addDeclarationKey(DeclarationKey(e))
        e.value.tagType match {
          case YType.Seq =>
            e.value.as[Seq[YMap]].foreach(map => parseEntries(map.entries, parent))
          case YType.Map  => parseEntries(e.value.as[YMap].entries, parent)
          case YType.Null =>
          case t =>
            ctx.eh.violation(InvalidSecuredByType, parent, s"Invalid type $t for 'securitySchemes' node.", e.value)
        }
      }
    )
  }

  private def parseEntries(entries: Seq[YMapEntry], parent: String): Unit = entries.foreach { entry =>
    ctx.declarations += ctx.factory
      .securitySchemeParser(entry, scheme => {
        val name = entry.key.as[String]
        scheme.withName(name).adopted(parent)
      })
      .parse()
      .add(DeclaredElement())
  }

  private def parseSchemaDeclarations(map: YMap, parent: String): Unit = {
    map.key("schemas").foreach { e =>
      addDeclarationKey(DeclarationKey(e))
      e.value.tagType match {
        case YType.Map =>
          parseSchemaEntries(e.value.as[YMap].entries, parent)
        case YType.Null =>
        case YType.Seq =>
          parseSchemaEntries(e.value.as[Seq[YMap]].flatMap(_.entries), parent)
        case t => ctx.eh.violation(InvalidTypesType, parent, s"Invalid type $t for 'types' node.", e.value)
      }
    }
  }

  private def parseSchemaEntries(entries: Seq[YMapEntry], parent: String): Unit = {
    entries.foreach { entry =>
      if (RamlTypeDefMatcher.match08Type(entry.key.as[YScalar].text).isDefined) {
        ctx.eh.violation(
          InvalidTypeDefinition,
          parent,
          s"'${entry.key.as[YScalar].text}' cannot be used to name a custom type",
          entry.key
        )
      }

      Raml08TypeParser(entry,
                       shape => shape.withName(entry.key).adopted(parent),
                       isAnnotation = false,
                       StringDefaultType)
        .parse() match {
        case Some(shape) =>
          ctx.declarations += shape.add(DeclaredElement())
          // This is a workaround for the weird situations where we reuse a local RAML identifier inside a json schema without
          // a proper $ref
          val localRaml08RefInJson =
            platform.normalizePath(UriUtils.stripFileName(ctx.rootContextDocument) + shape.name.value())
          ctx.futureDeclarations.resolveRef(localRaml08RefInJson, shape)
        case None => ctx.eh.violation(InvalidTypeDefinition, parent, s"Error parsing shape '$entry'", entry)
      }
    }
  }
}
