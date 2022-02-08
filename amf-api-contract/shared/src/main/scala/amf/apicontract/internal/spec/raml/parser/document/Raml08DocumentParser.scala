package amf.apicontract.internal.spec.raml.parser.document

import amf.aml.internal.parse.common.DeclarationKey
import amf.apicontract.client.scala.model.domain.templates.{ResourceType, Trait}
import amf.apicontract.internal.spec.common.parser.{AbstractDeclarationParser, WebApiShapeParserContextAdapter}
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.{
  InvalidAbstractDeclarationType,
  InvalidSecuredByType,
  InvalidTypesType
}
import amf.core.client.scala.model.domain.templates.AbstractDeclaration
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.parser.{Root, YMapOps}
import amf.core.internal.remote.Spec
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.utils.{AmfStrings, UriUtils}
import amf.shapes.internal.spec.RamlTypeDefMatcher
import amf.shapes.internal.spec.raml.parser.{Raml08TypeParser, StringDefaultType}
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.InvalidTypeDefinition
import org.yaml.model.{YMap, YMapEntry, YScalar, YType}

/**
  * Raml 0.8 spec parser
  */
case class Raml08DocumentParser(root: Root)(implicit override val ctx: RamlWebApiContext)
    extends RamlDocumentParser(root, Spec.RAML08)
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
                         entry.value.location)
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
            ctx.eh.violation(InvalidSecuredByType,
                             parent,
                             s"Invalid type $t for 'securitySchemes' node.",
                             e.value.location)
        }
      }
    )
  }

  private def parseEntries(entries: Seq[YMapEntry], parent: String): Unit = entries.foreach { entry =>
    ctx.declarations += ctx.factory
      .securitySchemeParser(entry, scheme => {
        val name = entry.key.as[String]
        scheme.withName(name)
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
        case t => ctx.eh.violation(InvalidTypesType, parent, s"Invalid type $t for 'types' node.", e.value.location)
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
          entry.key.location
        )
      }

      Raml08TypeParser(entry, shape => shape.withName(entry.key), isAnnotation = false, StringDefaultType)(
        WebApiShapeParserContextAdapter(ctx))
        .parse() match {
        case Some(shape) =>
          ctx.declarations += shape.add(DeclaredElement())
          // This is a workaround for the weird situations where we reuse a local RAML identifier inside a json schema without
          // a proper $ref
          val localRaml08RefInJson =
            UriUtils.normalizePath(UriUtils.stripFileName(ctx.rootContextDocument) + shape.name.value())
          ctx.futureDeclarations.resolveRef(localRaml08RefInJson, shape)
        case None => ctx.eh.violation(InvalidTypeDefinition, parent, s"Error parsing shape '$entry'", entry.location)
      }
    }
  }
}
