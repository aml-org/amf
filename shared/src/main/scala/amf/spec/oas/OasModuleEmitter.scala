package amf.spec.oas

import amf.compiler.{OasFragmentHeader, OasHeader}
import amf.document.Fragment._
import amf.document.Module
import amf.domain.`abstract`.AbstractDeclaration
import amf.metadata.document.BaseUnitModel
import amf.metadata.document.FragmentsTypesModels.{ExtensionModel, OverlayModel}
import amf.parser.Position
import amf.remote.Oas
import amf.spec.{Emitter, SpecOrdering}
import org.yaml.model.YDocument

import scala.collection.mutable.ListBuffer

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

class OasFragmentEmitter(fragment: Fragment) extends OasDocumentEmitter(fragment) {
  def emitFragment(): YDocument = {

    val ordering: SpecOrdering = SpecOrdering.ordering(Oas, fragment.annotations)

    val typeEmitter: OasFragmentTypeEmitter = fragment match {
      case di: DocumentationItem         => DocumentationItemFragmentEmitter(di, ordering)
      case dt: DataType                  => DataTypeFragmentEmitter(dt, ordering)
      case rt: ResourceTypeFragment      => ResourceTypeFragmentEmitter(rt, ordering)
      case tf: TraitFragment             => TraitFragmentEmitter(tf, ordering)
      case at: AnnotationTypeDeclaration => AnnotationFragmentEmitter(at, ordering)
      case ef: ExtensionFragment         => ExtensionFragmentEmitter(ef, ordering)
      case of: OverlayFragment           => OverlayFragmentEmitter(of, ordering)
      //      case _: NamedExample              => Raml10NamedExample
      case _ => throw new UnsupportedOperationException("Unsupported fragment type")
    }
    val referenceEmitter = Seq(ReferencesEmitter(fragment.references, ordering))
    val usageEmitter: Option[ValueEmitter] =
      fragment.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("x-usage", f))

    emitter.document({ () =>
      map { () =>
        traverse(
          Seq(OasHeaderEmitter(OasHeader.Oas20), typeEmitter.headerEmitter)
            ++ typeEmitter.elementsEmitters ++ usageEmitter ++ referenceEmitter)
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

    val elementsEmitters: Seq[Emitter] = OasCreativeWorkItemsEmitter(documentationItem.encodes, ordering).emitters()
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

  case class ExtensionFragmentEmitter(extension: ExtensionFragment, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val headerEmitter: Emitter = OasHeaderEmitter(OasFragmentHeader.Oas20Extension)

    val elementsEmitters: Seq[Emitter] = {
      val result: ListBuffer[Emitter] = ListBuffer()
      extension.fields
        .entry(ExtensionModel.Extends)
        .foreach(f => result += NamedRefEmitter("extends", f.scalar.toString, pos = pos(f.value.annotations)))
      result ++= WebApiEmitter(extension.encodes, ordering, Some(Oas)).emitters
      result
    }
  }

  case class OverlayFragmentEmitter(extension: OverlayFragment, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val headerEmitter: Emitter = OasHeaderEmitter(OasFragmentHeader.Oas20Overlay)

    val elementsEmitters: Seq[Emitter] = {
      val result: ListBuffer[Emitter] = ListBuffer()
      extension.fields
        .entry(OverlayModel.Extends)
        .foreach(f => result += NamedRefEmitter("extends", f.scalar.toString, pos = pos(f.value.annotations)))
      result ++= WebApiEmitter(extension.encodes, ordering, Some(Oas)).emitters
      result
    }
  }

  case class OasHeaderEmitter(oasHeader: OasHeader) extends Emitter {
    override def emit(): Unit = {
      EntryEmitter(oasHeader.key, oasHeader.value).emit()
    }

    override def position(): Position = Position.ZERO
  }
}
