package amf.spec.dialects

import amf.compiler.Root
import amf.dialects._
import amf.document.Fragment.Fragment
import amf.document.{BaseUnit, EncodesModel, Module}
import amf.domain.dialects.DomainEntity
import amf.metadata.{Field, Obj, Type}
import amf.model.{AmfArray, AmfScalar}
import amf.parser.YValueOps
import amf.spec.ParserContext
import amf.spec.common.ValueNode
import amf.spec.dialects.Dialect.retrieveDomainEntity
import amf.spec.raml.RamlSpecParser
import amf.validation.Validation
import amf.vocabulary.{Namespace, ValueType}
import org.yaml.model.YValue

import scala.collection.mutable

/**
  * Created by Pavel Petrochenko on 12/09/17.
  */
case class Dialect(name: String,
                   version: String,
                   root: DialectNode,
                   resolver: ResolverFactory = NullReferenceResolverFactory,
                   module: Option[DialectNode] = None,
                   fragments: Map[String, DialectNode] = Map(),
                   kind: DialectKind = DocumentKind) {

  root.dialect = Some(this)

  def knows(nodeType: String): Option[DialectNode] = {
    findNodeType(nodeType, Seq(root), Set(root))
  }

  private def findNodeType(nodeType: String,
                           nodes: Seq[DialectNode],
                           visited: Set[DialectNode] = Set()): Option[DialectNode] = {
    if (nodes.isEmpty) {
      None
    } else {
      val node = nodes.head
      node.`type`.find(_.iri() == nodeType) match {
        case Some(_) => Some(node)
        case None =>
          val types = node.mappings().flatMap(mapping => Seq(mapping.range) ++ mapping.unionTypes.getOrElse(Seq()))
          val dialectNodes: Set[DialectNode] = types
            .filter(_.isInstanceOf[DialectNode])
            .filter(n => !visited.contains(n.asInstanceOf[DialectNode]))
            .toSet
            .asInstanceOf[Set[DialectNode]]
          findNodeType(nodeType, dialectNodes.toSeq ++ nodes.tail, visited + node)
      }
    }
  }

  var refiner: Option[Refiner] = None
  def header: String           = ("#%" + name + " " + version).trim
}

trait ResolverFactory {
  def resolver(root: Root, references: Map[String, BaseUnit], ctx: ParserContext): ReferenceResolver
}

object NullReferenceResolverFactory extends ResolverFactory {
  override def resolver(root: Root, references: Map[String, BaseUnit], ctx: ParserContext): ReferenceResolver =
    NullReferenceResolver
}

trait LocalNameProviderFactory {
  def apply(root: DomainEntity): LocalNameProvider
}

trait ReferenceResolver {
  def resolveRef(ref: String): Option[String]

  def resolve(root: Root, name: String, t: Type): Option[String]

  def resolveToEntity(root: Root, name: String, t: Type): Option[DomainEntity]
}

object NullReferenceResolver extends ReferenceResolver {
  override def resolveRef(ref: String): Option[String] = None

  override def resolve(root: Root, name: String, t: Type): Option[String] = None

  override def resolveToEntity(root: Root, name: String, t: Type): Option[DomainEntity] = None

}

trait LocalNameProvider {
  def localName(refValue: String, property: DialectPropertyMapping): String
}

trait Refiner {
  def refine(root: DomainEntity, resolver: ReferenceResolver)
}

