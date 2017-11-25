package amf.plugins.document.vocabularies.core

import amf.core.Root
import amf.dialects.RAML_1_0_DialectTopLevel
import amf.dialects.RAML_1_0_DialectTopLevel.{DeclarationObject, NodeDefinitionObject, PropertyMappingObject}
import amf.core.model.document.{BaseUnit, Document, Module}
import amf.core.metamodel.Type
import amf.core.model.domain.AmfScalar
import amf.plugins.document.vocabularies.model.domain.DomainEntity
import amf.plugins.document.vocabularies.spec
import amf.plugins.document.vocabularies.spec._
import amf.plugins.document.webapi.model.DialectFragment
import amf.core.vocabulary.Namespace

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

class DialectLoader(val document: BaseUnit) {

  val builtins: TypeBuiltins = new TypeBuiltins {
    override def resolveToEntity(root: Root, name: String, t: Type): Option[DomainEntity] = None
    override def resolveRef(ref: String): Option[String]                                  = None
  }

  private def retrieveDomainEntity(unit: BaseUnit) = unit match {
    case document: Document => document.encodes.asInstanceOf[DomainEntity]
    case _                  => throw new Exception(s"Cannot load a dialect from a unit that is not a document $unit")
  }

  def loadDialect(): Dialect = loadDialect(retrieveDomainEntity(document), document)

  private def registerType(n: NodeDefinitionObject, dialectMap: mutable.Map[String, DialectNode]) =
    NamespaceMap(n.classTerm().get) match {
      case Some(ns) =>
        val node = new DialectNode(ns.name, ns.namespace)
        node.id = Some(n.entity.id)
        dialectMap.put(n.entity.id, node)
      case _ => // ignore
    }

  /**
    * This is loading all declared entities in the referenced libraries library
    * and loading them lazily into imports
    */
  lazy val imports: List[NodeDefinitionObject] = {
    var imports       = List[NodeDefinitionObject]()
    val dialectObject = RAML_1_0_DialectTopLevel.dialectObject(retrieveDomainEntity(document))

    document.references.foreach {
      case module: Module =>
        module.declares.foreach {
            case declaredEntity: DomainEntity => imports=imports. ::(NodeDefinitionObject(declaredEntity, Some(dialectObject)))

            case _ => // not possible
          }

      case _ => // ignore libraries
    }
    // return the accumulated declarations
    imports
  }
   def resolvedEncodes(ramlDocument:RAML_1_0_DialectTopLevel.DocumentContentDeclarationObject,u:BaseUnit): Option[RAML_1_0_DialectTopLevel.NodeDefinitionObject] ={
     val r=ramlDocument.resolvedEncodes();
     if (r.isDefined){
       r
     }
     else{
       val enc=ramlDocument.encodes();
       enc.flatMap(value=>{
         var element:Option[DomainEntity]=None;
         u.references.foreach {
           case r: DialectFragment => {
             if (r.encodes.id == value) {
               element = Some(r.encodes.asInstanceOf[DomainEntity]);
             }
           }
           case m: Module => {
             m.declares.foreach(v => {
               if (v.id == value) {
                 element = Some(v.asInstanceOf[DomainEntity]);
               }
             })
           }
           case _ => {}
         }

         element.map(RAML_1_0_DialectTopLevel.NodeDefinitionObject(_,Some(ramlDocument.root)));
       })

     }
   }

