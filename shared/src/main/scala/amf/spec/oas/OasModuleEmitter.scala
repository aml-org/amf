package amf.spec.oas

import amf.compiler.{OasFragmentHeader, OasHeader}
import amf.document.Fragment._
import amf.document.Module
import amf.domain.`abstract`.AbstractDeclaration
import amf.metadata.document.BaseUnitModel
import amf.parser.Position
import amf.remote.Oas
import amf.spec.{Emitter, SpecOrdering}
import org.yaml.model.YDocument

/**
  *
  */
case class OasModuleEmitter(module: Module) extends OasSpecEmitter {

  def emitModule(): YDocument = {

    val ordering: SpecOrdering = SpecOrdering.ordering(Oas, module.annotations)

    // TODO ordering??
    val declares         = DeclarationsEmitter(module.declares, ordering).emitters
    val referenceEmitter = Seq(ReferencesEmitter(module.references, ordering))

    val usageEmitter: Option[ValueEmitter] =
      module.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("x-usage", f))

    // TODO invoke traits end resource types

    emitter.document({ () =>
      map { () =>
        entry { () =>
          raw("swagger")
          raw("2.0")
        }
        traverse(ordering.sorted(declares ++ usageEmitter ++ referenceEmitter))
      }
    })
  }

}

case class OasFragmentEmitter(fragment: Fragment) extends OasSpecEmitter {
  def emitFragment(): YDocument = {

    val ordering: SpecOrdering = SpecOrdering.ordering(Oas, fragment.annotations)

    val typeEmitter: OasFragmentTypeEmitter = fragment match {
      case di: DocumentationItem         => DocumentationItemFragmentEmitter(di, ordering)
      case dt: DataType                  => DataTypeFragmentEmitter(dt, ordering)
      case rt: ResourceTypeFragment      => ResourceTypeFragmentEmitter(rt, ordering)
      case tf: TraitFragment             => TraitFragmentEmitter(tf, ordering)
      case at: AnnotationTypeDeclaration => AnnotationFragmentEmitter(at, ordering)
      //      case _: NamedExample              => Raml10NamedExample
      case _ => throw new UnsupportedOperationException("Unsupported fragment type")
    }
    val referenceEmitter = Seq(ReferencesEmitter(fragment.references, ordering))

    emitter.document({ () =>
      map { () =>
        traverse(
          Seq(OasHeaderEmitter(OasHeader.Oas20), typeEmitter.headerEmitter)
            ++ typeEmitter.elementsEmitters ++ referenceEmitter)
      }
    })

  }

  trait OasFragmentTypeEmitter {
    val headerEmitter: Emitter

    val elementsEmitters: Seq[Emitter]
  }

  case class DocumentationItemFragmentEmitter(documentationItem: DocumentationItem, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val headerEmitter: Emitter = OasHeaderEmitter(OasFragmentHeader.Oas20DocumentationItem)

    val elementsEmitters: Seq[Emitter] = Seq(UserDocumentationEmitter(documentationItem.encodes, ordering))
  }

  case class DataTypeFragmentEmitter(dataType: DataType, ordering: SpecOrdering) extends OasFragmentTypeEmitter {

    override val headerEmitter: Emitter = OasHeaderEmitter(OasFragmentHeader.Oas20DataType)

    val elementsEmitters: Seq[Emitter] = OasTypeEmitter(dataType.encodes, ordering).emitters()
  }

  case class AnnotationFragmentEmitter(annotation: AnnotationTypeDeclaration, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val headerEmitter: Emitter = OasHeaderEmitter(OasFragmentHeader.Oas20AnnotationTypeDeclaration)

    val elementsEmitters: Seq[Emitter] =
      AnnotationTypeEmitter(annotation.encodes, ordering).emitters()
  }

  case class ResourceTypeFragmentEmitter(resourceTypeFragment: ResourceTypeFragment, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val headerEmitter: Emitter = OasHeaderEmitter(OasFragmentHeader.Oas20ResourceType)

    val elementsEmitters: Seq[Emitter] =
      DataNodeEmitter(resourceTypeFragment.encodes.asInstanceOf[AbstractDeclaration].dataNode, ordering)
        .emitters() // todo review with gute the map and sequence for oas
  }

  case class TraitFragmentEmitter(traitFragment: TraitFragment, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val headerEmitter: Emitter = OasHeaderEmitter(OasFragmentHeader.Oas20Trait)

    val elementsEmitters: Seq[Emitter] =
      Seq(DataNodeEmitter(traitFragment.encodes.asInstanceOf[AbstractDeclaration].dataNode, ordering)) // todo review with gute the map and sequence for oas
  }

  case class OasHeaderEmitter(oasHeader: OasHeader) extends Emitter {
    override def emit(): Unit = {
      EntryEmitter(oasHeader.key, oasHeader.value).emit()
    }

    override def position(): Position = Position.ZERO
  }
}
