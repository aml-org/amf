package amf.dialects

import amf.compiler.Root
import amf.dialects.RAML_1_0_DialectTopLevel.{DeclarationObject, NodeDefinitionObject, PropertyMappingObject}
import amf.document.{BaseUnit, Document, Module}
import amf.domain.dialects.DomainEntity
import amf.metadata.Type
import amf.model.{AmfArray, AmfScalar}
import amf.spec.dialects._
import amf.vocabulary.Namespace

import scala.collection.mutable

/**
  * Created by Pavel Petrochenko on 15/09/17.
  */
case class NamespaceMap(namespace: Namespace, name: String) {}
object NamespaceMap {
  def apply(s: String): Option[NamespaceMap] = {

    val ind: Int = Math.max(s.lastIndexOf('/'), s.lastIndexOf('#'))

    if (ind > 0) {
      val namespace = Namespace(s.substring(0, ind + 1))
      val str1      = s.substring(ind + 1)
      Some(NamespaceMap(namespace, str1))
    } else {
      None
    }
  }
}

class DialectLoader(val document:BaseUnit) {

  val builtins: TypeBuiltins = new TypeBuiltins {
    override def resolveToEntity(root: Root, name: String, t: Type): Option[DomainEntity] = None
    override def resolveRef(ref: String): Option[String] = None
  }

  private def retrieveDomainEntity(unit: BaseUnit) = unit match {
    case document: Document => document.encodes.asInstanceOf[DomainEntity]
    case _                  => throw new Exception(s"Cannot load a dialect from a unit that is not a document $unit")
  }

  def loadDialect(): Dialect = loadDialect(retrieveDomainEntity(document),document)

  private def registerType(n: NodeDefinitionObject, dialectMap: mutable.Map[String, DialectNode]) =
    NamespaceMap(n.classTerm().get) match {
      case Some(ns) =>
        val node = new DialectNode(ns.name, ns.namespace)
        node.id = Some(n.entity.id)
        dialectMap.put(n.entity.id, node)
      case _        => // ignore
    }

  lazy val imports: List[NodeDefinitionObject] ={
    var imports=List[NodeDefinitionObject]()
    val dialectObject = RAML_1_0_DialectTopLevel.dialectObject(retrieveDomainEntity(document))
    document.references.foreach(u=>{
      if (u.isInstanceOf[Module]){
        u.asInstanceOf[Module].declares.foreach(d=>{
          val element = d.fields.get(DialectModuleDefinition.nodeMappings.field())
          if (element.isInstanceOf[AmfArray]){
            val values=element.asInstanceOf[AmfArray].values;
            val localDeclarations:Seq[NodeDefinitionObject]=values.map(v=>new NodeDefinitionObject(v.asInstanceOf[DomainEntity],Some(dialectObject)));
            imports=imports:::(localDeclarations.toList);
          }
        })
      }
    })
    imports
  }

  def loadDialect(domainEntity: DomainEntity,unit:BaseUnit): Dialect = {
    val modelDocument=unit.asInstanceOf[Document];
    val dialectObject = RAML_1_0_DialectTopLevel.dialectObject(domainEntity)

    val rootEntity = for {
      ramlNode     <- dialectObject.raml()
      ramlDocument <- ramlNode.document()
      root         <- ramlDocument.resolvedEncodes()

    } yield {
      root
    }

    rootEntity match {

      case Some(encodedRootEntity) =>
        val dialectMap = mutable.Map[String, DialectNode]()
        processMappings(encodedRootEntity, dialectObject, dialectMap,imports)

        val dialect = for {
          dialectName    <- dialectObject.dialect()
          dialectVersion <- dialectObject.version()
          dialectNode    <- dialectMap.get(encodedRootEntity.entity.id)
        } yield {

          val fragmentList: mutable.Map[String, DialectNode] = processFragments(dialectObject, dialectMap)

          val moduleInfo   = processModuleInfo(dialectObject, dialectMap)
          processDeclarationsInfo(dialectNode, dialectObject, dialectMap)

          Dialect(dialectName,
                  dialectVersion,
                  dialectNode,
                  resolver = (root, refs) => BasicResolver(root, List(), refs),
                  moduleInfo,
                  fragmentList.toMap)
        }

        dialect match {
          case Some(dialectFound) => dialectFound
          case _                  => throw new Exception("Cannot load dialect, dialect not found")
        }

      case _ => throw new Exception("Cannot load dialect, root entity not found")
    }
  }

