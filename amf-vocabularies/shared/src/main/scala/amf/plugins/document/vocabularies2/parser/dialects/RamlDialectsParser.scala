package amf.plugins.document.vocabularies2.parser.dialects

import amf.core.Root
import amf.core.annotations.Aliases
import amf.core.utils._
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.model.domain.{AmfScalar, DomainElement}
import amf.core.parser.{Annotations, BaseSpecParser, ErrorHandler, FutureDeclarations, ParserContext, _}
import amf.plugins.document.vocabularies2.metamodel.document.DialectModel
import amf.plugins.document.vocabularies2.model.document.{Dialect, Vocabulary}
import amf.plugins.document.vocabularies2.model.domain.{ClassTerm, NodeMapping, PropertyMapping, PropertyTerm}
import amf.plugins.document.vocabularies2.parser.common.SyntaxErrorReporter
import amf.plugins.document.vocabularies2.parser.vocabularies.VocabularyDeclarations
import org.yaml.model._

import scala.collection.mutable

class DialectDeclarations(var nodeMappings: Map[String, NodeMapping] = Map(),
                          errorHandler: Option[ErrorHandler],
                          futureDeclarations: FutureDeclarations)
  extends VocabularyDeclarations(Map(), Map(), Map(), Map(), errorHandler, futureDeclarations) {

  /** Get or create specified library. */
  override def getOrCreateLibrary(alias: String): DialectDeclarations = {
    libraries.get(alias) match {
      case Some(lib: DialectDeclarations) => lib
      case _ =>
        val result = new DialectDeclarations(errorHandler = errorHandler, futureDeclarations = EmptyFutureDeclarations())
        libraries = libraries + (alias -> result)
        result
    }
  }


  def +=(nodeMapping: NodeMapping): Declarations = {
    nodeMappings += (nodeMapping.name -> nodeMapping)
    this
  }

  def findNodeMapping(key: String, scope: SearchScope.Scope): Option[NodeMapping] =
    findForType(key, _.asInstanceOf[DialectDeclarations].nodeMappings, scope) collect {
      case nm: NodeMapping => nm
    }

  def findClassTerm(key: String, scope: SearchScope.Scope): Option[ClassTerm] =
    findForType(key, _.asInstanceOf[DialectDeclarations].classTerms, scope) collect {
      case ct: ClassTerm => ct
    }

  def findPropertyTerm(key: String, scope: SearchScope.Scope): Option[PropertyTerm] =
    findForType(key, _.asInstanceOf[DialectDeclarations].propertyTerms, scope) collect {
      case pt: PropertyTerm => pt
    }

  override def declarables(): Seq[DomainElement] = nodeMappings.values.toSeq

}

trait DialectSyntax {this: DialectContext =>
  val dialect: Map[String,String] = Map(
    "dialect" -> "string",
    "version" -> "string",
    "usage" -> "string",
    "uses" -> "DeclaresModel[]",
    "nodeMappings" -> "NodeMapping[]"
  )

  val nodeMapping: Map[String,String] = Map(
    "classTerm" -> "string",
    "mapping" -> "propertyMapping"
  )

  val propertyMapping: Map[String,String] = Map(
    "propertyTerm" -> "string"
  )

  def closedNode(nodeType: String, id: String, map: YMap): Unit = {
    val allowedProps = nodeType match {
      case "dialect"         => dialect
      case "nodeMapping"     => nodeMapping
      case "propertyMapping" => propertyMapping
    }
    map.map.keySet.map(_.as[String]).foreach { property =>
      allowedProps.get(property) match {
        case Some(_) => // correct
        case None => closedNodeViolation(id, property, nodeType, map)
      }
    }
  }

  private def isInclude(node: YNode) = node.tagType == YType.Include

  def link(node: YNode): Either[String, YNode] = {
    node match {
      case _ if isInclude(node) => Left(node.as[YScalar].text)
      case _                    => Right(node)
    }
  }
}

class DialectContext(private val wrapped: ParserContext, private val ds: Option[DialectDeclarations] = None)
  extends ParserContext(wrapped.rootContextDocument, wrapped.refs, wrapped.futureDeclarations)
    with DialectSyntax with SyntaxErrorReporter {

  val declarations: DialectDeclarations =
    ds.getOrElse(new DialectDeclarations(errorHandler = Some(this), futureDeclarations = futureDeclarations))

}

