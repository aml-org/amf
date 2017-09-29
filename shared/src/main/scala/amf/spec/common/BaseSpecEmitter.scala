package amf.spec.common

import amf.domain.Annotation.LexicalInformation
import amf.domain.`abstract`.{
  AbstractDeclaration,
  ParametrizedDeclaration,
  ParametrizedResourceType,
  ParametrizedTrait
}
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
import org.yaml.model.YType

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

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

  protected def raw(content: String, tag: YType = YType.Str): Unit = emitter.scalar(content, tag)

  protected def entry(inner: () => Unit): Unit = emitter.entry(inner)

  protected def array(inner: () => Unit): Unit = emitter.sequence(inner)

  protected def map(inner: () => Unit): Unit = emitter.mapping(inner)

  protected def comment(text: String): Unit = emitter.comment(text)

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

  case class ValueEmitter(key: String, f: FieldEntry, tag: YType = YType.Str) extends Emitter {
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

  case class EntryEmitter(key: String, value: String, tag: YType = YType.Str, position: Position = Position.ZERO)
      extends Emitter {
    override def emit(): Unit = {
      entry { () =>
        raw(key)
        raw(value, tag)
      }
    }
  }

  protected def link(id: String): Unit = map { () =>
    entry { () =>
      raw("@id"); raw(id.trim)
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
        case Some(t) if t == xsdInteger => raw(scalar.value, YType.Int)
        case Some(t) if t == xsdFloat   => raw(scalar.value, YType.Float)
        case Some(t) if t == xsdBoolean => raw(scalar.value, YType.Bool)
        case Some(t) if t == xsdNil     => raw("null")
        case _                          => raw(scalar.value)
      }
    }

    override def position(): Position = pos(dataNode.annotations)
  }

  case class ExtendsEmitter(prefix: String, field: FieldEntry, ordering: SpecOrdering) {
    def emitters(): Seq[Emitter] = {
      val result = ListBuffer[Emitter]()
      val resourceTypes: Seq[ParametrizedResourceType] = field.array.values.collect {
        case a: ParametrizedResourceType => a
      }
      val traits: Seq[ParametrizedTrait] = field.array.values.collect { case a: ParametrizedTrait => a }

      if (resourceTypes.nonEmpty) result += EndPointExtendsEmitter(prefix, resourceTypes, ordering)

      if (traits.nonEmpty) result += TraitExtendsEmitter(prefix, traits, ordering)

      result
    }
  }

  case class TraitExtendsEmitter(prefix: String, traits: Seq[ParametrizedTrait], ordering: SpecOrdering)
      extends Emitter {
    override def emit(): Unit = {
      entry { () =>
        raw(prefix + "is")

        array { () =>
          val result = ListBuffer[Emitter]()

          traits.foreach(t => result += ParametrizedDeclarationEmitter(t, ordering))

          traverse(ordering.sorted(result))
        }
      }
    }

    override def position(): Position = traits.headOption.map(rt => pos(rt.annotations)).getOrElse(Position.ZERO)
  }

  case class EndPointExtendsEmitter(prefix: String,
                                    resourceTypes: Seq[ParametrizedResourceType],
                                    ordering: SpecOrdering)
      extends Emitter {
    override def emit(): Unit = {
      entry { () =>
        raw(prefix + "type")
        ParametrizedDeclarationEmitter(resourceTypes.head, ordering).emit()
      }
    }

    override def position(): Position =
      resourceTypes.headOption.map(rt => pos(rt.annotations)).getOrElse(Position.ZERO)
  }

  case class ParametrizedDeclarationEmitter(declaration: ParametrizedDeclaration, ordering: SpecOrdering)
      extends Emitter {
    override def emit(): Unit = {
      if (declaration.variables.nonEmpty) {
        map { () =>
          entry { () =>
            val result = ListBuffer[Emitter]()

            declaration.variables.foreach(variable =>
              result += EntryEmitter(variable.name, variable.value, position = pos(variable.annotations)))

            raw(declaration.name)
            map { () =>
              traverse(ordering.sorted(result))
            }
          }
        }
      } else {
        raw(declaration.name)
      }
    }

    override def position(): Position = pos(declaration.annotations)
  }

  case class AbstractDeclarationsEmitter(key: String, declarations: Seq[AbstractDeclaration], ordering: SpecOrdering)
      extends Emitter {
    override def emit(): Unit = {
      entry { () =>
        raw(key)
        map { () =>
          traverse(ordering.sorted(declarations.map(d => AbstractDeclarationEmitter(d, ordering))))
        }
      }
    }

    override def position(): Position = declarations.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)
  }

  case class AbstractDeclarationEmitter(declaration: AbstractDeclaration, ordering: SpecOrdering) extends Emitter {
    override def position(): Position = pos(declaration.annotations)

    override def emit(): Unit = {
      entry { () =>
        val name = Option(declaration.name)
          .getOrElse(throw new Exception(s"Cannot declare abstract declaration without name $declaration"))

        raw(name)
        DataNodeEmitter(declaration.dataNode, ordering).emit()
      }
    }
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

  case class ArrayEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends Emitter {
    override def emit(): Unit = {
      sourceOr(
        f.value,
        entry { () =>
          raw(key)

          val result = mutable.ListBuffer[Emitter]()

          f.array.scalars
            .foreach(v => {
              result += ScalarEmitter(v)
            })

          array { () =>
            traverse(ordering.sorted(result))
          }
        }
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }
}
