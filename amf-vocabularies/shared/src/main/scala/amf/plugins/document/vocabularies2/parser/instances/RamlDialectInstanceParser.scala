package amf.plugins.document.vocabularies2.parser.instances

import amf.core.Root
import amf.core.utils._
import amf.core.parser.{Annotations, BaseSpecParser, Declarations, EmptyFutureDeclarations, ErrorHandler, FutureDeclarations, ParserContext}
import amf.core.vocabulary.Namespace
import amf.plugins.document.vocabularies2.model.document.{Dialect, DialectInstance}
import amf.plugins.document.vocabularies2.model.domain._
import amf.plugins.document.vocabularies2.parser.common.SyntaxErrorReporter
import org.yaml.convert.YRead
import org.yaml.model._

protected class PropertyType
protected object LiteralProperty extends PropertyType
protected object ObjectProperty extends PropertyType
protected object ObjectPropertyCollection extends PropertyType
protected object ObjectMapProperty extends PropertyType
protected object LiteralPropertyCollection extends PropertyType

class DialectInstanceDeclarations(errorHandler: Option[ErrorHandler],
                                  futureDeclarations: FutureDeclarations)
  extends Declarations(Map(), Map(), Map(), errorHandler, futureDeclarations) {

  /** Get or create specified library. */
  override def getOrCreateLibrary(alias: String): DialectInstanceDeclarations = {
    libraries.get(alias) match {
      case Some(lib: DialectInstanceDeclarations) => lib
      case _ =>
        val result = new DialectInstanceDeclarations(errorHandler = errorHandler, futureDeclarations = EmptyFutureDeclarations())
        libraries = libraries + (alias -> result)
        result
    }
  }

  /*
  def +=(nodeMapping: NodeMapping): DialectDeclarations = {
    nodeMappings += (nodeMapping.name -> nodeMapping)
    this
  }


  def findNodeMapping(key: String, scope: SearchScope.Scope): Option[NodeMapping] =
    findForType(key, _.asInstanceOf[DialectDeclarations].nodeMappings, scope) collect {
      case nm: NodeMapping => nm
    }
  */

}


class DialectInstanceContext(private val wrapped: ParserContext, private val ds: Option[DialectInstanceDeclarations] = None)
  extends ParserContext(wrapped.rootContextDocument, wrapped.refs, wrapped.futureDeclarations) with SyntaxErrorReporter {


  val declarations: DialectInstanceDeclarations =
    ds.getOrElse(new DialectInstanceDeclarations(errorHandler = Some(this), futureDeclarations = futureDeclarations))

}


class RamlDialectInstanceParser(root: Root, dialect: Dialect)(implicit override val ctx: DialectInstanceContext) extends BaseSpecParser {
  val map: YMap = root.parsed.document.as[YMap]
  val dialectInstance: DialectInstance = DialectInstance(Annotations(map)).withLocation(root.location).withId(root.location + "#").withDefinedBy(dialect.id)

  def parseDocument(): Option[DialectInstance] = {
    parseEncoded() match {
      case Some(dialectDomainElement) => Some(dialectInstance.withEncodes(dialectDomainElement))
      case _                          => None
    }
  }

  protected def parseEncoded(): Option[DialectDomainElement] = {
    Option(dialect.documents()) flatMap {
      documents: DocumentsModel =>
        Option(documents.root()) flatMap {
          mapping =>
            findNodeMapping(mapping.encoded()) match {
              case Some(nodeMapping) => parseNode(dialectInstance.id + "/", map, nodeMapping)
              case _ => None
            }
        }
    }
  }

  def parseProperty(id: String, propertyEntry: YMapEntry, property: PropertyMapping, node: DialectDomainElement): Unit = {
    propertyMappingType(property) match {
      case LiteralProperty           => parseLiteralProperty(id, propertyEntry, property, node)
      case LiteralPropertyCollection => parseLiteralCollectionProperty(id, propertyEntry, property, node)
      case ObjectProperty            => parseObjectProperty(id, propertyEntry, property, node)
      case ObjectPropertyCollection  => parseObjectCollectionProperty(id, propertyEntry, property, node)
      case ObjectMapProperty         => parseObjectMapProperty(id, propertyEntry,property, node)
      case _ => // TODO: throw exception
    }
  }