case class ReferenceDeclarations(references: mutable.Map[String, Any] = mutable.Map())(implicit ctx: DialectContext) {
  def +=(alias: String, unit: BaseUnit): Unit = {
    references += (alias -> unit)
    unit match {
      case d: Vocabulary =>
        val library = ctx.declarations.getOrCreateLibrary(alias)
        d.declares.foreach {
          case prop: PropertyTerm => library.registerTerm(prop)
          case cls: ClassTerm     => library.registerTerm(cls)
        }
      case m: DeclaresModel =>
        val library = ctx.declarations.getOrCreateLibrary(alias)
        m.declares.foreach { library += _ }
    }
  }

  /*
  def += (external: External): Unit = {
    references += (external.alias -> external)
    ctx.declarations.externals += (external.alias -> external)
  }
  */

  def baseUnitReferences(): Seq[BaseUnit] = references.values.toSet.filter(_.isInstanceOf[BaseUnit]).toSeq.asInstanceOf[Seq[BaseUnit]]
}

case class DialectsReferencesParser(dialect: Dialect, map: YMap, references: Seq[ParsedReference])(implicit ctx: DialectContext) {

  def parse(location: String): ReferenceDeclarations = {
    val result = ReferenceDeclarations()
    parseLibraries(dialect, result, location)
    // parseExternals(result, location)
    result
  }

  private def target(url: String): Option[BaseUnit] =
    references.find(r => r.origin.url.equals(url)).map(_.unit)


  private def parseLibraries(dialect: Dialect, result: ReferenceDeclarations, id: String): Unit = {
    map.key(
      "uses",
      entry =>
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val alias: String = e.key
            val url: String   = library(e)
            target(url).foreach {
              case module: DeclaresModel =>
                collectAlias(dialect, alias -> module.id)
                result += (alias, module)
              case other =>
                ctx.violation(id, s"Expected vocabulary module but found: $other", e) // todo Uses should only reference modules...
            }
          })
    )
  }

  private def library(e: YMapEntry): String = e.value.tagType match {
    case YType.Include => e.value.as[YScalar].text
    case _             => e.value
  }

  /*
  private def parseExternals(result: ReferenceDeclarations, id: String): Unit = {
    map.key(
      "external",
      entry =>
        entry.value
          .as[YMap]
          .entries
          .foreach(e => {
            val alias: String = e.key
            val base: String   = e.value
            val external = External()
            result += external.withAlias(alias).withBase(base)
          })
    )
  }
  */

  private def collectAlias(aliasCollectorUnit: BaseUnit, alias: (String, String)): BaseUnit = {
    aliasCollectorUnit.annotations.find(classOf[Aliases]) match {
      case Some(aliases) =>
        aliasCollectorUnit.annotations.reject(_.isInstanceOf[Aliases])
        aliasCollectorUnit.add(aliases.copy(aliases = aliases.aliases + alias))
      case None => aliasCollectorUnit.add(Aliases(Set(alias)))
    }
  }
}


class RamlDialectsParser(root: Root)(implicit override val ctx: DialectContext) extends BaseSpecParser {

  val map: YMap = root.parsed.document.as[YMap]
  val dialect: Dialect = Dialect(Annotations(map)).withLocation(root.location).withId(root.location + "#")


  def parseDocument(): BaseUnit = {

    map.key("dialect", entry => {
      val value = ValueNode(entry.value)
      dialect.set(DialectModel.Name, value.string(), Annotations(entry))
    })

    map.key("usage", entry => {
      val value = ValueNode(entry.value)
      dialect.set(DialectModel.Usage, value.string(), Annotations(entry))
    })

    map.key("version", entry => {
      val value = ValueNode(entry.value)
      val version = value.text().value.toString
      dialect.set(DialectModel.Version, AmfScalar(version, Annotations(entry.value)), Annotations(entry))
    })


    // closed node validation
    ctx.closedNode("dialect", dialect.id, map)

    val references = DialectsReferencesParser(dialect, map, root.references).parse(dialect.location)
    parseDeclarations(root, map)

    val declarables = ctx.declarations.declarables()
    if (declarables.nonEmpty) dialect.withDeclares(declarables)
    if (references.baseUnitReferences().nonEmpty) dialect.withReferences(references.baseUnitReferences())

    dialect
  }

