package amf.spec.raml

import amf.compiler.{RamlFragmentHeader, RamlHeader}
import amf.document.Fragment._
import amf.document.Module
import amf.domain.`abstract`.AbstractDeclaration
import amf.metadata.document.BaseUnitModel
import amf.metadata.document.FragmentsTypesModels.{ExtensionModel, OverlayModel}
import amf.remote.Raml
import amf.spec.{Emitter, SpecOrdering}
import org.yaml.model.YDocument

import scala.collection.mutable.ListBuffer

/**
  *
  */
case class RamlModuleEmitter(module: Module) extends RamlSpecEmitter {

  def emitModule(): YDocument = {

    val ordering: SpecOrdering = SpecOrdering.ordering(Raml, module.annotations)

    // TODO ordering??
    val declares         = DeclarationsEmitter(module.declares, module.references, ordering).emitters
    val referenceEmitter = Seq(ReferencesEmitter(module.references, ordering))

    val usageEmitter: Option[ValueEmitter] =
      module.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("usage", f))

    // TODO invoke traits end resource types

    emitter.document({ () =>
      comment(RamlHeader.Raml10Library.text)
      map { () =>
        traverse(ordering.sorted(declares ++ usageEmitter ++ referenceEmitter))
      }
    })
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
      case sc: SecurityScheme            => SecuritySchemeFragmentEmitter(sc, ordering)
      case _                             => throw new UnsupportedOperationException("Unsupported fragment type")
    }

    val usageEmitter: Option[ValueEmitter] =
      fragment.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("usage", f))

    val referenceEmitter = Seq(ReferencesEmitter(fragment.references, ordering))

    emitter.document({ () =>
      map { () =>
        comment(typeEmitter.header.text)
        traverse(ordering.sorted(typeEmitter.elementsEmitters ++ usageEmitter ++ referenceEmitter))
      }
    })
  }

  trait RamlFragmentTypeEmitter {
    val header: RamlHeader

    val elementsEmitters: Seq[Emitter]
  }

  case class DocumentationItemFragmentEmitter(documentationItem: DocumentationItem, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10DocumentationItem

    val elementsEmitters: Seq[Emitter] = Seq(UserDocumentationEmitter(documentationItem.encodes, ordering))
  }

  case class DataTypeFragmentEmitter(dataType: DataType, ordering: SpecOrdering) extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10DataType

    val elementsEmitters: Seq[Emitter] = RamlTypeEmitter(dataType.encodes, ordering).emitters()
  }

  case class AnnotationFragmentEmitter(annotation: AnnotationTypeDeclaration, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10AnnotationTypeDeclaration

    val elementsEmitters: Seq[Emitter] =
      AnnotationTypeEmitter(annotation.encodes, ordering).emitters()
  }

  case class ExtensionFragmentEmitter(extension: ExtensionFragment, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10Extension

    val elementsEmitters: Seq[Emitter] = {
      val result: ListBuffer[Emitter] = ListBuffer()
      extension.fields
        .entry(ExtensionModel.Extends)
        .foreach(f => result += EntryEmitter("extends", f.scalar.toString, position = pos(f.value.annotations)))
      result ++= WebApiEmitter(extension.encodes, ordering, Some(Raml)).emitters // RamlDocumentEmitter(extension).emitWebApi(ordering)
      result
    }
  }

  case class OverlayFragmentEmitter(extension: OverlayFragment, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10Overlay

    val elementsEmitters: Seq[Emitter] = {
      val result: ListBuffer[Emitter] = ListBuffer()
      extension.fields
        .entry(OverlayModel.Extends)
        .foreach(f => result += EntryEmitter("extends", f.scalar.toString, position = pos(f.value.annotations)))
      result ++= WebApiEmitter(extension.encodes, ordering, Some(Raml)).emitters
      result
    }
  }

  case class SecuritySchemeFragmentEmitter(securityScheme: SecurityScheme, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10SecurityScheme

    val elementsEmitters: Seq[Emitter] = SecuritySchemeEmitter(securityScheme.encodes, ordering).emitters()
  }

  case class ResourceTypeFragmentEmitter(resourceTypeFragment: ResourceTypeFragment, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10ResourceType

    val elementsEmitters: Seq[Emitter] =
      DataNodeEmitter(resourceTypeFragment.encodes.asInstanceOf[AbstractDeclaration].dataNode, ordering)
        .emitters() // todo review with gute the map and sequence for oas
  }

  case class TraitFragmentEmitter(traitFragment: TraitFragment, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10Trait

    val elementsEmitters: Seq[Emitter] =
      DataNodeEmitter(traitFragment.encodes.asInstanceOf[AbstractDeclaration].dataNode, ordering)
        .emitters() // todo review with gute the map and sequence for oas
  }
}
