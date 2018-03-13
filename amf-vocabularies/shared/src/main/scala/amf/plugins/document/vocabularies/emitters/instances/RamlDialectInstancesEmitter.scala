package amf.plugins.document.vocabularies.emitters.instances

import amf.core.annotations.{Aliases, LexicalInformation}
import amf.core.emitter.BaseEmitters.{ValueEmitter, _}
import amf.core.emitter.SpecOrdering.Lexical
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.Position.ZERO
import amf.core.parser.{FieldEntry, Position, Value}
import amf.core.utils._
import amf.plugins.document.vocabularies.annotations.{AliasesLocation, CustomId}
import amf.plugins.document.vocabularies.emitters.common.IdCounter
import amf.plugins.document.vocabularies.model.document._
import amf.plugins.document.vocabularies.model.domain._
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.{YDocument, YNode}

trait DialectEmitterHelper {
  val dialect: Dialect

  def findNodeMappingById(nodeMappingId: String): NodeMapping = {
    maybeFindNodeMappingById(nodeMappingId).getOrElse{
      throw new Exception(s"Cannot find node mapping $nodeMappingId")
    }
  }

  def maybeFindNodeMappingById(nodeMappingId: String): Option[NodeMapping] = {
    dialect.declares.find {
      case nodeMapping: NodeMapping => nodeMapping.id == nodeMappingId
    }.asInstanceOf[Option[NodeMapping]].orElse {
      dialect.references.collect {
        case lib: DialectLibrary =>
          lib.declares.find(_.id == nodeMappingId)
      }.collectFirst { case Some(mapping: NodeMapping) =>
        mapping
      }
    }
  }
}

case class ReferencesEmitter(baseUnit: BaseUnit, ordering: SpecOrdering, aliases: Map[String,(String, String)]) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val modules = baseUnit.references.collect({ case m: DeclaresModel => m })
    if (modules.nonEmpty) {

      b.entry("uses", _.obj { b =>
        traverse(ordering.sorted(modules.map(r => ReferenceEmitter(r, ordering, aliases))), b)
      })
    }
  }

  override def position(): Position =
    baseUnit.annotations.find(classOf[AliasesLocation]).map(annot => Position((annot.position, 0))).getOrElse(ZERO)
}

case class ReferenceEmitter(reference: BaseUnit, ordering: SpecOrdering, aliases: Map[String,(String, String)])
  extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    aliases.get(reference.id) match {
      case Some((alias, location)) =>
        MapEntryEmitter(alias, location).emit(b)
      case _ => // TODO: emit violation
    }
  }

  override def position(): Position = ZERO
}


case class RamlDialectInstancesEmitter(instance: DialectInstance, dialect: Dialect) extends DialectEmitterHelper  {
  val ordering: SpecOrdering = Lexical
  val aliases:  Map[String,(String, String)] = collectAliases()

  def collectAliases(): Map[String,(String, String)] = {
    val vocabFile = instance.location.split("/").last
    val vocabFilePrefix = instance.location.replace(vocabFile, "")

    val maps = instance.annotations.find(classOf[Aliases]).map { aliases =>
      aliases.aliases.foldLeft(Map[String,String]()) { case (acc, (alias, id)) =>
        acc + (id -> alias)
      }
    }.getOrElse(Map())
    val idCounter = new IdCounter()
    instance.references.foldLeft(Map[String,(String,String)]()) {
      case (acc: Map[String,(String,String)], m: DeclaresModel) =>
        val location = Option(m.location).getOrElse(m.id).replace("#","")
        val importLocation: String = if (location.contains(vocabFilePrefix)) {
          location.replace(vocabFilePrefix, "")
        } else {
          location.replace("file://", "")
        }

        if (maps.get(m.id).isDefined) {
          val alias = maps(m.id)
          acc + (m.id -> (alias, importLocation))
        } else {
          val nextAlias = idCounter.genId("uses_")
          acc + (m.id -> (nextAlias, importLocation))
        }
      case (acc: Map[String,(String,String)], _) => acc
    }
  }


  def emitInstance(): YDocument = {
    YDocument(b => {
      b.comment(s"%${dialect.name()} ${dialect.version()}")
      val rootNodeMapping = findNodeMappingById(dialect.documents().root().encoded())
      DialectNodeEmitter(instance.encodes.asInstanceOf[DialectDomainElement], rootNodeMapping, instance, dialect, ordering, aliases, None, rootNode = true).emit(b)
    })
  }
}

