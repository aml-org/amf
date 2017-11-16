package amf.spec.dialects

import amf.document.{BaseUnit, Module}
import amf.domain.Annotation.{DomainElementReference}
import amf.domain.{FieldEntry, Link}
import amf.domain.dialects.DomainEntity
import amf.metadata.domain.LinkableElementModel
import amf.model.{AmfArray, AmfElement, AmfScalar}
import amf.parser.Position
import amf.parser.Position.ZERO
import amf.spec.dialects.Dialect.retrieveDomainEntity
import amf.spec.raml.RamlSpecEmitter
import amf.spec.{Emitter, EntryEmitter}
import org.yaml.model.YDocument
import org.yaml.model.YDocument.{BaseBuilder, EntryBuilder, PartBuilder}
import amf.spec.common.BaseEmitters._
import amf.vocabulary.Namespace

/**
  * Created by Pavel Petrochenko on 13/09/17.
  */
class DialectEmitter(val unit: BaseUnit) extends RamlSpecEmitter {

  val root: DomainEntity = retrieveDomainEntity(unit)
  var nameProvider: Option[LocalNameProvider] = root.definition.nameProvider match {
    case Some(np) => Some(np(unit))
    case None     => None
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
    YDocument {
      ObjectEmitter(root, Some(root.definition.dialect.get.header.substring(1)),true).emit(_)
    }
  }

  def createEmitter(domainEntity: DomainEntity, mapping: DialectPropertyMapping): Option[EntryEmitter] = {
    var res: Option[EntryEmitter] = None

    val field = mapping.field()
    val value = domainEntity.fields.get(field)

    if (!mapping.noRAML && Option(value).isDefined&&(!(value.isInstanceOf[AmfArray]&&value.asInstanceOf[AmfArray].values.isEmpty))) {
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
            case obj: DomainEntity => res = Some(ObjectKVEmitter(mapping, value.asInstanceOf[DomainEntity]))
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
          values.values.foreach {
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
      if (values.values.nonEmpty) {
        b.entry(
          mapping.name,
          _.obj { b =>
            values.values.foreach {
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

  case class MapPairEmitter(mapping: DialectPropertyMapping, values: AmfArray) extends EntryEmitter {

    override def emit(b: EntryBuilder): Unit = {
      if (values.values.nonEmpty) {
        b.entry(
          mapping.name,
          _.obj { b =>
            values.values.foreach {
              case entity: DomainEntity =>
                b.complexEntry(
                  b => {
                    if (mapping.noLastSegmentTrimInMaps) raw(b, localId(mapping, entity))
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

  case class ObjectEmitter(obj: DomainEntity, comment: Option[String] = None,root:Boolean=false) extends Emitter {

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
            case scalar: AmfScalar => {
              val tid=obj.fields.getValue(LinkableElementModel.TargetId).value.toString
              var libEntity:Option[DomainEntity]=None;
              unit.references.foreach({
                case m:Module=>{
                  m.declares.foreach(v=>{
                    if (v.id==tid){
                      if (!libEntity.isDefined) {
                        libEntity=Some(v.asInstanceOf[DomainEntity]);
                      }
                    }
                  })
                }
                case _ =>
              })
              if (libEntity.isDefined){
                raw(b, scalar.toString)
              }
              else raw(b, "!include " + scalar.toString)
            }
            case _                 => {
              obj.annotations.find(classOf[DomainElementReference]) match {
                case Some(ref) => {
                  raw(b, ref.name)
                }
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

    private def emitUsesMap(b: EntryBuilder) = {
      if (unit.references.find(r=>r.fields.raw(NamespaceExtraFields.NAMESPACE).isDefined).isDefined){
        b.entry(
          "uses",
          _.obj { b =>
            unit.references.foreach(r=>{
              r.fields.get(NamespaceExtraFields.NAMESPACE) match {
                case n:AmfScalar=>{
                  val namespace=n.toString;
                  val path=r.fields.get(NamespaceExtraFields.PATH).toString
                  MapEntryEmitter((namespace,path)).emit(b);
                }
                case _ =>
              }
            })
          }
        )
      }
    }

    private def emitObject(b: EntryBuilder) = {
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