  def checkHashProperties(node: DialectDomainElement, propertyMapping: PropertyMapping, propertyEntry: YMapEntry): DialectDomainElement = {
    Option(propertyMapping.mapKeyProperty()) match {
        case Some(propId) => node.setMapKeyField(propId, propertyEntry.key.as[String])
        case None         => node
    }
  }

  def parseObjectProperty(id: String, propertyEntry: YMapEntry, property: PropertyMapping, node: DialectDomainElement): Unit = {
    property.objectRange() match {
      case range: Seq[String] if range.size > 1  => // TODO: parse unions
      case range: Seq[String] if range.size == 1 =>
        dialect.declares.find(_.id == range.head) match {
          case Some(nodeMapping: NodeMapping) =>
            val nestedObjectId = pathSegment(id, propertyEntry.key.as[String].urlEncoded)
            parseNestedNode(nestedObjectId, propertyEntry.value, nodeMapping) match {
              case Some(dialectDomainElement) => node.setObjectField(property, dialectDomainElement)
              case None                       => // ignore
            }
        }
      case _ => // TODO: throw exception, illegal range
    }
  }

  def parseObjectMapProperty(id: String, propertyEntry: YMapEntry, property: PropertyMapping, node: DialectDomainElement): Unit = {
    val nested = propertyEntry.value.as[YMap].entries.zipWithIndex.map { case (keyEntry, elemId) =>
        property.objectRange() match {
          case range: Seq[String] if range.size > 1  => // TODO: parse unions
          case range: Seq[String] if range.size == 1 =>
            dialect.declares.find(_.id == range.head) match {
              case Some(nodeMapping: NodeMapping) =>
                val nestedObjectId = pathSegment(id, propertyEntry.key.as[String].urlEncoded) + s"/$elemId"
                parseNestedNode(nestedObjectId, keyEntry.value, nodeMapping) match {
                  case Some(dialectDomainElement) => Some(checkHashProperties(dialectDomainElement, property, keyEntry))
                  case None                       => None
                }
            }
          case _ => None
        }
    }
    node.setObjectField(property, nested.collect { case Some(node: DialectDomainElement) => node })
  }

  def parseObjectCollectionProperty(id: String, propertyEntry: YMapEntry, property: PropertyMapping, node: DialectDomainElement): Unit = {
    val res = propertyEntry.value.as[YSequence].nodes.zipWithIndex.map { case (node, nextElem) =>
      property.objectRange() match {
        case range: Seq[String] if range.size > 1  => // TODO: parse unions
        case range: Seq[String] if range.size == 1 =>
          dialect.declares.find(_.id == range.head) match {
            case Some(nodeMapping: NodeMapping) =>
              val nestedObjectId = pathSegment(id, propertyEntry.key.as[String].urlEncoded) + s"/$nextElem"
              parseNestedNode(nestedObjectId, node, nodeMapping) match {
                case Some(dialectDomainElement) => Some(dialectDomainElement)
                case None                       => None
              }
          }
        case _ => None
      }
    }
    val elems: Seq[DialectDomainElement] = res.collect { case Some(x: DialectDomainElement) => x}
    node.setObjectField(property, elems)
  }

  def pathSegment(parent: String, next: String): String = {
    if (parent.endsWith("/")) {
      parent + next.urlEncoded
    } else {
      parent + "/" + next.urlEncoded
    }
  }