case class DeclarationsGroupEmitter(declared: Seq[DialectDomainElement],
                                    publicNodeMapping: PublicNodeMapping,
                                    nodeMapping: NodeMapping,
                                    instance: DialectInstance,
                                    dialect: Dialect,
                                    ordering: SpecOrdering,
                                    aliases: Map[String, (String, String)],
                                    keyPropertyId: Option[String] = None) extends EntryEmitter with DialectEmitterHelper {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    val declarationKey = publicNodeMapping.name()
    b.entry(declarationKey, _.obj { b =>
      sortedDeclarations().foreach { decl =>
        b.entry(
          YNode(decl.id.split("#").last.split("/").last.urlDecoded), // we are using the last part of the URL as the identifier in dialects
          (b) => DialectNodeEmitter(decl, nodeMapping, instance, dialect, ordering, aliases).emit(b)
        )
      }
    })
  }

  override def position(): Position =
    declared
      .flatMap(_.annotations.find(classOf[LexicalInformation]).map { lexInfo => lexInfo.range.start })
      .sorted
      .headOption
      .getOrElse(ZERO)

  def sortedDeclarations() = {
    declared.sortBy(_.annotations.find(classOf[LexicalInformation]).map { lexInfo => lexInfo.range.start }.getOrElse(ZERO))
  }
}

