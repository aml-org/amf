package amf.plugins.document.vocabularies.spec


import amf.core.annotations.Aliases
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{Emitter, EntryEmitter}
import amf.core.metamodel.domain.LinkableElementModel
import amf.core.model.document.{BaseUnit, Module}
import amf.core.model.domain.{AmfArray, AmfElement, AmfScalar}
import amf.core.parser.Position.ZERO
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.vocabularies.core.Vocabulary
import amf.plugins.document.vocabularies.model.domain.DomainEntity
import amf.plugins.document.vocabularies.spec.Dialect.retrieveDomainEntity
import org.yaml.model.YDocument
import org.yaml.model.YDocument.{BaseBuilder, EntryBuilder, PartBuilder}

import scala.collection.mutable

/**
  * Created by Pavel Petrochenko on 13/09/17.
  */
class DialectEmitter(val unit: BaseUnit) {

  val root: DomainEntity = retrieveDomainEntity(unit)
  var nameProvider: Option[LocalNameProvider] = root.definition.nameProvider match {
    case Some(np) => Some(np(unit))
    case None     => None
  }

  private val externals = mutable.Map[String, DomainEntity]()

  {
    root.fields
      .?[AmfArray](Vocabulary.externalTerms.field())
      .foreach(e => {
        e.values.foreach(e => {
          val entity = e.asInstanceOf[DomainEntity]
          externals.put(entity.id, entity)
        })

      })
  }

  private def emitRef(parent: DialectPropertyMapping, element: AmfElement, b: PartBuilder): Unit = {
    element match {
      case e: DomainEntity => ObjectEmitter(e).emit(b)
      case s: AmfScalar    => emitRef(parent, s.toString, b)
      case _               => throw new Exception("References can only be emitted from entities or scalars")
    }
  }