  def parseLiteralValue(value: YNode, property: PropertyMapping, node: DialectDomainElement): Option[_] = {

    implicit val reader:YRead[Float] = new YRead[Float]() {
      override def read(node: YNode): Either[YError, Float] = Right(value.toString.toFloat)
      override def defaultValue: Float = 0
    }

    value.tagType match {
      case YType.Bool if property.literalRange() == (Namespace.Xsd + "boolean").iri() =>
        Some(value.as[Boolean])
      case YType.Bool  =>
        ctx.inconsistentPropertyRangeValueViolation(node.id, property, property.literalRange(), (Namespace.Xsd + "boolean").iri(), value)
        None
      case YType.Int   if property.literalRange() == (Namespace.Xsd + "integer").iri() =>
        Some(value.as[Int])
      case YType.Int  =>
        ctx.inconsistentPropertyRangeValueViolation(node.id, property, property.literalRange(), (Namespace.Xsd + "integer").iri(), value)
        None
      case YType.Str   if property.literalRange() == (Namespace.Xsd + "string").iri() =>
        Some(value.as[String])
      case YType.Str  =>
        ctx.inconsistentPropertyRangeValueViolation(node.id, property, property.literalRange(), (Namespace.Xsd + "string").iri(), value)
        None
      case YType.Float if property.literalRange() == (Namespace.Xsd + "float").iri() =>
        Some(value.as[Float])
      case YType.Float  =>
        ctx.inconsistentPropertyRangeValueViolation(node.id, property, property.literalRange(), (Namespace.Xsd + "float").iri(), value)
        None
      case _           =>
        ctx.violation(node.id, s"Unsupported scalar type ${value.tagType}", value)
        None
    }
  }

  def setLiteralValue(value: YNode, property: PropertyMapping, node: DialectDomainElement) = {
    parseLiteralValue(value, property, node) match {
      case Some(b: Boolean) => node.setLiteralField(property, b)
      case Some(i: Int)     => node.setLiteralField(property, i)
      case Some(f: Float)   => node.setLiteralField(property, f)
      case Some(s: String)  => node.setLiteralField(property, s)
      case other            => // ignore
    }
  }

  def parseLiteralProperty(id: String, propertyEntry: YMapEntry, property: PropertyMapping, node: DialectDomainElement): Unit = {
    setLiteralValue(propertyEntry.value, property, node)
  }

  def parseLiteralCollectionProperty(id: String, propertyEntry: YMapEntry, property: PropertyMapping, node: DialectDomainElement): Unit = {
    propertyEntry.value.tagType match {
      case YType.Seq =>
        val values = propertyEntry.value.as[YSequence].nodes.map { elemValue => parseLiteralValue(elemValue, property, node) }.collect { case Some(v) => v }
        node.setLiteralField(property, values)
      case _ =>
        parseLiteralValue(propertyEntry.value, property, node) match {
          case Some(v) => node.setLiteralField(property, Seq(v))
          case _       => // ignore
        }
    }
  }

  def propertyMappingType(mapping: PropertyMapping): PropertyType = {
    val isLiteral = Option(mapping.literalRange()).isDefined
    val isObject = Option(mapping.objectRange()).isDefined && mapping.objectRange().nonEmpty
    val multiple = Option(mapping.allowMultiple()).getOrElse(false)
    val isMap = Option(mapping.mapKeyProperty()).isDefined

    if (isLiteral && !multiple)
      LiteralProperty
    else if (isLiteral)
      LiteralPropertyCollection
    else if (isObject && isMap)
      ObjectMapProperty
    else if (isObject && !multiple)
      ObjectProperty
    else
      ObjectPropertyCollection
  }

  protected def parseNestedNode(id: String, entry: YNode, mapping: NodeMapping): Option[DialectDomainElement] = {
    entry.tagType match {
      case YType.Map     => parseNode(id, entry.as[YMap], mapping)
      case YType.Include => throw new Exception("Node includes not supported yet")   // TODO not supported yet
      case YType.Str     => throw new Exception("Node references not supported yet") // TODO not supported yet
      case _             => throw new Exception("Error in reference")                // TODO violation here instead of exception
    }
  }

  protected def parseNode(id: String, map: YMap, mapping: NodeMapping): Option[DialectDomainElement] = {
    val node: DialectDomainElement = DialectDomainElement(map).withId(id).withDefinedBy(mapping)

    node.withInstanceTypes(Seq(mapping.nodetypeMapping))

    mapping.propertiesMapping().foreach { propertyMapping =>
      val propertyName = propertyMapping.name()
      map.entries.find(_.key.as[String] == propertyName) match {
        case Some(entry) => parseProperty(id, entry, propertyMapping, node)
        case None        => // ignore
      }
    }

    Some(node)
  }

  protected def findNodeMapping(mappingId: String): Option[NodeMapping] = {
    dialect.declares.collectFirst {
      case mapping: NodeMapping if mapping.id == mappingId => mapping
    }
  }
}