  def loadDialect(domainEntity: DomainEntity, unit: BaseUnit): Dialect = {
    val modelDocument = unit.asInstanceOf[Document]
    val dialectObject = RAML_1_0_DialectTopLevel.dialectObject(domainEntity)

    val rootEntity = for {
      ramlNode     <- dialectObject.raml()
      ramlDocument <- ramlNode.document()
      root         <-  resolvedEncodes(ramlDocument,unit)

    } yield {
      root
    }

    rootEntity match {

      case Some(encodedRootEntity) =>
        val dialectMap = mutable.Map[String, DialectNode]()
        processMappings(encodedRootEntity, dialectObject, dialectMap, imports)

        val dialect = for {
          dialectName    <- dialectObject.dialect()
          dialectVersion <- dialectObject.version()
          dialectNode    <- dialectMap.get(encodedRootEntity.entity.id)
        } yield {

          val fragmentList: mutable.Map[String, DialectNode] = processFragments(dialectObject, dialectMap)

          val moduleInfo = processModuleInfo(dialectObject, dialectMap)
          processDeclarationsInfo(dialectNode, dialectObject, dialectMap)

          spec.Dialect(dialectName,
                  dialectVersion,
                  dialectNode,
                  resolver = (root, refs, ctx) => BasicResolver(root, List(), refs)(ctx),
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
          fragmentName         <- encodedFragment.id()
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
      case Some(declarationMap: mutable.Map[String, DialectNode]) if declarationMap.nonEmpty =>
        val moduleNode = new DialectNode("module", Namespace.Document)
        declarationMap.keys.foreach { key =>
          moduleNode.map(key,
                         DialectPropertyMapping("name", Type.Str, namespace = Some(Namespace.Schema)),
                         declarationMap(key),
                         _.copy(isDeclaration = true))
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
      case Some(declarationMap: mutable.Map[String, DialectNode]) if declarationMap.nonEmpty =>
        declarationMap.keys.foreach { key =>
          documentDialectNode.map(key,
                                  DialectPropertyMapping("name", Type.Str, namespace = Some(Namespace.Schema)),
                                  declarationMap(key),
                                  _.copy(isDeclaration = true))
        }
      case _ =>
    }
  }

  private def processMappings(encodedRootEntity: RAML_1_0_DialectTopLevel.NodeDefinitionObject,
                              dialectObject: RAML_1_0_DialectTopLevel.dialectObject,
                              dialectMap: mutable.Map[String, DialectNode],
                              imports: List[NodeDefinitionObject]) = {

    val propertyMap = mutable.Map[DialectPropertyMapping, PropertyMappingObject]()

    // process all the node mappings
    dialectObject.nodeMappings().foreach { registerType(_, dialectMap) }
    imports.foreach(registerType(_, dialectMap));
    imports.foreach { n =>
      parseNodeMapping(n, dialectMap, propertyMap)
    }
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
      nodeName     <- declaration.id()
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
          processHashRange(dialectPropertyMapping, hash, v.hashValue(), unionOption)
        }
      } else {
        processHashRange(dialectPropertyMapping, hash, v.hashValue(), range)
      }
    }
  }

  private def processHashRange(dialectPropertyMapping: DialectPropertyMapping,
                               hash: String,
                               hashValue: Option[String],
                               r: Type) = {
    r match {
      case rangeNode: DialectNode if hashValue.isDefined =>
        for {
          property      <- rangeNode.mappings() if property.iri() == hash
          valueProperty <- rangeNode.mappings() if valueProperty.iri() == hashValue.get
        } yield {
          connectHash(dialectPropertyMapping, property, Some(valueProperty), r)
        }
      case rangeNode: DialectNode =>
        for {
          property <- rangeNode.mappings() if property.iri() == hash
        } yield {
          connectHash(dialectPropertyMapping, property, None, r)
        }
      case _ => // ignore
    }
  }

  private def connectHash(hashedProperty: DialectPropertyMapping,
                          hashProperty: DialectPropertyMapping,
                          hasPropertyValue: Option[DialectPropertyMapping],
                          r: Type) = {
    hashedProperty.owningNode.get.add(
      hashedProperty.copy(
        hash = Option(hashProperty),
        hashValue = hasPropertyValue
      ))
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
        val imported = imports.find(e => e.entity.id == rangeString)
        if (imported.isDefined) {
          dialects(imported.get.entity.id)
        } else {
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