  case class RefValueEmitter(parent: DialectPropertyMapping, key: String, field: FieldEntry) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      sourceOr(field.value,
               b.entry(
                 key,
                 emitRef(parent, field.element, _)
               ))
    }

    override def position(): Position = pos(field.value.annotations)
  }

  /** Emit a single value from an array as an entry. */
  case class RefArrayValueEmitter(parent: DialectPropertyMapping, key: String, field: FieldEntry)
      extends EntryEmitter {

    override def emit(b: EntryBuilder): Unit = {
      sourceOr(
        field.value,
        b.entry(
          key,
          b =>
            field.array.values match {
              case Seq(member)                 => emitRef(parent, member, b)
              case members if members.nonEmpty => b.list(b => members.foreach(emitRef(parent, _, b)))
              case _                           => // ignore
          }
        )
      )
    }

    override def position(): Position = pos(field.value.annotations)
  }

  /** Emit array or single value from an entry. */
  // TODO why ArrayValueEmitter emits just one value?
  case class SimpleArrayValueEmitter(parent: DialectPropertyMapping, key: String, field: FieldEntry)
      extends EntryEmitter {

    override def emit(b: EntryBuilder): Unit = {
      sourceOr(
        field.value,
        b.entry(
          key,
          b =>
            field.array.values match {
              case Seq(member)                 => raw(b, member.toString)
              case members if members.nonEmpty => b.list(b => members.foreach(value => raw(b, value.toString)))
              case _                           => // ignore
          }
        )
      )
    }

    override def position(): Position = pos(field.value.annotations)
  }

  private def emitRef(mapping: DialectPropertyMapping, name: String, b: PartBuilder): Unit = {
    nameProvider match {
      case None     => raw(b, name)
      case Some(np) => raw(b, Option(np.localName(name, mapping)).getOrElse(name))
    }
  }

  def emit(): YDocument = {
    root.definition.dialect.get.ramlRefiner.foreach(r => {
      r.refine(root)
    })
    YDocument {
      ObjectEmitter(root, Some(root.definition.dialect.get.header.substring(1)), root = true).emit(_)
    }
  }

  def createEmitter(domainEntity: DomainEntity, mapping: DialectPropertyMapping): Option[EntryEmitter] = {
    var res: Option[EntryEmitter] = None

    val field = mapping.field()
    val value = domainEntity.fields.get(field)

    if (!mapping.noRAML && Option(value).isDefined && (!(value
          .isInstanceOf[AmfArray] && value.asInstanceOf[AmfArray].values.isEmpty))) {
      if (mapping.isScalar) {
        if (mapping.collection) {
          if (value.isInstanceOf[AmfArray]) {
            val value = domainEntity.fields.getValue(field)
            if (Option(value).isDefined && Option(value.value).isDefined) {
              if (mapping.isRef) {
                res = Some(RefArrayValueEmitter(mapping, mapping.name, FieldEntry(field, value)))
              } else
                res = Some(SimpleArrayValueEmitter(mapping, mapping.name, FieldEntry(field, value)))
            }
          }
        } else {
          if (mapping.isRef) {
            res = Some(RefValueEmitter(mapping, mapping.name, FieldEntry(field, domainEntity.fields.getValue(field))))
          } else res = Some(ValueEmitter(mapping.name, FieldEntry(field, domainEntity.fields.getValue(field))))
        }
      } else {

        if (mapping.collection) {
          value match {
            case array: AmfArray   => res = Some(ObjectArrayEmitter(mapping, array))
            case obj: DomainEntity => res = Some(ObjectKVEmitter(mapping, obj))
          }
        } else if (mapping.isMap && mapping.hashValue.isEmpty) {
          value match {
            case array: AmfArray =>
              res = Some(ObjectMapEmitter(mapping, array))
            case _ => // ignore
          }
        } else if (mapping.isMap && mapping.hashValue.isDefined) {
          value match {
            case array: AmfArray =>
              res = Some(MapPairEmitter(mapping, array))
            case _ => // ignore
          }
        } else {
          res = Some(ObjectKVEmitter(mapping, value.asInstanceOf[DomainEntity]))
        }
      }
    }

    res
  }

  case class ObjectArrayEmitter(mapping: DialectPropertyMapping, values: AmfArray) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        mapping.name,
        _.list { l =>
          values.values.filter(x => isExternal(x)).foreach {
            case v: DomainEntity =>
              ObjectEmitter(v).emit(l)
          }
        }
      )
    }

    override def position(): Position = pos(values.annotations)
  }

  case class ObjectKVEmitter(mapping: DialectPropertyMapping, domainEntity: DomainEntity) extends EntryEmitter {

    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        mapping.name,
        ObjectEmitter(domainEntity).emit(_)
      )
    }

    override def position(): Position = ZERO
  }

  case class ObjectMapEmitter(mapping: DialectPropertyMapping, values: AmfArray) extends EntryEmitter {

    override def emit(b: EntryBuilder): Unit = {
      val elements = values.values.filter(x => isExternal(x))
      if (elements.nonEmpty) {
        b.entry(
          mapping.name,
          _.obj { b =>
            elements.foreach {
              case entity: DomainEntity =>
                b.complexEntry(
                  b => {
                    if (mapping.noLastSegmentTrimInMaps) raw(b, localId(mapping, entity))
                    else raw(b, lastSegment(entity))
                  },
                  ObjectEmitter(entity).emit(_)
                )
            }
          }
        )
      }
    }

    override def position(): Position = ZERO
  }

  private def isExternal(x: AmfElement) = {
    !externals.contains(x.asInstanceOf[DomainEntity].id)
  }

  case class MapPairEmitter(mapping: DialectPropertyMapping, values: AmfArray) extends EntryEmitter {

    override def emit(b: EntryBuilder): Unit = {
      val elements = values.values.filter(x => isExternal(x))
      if (elements.nonEmpty) {
        b.entry(
          mapping.name,
          _.obj { b =>
            elements.foreach {
              case entity: DomainEntity =>
                b.complexEntry(
                  b => {
                    if (mapping.hash.isDefined) {
                      mapping.hash match {
                        case Some(hash) => entity.fields.get(hash.field()) match {
                          case hashValueScalar: AmfScalar => raw(b, hashValueScalar.toString)
                          case _                          => raw(b, "")
                        }
                        case _ => raw(b, "")
                      }
                    } else if (mapping.noLastSegmentTrimInMaps) raw(b, localId(mapping, entity))
                    else raw(b, lastSegment(entity))
                  },
                  entity.fields.get(mapping.hashValue.get.field()) match {
                    case hashValueScalar: AmfScalar => raw(_, hashValueScalar.asInstanceOf[AmfScalar].toString)
                    case _                          => raw(_, "")
                  }
                )
            }
          }
        )
      }
    }

    override def position(): Position = ZERO
  }

  def lastSegment(obj: DomainEntity): String = {
    val ind: Int = Math.max(obj.id.lastIndexOf('/'), obj.id.lastIndexOf('#'))
    if (ind > 0) { obj.id.substring(ind + 1) } else { obj.id }
  }

  def localId(dialectPropertyMapping: DialectPropertyMapping, obj: DomainEntity): String = {
    nameProvider match {
      case Some(np) =>
        Option(np.localName(obj.id, dialectPropertyMapping)).getOrElse(obj.id)
      case _ =>
        obj.id
    }
  }

  case class ObjectEmitter(obj: DomainEntity, comment: Option[String] = None, root: Boolean = false) extends Emitter {

    def emit(b: BaseBuilder): Unit = {

      obj.definition.mappings().find(_.fromVal) match {
        case Some(scalarProp) =>
          val em = obj.string(scalarProp)
          if (scalarProp.isRef) {
            nameProvider match {
              case Some(np) => raw(b, np.localName(em.getOrElse("!!"), scalarProp))
              case _        => // ignore
            }
          } else raw(b, em.getOrElse("null"))

        case None =>
          obj.fields.get(LinkableElementModel.Label) match {
            case scalar: AmfScalar =>
              val tid                             = obj.fields.getValue(LinkableElementModel.TargetId).value.toString
              var libEntity: Option[DomainEntity] = None
              unit.references.foreach({
                case m: Module =>
                  m.declares.foreach(v => {
                    if (v.id == tid) {
                      if (libEntity.isEmpty) {
                        libEntity = Some(v.asInstanceOf[DomainEntity])
                      }
                    }
                  })
                case _ =>
              })
              if (libEntity.isDefined) {
                raw(b, scalar.toString)
              } else raw(b, "!include " + scalar.toString)
            case _ =>
              comment.foreach(b.comment)
              b.obj { b =>
                if (root) {
                  emitUsesMap(b)
                }
                emitObject(b)
              }
          }

      }
    }

    implicit def toEntryBuilder(b: BaseBuilder): EntryBuilder = b match {
      case e: EntryBuilder => e
      case _               => throw new Exception(s"Expected EntryBuilder but $b found")
    }

    implicit def toPartBuilder(b: BaseBuilder): PartBuilder = b match {
      case p: PartBuilder => p
      case _              => throw new Exception(s"Expected PartBuilder but $b found")
    }

    private def emitUsesMap(b: EntryBuilder): Unit = {
      var umap = Map[String, String]()
      unit.references.foreach(v => {
        v.annotations
          .find(classOf[Aliases])
          .foreach(aliases => {
            aliases.aliases.foreach(e => {
              umap = umap + e
            })
          })
      })
      if (umap.nonEmpty) {
        b.entry(
          "uses",
          _.obj { b =>
            umap.foreach(r => {
              MapEntryEmitter((r._1, r._2)).emit(b)
            })
          }
        )
      }

    }

    private def emitObject(b: EntryBuilder): Unit = {
      obj.definition
        .mappings()
        .foreach(mapping => {
          createEmitter(obj, mapping) foreach { emitter =>
            try {
              emitter.emit(b)
            } catch {
              case e: Exception => e.printStackTrace()
            }
          }
        })
    }

    override def position(): Position = ZERO
  }

}

object DialectEmitter {
  def apply(unit: BaseUnit) = new DialectEmitter(unit)
}
