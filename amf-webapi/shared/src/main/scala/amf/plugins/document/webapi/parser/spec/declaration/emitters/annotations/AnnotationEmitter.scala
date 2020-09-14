package amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations

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
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.OasSchemaEmitter
import amf.plugins.document.webapi.parser.spec.declaration.emitters.raml.{
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

case class DataPropertyEmitter(key: String,
                               value: DataNode,
                               ordering: SpecOrdering,
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
      DataNodeEmitter(value, ordering, referencesCollector)(eh).emit(_)
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
