package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.annotations.SourceAST
import amf.core.emitter.BaseEmitters._
import amf.core.emitter._
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.domain.extensions.CustomDomainPropertyModel
import amf.core.model.domain._
import amf.core.model.domain.extensions.{CustomDomainProperty, DomainExtension, ShapeExtension}
import amf.core.parser.{Annotations, FieldEntry, Position, Value}
import amf.core.utils._
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.oas.OasSpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{
  OasSchemaEmitter,
  Raml10TypeEmitter,
  RamlRecursiveShapeEmitter,
  RamlTypeExpressionEmitter
}
import amf.plugins.document.webapi.vocabulary.VocabularyMappings
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.webapi.annotations.OrphanOasExtension
import amf.validations.RenderSideValidations.RenderValidation
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  *
  */
case class AnnotationsEmitter(element: DomainElement, ordering: SpecOrdering)(implicit spec: SpecEmitterContext) {
  def emitters: Seq[EntryEmitter] =
    element.customDomainProperties
      .filter(!_.extension.annotations.contains(classOf[OrphanOasExtension]))
      .map(spec.factory.annotationEmitter(_, ordering))
}

case class OrphanAnnotationsEmitter(orphans: Seq[DomainExtension], ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext) {
  def emitters: Seq[EntryEmitter] = orphans.map(spec.factory.annotationEmitter(_, ordering))
}

case class OasAnnotationEmitter(domainExtension: DomainExtension, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends AnnotationEmitter(domainExtension, ordering) {

  override val name: String = "x-" + domainExtension.name.value()
}

case class RamlAnnotationEmitter(domainExtension: DomainExtension, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends AnnotationEmitter(domainExtension, ordering) {

  override val name: String = "(" + domainExtension.name.value() + ")"
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
        Option(domainExtension.extension).foreach { DataNodeEmitter(_, ordering)(spec.eh).emit(b) }
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

  override val name: String = s"facet-${shapeExtension.definedBy.name.value()}".asOasExtension
}

case class RamlFacetsInstanceEmitter(shapeExtension: ShapeExtension, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends FacetsInstanceEmitter(shapeExtension, ordering) {

  override val name: String = shapeExtension.definedBy.name.value()
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
        Option(shapeExtension.extension).foreach { DataNodeEmitter(_, ordering)(spec.eh).emit(b) }
      }
    )
  }

  override def position(): Position = pos(shapeExtension.annotations)
}

