package amf.spec.raml

import amf.compiler.{RamlFragmentHeader, RamlHeader}
import amf.document.Fragment.{AnnotationTypeDeclaration, DataType, DocumentationItem, Fragment}
import amf.document.Module
import amf.metadata.document.BaseUnitModel
import amf.remote.Raml
import amf.spec.{Emitter, SpecOrdering}
import org.yaml.model.YDocument

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

case class RamlFragmentEmitter(fragment: Fragment) extends RamlSpecEmitter {
  def emitFragment(): YDocument = {

    val ordering: SpecOrdering = SpecOrdering.ordering(Raml, fragment.annotations)

    val typeEmitter: RamlFragmentTypeEmitter = fragment match {
      case di: DocumentationItem => DocumentationItemFragmentEmitter(di, ordering)
      case dt: DataType          => DataTypeFragmentEmitter(dt, ordering)
      //      case _: NamedExample              => Raml10NamedExample
      //      case _: ResourceType              => Raml10ResourceType
      //      case _: Trait                     => Raml10Trait
      case at: AnnotationTypeDeclaration => AnnotationFragmentEmitter(at, ordering)
      case _                             => throw new UnsupportedOperationException("Unsupported fragment type")
    }
    val referenceEmitter = Seq(ReferencesEmitter(fragment.references, ordering))

    emitter.document({ () =>
      map { () =>
        comment(typeEmitter.header.text)
        traverse(typeEmitter.elementsEmitters ++ referenceEmitter)
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
}