case class DialectPropertyMapping(name: String,
                                  range: Type,
                                  unionTypes: Option[List[Type]] = None,
                                  required: Boolean = false,
                                  collection: Boolean = false,
                                  referenceTarget: Option[DialectNode] = None,
                                  noRAML: Boolean = false,
                                  noLastSegmentTrimInMaps: Boolean = false,
                                  hash: Option[DialectPropertyMapping] = None,
                                  hashValue: Option[DialectPropertyMapping] = None,
                                  fromVal: Boolean = false,
                                  isDeclaration: Boolean = false,
                                  namespace: Option[Namespace] = None,
                                  rdfName: Option[String] = None,
                                  jsonld: Boolean = true,
                                  owningNode: Option[DialectNode] = None,
                                  scalaNameOverride: Option[String] = None,
                                  allowInplace: Boolean = false,
                                  pattern: Option[String] = None,
                                  enum: Option[Seq[String]] = None,
                                  minimum: Option[Int] = None,
                                  maximum: Option[Int] = None,
                                  defaultValue: Option[AmfScalar] = None) {

  def isRef: Boolean = referenceTarget.isDefined

  def scalaName: String = scalaNameOverride.getOrElse(name)

  def isScalar: Boolean = range match {
    case _: Type.Scalar => true
    case _              => false
  }

  def isMap: Boolean = hash.isDefined

  def multivalue: Boolean = isMap || collection

  def adopt(dialectNode: DialectNode): DialectPropertyMapping =
    namespace match {
      case None => copy(namespace = Some(dialectNode.namespace), owningNode = Option(dialectNode))
      case _    => copy(owningNode = Option(dialectNode))
    }

  def fieldName: ValueType = this.rdfName match {
    case Some(rdf) => namespace.get + rdf
    case _         => namespace.get + name
  }

  def rangeAsDialect: Option[DialectNode] = this.range match {
    case node: DialectNode => Some(node)
    case _                 => None
  }

  def field(): amf.metadata.Field = {
    val `type` =
      if (collection || isMap) Type.Array(range)
      else range

    Field(`type`, fieldName, jsonld)
  }

  def iri(): String = this.fieldName.iri()

}

trait TypeCalculator {
  def calcTypes(domainEntity: DomainEntity): List[ValueType]
}

class FieldValueDiscriminator(val dialectPropertyMapping: DialectPropertyMapping,
                              val valueMap: mutable.Map[String, ValueType] = new mutable.ListMap())
    extends TypeCalculator {

  def add(n: String, v: ValueType): FieldValueDiscriminator = {
    valueMap.put(n, v)
    this
  }

  var defaultValue: Option[ValueType] = None

  def calcTypes(domainEntity: DomainEntity): List[ValueType] = {

    val field = dialectPropertyMapping.field()

    domainEntity.fields.get(field) match {
      case scalar: AmfScalar => calcScalar(scalar)
      case arr: AmfArray     => calcArray(arr)
      case _                 => List()
    }
  }

  private def calcScalar(scalar: AmfScalar) = {
    val dv = scalar.toString
    if (valueMap.contains(dv)) {
      List(valueMap(dv))
    } else {
      defaultValue match {
        case Some(default) => List(default)
        case None          => List()
      }
    }
  }

  private def calcArray(arr: AmfArray) = {
    var buf: Set[ValueType] = Set()

    for {
      member <- arr.values
      iri    <- valueMap.get(member.toString)
    } yield {
      buf += iri
    }

    if (buf.isEmpty && this.defaultValue.isDefined) {
      buf += defaultValue.get
    }
    buf.toList
  }

}

object FieldValueDiscriminator {
  def apply(dialectPropertyMapping: DialectPropertyMapping): FieldValueDiscriminator =
    apply(dialectPropertyMapping, mutable.Map())

  def apply(dialectPropertyMapping: DialectPropertyMapping,
            valueMap: mutable.Map[String, ValueType]): FieldValueDiscriminator =
    new FieldValueDiscriminator(dialectPropertyMapping, valueMap)
}

trait Builtins extends LocalNameProvider with ReferenceResolver {

  override def resolve(root: Root, name: String, t: Type): Option[String] = b2id.get(name)

  override def localName(refValue: String, property: DialectPropertyMapping): String =
    id2b.getOrElse(refValue, refValue)

  val b2id: mutable.HashMap[String, String] = mutable.HashMap[String, String]()
  val id2b: mutable.HashMap[String, String] = mutable.HashMap[String, String]()
  val id2t: mutable.HashMap[String, Type]   = mutable.HashMap[String, Type]()

  def buitInType(id: String): Option[Type] = id2t.get(id)

  def add(id: String, builtin: String, t: Type): Builtins = {
    b2id.put(builtin, id)
    id2t.put(id, t)
    id2b.put(id, builtin)
    this
  }
}