  protected def parseDeclarations(root: Root, map: YMap): Unit = {
    val parent = root.location + "#/declarations"
    parseNodeMappingDeclarations(map, parent)
  }

  def parsePropertyMapping(entry: YMapEntry, adopt: PropertyMapping => Any): PropertyMapping = {
    val map = entry.value.as[YMap]
    val propertyMapping = PropertyMapping(map)

    adopt(propertyMapping)
    ctx.closedNode("propertyMapping", propertyMapping.id, map)

    map.key("propertyTerm", entry => {
      val value = ValueNode(entry.value)
      val propertyTermId = value.string().toString
      ctx.declarations.findPropertyTerm(propertyTermId, SearchScope.All) match {
        case Some(propertyTerm) =>
          propertyMapping.withNodePropertyMapping(propertyTerm.id)
        case _ =>
          ctx.violation(propertyMapping.id, s"Cannot find property term with alias $propertyTermId", entry.value)
      }
    })

    propertyMapping
  }

  def parseNodeMapping(entry: YMapEntry, adopt: NodeMapping => Any): Option[NodeMapping] = {
    entry.value.tagType match {
      case YType.Map =>
        val map = entry.value.as[YMap]
        val nodeMapping = NodeMapping(map)

        adopt(nodeMapping)
        ctx.closedNode("nodeMapping", nodeMapping.id, map)

        map.key("classTerm", entry => {
          val value = ValueNode(entry.value)
          val classTermId = value.string().toString
          ctx.declarations.findClassTerm(classTermId, SearchScope.All) match {
            case Some(classTerm) =>
              nodeMapping.withNodeTypeMapping(classTerm.id)
            case _ =>
              ctx.violation(nodeMapping.id, s"Cannot find class term with alias $classTermId", entry.value)
          }
        })

        map.key("mapping", entry => {
          val properties = entry.value.as[YMap].entries.map { entry =>
            parsePropertyMapping(entry, propertyMapping => propertyMapping.withName(entry.key).adopted(nodeMapping.id + "/property/" + entry.key.as[String].urlEncoded))
          }
          nodeMapping.withPropertiesMapping(properties)
        })

        Some(nodeMapping)

      case YType.Str if entry.value.toOption[YScalar].isDefined => {
        val refTuple = ctx.link(entry.value) match {
          case Left(key) =>
            (key, ctx.declarations.findNodeMapping(key, SearchScope.Fragments))
          case _ =>
            val text = entry.value.as[YScalar].text
            (text, ctx.declarations.findNodeMapping(text, SearchScope.Named))
        }
        refTuple match {
          case (text: String, Some(s)) =>
            // TODO: Make nodeMappings linkable here
            throw new Exception("mappings references not supported yet")
            /*
            s.link(text, Annotations(node.value))
              .asInstanceOf[Shape]
              .withName(name) // we setup the local reference in the name
              .withId(shape.id) // and the ID of the link at that position in the tree, not the ID of the linked element, tha goes in link-target
            */
          case (text: String, _) =>
            // TODO: Unresolved reference here
            throw new Exception("mappings references not supported yet")
            /*
            val shape = UnresolvedShape(text, node).withName(text)
            shape.withContext(ctx)
            adopt(shape)
            shape.unresolved(text, node)
            shape
            */
        }
      }

      case _ => None
    }
  }

  private def parseNodeMappingDeclarations(map: YMap, parent: String): Unit = {
    map.key("nodeMappings").foreach { e =>
      e.value.tagType match {
        case YType.Map =>
          e.value.as[YMap].entries.foreach { entry =>
            parseNodeMapping(entry, nodeMapping => nodeMapping.withName(entry.key).adopted(parent)) match {
              case Some(nodeMapping: NodeMapping) => ctx.declarations += nodeMapping
              case None                           => ctx.violation(parent, s"Error parsing shape '$entry'", entry)
            }
          }
        case YType.Null =>
        case t          => ctx.violation(parent, s"Invalid type $t for 'nodeMappings' node.", e.value)
      }
    }
  }

}
