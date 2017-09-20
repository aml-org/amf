package amf.spec.common

import amf.domain.Annotation.LexicalInformation
import amf.domain.extensions.{
  DataNode,
  DomainExtension,
  ArrayNode => DataArrayNode,
  ObjectNode => DataObjectNode,
  ScalarNode => DataScalarNode
}
import amf.domain.{Annotations, DomainElement, FieldEntry, Value}
import amf.model.AmfScalar
import amf.parser.Position.ZERO
import amf.parser.{ASTEmitter, Position}
import amf.spec.{Emitter, SpecOrdering}
import amf.vocabulary.Namespace
import org.yaml.model.YTag

trait AnnotationFormat {}

object RamlAnnotationFormat extends AnnotationFormat {}
object OasAnnotationFormat  extends AnnotationFormat {}

trait BaseSpecEmitter {

  val emitter: ASTEmitter

  protected def pos(annotations: Annotations): Position =
    annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)

  protected def traverse(emitters: Seq[Emitter]): Unit = {
    emitters.foreach(e => {
      e.emit()
    })
  }

  protected def raw(content: String, tag: YTag = YTag.Str): Unit = emitter.scalar(content, tag)

  protected def entry(inner: () => Unit): Unit = emitter.entry(inner)

  protected def array(inner: () => Unit): Unit = emitter.sequence(inner)

  protected def map(inner: () => Unit): Unit = emitter.mapping(inner)

  /** Emit a single value from an array as an entry. */
  case class ArrayValueEmitter(key: String, f: FieldEntry) extends Emitter {
    override def emit(): Unit = {
      sourceOr(f.value, entry { () =>
        raw(key)
        raw(f.array.scalars.headOption.map(_.toString).getOrElse(""))
      })
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class ScalarEmitter(v: AmfScalar) extends Emitter {
    override def emit(): Unit = sourceOr(v.annotations, raw(v.value.toString))

    override def position(): Position = pos(v.annotations)
  }

  case class ValueEmitter(key: String, f: FieldEntry, tag: YTag = YTag.Str) extends Emitter {
    override def emit(): Unit = {
      sourceOr(f.value, entry { () =>
        raw(key)
        raw(f.scalar.toString, tag)
      })
    }

    override def position(): Position = pos(f.value.annotations)
  }

  protected def sourceOr(value: Value, inner: => Unit): Unit = sourceOr(value.annotations, inner)

  protected def sourceOr(annotations: Annotations, inner: => Unit): Unit = {
    //    annotations
    //      .find(classOf[SourceAST])
    //      .fold(inner)(a => emitter.addChild(a.ast))
    inner
  }

  case class EntryEmitter(key: String, value: String, tag: YTag = YTag.Str, position: Position = Position.ZERO)
      extends Emitter {
    override def emit(): Unit = {
      entry { () =>
        raw(key)
        raw(value, tag)
      }
    }
  }

  case class AnnotationsEmitter(domainElement: DomainElement, ordering: SpecOrdering, format: AnnotationFormat) {
    def emitters: Seq[Emitter] = {
      domainElement.customDomainProperties.map { pro =>
        AnnotationEmitter(pro, ordering, format)
      }
    }
  }

  case class AnnotationEmitter(domainExtension: DomainExtension, ordering: SpecOrdering, format: AnnotationFormat)
      extends Emitter {
    override def emit(): Unit = {
      entry { () =>
        format match {
          case RamlAnnotationFormat => raw("(" + domainExtension.definedBy.name + ")")
          case OasAnnotationFormat  => raw("x-" + domainExtension.definedBy.name)
        }

        Option(domainExtension.extension).foreach { dataNode =>
          DataNodeEmitter(dataNode, ordering).emit()
        }
      }
    }

    override def position(): Position = pos(domainExtension.annotations)
  }

  object RamlAnnotationsEmitter {
    def apply(domainElement: DomainElement, ordering: SpecOrdering) =
      AnnotationsEmitter(domainElement, ordering, RamlAnnotationFormat)
  }

  object OasAnnotationsEmitter {
    def apply(domainElement: DomainElement, ordering: SpecOrdering) =
      AnnotationsEmitter(domainElement, ordering, OasAnnotationFormat)
  }

  case class DataNodeEmitter(dataNode: DataNode, ordering: SpecOrdering) extends Emitter {
    private val xsdString: String  = (Namespace.Xsd + "string").iri()
    private val xsdInteger: String = (Namespace.Xsd + "integer").iri()
    private val xsdFloat: String   = (Namespace.Xsd + "float").iri()
    private val xsdBoolean: String = (Namespace.Xsd + "boolean").iri()
    private val xsdNil: String     = (Namespace.Xsd + "nil").iri()

    override def emit(): Unit = {
      dataNode match {
        case scalar: DataScalarNode => emitScalar(scalar)
        case array: DataArrayNode   => emitArray(array)
        case obj: DataObjectNode    => emitObject(obj)
      }
    }

    def emitObject(objectNode: DataObjectNode): Unit = {
      val emitters = objectNode.properties.keys.map { property =>
        DataPropertyEmitter(property, objectNode, ordering)
      }.toSeq
      map { () =>
        ordering.sorted(emitters).foreach(_.emit())
      }
    }

    def emitArray(arrayNode: DataArrayNode): Unit = {
      val emitters = arrayNode.members.map(DataNodeEmitter(_, ordering))
      array { () =>
        ordering.sorted(emitters).foreach(_.emit())
      }
    }

    def emitScalar(scalar: DataScalarNode): Unit = {
      scalar.dataType match {
        case Some(t) if t == xsdString  => raw(scalar.value)
        case Some(t) if t == xsdInteger => raw(scalar.value, YTag.Int)
        case Some(t) if t == xsdFloat   => raw(scalar.value, YTag.Float)
        case Some(t) if t == xsdBoolean => raw(scalar.value, YTag.Bool)
        case Some(t) if t == xsdNil     => raw("null")
        case _                          => raw(scalar.value)
      }
    }

    override def position(): Position = pos(dataNode.annotations)
  }

  case class DataPropertyEmitter(property: String, dataNode: DataObjectNode, ordering: SpecOrdering) extends Emitter {
    val annotations: Annotations     = dataNode.propertyAnnotations(property)
    val propertyValue: Seq[DataNode] = dataNode.properties(property)

    override def emit(): Unit = {
      entry { () =>
        raw(property)
        // In the current implementation ther can only be one value, we are NOT flattening arrays
        DataNodeEmitter(propertyValue.head, ordering).emit()
      }
    }

    override def position(): Position = pos(annotations)
  }

}
