package amf.spec.declaration

import amf.framework.model.domain.{ArrayNode => DataArrayNode, ObjectNode => DataObjectNode, ScalarNode => DataScalarNode}
import amf.framework.model.domain._
import amf.framework.parser.{Annotations, FieldEntry, Position, Value}
import amf.plugins.domain.shapes.models.{Shape, ShapeExtension}
import amf.plugins.domain.webapi.metamodel.CustomDomainPropertyModel
import amf.plugins.domain.webapi.models.CustomDomainProperty
import amf.plugins.domain.webapi.models.extensions.DomainExtension
import amf.remote.{Oas, Raml}
import amf.spec.common.BaseEmitters._
import amf.spec.common.SpecEmitterContext
import amf.spec.{Emitter, EntryEmitter, PartEmitter, SpecOrdering}
import amf.vocabulary.{Namespace, VocabularyMappings}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.YType

import scala.collection.mutable.ListBuffer

/**
  *
  */
case class AnnotationsEmitter(element: DomainElement, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters: Seq[EntryEmitter] = element.customDomainProperties.map(AnnotationEmitter(_, ordering))
}

case class AnnotationEmitter(domainExtension: DomainExtension, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.complexEntry(
      b => {
        val name = domainExtension.name
        spec.vendor match {
          case Raml  => b += "(" + name + ")"
          case Oas   => b += "x-" + name
          case other => throw new IllegalArgumentException(s"Unsupported annotation format $other")
        }
      },
      b => {
        Option(domainExtension.extension).foreach { DataNodeEmitter(_, ordering).emit(b) }
      }
    )
  }

  override def position(): Position = pos(domainExtension.annotations)
}

case class FacetsEmitter(element: Shape, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters: Seq[EntryEmitter] = element.customShapeProperties.map { extension: ShapeExtension =>
    FacetsInstanceEmitter(extension, ordering)
  }
}

case class FacetsInstanceEmitter(shapeExtension: ShapeExtension, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.complexEntry(
      b => {
        val name = shapeExtension.definedBy.name
        spec.vendor match {
          case Raml  => b += name
          case Oas   => b += "x-facet-" + name
          case other => throw new IllegalArgumentException(s"Unsupported facet format $other")
        }
      },
      b => {
        Option(shapeExtension.extension).foreach { DataNodeEmitter(_, ordering).emit(b) }
      }
    )
  }

  override def position(): Position = pos(shapeExtension.annotations)
}

case class DataNodeEmitter(dataNode: DataNode, ordering: SpecOrdering) extends PartEmitter {
  private val xsdString: String  = (Namespace.Xsd + "string").iri()
  private val xsdInteger: String = (Namespace.Xsd + "integer").iri()
  private val xsdFloat: String   = (Namespace.Xsd + "float").iri()
  private val xsdBoolean: String = (Namespace.Xsd + "boolean").iri()
  private val xsdNil: String     = (Namespace.Xsd + "nil").iri()

  override def emit(b: PartBuilder): Unit = {
    dataNode match {
      case scalar: ScalarNode => emitScalar(scalar, b)
      case array: ArrayNode   => emitArray(array, b)
      case obj: ObjectNode    => emitObject(obj, b)
    }
  }

  def emitters(): Seq[EntryEmitter] = {
    (dataNode match {
      case scalar: ScalarNode => Seq(scalarEmitter(scalar))
      case array: ArrayNode   => arrayEmitters(array)
      case obj: ObjectNode    => objectEmitters(obj)
    }) collect {
      case e: EntryEmitter => e
      case other           => throw new Exception(s"Unsupported seq of emitter type in data node emitters $other")
    }
  }

  def objectEmitters(objectNode: ObjectNode): Seq[EntryEmitter] = {
    objectNode.properties.keys.map { property =>
      DataPropertyEmitter(property, objectNode, ordering)
    }.toSeq
  }

  def emitObject(objectNode: ObjectNode, b: PartBuilder): Unit = {
    b.obj(b => ordering.sorted(objectEmitters(objectNode)).foreach(_.emit(b)))
  }

  def arrayEmitters(arrayNode: ArrayNode): Seq[PartEmitter] = arrayNode.members.map(DataNodeEmitter(_, ordering))

  def emitArray(arrayNode: ArrayNode, b: PartBuilder): Unit = {
    b.list(b => {
      ordering.sorted(arrayEmitters(arrayNode)).foreach(_.emit(b))
    })
  }

  def emitScalar(scalar: ScalarNode, b: PartBuilder): Unit = {
    scalarEmitter(scalar).emit(b)
  }

  def scalarEmitter(scalar: ScalarNode): PartEmitter = {
    scalar.dataType match {
      case Some(t) if t == xsdString  => TextScalarEmitter(scalar.value, scalar.annotations, YType.Str)
      case Some(t) if t == xsdInteger => TextScalarEmitter(scalar.value, scalar.annotations, YType.Int)
      case Some(t) if t == xsdFloat   => TextScalarEmitter(scalar.value, scalar.annotations, YType.Float)
      case Some(t) if t == xsdBoolean => TextScalarEmitter(scalar.value, scalar.annotations, YType.Bool)
      case Some(t) if t == xsdNil     => TextScalarEmitter("null", Annotations(), YType.Str)
      case _                          => TextScalarEmitter(scalar.value, Annotations(), YType.Str)
    }
  }

  override def position(): Position = pos(dataNode.annotations)
}

