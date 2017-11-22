package amf.spec.raml

import amf.document.Fragment._
import amf.document.{BaseUnit, Module}
import amf.domain.`abstract`.AbstractDeclaration
import amf.metadata.document.BaseUnitModel
import amf.plugins.document.webapi.parser.{RamlFragmentHeader, RamlHeader}
import amf.remote.Raml
import amf.spec.common.BaseEmitters._
import amf.spec.declaration._
import amf.spec.domain.NamedExampleEmitter
import amf.spec.{EntryEmitter, SpecOrdering}
import org.yaml.model.YDocument

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

    YDocument(b => {
      b.comment(RamlHeader.Raml10Library.text)
      b.obj(traverse(ordering.sorted(declares ++ usage ++ references), _))
    })
  }
}

class RamlFragmentEmitter(fragment: Fragment) extends RamlDocumentEmitter(fragment) {
  def emitFragment(): YDocument = {

    val ordering: SpecOrdering = SpecOrdering.ordering(Raml, fragment.annotations)

    val typeEmitter: RamlFragmentTypeEmitter = fragment match {
      case di: DocumentationItem         => DocumentationItemFragmentEmitter(di, ordering)
      case dt: DataType                  => DataTypeFragmentEmitter(dt, ordering)
      case ne: NamedExample              => FragmentNamedExampleEmitter(ne, ordering)
      case rt: ResourceTypeFragment      => ResourceTypeFragmentEmitter(rt, ordering)
      case tf: TraitFragment             => TraitFragmentEmitter(tf, ordering)
      case at: AnnotationTypeDeclaration => AnnotationFragmentEmitter(at, ordering)
      case sc: SecurityScheme            => SecuritySchemeFragmentEmitter(sc, ordering)
      case _                             => throw new UnsupportedOperationException("Unsupported fragment type")
    }

    val usage = fragment.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("usage", f))

    val references = Seq(ReferencesEmitter(fragment.references, ordering))

    YDocument(b => {
      b.comment(typeEmitter.header.text)
      b.obj(traverse(ordering.sorted(typeEmitter.emitters(fragment.references) ++ usage ++ references), _))
    })
  }

  trait RamlFragmentTypeEmitter {
    val header: RamlHeader

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter]
  }

  case class DocumentationItemFragmentEmitter(documentationItem: DocumentationItem, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10DocumentationItem

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] =
      RamlCreativeWorkItemsEmitter(documentationItem.encodes, ordering, withExtention = true).emitters()
  }

  case class DataTypeFragmentEmitter(dataType: DataType, ordering: SpecOrdering) extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10DataType

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] =
      RamlTypeEmitter(dataType.encodes, ordering, references = Nil).entries()
  }

  case class AnnotationFragmentEmitter(annotation: AnnotationTypeDeclaration, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10AnnotationTypeDeclaration

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] =
      AnnotationTypeEmitter(annotation.encodes, ordering).emitters() match {
        case Left(emitters) => emitters
        case Right(part)    => Seq(EntryPartEmitter("type", part))
      }
  }

  case class SecuritySchemeFragmentEmitter(securityScheme: SecurityScheme, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10SecurityScheme

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] =
      RamlSecuritySchemeEmitter(securityScheme.encodes, references, ordering).emitters()
  }

  case class ResourceTypeFragmentEmitter(fragment: ResourceTypeFragment, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10ResourceType

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] =
      DataNodeEmitter(fragment.encodes.asInstanceOf[AbstractDeclaration].dataNode, ordering)
        .emitters() collect {
        case e: EntryEmitter => e
        case other           => throw new Exception(s"Fragment not encoding DataObjectNode but $other")
      }
  }

  case class TraitFragmentEmitter(fragment: TraitFragment, ordering: SpecOrdering) extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10Trait

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] =
      DataNodeEmitter(fragment.encodes.asInstanceOf[AbstractDeclaration].dataNode, ordering)
        .emitters() collect {
        case e: EntryEmitter => e
        case other           => throw new Exception(s"Fragment not encoding DataObjectNode but $other")
      }
  }

  case class FragmentNamedExampleEmitter(example: NamedExample, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10NamedExample

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] = Seq(NamedExampleEmitter(example.encodes, ordering))

  }
}
