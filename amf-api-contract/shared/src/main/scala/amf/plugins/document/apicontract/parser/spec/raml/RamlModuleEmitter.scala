package amf.plugins.document.apicontract.parser.spec.raml

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Fragment, Module}
import amf.core.client.scala.model.domain.templates.AbstractDeclaration
import amf.core.internal.metamodel.document.BaseUnitModel
import amf.core.internal.remote.Raml10
import amf.core.internal.render.BaseEmitters.{EntryPartEmitter, ValueEmitter, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.validation.CoreValidations.ResolutionValidation
import amf.plugins.document.apicontract.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.apicontract.model._
import amf.plugins.document.apicontract.parser.spec.declaration._
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.{
  AnnotationsEmitter,
  DataNodeEmitter
}
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml.Raml10TypeEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{
  AgnosticShapeEmitterContextAdapter,
  RamlShapeEmitterContext,
  RamlShapeEmitterContextAdapter,
  ShapeEmitterContext
}
import amf.plugins.document.apicontract.parser.spec.domain.NamedExampleEmitter
import amf.plugins.document.apicontract.parser.spec.raml.emitters.Raml10SecuritySchemeEmitter
import amf.plugins.document.apicontract.parser.{RamlFragmentHeader, RamlHeader}
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.YDocument

/**
  *
  */
case class RamlModuleEmitter(module: Module)(implicit val spec: RamlSpecEmitterContext) {
  protected implicit val shapeCtx: RamlShapeEmitterContext = RamlShapeEmitterContextAdapter(spec)
  def emitModule(): YDocument = {

    val ordering: SpecOrdering = SpecOrdering.ordering(Raml10, module.annotations)

    // TODO ordering??
    val emitters = spec.factory
      .rootLevelEmitters(module, ordering)
      .emitters ++ AnnotationsEmitter(module, ordering).emitters

    // TODO invoke traits end resource types

    YDocument { b =>
      b.comment(RamlHeader.Raml10Library.text)
      if (emitters.nonEmpty) b.obj(traverse(ordering.sorted(emitters), _))
    }
  }
}

class RamlFragmentEmitter(fragment: Fragment)(implicit val spec: RamlSpecEmitterContext) {

  protected implicit val shapeCtx: RamlShapeEmitterContext = RamlShapeEmitterContextAdapter(spec)

  def emitFragment(): YDocument = {

    val ordering: SpecOrdering = SpecOrdering.ordering(Raml10, fragment.encodes.annotations)

    val typeEmitter: RamlFragmentTypeEmitter = fragment match {
      case di: DocumentationItemFragment         => DocumentationItemFragmentEmitter(di, ordering)
      case dt: DataTypeFragment                  => DataTypeFragmentEmitter(dt, ordering)
      case ne: NamedExampleFragment              => FragmentNamedExampleEmitter(ne, ordering)
      case rt: ResourceTypeFragment              => ResourceTypeFragmentEmitter(rt, ordering)(spec.eh)
      case tf: TraitFragment                     => TraitFragmentEmitter(tf, ordering)(spec.eh)
      case at: AnnotationTypeDeclarationFragment => AnnotationFragmentEmitter(at, ordering)
      case sc: SecuritySchemeFragment            => SecuritySchemeFragmentEmitter(sc, ordering)
      case _                                     => throw new UnsupportedOperationException(s"Unsupported fragment type: $fragment")
    }

    val usage = fragment.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("usage", f))
    // should ignore for 08?
    val references = Seq(ReferencesEmitter(fragment, ordering))

    YDocument(b => {
      b.comment(typeEmitter.header.text)
      b.obj(traverse(ordering.sorted(typeEmitter.emitters(fragment.references) ++ usage ++ references), _))
    })
  }

  trait RamlFragmentTypeEmitter {
    val header: RamlHeader

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter]
  }

  case class DocumentationItemFragmentEmitter(documentationItem: DocumentationItemFragment, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10DocumentationItem

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] =
      RamlCreativeWorkItemsEmitter(documentationItem.encodes, ordering, withExtention = true).emitters()
  }

  case class DataTypeFragmentEmitter(dataType: DataTypeFragment, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10DataType

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] =
      Option(dataType.encodes) match {
        case Some(shape: AnyShape) => Raml10TypeEmitter(shape, ordering, references = Nil).entries()
        case Some(other) =>
          spec.eh.violation(ResolutionValidation,
                            other.id,
                            None,
                            "Cannot emit non WebApi Shape",
                            other.position(),
                            other.location())
          Nil
        case _ => Nil // ignore
      }

  }

  case class AnnotationFragmentEmitter(annotation: AnnotationTypeDeclarationFragment, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10AnnotationTypeDeclaration

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] =
      spec.factory.annotationTypeEmitter(annotation.encodes, ordering).emitters() match {
        case Left(emitters) => emitters
        case Right(part)    => Seq(EntryPartEmitter("type", part))
      }
  }

  case class SecuritySchemeFragmentEmitter(securityScheme: SecuritySchemeFragment, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10SecurityScheme

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] =
      Raml10SecuritySchemeEmitter(securityScheme.encodes, references, ordering).emitters()
  }

  case class ResourceTypeFragmentEmitter(fragment: ResourceTypeFragment, ordering: SpecOrdering)(
      implicit eh: AMFErrorHandler)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10ResourceType

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] =
      DataNodeEmitter(fragment.encodes.asInstanceOf[AbstractDeclaration].dataNode, ordering)
        .emitters() collect {
        case e: EntryEmitter => e
        case other           => throw new Exception(s"Fragment not encoding DataObjectNode but $other")
      }
  }

  case class TraitFragmentEmitter(fragment: TraitFragment, ordering: SpecOrdering)(implicit eh: AMFErrorHandler)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10Trait

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] =
      DataNodeEmitter(fragment.encodes.asInstanceOf[AbstractDeclaration].dataNode, ordering)
        .emitters() collect {
        case e: EntryEmitter => e
        case other           => throw new Exception(s"Fragment not encoding DataObjectNode but $other")
      }
  }

  case class FragmentNamedExampleEmitter(example: NamedExampleFragment, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)

    override val header: RamlHeader = RamlFragmentHeader.Raml10NamedExample

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] = Seq(NamedExampleEmitter(example.encodes, ordering))

  }
}