trait TypeBuiltins extends Builtins {
  add(TypeBuiltins.STRING, "string", Type.Str)
  add(TypeBuiltins.INTEGER, "integer", Type.Int)
  add(TypeBuiltins.NUMBER, "number", Type.Int)
  add(TypeBuiltins.FLOAT, "number", Type.Int)
  add(TypeBuiltins.BOOLEAN, "boolean", Type.Bool)
  add(TypeBuiltins.DATE, "date", Type.Str)
  add(TypeBuiltins.DATE_TIME, "dateTime", Type.Str)
  add(TypeBuiltins.TIME, "time", Type.Str)
  add(TypeBuiltins.URI, "uri", Type.Iri)
  add(TypeBuiltins.ANY, "any", Type.Any)
}

object TypeBuiltins {
  val STRING: String    = (Namespace.Xsd + "string").iri()
  val INTEGER: String   = (Namespace.Xsd + "integer").iri()
  val FLOAT: String     = (Namespace.Xsd + "float").iri()
  val NUMBER: String    = (Namespace.Xsd + "float").iri()
  val BOOLEAN: String   = (Namespace.Xsd + "boolean").iri()
  val DATE: String      = (Namespace.Xsd + "date").iri()
  val DATE_TIME: String = (Namespace.Xsd + "dateTime").iri()
  val TIME: String      = (Namespace.Xsd + "time").iri()
  val URI: String       = (Namespace.Xsd + "anyURI").iri()
  val ANY: String       = (Namespace.Xsd + "anyType").iri()

}
class BasicResolver(root: Root, val externals: List[DialectPropertyMapping], references: Map[String, BaseUnit])(
    implicit val ctx: ParserContext)
    extends RamlSpecParser
    with TypeBuiltins {

  val REGEX_URI =
    "^([a-z][a-z0-9+.-]*):(?://((?:(?=((?:[a-z0-9-._~!$&'()*+,;=:]|%[0-9A-F]{2})*))(\\3)@)?(?=([[0-9A-F:.]{2,}]|(?:[a-z0-9-._~!$&'()*+,;=]|%[0-9A-F]{2})*))\\5(?::(?=(\\d*))\\6)?)(\\/(?=((?:[a-z0-9-._~!$&'()*+,;=:@\\/]|%[0-9A-F]{2})*))\\8)?|(\\/?(?!\\/)(?=((?:[a-z0-9-._~!$&'()*+,;=:@\\/]|%[0-9A-F]{2})*))\\10)?)(?:\\?(?=((?:[a-z0-9-._~!$&'()*+,;=:@\\/?]|%[0-9A-F]{2})*))\\11)?(?:#(?=((?:[a-z0-9-._~!$&'()*+,;=:@\\/?]|%[0-9A-F]{2})*))\\12)?$"
  private val externalsMap: mutable.HashMap[String, String] = new mutable.HashMap()
  private val declarationsFromLibraries                     = mutable.Map[String, DomainEntity]()
  private val declarationsFromFragments                     = mutable.Map[String, DomainEntity]()
  private var base: String                                  = root.location + "#"

  initReferences(root)

  def typeId(t: Type): String = {
    t match {
      case d: DialectNode => d.namespace.toString + d.shortName;
      case _              => ""
    }
  }

  // All the external terms will be stored here, so we can generate
  // transient 'ExternalTerms' in the JSON-LD serialisation
  val resolvedExternals: mutable.Set[String] = mutable.Set.empty

  override def resolveToEntity(root: Root, name: String, t: Type): Option[DomainEntity] =
    declarationsFromLibraries.get(name).orElse(declarationsFromFragments.get(name))

  def resolveBasicRef(name: String): String =
    if (Option(name).isEmpty) {
      throw new Exception("Empty name for basic ref")
    } else if (name.indexOf(".") > -1) {
      if (declarationsFromLibraries.contains(name)) {
        declarationsFromLibraries(name).id
      } else
        name.split("\\.") match {
          case Array(alias, suffix) =>
            externalsMap.get(alias) match {
              case Some(resolved) => {
                val resolvedUri = s"$resolved$suffix"
                resolvedExternals += resolvedUri
                resolvedUri
              }
              case _ =>
                if (references.contains(alias)) {
                  throw new Exception(s"Cannot find entity '$suffix' in '$alias'")
                }
                throw new Exception(s"Cannot find prefix '$name'")
            }
          case _ =>
            throw new Exception(s"Error in class/property name '$name', multiple .")
        }
    } else {
      if (name.matches(REGEX_URI)) {
        name
      } else {
        s"$base$name"
      }
    }

  protected def initReferences(root: Root): Unit = {
    // val ast = root.ast.last
    // val entries = Entries(ast)

    references.foreach {
      case (namespace, unit) =>
        val ent = retrieveDomainEntity(unit)
        unit match {
          case fragment: Fragment =>
            declarationsFromFragments.put(namespace, ent)
          case _ =>
            ent.definition.props.values.foreach(p => {
              if (p.isMap)
                ent
                  .entities(p)
                  .foreach(decl => {
                    if (decl.linkValue.isDefined) {
                      declarationsFromLibraries.put(namespace + "." + decl.linkValue.get, decl)
                    }
//                    p.hash.foreach(h => {
//                      decl
//                        .string(h)
//                        .foreach(localName => {
//                          declarationsFromLibraries.put(namespace + "." + localName, decl)
//                        })
//                    })
                  })
            })
        }
    }

    root.document.value.foreach { value: YValue =>
      val entries = value.toMap.entries

      for {
        mapping <- externals
        node    <- entries.find(_.key.value.toScalar.text == mapping.name)
      } yield
        for {
          (alias, nestedEntry) <- node.value.value.toMap.map
        } yield {
          externalsMap.put(alias, nestedEntry)
        }

      for {
        entry <- entries.find(_.key.value.toScalar.text == "base")
        node = ValueNode(entry.value).string().value
        if node.isInstanceOf[String]
      } yield {
        base = fixBase(node.asInstanceOf[String])
      }
    }
    def fixBase(str: String): String = {
      if (!str.endsWith("/") && (!str.endsWith("#"))) {
        str + "/";
      } else {
        str
      }
    }
  }

  override def resolve(root: Root, name: String, t: Type): Option[String] = {
    try {
      t match {
        case ClassTerm =>
          Option(name) match {
            case Some(range) =>
              super.resolve(root, range, t) match {
                case Some(bid) => Some(bid)
                case _         => Some(resolveBasicRef(name))
              }
            case None => Some(TypeBuiltins.ANY)
          }

        case _ => Some(resolveBasicRef(name))
      }
    } catch {
      case _: Exception => None
    }
  }

  override def resolveRef(ref: String) =
    try {
      Some(resolveBasicRef(ref))
    } catch {
      case _: Exception => None
    }
}