  private def processFragments(dialectObject: RAML_1_0_DialectTopLevel.dialectObject,
                               dialectMap: mutable.Map[String, DialectNode]) = {

    val fragmentList: mutable.Map[String, DialectNode] = mutable.Map()

    for {
      ramlMapping          <- dialectObject.raml()
      fragmentDeclarations <- ramlMapping.fragments()
    } yield {
      fragmentDeclarations.encodes() foreach { encodedFragment =>
        for {
          resolvedDeclaredNode <- encodedFragment.resolvedDeclaredNode()
          fragmentName         <- encodedFragment.name()
          fragmentNode         <- dialectMap.get(resolvedDeclaredNode.entity.id)
        } yield {
          fragmentList.put(fragmentName, fragmentNode)
        }
      }
    }

    fragmentList
  }

  private def processModuleInfo(dialectObject: RAML_1_0_DialectTopLevel.dialectObject,
                                dialectMap: mutable.Map[String, DialectNode]) = {
    fillModule(dialectMap, dialectObject) match {
      case Some(declarationMap:mutable.Map[String, DialectNode]) if declarationMap.nonEmpty =>
        val moduleNode = new DialectNode("module", Namespace.Document)
        declarationMap.keys.foreach { key =>
          moduleNode.map(key, DialectPropertyMapping("name", Type.Str, namespace = Some(Namespace.Schema)), declarationMap(key), _.copy(isDeclaration = true))
        }
        // now we have a library node
        Some(moduleNode)
      case _ => None
    }
  }

  def processDeclarationsInfo(documentDialectNode: DialectNode,
                              dialectObject: RAML_1_0_DialectTopLevel.dialectObject,
                              dialectMap: mutable.Map[String, DialectNode]) = {
    fillDocument(dialectMap, dialectObject) match {
      case Some(declarationMap:mutable.Map[String, DialectNode]) if declarationMap.nonEmpty =>
        declarationMap.keys.foreach { key =>
          documentDialectNode.map(key, DialectPropertyMapping("name", Type.Str, namespace = Some(Namespace.Schema)), declarationMap(key), _.copy(isDeclaration = true))
        }
      case _ =>
    }
  }

  private def processMappings(encodedRootEntity: RAML_1_0_DialectTopLevel.NodeDefinitionObject,
                              dialectObject: RAML_1_0_DialectTopLevel.dialectObject,
                              dialectMap: mutable.Map[String, DialectNode],imports:List[NodeDefinitionObject]) = {

    val propertyMap = mutable.Map[DialectPropertyMapping, PropertyMappingObject]()

    // process all the node mappings
    dialectObject.nodeMappings().foreach { registerType(_, dialectMap) }
    imports.foreach(registerType(_,dialectMap));
    imports.foreach(
      n =>
        parseNodeMapping(n, dialectMap, propertyMap)
    );
    dialectObject.nodeMappings().foreach { n =>
      parseNodeMapping(n, dialectMap, propertyMap)
    }

    // if it hasn't been processed, also the root mapping
    if (!dialectMap.contains(encodedRootEntity.entity.id)) {
      registerType(encodedRootEntity, dialectMap)
      parseNodeMapping(encodedRootEntity, dialectMap, propertyMap)
    }

    // now we fill the hashes
    fillHashes(propertyMap)

  }

  private def fillModule(dialectMap: mutable.Map[String, DialectNode],
                         dialectObject: RAML_1_0_DialectTopLevel.dialectObject) = {
    for {
      raml   <- dialectObject.raml()
      module <- raml.module()
    } yield {
      fillDeclarations(module.declares(), dialectMap)
    }
  }

  private def fillDocument(dialectMap: mutable.Map[String, DialectNode],
                           dialectObject: RAML_1_0_DialectTopLevel.dialectObject) = {
    for {
      raml     <- dialectObject.raml()
      document <- raml.document()
    } yield {
      fillDeclarations(document.declares(), dialectMap)
    }
  }

  private def fillDeclarations(declarations: Seq[DeclarationObject], dialectMap: mutable.Map[String, DialectNode]) = {
    val dmap: mutable.Map[String, DialectNode] = mutable.Map()
    for {
      declaration  <- declarations
      nodeName     <- declaration.name()
      resolvedNode <- declaration.resolvedDeclaredNode()
    } yield {
      dialectMap.get(resolvedNode.entity.id).foreach(d => dmap.put(nodeName, d))
    }
    dmap
  }