case class DataPropertyEmitter(property: String, dataNode: ObjectNode, ordering: SpecOrdering)
    extends EntryEmitter {
  val annotations: Annotations = dataNode.propertyAnnotations(property)
  val propertyValue: DataNode  = dataNode.properties(property)

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      property,
      b => {
        // In the current implementation ther can only be one value, we are NOT flattening arrays
        DataNodeEmitter(propertyValue, ordering).emit(b)
      }
    )
  }

  override def position(): Position = pos(annotations)
}

case class AnnotationTypeEmitter(property: CustomDomainProperty, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext) {

  private val fs = property.fields
  private val shapeEmitters: Seq[Emitter] = fs
    .entry(CustomDomainPropertyModel.Schema)
    .map({ f =>
      spec.vendor match {
        case Oas =>
          // OAS we emit in the 'schema' property
          Seq(OasSchemaEmitter(f, ordering, Nil))
        case Raml =>
          // we merge in the main body
          val shape = f.value.value.asInstanceOf[Shape]

          RamlTypeEmitter(shape, ordering, Nil, Nil).emitters() match {
            case es if es.forall(_.isInstanceOf[RamlTypeExpressionEmitter]) => es
            case es if es.forall(_.isInstanceOf[EntryEmitter])              => es.collect { case e: EntryEmitter => e }
            case other                                                      => throw new Exception(s"IllegalTypeDeclarations found: $other")
          }
        case other => throw new IllegalArgumentException(s"Unsupported vendor $other for annotation type generation")
      }
    }) match {
    case Some(emitters) => emitters
    case _              => Nil
  }

  def emitters(): Either[Seq[EntryEmitter], PartEmitter] = {

    shapeEmitters.toList match {
      case (head: EntryEmitter) :: tail =>
        val result = ListBuffer[EntryEmitter]()
        fs.entry(CustomDomainPropertyModel.DisplayName).map(f => result += ValueEmitter("displayName", f))

        fs.entry(CustomDomainPropertyModel.Description).map(f => result += ValueEmitter("description", f))

        fs.entry(CustomDomainPropertyModel.Domain).map { f =>
          val scalars = f.array.scalars.map { s =>
            VocabularyMappings.uriToRaml.get(s.toString) match {
              case Some(identifier) => AmfScalar(identifier, s.annotations)
              case None             => s
            }
          }
          val finalArray      = AmfArray(scalars, f.array.annotations)
          val finalFieldEntry = FieldEntry(f.field, Value(finalArray, f.value.annotations))

          result += ArrayEmitter("allowedTargets", finalFieldEntry, ordering)
        }

        result ++= shapeEmitters.map(_.asInstanceOf[EntryEmitter])

        result ++= AnnotationsEmitter(property, ordering).emitters
        Left(result)
      case (head: PartEmitter) :: Nil => Right(head)
      case other =>
        throw new Exception(s"IllegalTypeDeclarations found: $other") // todo handle
    }
  }
}
