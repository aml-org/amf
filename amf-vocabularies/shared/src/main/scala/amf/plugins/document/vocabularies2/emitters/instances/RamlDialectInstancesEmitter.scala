package amf.plugins.document.vocabularies2.emitters.instances

import amf.core.annotations.LexicalInformation
import amf.core.emitter.BaseEmitters.ValueEmitter
import amf.core.emitter.SpecOrdering.Lexical
import amf.core.emitter.{Emitter, EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.Position.ZERO
import amf.core.parser.{FieldEntry, Fields, Position, Value}
import amf.plugins.document.vocabularies2.model.document.{Dialect, DialectInstance}
import amf.plugins.document.vocabularies2.model.domain._
import org.yaml.model.YDocument
import amf.core.emitter.BaseEmitters._

case class RamlDialectInstancesEmitter(instance: DialectInstance, dialect: Dialect) {

  val ordering: SpecOrdering = Lexical

  def emitInstance(): YDocument = {
    YDocument(b => {
      b.comment(s"%${dialect.name()} ${dialect.version()}")
      DialectNodeEmitter(instance.encodes.asInstanceOf[DialectDomainElement], rootNodeMapping(), instance, dialect, ordering).emit(b)
    })
  }

  def rootNodeMapping(): NodeMapping = {
    dialect.declares.find {
      case nodeMapping: NodeMapping => nodeMapping.id == dialect.documents().root().encoded()
    }.getOrElse {
      throw new Exception(s"Cannot find root level encode node mapping ${dialect.documents().root().encoded()}")
    }.asInstanceOf[NodeMapping]
  }
}

case class DialectNodeEmitter(node: DialectDomainElement,
                              nodeMapping: NodeMapping,
                              instance: DialectInstance,
                              dialect: Dialect,
                              ordering: SpecOrdering,
                              keyPropertyId: Option[String] = None) extends PartEmitter {

  override def emit(b: YDocument.PartBuilder): Unit = {
    var emitters: Seq[EntryEmitter] = Nil
    node.dynamicFields.foreach { field =>
      findPropertyMapping(field) foreach { propertyMapping =>
        if (keyPropertyId.isEmpty || propertyMapping.id != keyPropertyId.get) {
          val key = propertyMapping.name()
          val propertyClassification = propertyMapping.classification()
          node.valueForField(field) match {
            case Some(scalar: AmfScalar) =>
              emitters ++= Seq(ValueEmitter(key, FieldEntry(field, Value(scalar, scalar.annotations))))

            case Some(array: AmfArray) if propertyClassification == LiteralPropertyCollection =>
              emitters ++= Seq(ArrayEmitter(key, FieldEntry(field, Value(array, array.annotations)), ordering))

            case Some(element: DialectDomainElement) if propertyClassification == ObjectProperty && !propertyMapping.isUnion =>
              val nextNodeMapping = findNodeMapping(propertyMapping.objectRange().head, dialect)
              emitters ++= Seq(EntryPartEmitter(key, DialectNodeEmitter(element, nextNodeMapping, instance, dialect, ordering)))

            case Some(array: AmfArray) if propertyClassification == ObjectPropertyCollection && !propertyMapping.isUnion =>
              emitters ++= Seq(new EntryEmitter() {
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

            case Some(array: AmfArray) if propertyClassification == ObjectMapProperty =>
              emitters ++= Seq(new EntryEmitter() {
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
        }
      }
    }

    // finally emit the object
    b.obj { b => ordering.sorted(emitters).foreach(_.emit(b))}
  }

  override def position(): Position = node.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)

  protected def findPropertyMapping(field: Field): Option[PropertyMapping] = {
    val iri = field.value.iri()
    nodeMapping.propertiesMapping().find(_.nodePropertyMapping() == iri)
  }

  protected def findNodeMapping(nodeMappingId: String, dialect: Dialect): NodeMapping =
    dialect.declares.find(_.id == nodeMappingId).getOrElse { throw new Exception(s"Cannot find nodeMapping $nodeMappingId") }.asInstanceOf[NodeMapping]

}
