package amf.plugins.document.webapi.parser.spec.oas

import amf.core.emitter.BaseEmitters.{ValueEmitter, _}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.document.BaseUnitModel
import amf.core.model.document.{Module, _}
import amf.core.model.domain.templates.AbstractDeclaration
import amf.core.parser.Position
import amf.core.remote.Oas
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.contexts.emitter.oas.OasSpecEmitterContext
import amf.plugins.document.webapi.model._
import amf.plugins.document.webapi.parser.OasHeader
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.OasTypeEmitter
import amf.plugins.document.webapi.parser.spec.domain.NamedExampleEmitter
import amf.plugins.document.webapi.parser.spec.oas.emitters.OasSecuritySchemeEmitter
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.{YDocument, YNode, YScalar, YType}

/**
  *
  */
abstract class OasModuleEmitter(module: Module)(implicit override val spec: OasSpecEmitterContext)
    extends OasSpecEmitter {

  def emitModule(): YDocument = {

    val ordering = SpecOrdering.ordering(Oas, module.annotations)

    val references = Seq(ReferencesEmitter(module, ordering))
    val declares   = OasDeclarationsEmitter(module.declares, ordering, module.references).emitters
    val usages     = module.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("usage".asOasExtension, f))

    YDocument {
      _.obj { b =>
        docVersion(b)
        traverse(ordering.sorted(declares ++ usages ++ references), b)
      }
    }
  }

  protected def docVersion(builder: EntryBuilder): Unit
}

case class Oas30ModuleEmitter(module: Module)(implicit override val spec: OasSpecEmitterContext)
    extends OasModuleEmitter(module) {
  override protected def docVersion(builder: EntryBuilder): Unit = builder.openapi = "3.0.0"
}

case class Oas20ModuleEmitter(module: Module)(implicit override val spec: OasSpecEmitterContext)
    extends OasModuleEmitter(module) {
  override protected def docVersion(builder: EntryBuilder): Unit = builder.swagger = "2.0"
}

class OasFragmentEmitter(fragment: Fragment)(implicit override val spec: OasSpecEmitterContext)
    extends OasDocumentEmitter(fragment) {
  override protected def versionEntry(b: YDocument.EntryBuilder): Unit = b.swagger = YNode(YScalar("2.0"), YType.Str)

  def emitFragment(): YDocument = {

    val ordering: SpecOrdering = SpecOrdering.ordering(Oas, fragment.annotations)

    val typeEmitter: OasFragmentTypeEmitter = fragment match {
      case di: DocumentationItemFragment         => DocumentationItemFragmentEmitter(di, ordering)
      case dt: DataTypeFragment                  => DataTypeFragmentEmitter(dt, ordering)
      case rt: ResourceTypeFragment              => ResourceTypeFragmentEmitter(rt, ordering)(spec.eh)
      case tf: TraitFragment                     => TraitFragmentEmitter(tf, ordering)(spec.eh)
      case at: AnnotationTypeDeclarationFragment => AnnotationFragmentEmitter(at, ordering)
      case sc: SecuritySchemeFragment            => SecuritySchemeFragmentEmitter(sc, ordering)
      case ne: NamedExampleFragment              => NamedExampleFragmentEmitter(ne, ordering)
      case _                                     => throw new UnsupportedOperationException("Unsupported fragment type")
    }
    val references = ReferencesEmitter(fragment, ordering)
    val usage: Option[ValueEmitter] =
      fragment.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("usage".asOasExtension, f))

    YDocument {
      _.obj { b =>
        traverse(Seq(typeEmitter.header)
                   ++ typeEmitter.emitters ++ usage :+ references,
                 b)
      }
    }
  }

  trait OasFragmentTypeEmitter {
    val header: EntryEmitter

    val emitters: Seq[EntryEmitter]
  }

  case class DocumentationItemFragmentEmitter(documentationItem: DocumentationItemFragment, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val header: EntryEmitter = OasHeaderEmitter(OasHeader.Oas20DocumentationItem)

    val emitters: Seq[EntryEmitter] = OasCreativeWorkItemsEmitter(documentationItem.encodes, ordering).emitters()
  }

  case class DataTypeFragmentEmitter(dataType: DataTypeFragment, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val header: OasHeaderEmitter = OasHeaderEmitter(OasHeader.Oas20DataType)

    val emitters: Seq[EntryEmitter] =
      oas.OasTypeEmitter(dataType.encodes, ordering, references = dataType.references).entries()
  }

  case class AnnotationFragmentEmitter(annotation: AnnotationTypeDeclarationFragment, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val header: EntryEmitter = OasHeaderEmitter(OasHeader.Oas20AnnotationTypeDeclaration)

    val emitters: Seq[EntryEmitter] =
      spec.factory.annotationTypeEmitter(annotation.encodes, ordering).emitters() match {
        case Left(list)  => list
        case Right(part) => Seq(EntryPartEmitter("type", part))
      }
  }

  case class ResourceTypeFragmentEmitter(resourceTypeFragment: ResourceTypeFragment, ordering: SpecOrdering)(
      implicit eh: ErrorHandler)
      extends OasFragmentTypeEmitter {

    override val header: EntryEmitter = OasHeaderEmitter(OasHeader.Oas20ResourceType)

    val emitters: Seq[EntryEmitter] =
      DataNodeEmitter(resourceTypeFragment.encodes.asInstanceOf[AbstractDeclaration].dataNode, ordering)(eh)
        .emitters() collect {
        case e: EntryEmitter => e
        case other           => throw new Exception(s"Fragment not encoding DataObjectNode but $other")
      }
  }

  case class TraitFragmentEmitter(traitFragment: TraitFragment, ordering: SpecOrdering)(implicit eh: ErrorHandler)
      extends OasFragmentTypeEmitter {

    override val header: EntryEmitter = OasHeaderEmitter(OasHeader.Oas20Trait)

    val emitters: Seq[EntryEmitter] =
      DataNodeEmitter(traitFragment.encodes.asInstanceOf[AbstractDeclaration].dataNode, ordering)(eh)
        .emitters() collect {
        case e: EntryEmitter => e
        case other           => throw new Exception(s"Fragment not encoding DataObjectNode but $other")
      }
  }

  case class SecuritySchemeFragmentEmitter(securityScheme: SecuritySchemeFragment, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val header: EntryEmitter = OasHeaderEmitter(OasHeader.Oas20SecurityScheme)

    val emitters: Seq[EntryEmitter] =
      new OasSecuritySchemeEmitter(
        securityScheme.encodes,
        OasLikeSecuritySchemeTypeMappings.mapsTo(spec.vendor, securityScheme.encodes.`type`.value()),
        ordering).emitters()
  }

  case class NamedExampleFragmentEmitter(namedExample: NamedExampleFragment, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val header: EntryEmitter = OasHeaderEmitter(OasHeader.Oas20NamedExample)

    val emitters: Seq[EntryEmitter] = Seq(NamedExampleEmitter(namedExample.encodes, ordering))
  }

  case class OasHeaderEmitter(oasHeader: OasHeader) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      MapEntryEmitter(oasHeader.key, oasHeader.value).emit(b)
    }

    override def position(): Position = Position.ZERO
  }

}
