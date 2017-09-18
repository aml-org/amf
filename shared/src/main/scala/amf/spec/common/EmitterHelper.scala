package amf.spec.common

import amf.common.AMFToken._
import amf.common.{AMFAST, AMFToken}
import amf.domain.Annotation.LexicalInformation
import amf.domain.extensions.{
  DataNode,
  DomainExtension,
  ArrayNode => DataArrayNode,
  ObjectNode => DataObjectNode,
  ScalarNode => DataScalarNode
}
import amf.domain.{Annotations, DomainElement}
import amf.parser.Position.ZERO
import amf.parser.{ASTEmitter, Position}
import amf.spec.{Emitter, SpecOrdering}
import amf.vocabulary.Namespace

trait AnnotationFormat {}

object RamlAnnotationFormat extends AnnotationFormat {}
object OasAnnotationFormat  extends AnnotationFormat {}

trait EmitterHelper {

  val emitter: ASTEmitter[AMFToken, AMFAST]

  protected def pos(annotations: Annotations): Position =
    annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)

  protected def raw(content: String, token: AMFToken = StringToken): Unit = {
    try {
      emitter.value(token, content)
    } catch {
      case e: Exception => {
        println(e)
        throw (e)
      }
    }

  }

  protected def traverse(emitters: Seq[Emitter]): Unit = {
    emitters.foreach(e => {
      e.emit()
    })
  }

  protected def entry(inner: () => Unit): Unit = node(Entry)(inner)

  protected def array(inner: () => Unit): Unit = node(SequenceToken)(inner)

  protected def map(inner: () => Unit): Unit = node(MapToken)(inner)

  protected def node(t: AMFToken)(inner: () => Unit): emitter.type = {
    emitter.beginNode()
    inner()
    emitter.endNode(t)
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
        case Some(t) if t == xsdInteger => raw(scalar.value, IntToken)
        case Some(t) if t == xsdFloat   => raw(scalar.value, FloatToken)
        case Some(t) if t == xsdBoolean => raw(scalar.value, BooleanToken)
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
