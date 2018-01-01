package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.emitter.BaseEmitters._
import amf.core.emitter._
import amf.core.metamodel.domain.extensions.CustomDomainPropertyModel
import amf.core.model.domain._
import amf.core.model.domain.extensions.{CustomDomainProperty, DomainExtension, ShapeExtension}
import amf.core.parser.{Annotations, FieldEntry, Position, Value}
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.contexts.{OasSpecEmitterContext, RamlSpecEmitterContext, SpecEmitterContext}
import amf.plugins.document.webapi.vocabulary.VocabularyMappings
import amf.plugins.domain.shapes.models.AnyShape
import amf.core.utils._
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.YType

import scala.collection.mutable.ListBuffer

/**
  *
  */
case class AnnotationsEmitter(element: DomainElement, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters: Seq[EntryEmitter] = element.customDomainProperties.map(spec.factory.annotationEmitter(_, ordering))
}

case class OasAnnotationEmitter(domainExtension: DomainExtension, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends AnnotationEmitter(domainExtension, ordering) {

  override val name: String = "x-" + domainExtension.name
}

case class RamlAnnotationEmitter(domainExtension: DomainExtension, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends AnnotationEmitter(domainExtension, ordering) {

  override val name: String = "(" + domainExtension.name + ")"
}

abstract class AnnotationEmitter(domainExtension: DomainExtension, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  val name: String

  override def emit(b: EntryBuilder): Unit = {
    b.complexEntry(
      b => {
        b += name
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
    spec.factory.facetsInstanceEmitter(extension, ordering)
  }
}

case class OasFacetsInstanceEmitter(shapeExtension: ShapeExtension, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends FacetsInstanceEmitter(shapeExtension, ordering) {

  override val name: String = "x-facet-" + shapeExtension.definedBy.name
}

case class RamlFacetsInstanceEmitter(shapeExtension: ShapeExtension, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends FacetsInstanceEmitter(shapeExtension, ordering) {

  override val name: String = shapeExtension.definedBy.name
}

abstract class FacetsInstanceEmitter(shapeExtension: ShapeExtension, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  val name: String
  override def emit(b: EntryBuilder): Unit = {
    b.complexEntry(
      b => {
        b += name
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

case class DataPropertyEmitter(property: String, dataNode: ObjectNode, ordering: SpecOrdering) extends EntryEmitter {
  val annotations: Annotations = dataNode.propertyAnnotations(property)
  val propertyValue: DataNode  = dataNode.properties(property)

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      property.urlDecoded,
      b => {
        // In the current implementation ther can only be one value, we are NOT flattening arrays
        DataNodeEmitter(propertyValue, ordering).emit(b)
      }
    )
  }

  override def position(): Position = pos(annotations)
}

case class RamlAnnotationTypeEmitter(property: CustomDomainProperty, ordering: SpecOrdering)(
    implicit spec: RamlSpecEmitterContext)
    extends AnnotationTypeEmitter(property, ordering) {

  private val fs = property.fields
  override protected val shapeEmitters: Seq[Emitter] = fs
    .entry(CustomDomainPropertyModel.Schema)
    .map({ f =>
      // we merge in the main body
      Option(f.value.value) match {
        case Some(shape: AnyShape) =>
          Raml10TypeEmitter(shape, ordering, Nil, Nil).emitters() match {
            case es if es.forall(_.isInstanceOf[RamlTypeExpressionEmitter]) => es
            case es if es.forall(_.isInstanceOf[EntryEmitter])              => es.collect { case e: EntryEmitter => e }
            case other                                                      => throw new Exception(s"IllegalTypeDeclarations found: $other")
          }
        case Some(x) => throw new Exception("Cannot emit raml type for a shape that is not an AnyShape")
        case _       => Nil // ignore
      }
    }) match {
    case Some(emitters) => emitters
    case _              => Nil
  }
}

case class OasAnnotationTypeEmitter(property: CustomDomainProperty, ordering: SpecOrdering)(
    implicit spec: OasSpecEmitterContext)
    extends AnnotationTypeEmitter(property, ordering) {

  private val fs = property.fields
  override protected val shapeEmitters: Seq[Emitter] = fs
    .entry(CustomDomainPropertyModel.Schema)
    .map({ f =>
      OasSchemaEmitter(f, ordering, Nil)
    })
    .toSeq
}

abstract class AnnotationTypeEmitter(property: CustomDomainProperty, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext) {

  private val fs = property.fields
  protected val shapeEmitters: Seq[Emitter]

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
      case Nil                        => Left(Nil)
      case other =>
        throw new Exception(s"IllegalTypeDeclarations found: $other") // todo handle
    }
  }
}