case class DialectNodeEmitter(node: DialectDomainElement,
                              nodeMapping: NodeMapping,
                              instance: DialectInstance,
                              dialect: Dialect,
                              ordering: SpecOrdering,
                              aliases: Map[String, (String, String)],
                              keyPropertyId: Option[String] = None,
                              rootNode: Boolean = false,
                              discriminator: Option[(String, String)] = None) extends PartEmitter with DialectEmitterHelper {

  override def emit(b: YDocument.PartBuilder): Unit = {
    if (node.isLink) {
      if (isFragment(node, instance)) emitLink(node).emit(b)
      else if(isLibrary(node, instance)) {
        emitLibrarRef(node, instance).emit(b)
      } else {
        emitRef(node).emit(b)
      }
    } else {
      var emitters: Seq[EntryEmitter] = Nil
      if(discriminator.isDefined) {
        val (discriminatorName, discriminatorValue) = discriminator.get
        emitters ++= Seq(MapEntryEmitter(discriminatorName, discriminatorValue))
      }
      if (node.annotations.find(classOf[CustomId]).isDefined) {
        val customId = if (node.id.contains(dialect.location)) {
          node.id.replace(dialect.id,"")
        } else {
          node.id
        }
        emitters ++= Seq(MapEntryEmitter("$id", customId))
      }
      node.dynamicFields.foreach { field =>
        findPropertyMapping(field) foreach { propertyMapping =>
          if (keyPropertyId.isEmpty || propertyMapping.nodePropertyMapping() != keyPropertyId.get) {

            val key = propertyMapping.name()
            val propertyClassification = propertyMapping.classification()

            val nextEmitter: Seq[EntryEmitter] = node.valueForField(field) match {
              case Some(scalar: AmfScalar) =>
                emitScalar(key, field, scalar)

              case Some(array: AmfArray) if propertyClassification == LiteralPropertyCollection =>
                emitScalarArray(key, field, array)

              case Some(element: DialectDomainElement) if propertyClassification == ObjectProperty && !propertyMapping.isUnion =>
                emitObject(key, element, propertyMapping)

              case Some(array: AmfArray) if propertyClassification == ObjectPropertyCollection && !propertyMapping.isUnion =>
                emitObjectArray(key, array, propertyMapping)

              case Some(array: AmfArray) if propertyClassification == ObjectMapProperty =>
                emitObjectMap(key, array, propertyMapping)

              case Some(element: DialectDomainElement) if propertyClassification == ObjectProperty && propertyMapping.isUnion =>
                emitObjectUnion(key, element, propertyMapping)

              case Some(array: AmfArray) if propertyClassification == ObjectPropertyCollection && propertyMapping.isUnion =>
                emitObjectUnionArray(key, array, propertyMapping)

              case Some(array: AmfArray) if propertyClassification == ObjectPairProperty =>
                emitObjectPairs(key, array, propertyMapping)
            }
            emitters ++= nextEmitter
          }
        }
      }

      // in case this is the root dialect node, we look for declarations
      if (rootNode)
        emitters ++= declarationsEmitters(b)

      // and also for use of libraries
      if (rootNode)
        emitters ++= Seq(ReferencesEmitter(instance, ordering, aliases))

      // finally emit the object
      b.obj { b => ordering.sorted(emitters).foreach(_.emit(b)) }
    }
  }

  override def position(): Position = node.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)

  protected def emitLink(node: DialectDomainElement): PartEmitter = new PartEmitter {
    override def emit(b: YDocument.PartBuilder): Unit =
      b += YNode.include(node.includeName)

    override def position(): Position = node.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
  }

  protected def emitRef(node: DialectDomainElement): PartEmitter = TextScalarEmitter(node.localRefName, node.annotations)


  protected def emitScalar(key: String, field: Field, scalar: AmfScalar): Seq[EntryEmitter] =
    Seq(ValueEmitter(key, FieldEntry(field, Value(scalar, scalar.annotations))))

  protected def emitScalarArray(key: String, field: Field, array: AmfArray): Seq[EntryEmitter] =
    Seq(ArrayEmitter(key, FieldEntry(field, Value(array, array.annotations)), ordering))

  protected def emitObjectUnion(key: String, element: DialectDomainElement, propertyMapping: PropertyMapping): Seq[EntryEmitter] = {
    val nodeMappings: Seq[NodeMapping] = propertyMapping.objectRange().map { rangeNodeMapping =>
      findNodeMapping(rangeNodeMapping, dialect)
    }

    nodeMappings.find(nodeMapping => element.dynamicType.map(_.iri()).contains(nodeMapping.nodetypeMapping)) match {
      case Some(nextNodeMapping) => Seq(EntryPartEmitter(key, DialectNodeEmitter(element, nextNodeMapping, instance, dialect, ordering, aliases)))
      case _                     => Nil // TODO: raise violation
    }
  }

  protected def emitObjectUnionArray(key: String, array: AmfArray, propertyMapping: PropertyMapping): Seq[EntryEmitter] = {
    Seq(new EntryEmitter() {
      // potential node range based in the objectRange
      val objectRangeMappings: Seq[NodeMapping] = Option(propertyMapping.objectRange()).getOrElse(Nil).map { rangeNodeMapping =>
        findNodeMapping(rangeNodeMapping, dialect)
      }

      // potential node range based in discriminators map
      val discriminatorsMappings: Map[String, NodeMapping] = Option(propertyMapping.typeDiscrminator()).getOrElse(Map()).foldLeft(Map[String, NodeMapping]()) {
        case (acc, (alias, mappingId)) =>
          findNodeMapping(mappingId, dialect) match {
            case nodeMapping: NodeMapping => acc + (alias -> nodeMapping)
            case _                        => acc // TODO: violation here
          }
      }

      val arrayElements: Seq[DialectNodeEmitter] = array.values.map {
        case dialectDomainElement: DialectDomainElement =>
          val elementTypes = dialectDomainElement.dynamicType.map(_.iri())
          objectRangeMappings.find(nodeMapping => elementTypes.contains(nodeMapping.nodetypeMapping)) match {
            case Some(nextNodeMapping) =>
              Some(DialectNodeEmitter(dialectDomainElement, nextNodeMapping, instance, dialect, ordering, aliases))
            case _ => discriminatorsMappings.find { case (_, discriminatorMapping) => elementTypes.contains(discriminatorMapping.nodetypeMapping) } match {
              case Some((alias, discriminatorMapping)) =>
                val discriminatorName = propertyMapping.typeDiscriminatorName()
                Some(DialectNodeEmitter(dialectDomainElement, discriminatorMapping, instance, dialect, ordering, aliases, discriminator = Some((discriminatorName, alias))))
              case None => None
            }
          }
        case _ => None
      } collect { case Some(parsed) => parsed }

      override def emit(b: YDocument.EntryBuilder): Unit = {
        b.entry(key, _.list { b =>
          ordering.sorted(arrayElements).foreach(_.emit(b))
        })
      }

      override def position(): Position = array.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)

    })
  }

  protected def emitObject(key: String, element: DialectDomainElement, propertyMapping: PropertyMapping): Seq[EntryEmitter] = {
    val nextNodeMapping = findNodeMapping(propertyMapping.objectRange().head, dialect)
    Seq(EntryPartEmitter(key, DialectNodeEmitter(element, nextNodeMapping, instance, dialect, ordering, aliases)))
  }

  protected def emitObjectArray(key: String, array: AmfArray, propertyMapping: PropertyMapping): Seq[EntryEmitter] = {
    Seq(new EntryEmitter() {
      val nextNodeMapping: NodeMapping = findNodeMapping(propertyMapping.objectRange().head, dialect)
      val arrayElements: Seq[DialectNodeEmitter] = array.values.map { case dialectDomainElement: DialectDomainElement =>
        DialectNodeEmitter(dialectDomainElement, nextNodeMapping, instance, dialect, ordering, aliases)
      }

      override def emit(b: YDocument.EntryBuilder): Unit = {
        b.entry(key, _.list { b =>
          ordering.sorted(arrayElements).foreach(_.emit(b))
        })
      }

      override def position(): Position = array.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)

    })
  }

  protected def emitObjectMap(key: String, array: AmfArray, propertyMapping: PropertyMapping): Seq[EntryEmitter] = {
    Seq(new EntryEmitter() {
      // val nextNodeMapping: NodeMapping = findNodeMapping(propertyMapping.objectRange().head, dialect)
      val nextNodeMappings: Seq[NodeMapping] = propertyMapping.objectRange().map { rangeNodeMapping =>
        findNodeMapping(rangeNodeMapping, dialect)
      }
      val mapElements = array.values.foldLeft(Map[Option[DialectNodeEmitter], DialectDomainElement]()) {
        case (acc, dialectDomainElement: DialectDomainElement) =>
          nextNodeMappings.find(nodeMapping => dialectDomainElement.dynamicType.map(_.iri()).contains(nodeMapping.nodetypeMapping)) match {
            case Some(nextNodeMapping) =>
              acc + (Some(DialectNodeEmitter(dialectDomainElement, nextNodeMapping, instance, dialect, ordering, aliases, Some(propertyMapping.mapKeyProperty()))) -> dialectDomainElement)
            case _ =>
              acc // TODO: raise violation
          }
        case (acc, _) => acc
      } collect { case (Some(parsed),x) => (parsed, x) }

      /*
      val mapElements = array.values.foldLeft(Map[DialectNodeEmitter, DialectDomainElement]()) { case (acc, dialectDomainElement: DialectDomainElement) =>
        acc + (DialectNodeEmitter(dialectDomainElement, nextNodeMapping, instance, dialect, ordering, aliases, Some(propertyMapping.mapKeyProperty())) -> dialectDomainElement)
      }
      */

      override def emit(b: YDocument.EntryBuilder): Unit = {
        b.entry(key, _.obj { b =>
          ordering.sorted(mapElements.keys.toSeq).foreach { emitter =>
            val dialectDomainElement = mapElements(emitter)
            val mapKeyField = dialectDomainElement.dynamicFields.find(_.value.iri() == propertyMapping.mapKeyProperty()).get
            val mapKeyValue = dialectDomainElement.valueForField(mapKeyField).get.toString
            EntryPartEmitter(mapKeyValue, emitter).emit(b)
          }
        })
      }

      override def position(): Position = array.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
    })
  }

  protected def emitObjectPairs(key: String, array: AmfArray, propertyMapping: PropertyMapping): Seq[EntryEmitter] = {
    val keyProperty = propertyMapping.mapKeyProperty()
    val valueProperty = propertyMapping.mapValueProperty()

    Seq(new EntryEmitter() {
      override def emit(b: YDocument.EntryBuilder): Unit = {
        b.entry(key, _.obj { b =>
          val sortedElements = array.values.sortBy { elem =>
            elem.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
          }
          sortedElements.foreach {
            case element: DialectDomainElement =>
              val keyField = element.dynamicFields.find(_.value.iri() == keyProperty)
              val valueField = element.dynamicFields.find(_.value.iri() == valueProperty)
              if (keyField.isDefined && valueField.isDefined) {
                val keyLiteral = element.valueForField(keyField.get)
                val valueLiteral = element.valueForField(valueField.get)
                (keyLiteral, valueLiteral) match {
                  case (Some(keyScalar: AmfScalar), Some(valueScalar: AmfScalar)) =>
                    MapEntryEmitter(keyScalar.value.toString, valueScalar.value.toString).emit(b)
                  case _ => throw new Exception("Cannot generate object pair without scalar values for key and value")
                }
              } else {
                throw new Exception("Cannot generate object pair with undefined key or value")
              }
            case _ => // ignore
          }
        })
      }

      override def position(): Position = array.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
    })
  }

  def isFragment(elem: DialectDomainElement, instance: DialectInstance): Boolean = {
    elem.linkTarget match {
      case Some(domainElement) =>
        instance.references.exists {
          case ref: DialectInstanceFragment => ref.encodes.id == domainElement.id
          case _                            => false
        }
      case _                   =>  throw new Exception(s"Cannot check fragment for an element without target for element ${elem.id}")
    }
  }

  def isLibrary(elem: DialectDomainElement, instance: DialectInstance): Boolean = {
    instance.references.exists {
      case lib: DeclaresModel =>
        lib.declares.exists(_.id == elem.linkTarget.get.id)
      case _ => false
    }
  }

  def emitLibrarRef(elem: DialectDomainElement, instance: DialectInstance): TextScalarEmitter = {
    val lib = instance.references.find {
      case lib: DeclaresModel =>
        lib.declares.exists(_.id == elem.linkTarget.get.id)
      case _ => false
    }
    val alias = aliases(lib.get.id)._1
    TextScalarEmitter(s"$alias.${elem.localRefName}", elem.annotations)
  }

  def declarationsEmitters(b: YDocument.PartBuilder): Seq[EntryEmitter] = {
    val emitters = for {
      docs         <- Option(dialect.documents())
      root         <- Option(docs.root())
      declarations <- Option(root.declaredNodes())
    } yield {
      if (root.encoded() == node.id) {
        Nil
      } else {
        declarations.foldLeft(Seq[EntryEmitter]()) { case (acc, publicNodeMapping) =>
          val declared = instance.declares.collect {
            case elem: DialectDomainElement if elem.definedBy.id == publicNodeMapping.mappedNode() => elem
          }
          if (declared.nonEmpty) {
            val nodeMapping = findNodeMappingById(publicNodeMapping.mappedNode())
            acc ++ Seq(DeclarationsGroupEmitter(declared, publicNodeMapping, nodeMapping, instance, dialect, ordering, aliases))
          } else acc
        }
      }
    }
    emitters.getOrElse(Nil)
  }

  protected def findPropertyMapping(field: Field): Option[PropertyMapping] = {
    val iri = field.value.iri()
    nodeMapping.propertiesMapping().find(_.nodePropertyMapping() == iri)
  }

  protected def findNodeMapping(nodeMappingId: String, dialect: Dialect): NodeMapping =
    dialect.declares.find(_.id == nodeMappingId).getOrElse {
      throw new Exception(s"Cannot find nodeMapping $nodeMappingId")
    }.asInstanceOf[NodeMapping]

}
