package amf.spec

import amf.common.AMFToken.{Entry, MapToken, SequenceToken, StringToken}
import amf.common.{AMFAST, AMFASTNode, AMFToken}
import amf.metadata.Field
import amf.model._
import amf.parser.Range.NONE
import amf.spec.Matcher.KeyMatcher
import amf.spec.SpecFieldEmitter.StringValueEmitter.key

import scala.collection.mutable.ListBuffer

/**
  * Node emitters.
  */
object SpecFieldEmitter {

  case class SpecEmitter(map: Map[Field, (SpecField, Option[SpecFieldEmitter])]) {

    def emit(fields: Fields): NodeBuilder = {
      val principal = new LazyBuilder(MapToken) {
        override def build(): AMFAST = {
          new AMFASTNode(MapToken, "", NONE, nodes.map(_.build()))
        }
      }

      fields.foreach(entry => {
        val specField = map.get(entry._1)
        if (entry._2 != null && specField.isDefined) {
          if (specField.get._2.isDefined) {
            val builder = specField.get._2.get.emit(specField.get._1, entry._1, entry._2.value)
            if (!principal.nodes.contains(builder)) principal.add(builder)
          } else {
            val builder = specField.get._1.emitter.emit(specField.get._1, entry._1, entry._2.value)
            if (!principal.nodes.contains(builder)) principal.add(builder)
          }
        }
      })
      principal
    }
  }
  object SpecEmitter {
    def apply(specFields: List[SpecField]): SpecEmitter = emitters(specFields)
  }

  trait SpecFieldEmitter {
    def emit(spec: SpecField, field: Field, value: Any): NodeBuilder

    def key(spec: SpecField): String = spec.matcher match {
      case KeyMatcher(key) => key
      case _               => ""
    }
  }

  object StringValueEmitter extends SpecFieldEmitter {

    override def emit(spec: SpecField, field: Field, value: Any): NodeBuilder = {
      Resolved(entry(spec, stringNode(value.toString)))
    }

  }

  object StringListValueEmitter extends SpecFieldEmitter {

    override def emit(spec: SpecField, field: Field, value: Any): NodeBuilder = {
      Resolved(
        entry(spec,
              new AMFASTNode(SequenceToken,
                             "",
                             NONE,
                             value
                               .asInstanceOf[List[String]]
                               .map(sc => {
                                 stringNode(sc)
                               }))))
    }

  }

  class VirtualNodeEmitter extends SpecFieldEmitter {

    var parent: Option[LazyBuilder] = None

    override def emit(spec: SpecField, field: Field, value: Any): NodeBuilder = {
      if (parent.isEmpty)
        parent = Some(new LazyBuilder(Entry) {

          override def build(): AMFAST = {
            entry(spec, map(nodes.map(_.build())))
          }
        })

      val sonSpec = spec.children.find(sp => sp.fields.head == field).get
      parent.get.add(sonSpec.emitter.emit(sonSpec, field, value))

      parent.get
    }
  }

  object ObjectEmitter extends SpecFieldEmitter {

    override def emit(spec: SpecField, field: Field, value: Any): NodeBuilder = {
      val fields = value.asInstanceOf[FieldHolder].fields

      val parent = new LazyBuilder(Entry) {

        override def build(): AMFAST = {
          entry(spec, nodes.map(_.build()).head)
        }
      }
      parent.add(SpecEmitter(spec.children).emit(fields))
      parent
    }
  }

  private def map(entries: Seq[AMFAST]): AMFAST = new AMFASTNode(MapToken, "", NONE, entries)

  private def entry(spec: SpecField, value: AMFAST): AMFAST = {
    new AMFASTNode(Entry,
                   "",
                   NONE,
                   List(
                     stringNode(key(spec)),
                     value
                   ))
  }

  private def stringNode(value: String) = new AMFASTNode(StringToken, value, NONE)

  def nested(sf: SpecField): Seq[(Field, (SpecField, Option[SpecFieldEmitter]))] = {

    lazy val virtualNodeEmitter = new SpecFieldEmitter {
      var parent: Option[LazyBuilder] = None
      override def emit(spec: SpecField, field: Field, value: Any): NodeBuilder = {
        if (parent.isEmpty)
          parent = Some(new LazyBuilder(Entry) {

            override def build(): AMFAST = {
              entry(spec, map(nodes.map(_.build())))
            }
          })

        val sonSpec = spec.children.find(sp => sp.fields.head == field).get
        parent.get.add(sonSpec.emitter.emit(sonSpec, field, value))

        parent.get
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
    def build(): AMFAST
  }

  abstract class LazyBuilder(token: AMFToken, val nodes: ListBuffer[NodeBuilder] = ListBuffer()) extends NodeBuilder {
    def add(n: NodeBuilder): Unit = nodes += n

    override def build(): AMFAST
  }

  case class Resolved(node: AMFAST) extends NodeBuilder {
    override def build(): AMFAST = node
  }
}