  private def fillHashes(propertyMap: mutable.Map[DialectPropertyMapping, PropertyMappingObject]) = {
    for {
      (dialectPropertyMapping, v) <- propertyMap
      hash                        <- v.hash()
    } yield {
      val range = dialectPropertyMapping.range

      if (dialectPropertyMapping.unionTypes.isDefined) {
        dialectPropertyMapping.unionTypes.get.foreach { unionOption =>
          processHashRange(dialectPropertyMapping, hash, unionOption)
        }
      } else {
        processHashRange(dialectPropertyMapping, hash, range)
      }
    }
  }

  private def processHashRange(dialectPropertyMapping: DialectPropertyMapping, hash: String, r: Type) = {
    r match {
      case rangeNode: DialectNode =>
        for {
          property <- rangeNode.mappings() if property.iri() == hash
        } yield {
          connectHash(dialectPropertyMapping, property, r)
        }
      case _ => // ignore
    }
  }

  private def connectHash(hashedProperty: DialectPropertyMapping, hashProperty: DialectPropertyMapping, r: Type) = {
    hashedProperty.owningNode.get.add(hashedProperty.copy(hash = Option(hashProperty)))
    r.asInstanceOf[DialectNode].add(hashProperty.copy(noRAML = true))
  }

  def parsePropertyMapping(
      domainEntity: PropertyMappingObject,
      dialects: mutable.Map[String, DialectNode],
      props: mutable.Map[DialectPropertyMapping, PropertyMappingObject]): DialectPropertyMapping = {

    val name               = domainEntity.name()
    val `type`: List[Type] = resolveType(domainEntity, dialects, props)

    var res = DialectPropertyMapping(name.get, `type`.head)
    if (`type`.size > 1) {
      res = res.copy(unionTypes = Some(`type`))
    }
    domainEntity.mandatory().foreach { mandatory =>
      res = res.copy(required = mandatory)
    }

    domainEntity.allowMultiple().foreach { isCollection =>
      res = res.copy(collection = isCollection)
    }

    domainEntity.propertyTerm().foreach { term =>
      NamespaceMap(term) foreach { ns =>
        res = res.copy(namespace = Some(ns.namespace), rdfName = Some(ns.name))
      }
    }
    domainEntity.maximum().foreach { m =>
      res = res.copy(maximum = Some(m.toInt))
    }
    domainEntity.minimum().foreach { m =>
      res = res.copy(minimum = Some(m.toInt))
    }
    domainEntity.pattern().foreach { p =>
      res = res.copy(pattern = Some(p))
    }
    domainEntity
      .defaultValue()
      .foreach(v => {
        val sc: AmfScalar = res.range match {
          case Type.Int  => AmfScalar(v.toInt)
          case Type.Bool => AmfScalar(v.toBoolean)
          case _         => AmfScalar(v)
        }
        res = res.copy(defaultValue = Some(sc))
      })
    val ev = domainEntity.`enum`()

    if (ev.nonEmpty) {
      res = res.copy(`enum` = Some(ev))
    }

    res
  }

  private def resolveType(domainEntity: PropertyMappingObject,
                          dialects: mutable.Map[String, DialectNode],
                          props: mutable.Map[DialectPropertyMapping, PropertyMappingObject]): List[Type] = {
    val strings = domainEntity.entity.rawstrings(PropertyMapping.range)

    domainEntity.resolvedRange().zip(strings).map { rr =>
      val (range, rangeString) = rr
      if (range.isDefined) {
        // this is locally defined type
        val value = range.get
        val id    = value.entity.id
        if (dialects.contains(id)) {
          // return node from global declarations
          dialects(id)
        } else {
          // this is inplace range type definition
          registerType(value, dialects)
          parseNodeMapping(value, dialects, props)
        }
      } else {
        val imported=imports.find(e=>e.entity.id==rangeString)
        if (imported.isDefined){
            dialects(imported.get.entity.id)
        }
        else {
          // this is built in type
          builtins.buitInType(rangeString) match {
            case Some(t) => t
            case None =>
              throw new Exception(s"Cannot find dialect node type for $rangeString")
          }
        }
      }
    }
  }

  def parseNodeMapping(domainEntity: NodeDefinitionObject,
                       dialects: mutable.Map[String, DialectNode],
                       props: mutable.Map[DialectPropertyMapping, PropertyMappingObject]): DialectNode = {
    val node = dialects(domainEntity.entity.id)

    domainEntity.mapping().foreach { p =>
      val mapping = node.add(parsePropertyMapping(p, dialects, props))
      props.put(mapping, p);
    }

    node
  }
}
