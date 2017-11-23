package amf.spec.oas

import amf.framework.model.document.Fragment._
import amf.framework.model.document.Module
import amf.domain.`abstract`.AbstractDeclaration
import amf.framework.metamodel.document.BaseUnitModel
import amf.parser.Position
import amf.plugins.document.webapi.parser.OasHeader
import amf.remote.Oas
import amf.spec.common.BaseEmitters._
import amf.spec.declaration._
import amf.spec.domain.NamedExampleEmitter
import amf.spec.{EntryEmitter, SpecOrdering}
import org.yaml.model.YDocument
import org.yaml.model.YDocument.EntryBuilder

/**
  *
  */
case class OasModuleEmitter(module: Module) extends OasSpecEmitter {

  def emitModule(): YDocument = {

    val ordering = SpecOrdering.ordering(Oas, module.annotations)

    val references = Seq(ReferencesEmitter(module.references, ordering))
    val declares   = DeclarationsEmitter(module.declares, ordering, module.references).emitters
    val usages     = module.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("x-usage", f))

    YDocument {
      _.obj { b =>
        b.swagger = "2.0"
        traverse(ordering.sorted(declares ++ usages ++ references), b)
      }
    }
  }
}

class OasFragmentEmitter(fragment: Fragment) extends OasDocumentEmitter(fragment) {
  def emitFragment(): YDocument = {

    val ordering: SpecOrdering = SpecOrdering.ordering(Oas, fragment.annotations)

    val typeEmitter: OasFragmentTypeEmitter = fragment match {
      case di: DocumentationItem         => DocumentationItemFragmentEmitter(di, ordering)
      case dt: DataType                  => DataTypeFragmentEmitter(dt, ordering)
      case rt: ResourceTypeFragment      => ResourceTypeFragmentEmitter(rt, ordering)
      case tf: TraitFragment             => TraitFragmentEmitter(tf, ordering)
      case at: AnnotationTypeDeclaration => AnnotationFragmentEmitter(at, ordering)
      case sc: SecurityScheme            => SecuritySchemeFragmentEmitter(sc, ordering)
      case ne: NamedExample              => NamedExampleFragmentEmitter(ne, ordering)
      case _                             => throw new UnsupportedOperationException("Unsupported fragment type")
    }
    val references = ReferencesEmitter(fragment.references, ordering)
    val usage: Option[ValueEmitter] =
      fragment.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("x-usage", f))

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

  case class DocumentationItemFragmentEmitter(documentationItem: DocumentationItem, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val header = OasHeaderEmitter(OasHeader.Oas20DocumentationItem)

    val emitters: Seq[EntryEmitter] = OasCreativeWorkItemsEmitter(documentationItem.encodes, ordering).emitters()
  }

  case class DataTypeFragmentEmitter(dataType: DataType, ordering: SpecOrdering) extends OasFragmentTypeEmitter {

    override val header = OasHeaderEmitter(OasHeader.Oas20DataType)

    val emitters: Seq[EntryEmitter] =
      OasTypeEmitter(dataType.encodes, ordering, references = dataType.references).entries()
  }

  case class AnnotationFragmentEmitter(annotation: AnnotationTypeDeclaration, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val header = OasHeaderEmitter(OasHeader.Oas20AnnotationTypeDeclaration)

    val emitters: Seq[EntryEmitter] =
      AnnotationTypeEmitter(annotation.encodes, ordering).emitters() match {
        case Left(list)  => list
        case Right(part) => Seq(EntryPartEmitter("type", part))
      }
  }

  case class ResourceTypeFragmentEmitter(resourceTypeFragment: ResourceTypeFragment, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val header = OasHeaderEmitter(OasHeader.Oas20ResourceType)

    val emitters: Seq[EntryEmitter] =
      DataNodeEmitter(resourceTypeFragment.encodes.asInstanceOf[AbstractDeclaration].dataNode, ordering)
        .emitters() collect {
        case e: EntryEmitter => e
        case other           => throw new Exception(s"Fragment not encoding DataObjectNode but $other")
      }
  }

  case class TraitFragmentEmitter(traitFragment: TraitFragment, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val header = OasHeaderEmitter(OasHeader.Oas20Trait)

    val emitters: Seq[EntryEmitter] =
      DataNodeEmitter(traitFragment.encodes.asInstanceOf[AbstractDeclaration].dataNode, ordering).emitters() collect {
        case e: EntryEmitter => e
        case other           => throw new Exception(s"Fragment not encoding DataObjectNode but $other")
      }
  }

  case class SecuritySchemeFragmentEmitter(securityScheme: SecurityScheme, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val header = OasHeaderEmitter(OasHeader.Oas20SecurityScheme)

    val emitters: Seq[EntryEmitter] =
      OasSecuritySchemeEmitter(securityScheme.encodes,
                               OasSecuritySchemeTypeMapping.fromText(securityScheme.encodes.`type`),
                               ordering).emitters()
  }

  case class NamedExampleFragmentEmitter(namedExample: NamedExample, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val header = OasHeaderEmitter(OasHeader.Oas20NamedExample)

    val emitters: Seq[EntryEmitter] = Seq(NamedExampleEmitter(namedExample.encodes, ordering))
  }

  case class OasHeaderEmitter(oasHeader: OasHeader) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      MapEntryEmitter(oasHeader.key, oasHeader.value).emit(b)
    }

    override def position(): Position = Position.ZERO
  }

}
