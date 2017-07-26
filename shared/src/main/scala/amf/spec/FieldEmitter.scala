package amf.spec

import amf.common.AMFToken._
import amf.common.{AMFAST, AMFASTNode, AMFToken}
import amf.domain.{EndPoint, Fields, Operation}
import amf.metadata.Field
import amf.model.AmfElement
import amf.parser.Range.NONE
import amf.remote.Raml
import amf.spec.FieldEmitter.StringValueEmitter.key
import amf.spec.Matcher.KeyMatcher

import scala.collection.immutable.ListMap
import scala.collection.mutable.ListBuffer

/**
  * Node emitters.
  */
object FieldEmitter {

  case class SpecEmitter(map: Map[Field, (SpecField, Option[SpecFieldEmitter])]) {

    def emit(fields: Fields): NodeBuilder = {
      val principal = new LazyBuilder(MapToken) {
        override def build: AMFAST = {
          new AMFASTNode(MapToken, "", NONE, nodes.map(_.build))
        }
      }

      fields.foreach {
        case (key, value) if value != null =>
          map.get(key).foreach {
            case (specField, emitter) =>
              val builders =
                emitter.fold(specField.emitter.emit(specField, key, value.value))(_.emit(specField, key, value.value))

              builders.filter(!principal.nodes.contains(_)).foreach(principal.add)
          }
      }
      principal
    }
  }
  object SpecEmitter {
    def apply(specFields: List[SpecField]): SpecEmitter = emitters(specFields)
  }

  trait SpecFieldEmitter {
    def emit(spec: SpecField, field: Field, value: Any): List[NodeBuilder]

    def key(spec: SpecField): String = spec.matcher match {
      case KeyMatcher(key) => key
      case _               => ""
    }
  }

  abstract class PairValueEmmiter extends SpecFieldEmitter {

    def token: AMFToken

    override def emit(spec: SpecField, field: Field, value: Any): List[NodeBuilder] = {
      List(Resolved(entry(spec, valueNode(value, token))))
    }
  }

  object StringValueEmitter extends PairValueEmmiter {

    override def token: AMFToken = StringToken
  }

  object BooleanValueEmitter extends PairValueEmmiter {

    override def token: AMFToken = BooleanToken
  }

  object StringListEmitter extends SpecFieldEmitter {

    override def emit(spec: SpecField, field: Field, value: Any): List[NodeBuilder] = {
      List(
        Resolved(
          entry(spec,
                new AMFASTNode(SequenceToken,
                               "",
                               NONE,
                               value
                                 .asInstanceOf[Seq[String]]
                                 .map(sc => {
                                   valueNode(sc)
                                 })))))
    }

  }

  object ObjectEmitter extends SpecFieldEmitter {

    override def emit(spec: SpecField, field: Field, value: Any): List[NodeBuilder] = {
      val fields = value.asInstanceOf[AmfElement].fields

      val parent = new LazyBuilder(Entry) {

        override def build: AMFAST = {
          entry(spec, nodes.map(_.build).head)
        }
      }
      parent.add(SpecEmitter(spec.children).emit(fields))
      List(parent)
    }
  }

  object EndPointEmitter extends SpecFieldEmitter {
    override def emit(spec: SpecField, field: Field, value: Any): List[NodeBuilder] = {
      var eps: Map[EndPoint, LazyBuilder] = ListMap()
      val endPoints                       = value.asInstanceOf[List[EndPoint]]
      val vendor                          = spec.vendor

      endPoints.foreach(endPoint => {
        val builder: LazyBuilder = endPointBuilder(endPoint, spec)

        if (vendor == Raml) endPoint.parent.foreach(eps(_).add(builder))

        eps = eps + (endPoint -> builder)
      })

      if (vendor == Raml) eps.filterKeys(_.parent.isEmpty).values.toList
      else eps.values.toList
    }

    private def endPointBuilder(endPoint: EndPoint, spec: SpecField): LazyBuilder = new LazyBuilder(Entry) {
      override def build: AMFAST =
        entry(
          if (spec.vendor == Raml) endPoint.simplePath else endPoint.path,
          map(
            List
              .concat(SpecEmitter(spec.children).emit(endPoint.fields).asInstanceOf[LazyBuilder].nodes, nodes)
              .map(_.build))
        )
    }
  }

  object OperationEmitter extends SpecFieldEmitter {
    override def emit(spec: SpecField, field: Field, value: Any): List[NodeBuilder] = {
      val operations = value.asInstanceOf[List[Operation]]
      operations.map(operation => {
        operationBuilder(operation, spec)
      })
    }

    private def operationBuilder(operation: Operation, spec: SpecField): LazyBuilder = new LazyBuilder(Entry) {
      override def build: AMFAST =
        entry(operation.method,
              map(
                List
                  .concat(SpecEmitter(spec.children).emit(operation.fields).asInstanceOf[LazyBuilder].nodes, nodes)
                  .map(_.build)
              ))
    }
  }

  private def map(entries: Seq[AMFAST]): AMFAST = new AMFASTNode(MapToken, "", NONE, entries)

  private def entry(spec: SpecField, value: AMFAST): AMFAST = entry(key(spec), value)
  private def entry(key: String, value: AMFAST): AMFAST = {
    new AMFASTNode(Entry,
                   "",
                   NONE,
                   List(
                     valueNode(key),
                     value
                   ))
  }

  private def valueNode(value: Any, token: AMFToken = StringToken) = new AMFASTNode(token, value.toString, NONE)

  def nested(sf: SpecField): Seq[(Field, (SpecField, Option[SpecFieldEmitter]))] = {

    lazy val virtualNodeEmitter = new SpecFieldEmitter {
      var parent: Option[LazyBuilder] = None
      override def emit(spec: SpecField, field: Field, value: Any): List[NodeBuilder] = {
        if (parent.isEmpty)
          parent = Some(new LazyBuilder(Entry) {

            override def build: AMFAST = {
              entry(spec, map(nodes.map(_.build)))
            }
          })

        val sonSpec = spec.children.find(sp => sp.fields.head == field).get
        parent.get.add(sonSpec.emitter.emit(sonSpec, field, value))

        List(parent.get)
      }
    }

    for {
      spec <- sf.children
    } yield {
      (spec.fields.head, (sf, Some(virtualNodeEmitter)))
    }
  }

  def emitters(specFields: List[SpecField]): SpecEmitter = {

    var map: Map[Field, (SpecField, Option[SpecFieldEmitter])] = Map()

    specFields.foreach(sf =>
      sf.fields match {
        case Nil => map = map ++ nested(sf)
        case _   => map = map + (sf.fields.head -> (sf, None))
    })

    SpecEmitter(map)
  }

  trait NodeBuilder {
    def build: AMFAST
  }

  abstract class LazyBuilder(token: AMFToken, val nodes: ListBuffer[NodeBuilder] = ListBuffer()) extends NodeBuilder {
    def add(n: NodeBuilder): Unit       = nodes += n
    def add(n: List[NodeBuilder]): Unit = nodes ++= n

    override def build: AMFAST
  }

  case class Resolved(node: AMFAST) extends NodeBuilder {
    override def build: AMFAST = node
  }
}
