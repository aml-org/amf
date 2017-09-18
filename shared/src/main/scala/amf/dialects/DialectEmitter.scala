package amf.dialects

import amf.common.AMFAST
import amf.common.AMFToken.{Comment, Root}
import amf.document.{BaseUnit, Document}
import amf.domain.FieldEntry
import amf.model.AmfArray
import amf.parser.Position
import amf.spec.{ASTEmitterHelper, Emitter}

/**
  * Created by Pavel Petrochenko on 13/09/17.
  */
class DialectEmitter (val unit: BaseUnit) extends ASTEmitterHelper {

  val root: DomainEntity = retrieveDomainEntity(unit)
  var nameProvider: Option[LocalNameProvider] = root.definition.nameProvider match {
    case Some(np) => Some(np(root))
    case None     => None
  }

  private def retrieveDomainEntity(unit: BaseUnit) = unit match {
    case document: Document => document.encodes match {
      case unit: DomainEntity => unit
      case other              => throw new Exception(s"Encoded domain element is not a dialect domain entity $other")
    }
    case _                  => throw new Exception(s"Cannot extract domain entity from unit that is not a document: $unit")
  }

  case class RefValueEmitter(parent: DialectPropertyMapping, key: String, field: FieldEntry) extends Emitter {
    override def emit(): Unit = {
      sourceOr(field.value, entry { () =>
        raw(key)
        emitRef(parent,field.scalar.toString)
      })
    }

    override def position(): Position = pos(field.value.annotations)
  }

  /** Emit a single value from an array as an entry. */
  case class RefArrayValueEmitter(parent: DialectPropertyMapping, key: String, field: FieldEntry) extends Emitter {

    override def emit(): Unit = {
      sourceOr(field.value, entry { () =>
        raw(key)
        field.array.values match {
          case Seq(member) =>
            emitRef(parent, member.toString)
          case members if members.nonEmpty =>
            array(() => {
              members.foreach(value => { emitRef(parent, value.toString) })
            })
          case _ => // ignore
        }
      })
    }

    override def position(): Position = pos(field.value.annotations)
  }

  private def emitRef(mapping: DialectPropertyMapping, refName: String): Unit = {
    nameProvider match {
      case None     => raw(refName)
      case Some(np) => raw(Option(np.localName(refName, mapping)).getOrElse(refName))
    }
  }

  def emit(): AMFAST = {
    emitter.root(Root) { () =>
      raw(root.definition.dialect.get.header.substring(1), Comment)
      ObjectEmitter(root).emit()
    }
  }

  def createEmitter(domainEntity:DomainEntity, mapping:DialectPropertyMapping): Option[Emitter] = {
    var res:Option[Emitter] = None

    val field = mapping.field()
    val value = domainEntity.fields.get(field)
    if (!mapping.noRAML && Option(value).isDefined) {
      if (mapping.isScalar) {
        if (mapping.collection) {
          if (value.isInstanceOf[AmfArray]) {
            val value = domainEntity.fields.getValue(field)
            if (Option(value).isDefined && Option(value.value).isDefined) {
              if (mapping.isRef) {
                res = Some(RefArrayValueEmitter(mapping, mapping.name, FieldEntry(field, value)))
              }
              else
                res = Some(ArrayValueEmitter(mapping.name, FieldEntry(field, value)))
            }
          }
        }
        else {
          if (mapping.isRef) {
            res = Some(RefValueEmitter(mapping, mapping.name, FieldEntry(field, domainEntity.fields.getValue(field))))
          }
          else res = Some(ValueEmitter(mapping.name, FieldEntry(field, domainEntity.fields.getValue(field))))
        }
      }
      else {
        if (mapping.collection) throw new RuntimeException("Not implemented yet")
        else if (mapping.hash.isDefined) {
          value match {
            case array: AmfArray =>
              res = Some(ObjectMapEmmiter(mapping, array))
            case _ => // ignore
          }
        }
        else {
          res = Some(ObjectKVEmmiter(mapping, value.asInstanceOf[DomainEntity]))
        }
      }
    }

    res
  }

  case class ObjectKVEmmiter(mapping:DialectPropertyMapping, domainEntity: DomainEntity) extends Emitter{

    override def emit(): Unit = {
      entry { () =>
        raw(mapping.name)
        ObjectEmitter(domainEntity).emit()
      }
    }

    override def position(): Position = Position.ZERO
  }

  case class ObjectMapEmmiter(mapping:DialectPropertyMapping, values:AmfArray) extends Emitter{

    override def emit(): Unit = {
      if (values.values.nonEmpty) {
        entry { () =>
          raw(mapping.name)
          map { () =>
            values.values.foreach { case entity: DomainEntity =>
              entry { () =>
                if (mapping.noLastSegmentTrimInMaps) {
                  raw(localId(mapping, entity))
                } else {
                  raw(lastSegment(entity))
                }
                ObjectEmitter(entity).emit()
              }
            }
          }
        }
      }
    }

    override def position(): Position = Position.ZERO
  }

  def lastSegment(obj: DomainEntity): String = {
    val ind: Int = Math.max(obj.id.lastIndexOf('/'), obj.id.lastIndexOf('#'))
    if (ind > 0) { obj.id.substring(ind + 1) } else { obj.id }
  }

  def localId(dialectPropertyMapping: DialectPropertyMapping, obj: DomainEntity): String = {
    nameProvider match {
      case Some(np) =>
        Option(np.localName(obj.id, dialectPropertyMapping)).getOrElse(obj.id)
      case _        =>
        obj.id
    }
  }

  case class ObjectEmitter(obj: DomainEntity) extends Emitter {

    override def emit(): Unit = {
      obj.definition.mappings().find(_.fromVal) match {

        case Some(scalarProp) =>
          val em = obj.string(scalarProp)
          if (scalarProp.isRef) {
            nameProvider match {
              case Some(np) =>
                raw(np.localName(em.orNull, scalarProp))
              case _ => // ignore
            }
          } else {
            val str = em.getOrElse("null")
            raw(str)
          }

        case None =>
          map { () =>
            obj.definition.mappings().foreach(mapping => {
              createEmitter(obj, mapping) match {
                case Some(emitterCreated) => try {
                  emitterCreated.emit()
                } catch {
                  case e: Exception => e.printStackTrace()
                }

                case _             => // ignore
              }
            })
          }
      }
    }

    override def position(): Position = Position.ZERO
  }
}

object DialectEmitter{
  def apply(unit: BaseUnit) = new DialectEmitter(unit)
}