case class DataNodeEmitter(
    dataNode: DataNode,
    ordering: SpecOrdering,
    resolvedLinks: Boolean = false,
    referencesCollector: mutable.Map[String, DomainElement] = mutable.Map())(implicit eh: ErrorHandler)
    extends PartEmitter {
  private val xsdString: String  = Namespace.XsdTypes.xsdString.iri()
  private val xsdInteger: String = Namespace.XsdTypes.xsdInteger.iri()
  private val xsdFloat: String   = Namespace.XsdTypes.xsdFloat.iri()
  private val amlNumber: String  = Namespace.XsdTypes.amlNumber.iri()
  private val xsdDouble: String  = Namespace.XsdTypes.xsdDouble.iri()
  private val xsdBoolean: String = Namespace.XsdTypes.xsdBoolean.iri()
  private val xsdNil: String     = Namespace.XsdTypes.xsdNil.iri()

  override def emit(b: PartBuilder): Unit = {
    dataNode match {
      case scalar: ScalarNode => emitScalar(scalar, b)
      case array: ArrayNode   => emitArray(array, b)
      case obj: ObjectNode    => emitObject(obj, b)
      case link: LinkNode     => emitLink(link, b)
    }
  }

  def emitters(): Seq[EntryEmitter] = {
    val e: Seq[Option[EntryEmitter]] = (dataNode match {
      case scalar: ScalarNode => Seq(scalarEmitter(scalar))
      case array: ArrayNode   => arrayEmitters(array)
      case obj: ObjectNode    => objectEmitters(obj)
      case link: LinkNode     => linkEmitters(link)
    }) collect {
      case e: EntryEmitter => Some(e)
      case t: TextScalarEmitter =>
        Some(new EntryEmitter() {
          override def emit(b: EntryBuilder): Unit = b.entry(YNode("@value"), t.value)
          override def position(): Position        = t.position()
        })
      case n: NullEmitter =>
        Some(new EntryEmitter {
          override def emit(b: EntryBuilder): Unit = b.entry(YNode("@value"), n.emit(_))
          override def position(): Position        = n.position()
        })
      case other =>
        eh.violation(
          RenderValidation,
          dataNode.id,
          None,
          s"Unsupported seq of emitter type in data node emitters $other",
          dataNode.position(),
          dataNode.location()
        )
        None
    }
    e.flatten
  }

  def objectEmitters(objectNode: ObjectNode): Seq[EntryEmitter] = {
    objectNode
      .propertyFields()
      .map { f =>
        val value = objectNode.fields.getValue(f)
        DataPropertyEmitter(f.value.name.urlComponentDecoded,
                            value.value.asInstanceOf[DataNode],
                            ordering,
                            resolvedLinks,
                            referencesCollector,
                            value.annotations)
      }
      .toSeq
  }

  def emitObject(objectNode: ObjectNode, b: PartBuilder): Unit = {
    b.obj(b => {

      val ordered = ordering.sorted(objectEmitters(objectNode))
      ordered.foreach(_.emit(b))
    })
  }

  def arrayEmitters(arrayNode: ArrayNode): Seq[PartEmitter] =
    arrayNode.members.map(DataNodeEmitter(_, ordering, resolvedLinks, referencesCollector))

  def emitArray(arrayNode: ArrayNode, b: PartBuilder): Unit = {
    b.list(b => {
      ordering.sorted(arrayEmitters(arrayNode)).foreach(_.emit(b))
    })
  }

  def emitScalar(scalar: ScalarNode, b: PartBuilder): Unit = {
    scalarEmitter(scalar).emit(b)
  }

  def emitLink(link: LinkNode, b: PartBuilder): Unit =
    linkEmitters(link).foreach(_.emit(b))

  def linkEmitters(link: LinkNode): Seq[PartEmitter] = {
    link.linkedDomainElement.foreach(elem => referencesCollector.update(link.alias.value(), elem))
    if (resolvedLinks) {
      Seq(LinkScalaEmitter(link.alias.value(), link.annotations))
    } else {
      Seq(LinkScalaEmitter(link.alias.value(), link.annotations))
    }
  }

  def scalarEmitter(scalar: ScalarNode): PartEmitter = {
    scalar.dataType.option() match {
      case Some(t) if t == xsdString => TextScalarEmitter(scalar.value.value(), scalar.annotations)
      case Some(t) if t == xsdInteger =>
        TextScalarEmitter(scalar.value.value(), scalar.annotations, YType.Int)
      case Some(t) if t == xsdDouble | t == amlNumber =>
        TextScalarEmitter(scalar.value.value(), scalar.annotations, YType.Float)
      case Some(t) if t == xsdBoolean => TextScalarEmitter(scalar.value.value(), scalar.annotations, YType.Bool)
      case Some(t) if t == xsdNil     => NullEmitter(scalar.annotations)
      case _                          => TextScalarEmitter(scalar.value.value(), Annotations())
    }
  }

  override def position(): Position = pos(dataNode.annotations)
}

case class DataPropertyEmitter(key: String,
                               value: DataNode,
                               ordering: SpecOrdering,
                               resolvedLinks: Boolean = false,
                               referencesCollector: mutable.Map[String, DomainElement] = mutable.Map(),
                               propertyAnnotations: Annotations)(implicit eh: ErrorHandler)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {

    val keyAnnotations = propertyAnnotations
      .find(classOf[SourceAST])
      .map(_.ast)
      .collectFirst({ case e: YMapEntry => Annotations(e.key) })
      .getOrElse(propertyAnnotations)
    b.entry(
      YNode(YScalar.withLocation(key.urlComponentDecoded, YType.Str, keyAnnotations.sourceLocation), YType.Str),
      // In the current implementation there can only be one value, we are NOT flattening arrays
      DataNodeEmitter(value, ordering, resolvedLinks, referencesCollector)(eh).emit(_)
    )
  }

  override def position(): Position = pos(value.annotations)
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
        case Some(shape: RecursiveShape) => RamlRecursiveShapeEmitter(shape, ordering, Nil).emitters()
        case Some(x) =>
          spec.eh.violation(RenderValidation,
                            property.id,
                            None,
                            "Cannot emit raml type for a shape that is not an AnyShape",
                            x.position(),
                            x.location())
          Nil
        case _ => Nil // ignore
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
