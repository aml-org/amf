package amf.plugins.document.webapi.parser.spec.raml

import amf.core.metamodel.document.BaseUnitModel
import amf.core.model.document.{BaseUnit, Module, _}
import amf.core.model.domain.templates.AbstractDeclaration
import amf.core.remote.Raml
import amf.plugins.document.webapi.model._
import amf.plugins.document.webapi.parser.spec.{EntryEmitter, SpecOrdering}
import amf.plugins.document.webapi.parser.spec.common.BaseEmitters.ValueEmitter
import amf.plugins.document.webapi.parser.{RamlFragmentHeader, RamlHeader}
import amf.plugins.document.webapi.parser.spec.common.BaseEmitters._
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain.NamedExampleEmitter
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
      case di: DocumentationItemFragment         => DocumentationItemFragmentEmitter(di, ordering)
      case dt: DataTypeFragment                  => DataTypeFragmentEmitter(dt, ordering)
      case ne: NamedExampleFragment              => FragmentNamedExampleEmitter(ne, ordering)
      case rt: ResourceTypeFragment              => ResourceTypeFragmentEmitter(rt, ordering)
      case tf: TraitFragment                     => TraitFragmentEmitter(tf, ordering)
      case at: AnnotationTypeDeclarationFragment => AnnotationFragmentEmitter(at, ordering)
      case sc: SecuritySchemeFragment            => SecuritySchemeFragmentEmitter(sc, ordering)
      case _                                     => throw new UnsupportedOperationException("Unsupported fragment type")
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

  case class DocumentationItemFragmentEmitter(documentationItem: DocumentationItemFragment, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10DocumentationItem

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] =
      RamlCreativeWorkItemsEmitter(documentationItem.encodes, ordering, withExtention = true).emitters()
  }

  case class DataTypeFragmentEmitter(dataType: DataTypeFragment, ordering: SpecOrdering) extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10DataType

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] =
      RamlTypeEmitter(dataType.encodes, ordering, references = Nil).entries()
  }

  case class AnnotationFragmentEmitter(annotation: AnnotationTypeDeclarationFragment, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10AnnotationTypeDeclaration

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] =
      AnnotationTypeEmitter(annotation.encodes, ordering).emitters() match {
        case Left(emitters) => emitters
        case Right(part)    => Seq(EntryPartEmitter("type", part))
      }
  }

  case class SecuritySchemeFragmentEmitter(securityScheme: SecuritySchemeFragment, ordering: SpecOrdering)
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

  case class FragmentNamedExampleEmitter(example: NamedExampleFragment, ordering: SpecOrdering)
      extends RamlFragmentTypeEmitter {

    override val header: RamlHeader = RamlFragmentHeader.Raml10NamedExample

    def emitters(references: Seq[BaseUnit]): Seq[EntryEmitter] = Seq(NamedExampleEmitter(example.encodes, ordering))

  }
}