object BasicResolver {
  def apply(root: Root, externals: List[DialectPropertyMapping], uses: Map[String, BaseUnit])(
      implicit ctx: ParserContext) =
    new BasicResolver(root, externals, uses)
}

class BasicNameProvider(root: DomainEntity, val namespaceDeclarators: List[DialectPropertyMapping])
    extends TypeBuiltins {

  val namespaces: mutable.Map[String, String]         = mutable.Map[String, String]()
  var declarations: mutable.Map[String, DomainEntity] = mutable.Map[String, DomainEntity]()

  {
    for {
      declarations <- namespaceDeclarators
      entity       <- root.entities(declarations)
      uri          <- entity.string(External.uri)
      name         <- entity.string(External.name)
    } yield {
      namespaces.put(uri, name)
    }

    root.traverse {
      case (domainEntity: DomainEntity, mapping: DialectPropertyMapping) =>
        if (mapping.isDeclaration) declarations.put(domainEntity.id, domainEntity)
        true
    }
  }

  override def resolveToEntity(root: Root, name: String, t: Type): Option[DomainEntity] = None

  override def localName(uri: String, property: DialectPropertyMapping): String = {
    val foundLocalName = for {
      entity      <- declarations.get(uri)
      keyProperty <- entity.definition.keyProperty
    } yield {
      entity.string(keyProperty).getOrElse(uri)
    }

    foundLocalName match {
      case Some(localName) => localName
      case None =>
        val localName = super.localName(uri, property)
        if (localName != uri) {
          localName
        } else {
          if (uri.indexOf(root.id) > -1) {
            uri.replace(root.id, "")
          } else {
            namespaces.find {
              case (p, _) =>
                uri.indexOf(p) > -1
            } match {
              case Some((p, v)) => uri.replace(p, s"$v.")
              case _            => uri
            }
          }
        }
    }
  }

  override def resolveRef(ref: String) = None
}

