package amf.plugins.document.webapi.parser.spec.raml

import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{SpecOrdering, EntryEmitter}
import amf.core.metamodel.document.BaseUnitModel
import amf.core.model.document.{Module, BaseUnit, _}
import amf.core.model.domain.templates.AbstractDeclaration
import amf.core.parser.ErrorHandler
import amf.core.remote.Raml
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.webapi.model._
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain.NamedExampleEmitter
import amf.plugins.document.webapi.parser.{RamlFragmentHeader, RamlHeader}
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
import org.yaml.model.YDocument

/**
  *
  */
case class RamlModuleEmitter(module: Module)(implicit val spec: RamlSpecEmitterContext) {

  def emitModule(): YDocument = {

    val ordering: SpecOrdering = SpecOrdering.ordering(Raml, module.annotations)

    // TODO ordering??
    val emitters = spec.factory.rootLevelEmitters(module, ordering).emitters

    // TODO invoke traits end resource types

    YDocument { b =>
      b.comment(RamlHeader.Raml10Library.text)
      if (emitters.nonEmpty) b.obj(traverse(ordering.sorted(emitters), _))
    }
  }
}

class RamlFragmentEmitter(fragment: Fragment)(implicit val spec: RamlSpecEmitterContext) {
  def emitFragment(): YDocument = {

    val ordering: SpecOrdering = SpecOrdering.ordering(Raml, fragment.encodes.annotations)

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
      implicit eh: ErrorHandler)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10ResourceType

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] =
      DataNodeEmitter(fragment.encodes.asInstanceOf[AbstractDeclaration].dataNode, ordering)
        .emitters() collect {
        case e: EntryEmitter => e
        case other           => throw new Exception(s"Fragment not encoding DataObjectNode but $other")
      }
  }

  case class TraitFragmentEmitter(fragment: TraitFragment, ordering: SpecOrdering)(implicit eh: ErrorHandler)
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

    override val header: RamlHeader = RamlFragmentHeader.Raml10NamedExample

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] = Seq(NamedExampleEmitter(example.encodes, ordering))

  }
}
