package amf.spec.raml

import amf.compiler.{RamlFragmentHeader, RamlHeader}
import amf.document.Fragment._
import amf.document.Module
import amf.domain.`abstract`.AbstractDeclaration
import amf.metadata.document.BaseUnitModel
import amf.metadata.document.FragmentsTypesModels.{ExtensionModel, OverlayModel}
import amf.remote.Raml
import amf.spec.{Emitter, EntryEmitter, SpecOrdering}
import org.yaml.model.YDocument

import scala.collection.mutable.ListBuffer

/**
  *
  */
case class RamlModuleEmitter(module: Module) extends RamlSpecEmitter {

  def emitModule(): YDocument = {

    val ordering: SpecOrdering = SpecOrdering.ordering(Raml, module.annotations)

    // TODO ordering??
    val declares   = DeclarationsEmitter(module.declares, module.references, ordering).emitters
    val references = Seq(ReferencesEmitter(module.references, ordering))

    val usage: Option[ValueEmitter] =
      module.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("usage", f))

    // TODO invoke traits end resource types

    YDocument {
      _.map { b =>
        b.comment(RamlHeader.Raml10Library.text)
        traverse(ordering.sorted(declares ++ usage ++ references), b)
      }
    }
  }
}

class RamlFragmentEmitter(fragment: Fragment) extends RamlDocumentEmitter(fragment) {
  def emitFragment(): YDocument = {

    val ordering: SpecOrdering = SpecOrdering.ordering(Raml, fragment.annotations)

    val typeEmitter: RamlFragmentTypeEmitter = fragment match {
      case di: DocumentationItem => DocumentationItemFragmentEmitter(di, ordering)
      case dt: DataType          => DataTypeFragmentEmitter(dt, ordering)
      //      case _: NamedExample              => Raml10NamedExample
      case rt: ResourceTypeFragment      => ResourceTypeFragmentEmitter(rt, ordering)
      case tf: TraitFragment             => TraitFragmentEmitter(tf, ordering)
      case at: AnnotationTypeDeclaration => AnnotationFragmentEmitter(at, ordering)
      case ef: ExtensionFragment         => ExtensionFragmentEmitter(ef, ordering)
      case of: OverlayFragment           => OverlayFragmentEmitter(of, ordering)
      case _                             => throw new UnsupportedOperationException("Unsupported fragment type")
    }

    val usage = fragment.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("usage", f))

    val references = Seq(ReferencesEmitter(fragment.references, ordering))

    YDocument {
      _.map { b =>
        b.comment(typeEmitter.header.text)
        traverse(ordering.sorted(typeEmitter.emitters ++ usage ++ references), b)
      }
    }
  }

  trait RamlFragmentTypeEmitter {
    val header: RamlHeader

    val emitters: Seq[EntryEmitter]
  }

  case class DocumentationItemFragmentEmitter(documentationItem: DocumentationItem, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10DocumentationItem

    val emitters: Seq[EntryEmitter] =
      RamlCreativeWorkItemsEmitter(documentationItem.encodes, ordering, withExtention = true).emitters()
  }

  case class DataTypeFragmentEmitter(dataType: DataType, ordering: SpecOrdering) extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10DataType

    val emitters: Seq[EntryEmitter] = RamlTypeEmitter(dataType.encodes, ordering).emitters()
  }

  case class AnnotationFragmentEmitter(annotation: AnnotationTypeDeclaration, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10AnnotationTypeDeclaration

    val emitters: Seq[EntryEmitter] = AnnotationTypeEmitter(annotation.encodes, ordering).emitters()
  }

  case class ExtensionFragmentEmitter(extension: ExtensionFragment, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10Extension

    val emitters: Seq[EntryEmitter] = {
      val result: ListBuffer[EntryEmitter] = ListBuffer()
      extension.fields
        .entry(ExtensionModel.Extends)
        .foreach(f => result += MapEntryEmitter("extends", f.scalar.toString, position = pos(f.value.annotations)))
      result ++= WebApiEmitter(extension.encodes, ordering, Some(Raml)).emitters // RamlDocumentEmitter(extension).emitWebApi(ordering)
      result
    }
  }

  case class OverlayFragmentEmitter(extension: OverlayFragment, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10Overlay

    val emitters: Seq[Emitter] = {
      val result: ListBuffer[Emitter] = ListBuffer()
      extension.fields
        .entry(OverlayModel.Extends)
        .foreach(f => result += MapEntryEmitter("extends", f.scalar.toString, position = pos(f.value.annotations)))
      result ++= WebApiEmitter(extension.encodes, ordering, Some(Raml)).emitters
      result
    }
  }

  case class ResourceTypeFragmentEmitter(fragment: ResourceTypeFragment, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10ResourceType

    val emitters: Seq[EntryEmitter] =
      DataNodeEmitter(fragment.encodes.asInstanceOf[AbstractDeclaration].dataNode, ordering)
        .emitters() collect {
        case e: EntryEmitter => e
        case other           => throw new Exception(s"Fragment not encoding DataObjectNode but $other")
      }
  }

  case class TraitFragmentEmitter(fragment: TraitFragment, ordering: SpecOrdering) extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10Trait

    val emitters: Seq[EntryEmitter] =
      DataNodeEmitter(fragment.encodes.asInstanceOf[AbstractDeclaration].dataNode, ordering)
        .emitters() collect {
        case e: EntryEmitter => e
        case other           => throw new Exception(s"Fragment not encoding DataObjectNode but $other")
      }
  }
}
