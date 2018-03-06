package amf.plugins.document.vocabularies2.emitters.instances

import amf.core.annotations.LexicalInformation
import amf.core.emitter.BaseEmitters.{ValueEmitter, _}
import amf.core.emitter.SpecOrdering.Lexical
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.Position.ZERO
import amf.core.parser.{FieldEntry, Position, Value}
import amf.core.utils._
import amf.plugins.document.vocabularies2.model.document.{Dialect, DialectInstance, DialectInstanceFragment}
import amf.plugins.document.vocabularies2.model.domain._
import org.yaml.model.{YDocument, YNode}

trait DialectEmitterHelper {
  val dialect: Dialect

  def findNodeMappingById(nodeMappingId: String): NodeMapping = {
    dialect.declares.find {
      case nodeMapping: NodeMapping => nodeMapping.id == nodeMappingId
    }.getOrElse {
      throw new Exception(s"Cannot find node mapping $nodeMappingId")
    }.asInstanceOf[NodeMapping]
  }
}

case class RamlDialectInstancesEmitter(instance: DialectInstance, dialect: Dialect) extends DialectEmitterHelper  {
  val ordering: SpecOrdering = Lexical

  def emitInstance(): YDocument = {
    YDocument(b => {
      b.comment(s"%${dialect.name()} ${dialect.version()}")
      val rootNodeMapping = findNodeMappingById(dialect.documents().root().encoded())
      DialectNodeEmitter(instance.encodes.asInstanceOf[DialectDomainElement], rootNodeMapping, instance, dialect, ordering, None, rootNode = true).emit(b)
    })
  }
}

case class DeclarationsGroupEmitter(declared: Seq[DialectDomainElement],
                                    publicNodeMapping: PublicNodeMapping,
                                    nodeMapping: NodeMapping,
                                    instance: DialectInstance,
                                    dialect: Dialect,
                                    ordering: SpecOrdering,
                                    keyPropertyId: Option[String] = None) extends EntryEmitter with DialectEmitterHelper {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    val declarationKey = publicNodeMapping.name()
    b.entry(declarationKey, _.obj { b =>
      sortedDeclarations().foreach { decl =>
        b.entry(
          YNode(decl.id.split("#").last.split("/").last.urlDecoded), // we are using the last part of the URL as the identifier in dialects
          (b) => DialectNodeEmitter(decl, nodeMapping, instance, dialect, ordering).emit(b)
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
                              keyPropertyId: Option[String] = None,
                              rootNode: Boolean = false) extends PartEmitter with DialectEmitterHelper {

  override def emit(b: YDocument.PartBuilder): Unit = {
    if (node.isLink) {
      if (isFragment(node, instance)) emitLink(node).emit(b)
      else emitRef(node).emit(b)
    } else {
      var emitters: Seq[EntryEmitter] = Nil
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
            }
            emitters ++= nextEmitter
          }
        }
      }

      // in case this is the root dialect node, we look for declarations
      if (rootNode)
        emitters ++= declarationsEmitters(b)

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

  protected def emitObject(key: String, element: DialectDomainElement, propertyMapping: PropertyMapping): Seq[EntryEmitter] = {
    val nextNodeMapping = findNodeMapping(propertyMapping.objectRange().head, dialect)
    Seq(EntryPartEmitter(key, DialectNodeEmitter(element, nextNodeMapping, instance, dialect, ordering)))
  }

  protected def emitObjectArray(key: String, array: AmfArray, propertyMapping: PropertyMapping): Seq[EntryEmitter] = {
    Seq(new EntryEmitter() {
      val nextNodeMapping: NodeMapping = findNodeMapping(propertyMapping.objectRange().head, dialect)
      val arrayElements: Seq[DialectNodeEmitter] = array.values.map { case dialectDomainElement: DialectDomainElement =>
        DialectNodeEmitter(dialectDomainElement, nextNodeMapping, instance, dialect, ordering)
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
      val nextNodeMapping: NodeMapping = findNodeMapping(propertyMapping.objectRange().head, dialect)
      val mapElements = array.values.foldLeft(Map[DialectNodeEmitter, DialectDomainElement]()) { case (acc, dialectDomainElement: DialectDomainElement) =>
        acc + (DialectNodeEmitter(dialectDomainElement, nextNodeMapping, instance, dialect, ordering, Some(propertyMapping.mapKeyProperty())) -> dialectDomainElement)
      }

      override def emit(b: YDocument.EntryBuilder): Unit = {
        b.entry(key, _.obj { b =>
          ordering.sorted(mapElements.keys.toSeq).foreach { emitter =>
            val dialectDomainElement = mapElements(emitter)
            val mapKeyValue = dialectDomainElement.fields.fields().find(_.field.value.iri() == propertyMapping.mapKeyProperty()).get.value.toString
            EntryPartEmitter(mapKeyValue, emitter).emit(b)
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
            acc ++ Seq(DeclarationsGroupEmitter(declared, publicNodeMapping, nodeMapping, instance, dialect, ordering))
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
    dialect.declares.find(_.id == nodeMappingId).getOrElse { throw new Exception(s"Cannot find nodeMapping $nodeMappingId") }.asInstanceOf[NodeMapping]

}