class DialectNode(val shortName: String, val namespace: Namespace) extends Type with Obj {

  override val dynamicType: Boolean                       = true
  protected var typeCalculator: Option[TypeCalculator]    = None
  protected val extraTypes: mutable.ListBuffer[ValueType] = mutable.ListBuffer()
  override val `type`: List[ValueType]                    = List(ValueType(namespace, shortName))
  val keyProperty: Option[DialectPropertyMapping]         = None
  var nameProvider: Option[LocalNameProviderFactory]      = None
  val props: mutable.Map[String, DialectPropertyMapping]  = new mutable.LinkedHashMap()
  private[dialects] var dialect: Option[Dialect]          = None

  var id: Option[String] = None

  def mappings(): List[DialectPropertyMapping] = props.values.toList

  def obj(propertyMapping: String,
          dialectNode: DialectNode,
          adapter: (DialectPropertyMapping) => DialectPropertyMapping = identity): DialectPropertyMapping =
    add(adapter(DialectPropertyMapping(propertyMapping, dialectNode)))

  def str(propertyMapping: String,
          adapter: (DialectPropertyMapping) => DialectPropertyMapping = identity): DialectPropertyMapping =
    add(adapter(DialectPropertyMapping(propertyMapping, Type.Str)))

  def iri(propertyMapping: String,
          adapter: (DialectPropertyMapping) => DialectPropertyMapping = identity): DialectPropertyMapping =
    add(adapter(DialectPropertyMapping(propertyMapping, Type.Iri)))

  def bool(propertyMapping: String,
           adapter: (DialectPropertyMapping) => DialectPropertyMapping = identity): DialectPropertyMapping =
    add(adapter(DialectPropertyMapping(propertyMapping, Type.Bool)))

  def ref(propertyMapping: String,
          dialectNode: DialectNode,
          adapter: (DialectPropertyMapping) => DialectPropertyMapping = identity): DialectPropertyMapping =
    add(adapter(DialectPropertyMapping(propertyMapping, Type.Iri, referenceTarget = Some(dialectNode))))

  def map(propertyMapping: String,
          hash: DialectPropertyMapping,
          node: DialectNode,
          adapter: (DialectPropertyMapping) => DialectPropertyMapping = identity): DialectPropertyMapping =
    add(
      adapter(
        DialectPropertyMapping(propertyMapping,
                               node, // DialectNode inherits from Type !!!
                               hash = Some(hash))))

  def add(p: DialectPropertyMapping): DialectPropertyMapping = {
    val mapping = p.adopt(this)
    props.put(mapping.name, mapping)
    mapping
  }

  def withType(t: String): Unit = extraTypes += ValueType(t)

  def fieldValueDiscriminator(prop: DialectPropertyMapping): FieldValueDiscriminator = {
    val discriminator = FieldValueDiscriminator(prop)
    typeCalculator = Some(discriminator)
    discriminator
  }

  def fields: List[Field] = props.values.toList.map(_.field())

  def calcTypes(domainEntity: DomainEntity): List[ValueType] = {
    val calculated = typeCalculator match {
      case Some(calculator) => extraTypes.toList ++ calculator.calcTypes(domainEntity)
      case None             => extraTypes.toList
    }
    (calculated ++ domainEntity.definition.`type`).distinct
  }

  def withGlobalIdField(field: String): this.type = {
    id = Some(field)
    this
  }
}

sealed trait DialectKind

object DocumentKind extends DialectKind
object ModuleKind   extends DialectKind
object FragmentKind extends DialectKind

object Dialect {

  def retrieveDomainEntity(unit: BaseUnit): DomainEntity = unit match {
    case unit: EncodesModel =>
      unit.encodes match {
        case unit: DomainEntity => unit
        case other              => throw new Exception(s"Encoded domain element is not a dialect domain entity $other")
      }
    case module: Module =>
      (module.declares collect {
        case unit: DomainEntity => unit
      }).head
    case _ => throw new Exception(s"Cannot extract domain entity from unit: $unit")
  }
}

object DialectNode {
  def apply(namespace: Namespace, shortName: String) = new DialectNode(shortName, namespace)
}
