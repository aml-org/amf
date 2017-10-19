package amf.spec.common

import amf.domain.Annotation.{LexicalInformation, SingleValueArray}
import amf.domain._
import amf.domain.Annotation.LexicalInformation
import amf.domain._
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
import amf.metadata.domain.CreativeWorkModel
import amf.model.AmfScalar
import amf.parser.Position
import amf.parser.Position.ZERO
import amf.spec.{Emitter, EntryEmitter, PartEmitter, SpecOrdering}
import amf.vocabulary.Namespace
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.{YNode, YScalar, YType}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait AnnotationFormat {}

object RamlAnnotationFormat extends AnnotationFormat {}
object OasAnnotationFormat  extends AnnotationFormat {}

trait BaseSpecEmitter {

  protected def pos(annotations: Annotations): Position =
    annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)

  protected def traverse(emitters: Seq[EntryEmitter], b: EntryBuilder): Unit = {
    emitters.foreach(e => {
      e.emit(b)
    })
  }

  protected def traverse(emitters: Seq[PartEmitter], b: PartBuilder): Unit = {
    emitters.foreach(e => {
      e.emit(b)
    })
  }

  protected def raw(b: PartBuilder, content: String, tag: YType = YType.Str): Unit =
    b.scalar(YNode(YScalar(content), tag))

  case class ScalarEmitter(v: AmfScalar, tag: YType = YType.Str) extends PartEmitter {
    override def emit(b: PartBuilder): Unit = sourceOr(v.annotations, b.scalar(YNode(YScalar(v.value), tag)))

    override def position(): Position = pos(v.annotations)
  }

  case class ValueEmitter(key: String, f: FieldEntry, tag: YType = YType.Str) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      sourceOr(f.value,
               b.entry(
                 key,
                 YNode(YScalar(f.scalar.value), tag)
               ))
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

  case class MapEntryEmitter(key: String, value: String, tag: YType = YType.Str, position: Position = Position.ZERO)
      extends EntryEmitter {

    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        key,
        raw(_, value, tag)
      )
    }
  }

  object MapEntryEmitter {
    def apply(tuple: (String, String)): MapEntryEmitter =
      tuple match {
        case (key, value) => MapEntryEmitter(key, value)
      }
  }

  protected def link(b: PartBuilder, id: String): Unit = b.map(_.entry("@id", id.trim))

  case class AnnotationsEmitter(element: DomainElement, ordering: SpecOrdering, format: AnnotationFormat) {
    def emitters: Seq[EntryEmitter] = element.customDomainProperties.map(AnnotationEmitter(_, ordering, format))
  }

  case class AnnotationEmitter(domainExtension: DomainExtension, ordering: SpecOrdering, format: AnnotationFormat)
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.complexEntry(
        b => {
          format match {
            case RamlAnnotationFormat => b.scalar("(" + domainExtension.definedBy.name + ")")
            case OasAnnotationFormat  => raw(b, "x-" + domainExtension.definedBy.name)
          }
        },
        b => {
          Option(domainExtension.extension).foreach { DataNodeEmitter(_, ordering).emit(b) }
        }
      )
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

  case class DataNodeEmitter(dataNode: DataNode, ordering: SpecOrdering) extends PartEmitter {
    private val xsdString: String  = (Namespace.Xsd + "string").iri()
    private val xsdInteger: String = (Namespace.Xsd + "integer").iri()
    private val xsdFloat: String   = (Namespace.Xsd + "float").iri()
    private val xsdBoolean: String = (Namespace.Xsd + "boolean").iri()
    private val xsdNil: String     = (Namespace.Xsd + "nil").iri()

    override def emit(b: PartBuilder): Unit = {
      dataNode match {
        case scalar: DataScalarNode => emitScalar(scalar, b)
        case array: DataArrayNode   => emitArray(array, b)
        case obj: DataObjectNode    => emitObject(obj, b)
      }
    }

    def emitters(): Seq[Emitter] = {
      dataNode match {
        case scalar: DataScalarNode => Seq(scalarEmitter(scalar))
        case array: DataArrayNode   => arrayEmitters(array)
        case obj: DataObjectNode    => objectEmitters(obj)
      }
    }

    def objectEmitters(objectNode: DataObjectNode): Seq[EntryEmitter] = {
      objectNode.properties.keys.map { property =>
        DataPropertyEmitter(property, objectNode, ordering)
      }.toSeq
    }

    def emitObject(objectNode: DataObjectNode, b: PartBuilder): Unit = {
      b.map(b => {
        ordering.sorted(objectEmitters(objectNode)).foreach(_.emit(b))
      })
    }

    def arrayEmitters(arrayNode: DataArrayNode): Seq[PartEmitter] = arrayNode.members.map(DataNodeEmitter(_, ordering))

    def emitArray(arrayNode: DataArrayNode, b: PartBuilder): Unit = {
      b.list(b => {
        ordering.sorted(arrayEmitters(arrayNode)).foreach(_.emit(b))
      })
    }

    def emitScalar(scalar: DataScalarNode, b: PartBuilder): Unit = {
      scalarEmitter(scalar).emit(b)
    }

    def scalarEmitter(scalar: DataScalarNode): PartEmitter = {
      scalar.dataType match {
        case Some(t) if t == xsdString  => ScalarEmitter(AmfScalar(scalar.value, scalar.annotations), YType.Str)
        case Some(t) if t == xsdInteger => ScalarEmitter(AmfScalar(scalar.value, scalar.annotations), YType.Int)
        case Some(t) if t == xsdFloat   => ScalarEmitter(AmfScalar(scalar.value, scalar.annotations), YType.Float)
        case Some(t) if t == xsdBoolean => ScalarEmitter(AmfScalar(scalar.value, scalar.annotations), YType.Bool)
        case Some(t) if t == xsdNil     => ScalarEmitter(AmfScalar("null", Annotations()), YType.Str)
        case _                          => ScalarEmitter(AmfScalar(scalar.value, Annotations()), YType.Str)
      }
    }

    override def position(): Position = pos(dataNode.annotations)
  }

  case class ExtendsEmitter(prefix: String, field: FieldEntry, ordering: SpecOrdering) {
    def emitters(): Seq[EntryEmitter] = {
      val result = ListBuffer[EntryEmitter]()

      val resourceTypes: Seq[ParametrizedResourceType] = field.array.values.collect {
        case a: ParametrizedResourceType => a
      }
      if (resourceTypes.nonEmpty) result += EndPointExtendsEmitter(prefix, resourceTypes, ordering)

      val traits: Seq[ParametrizedTrait] = field.array.values.collect { case a: ParametrizedTrait => a }
      if (traits.nonEmpty) result += TraitExtendsEmitter(prefix, traits, ordering)

      result
    }
  }

  case class TraitExtendsEmitter(prefix: String, traits: Seq[ParametrizedTrait], ordering: SpecOrdering)
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        prefix + "is",
        _.list(traverse(ordering.sorted(traits.map(ParametrizedDeclarationEmitter(_, ordering))), _))
      )
    }

    override def position(): Position = traits.headOption.map(rt => pos(rt.annotations)).getOrElse(Position.ZERO)
  }

  case class EndPointExtendsEmitter(prefix: String,
                                    resourceTypes: Seq[ParametrizedResourceType],
                                    ordering: SpecOrdering)
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        prefix + "type",
        ParametrizedDeclarationEmitter(resourceTypes.head, ordering).emit(_)
      )
    }

    override def position(): Position =
      resourceTypes.headOption.map(rt => pos(rt.annotations)).getOrElse(Position.ZERO)
  }

  case class ParametrizedDeclarationEmitter(declaration: ParametrizedDeclaration, ordering: SpecOrdering)
      extends PartEmitter {
    override def emit(b: PartBuilder): Unit = {
      if (declaration.variables.nonEmpty) {
        b.map {
          _.entry(
            declaration.name,
            _.map { b =>
              val result = declaration.variables.map(variable =>
                MapEntryEmitter(variable.name, variable.value, position = pos(variable.annotations)))

              traverse(ordering.sorted(result), b)
            }
          )
        }
      } else {
        raw(b, declaration.name)
      }
    }

    override def position(): Position = pos(declaration.annotations)
  }

  case class AbstractDeclarationsEmitter(key: String,
                                         declarations: Seq[AbstractDeclaration],
                                         ordering: SpecOrdering,
                                         tagEmitter: (DomainElement, String) => PartEmitter)
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(key, _.map { b =>
        traverse(ordering.sorted(declarations.map(d => AbstractDeclarationEmitter(d, ordering, tagEmitter))), b)
      })
    }

    override def position(): Position = declarations.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)
  }

  case class AbstractDeclarationEmitter(declaration: AbstractDeclaration,
                                        ordering: SpecOrdering,
                                        tagEmitter: (DomainElement, String) => PartEmitter)
      extends EntryEmitter {

    override def emit(b: EntryBuilder): Unit = {
      val name = Option(declaration.name)
        .getOrElse(throw new Exception(s"Cannot declare abstract declaration without name $declaration"))

      b.entry(
        name,
        b => {
          if (declaration.isLink)
            declaration.linkTarget.foreach(l => tagEmitter(l, declaration.linkLabel.getOrElse(l.id)).emit(b))
          else
            DataNodeEmitter(declaration.dataNode, ordering).emit(b)
        }
      )
    }

    override def position(): Position = pos(declaration.annotations)
  }

  case class DataPropertyEmitter(property: String, dataNode: DataObjectNode, ordering: SpecOrdering)
      extends EntryEmitter {
    val annotations: Annotations     = dataNode.propertyAnnotations(property)
    val propertyValue: Seq[DataNode] = dataNode.properties(property)

    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        property,
        b => {
          // In the current implementation ther can only be one value, we are NOT flattening arrays
          DataNodeEmitter(propertyValue.head, ordering).emit(b)
        }
      )
    }

    override def position(): Position = pos(annotations)
  }

  case class ArrayEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, force: Boolean = false)
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val single = f.value.annotations.contains(classOf[SingleValueArray])

      sourceOr(
        f.value,
        if (single && !force) emitSingle(b) else emitValues(b)
      )
    }

    private def emitSingle(b: EntryBuilder) = {
      val value = f.array.scalars.headOption.map(_.toString).getOrElse("")
      b.entry(key, value)
    }

    private def emitValues(b: EntryBuilder) = {
      b.entry(
        key,
        b => {
          val result = mutable.ListBuffer[PartEmitter]()

          f.array.scalars
            .foreach(v => {
              result += ScalarEmitter(v)
            })

          b.list(b => {
            traverse(ordering.sorted(result), b)
          })
        }
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class RamlCreativeWorkItemsEmitter(documentation: CreativeWork, ordering: SpecOrdering, withExtention: Boolean) {
    def emitters(): Seq[EntryEmitter] = {
      val result = ListBuffer[EntryEmitter]()

      val fs = documentation.fields

      fs.entry(CreativeWorkModel.Url).map(f => result += ValueEmitter(if (withExtention) "(url)" else "url", f))

      fs.entry(CreativeWorkModel.Description).map(f => result += ValueEmitter("content", f))

      fs.entry(CreativeWorkModel.Title).map(f => result += ValueEmitter("title", f))

      result ++= RamlAnnotationsEmitter(documentation, ordering).emitters
      ordering.sorted(result)
    }
  }

  case class RamlCreativeWorkEmitter(documentation: CreativeWork, ordering: SpecOrdering, withExtension: Boolean)
      extends PartEmitter {
    override def emit(b: PartBuilder): Unit = {
      sourceOr(
        documentation.annotations,
        b.map(traverse(RamlCreativeWorkItemsEmitter(documentation, ordering, withExtension).emitters(), _))
      )
    }

    override def position(): Position = pos(documentation.annotations)
  }

  case class OasCreativeWorkItemsEmitter(document: CreativeWork, ordering: SpecOrdering) {
    def emitters(): Seq[EntryEmitter] = {
      val fs     = document.fields
      val result = ListBuffer[EntryEmitter]()

      fs.entry(CreativeWorkModel.Url).map(f => result += ValueEmitter("url", f))

      fs.entry(CreativeWorkModel.Description).map(f => result += ValueEmitter("description", f))

      fs.entry(CreativeWorkModel.Title).map(f => result += ValueEmitter("x-title", f))

      result ++= OasAnnotationsEmitter(document, ordering).emitters

      ordering.sorted(result)
    }
  }

  case class OasCreativeWorkEmitter(document: CreativeWork, ordering: SpecOrdering) extends PartEmitter {
    override def emit(b: PartBuilder): Unit = {
      if (document.isLink)
        raw(b, document.linkLabel.getOrElse(document.linkTarget.get.id))
      else
        b.map(traverse(OasCreativeWorkItemsEmitter(document, ordering).emitters(), _))
    }

    override def position(): Position = pos(document.annotations)
  }

  case class OasEntryCreativeWorkEmitter(key: String, documentation: CreativeWork, ordering: SpecOrdering)
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      sourceOr(
        documentation.annotations,
        b.entry(
          key,
          OasCreativeWorkEmitter(documentation, ordering).emit(_)
        )
      )
    }

    override def position(): Position = pos(documentation.annotations)
  }
}
