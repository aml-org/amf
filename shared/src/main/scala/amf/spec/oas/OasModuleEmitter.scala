package amf.spec.oas

import amf.compiler.{OasFragmentHeader, OasHeader}
import amf.document.Fragment._
import amf.document.Module
import amf.domain.`abstract`.AbstractDeclaration
import amf.metadata.document.BaseUnitModel
import amf.metadata.document.FragmentsTypesModels.{ExtensionModel, OverlayModel}
import amf.parser.Position
import amf.remote.Oas
import amf.spec.common.BaseEmitters._
import amf.spec.declaration._
import amf.spec.{EntryEmitter, SpecOrdering}
import org.yaml.model.YDocument
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable.ListBuffer

/**
  *
  */
case class OasModuleEmitter(module: Module) extends OasSpecEmitter {

  def emitModule(): YDocument = {

    val ordering = SpecOrdering.ordering(Oas, module.annotations)

    val declares   = DeclarationsEmitter(module.declares, ordering).emitters
    val references = Seq(ReferencesEmitter(module.references, ordering))
    val usages     = module.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("x-usage", f))

    YDocument {
      _.map { b =>
        b.entry("swagger", "2.0")
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
      case ef: ExtensionFragment         => ExtensionFragmentEmitter(ef, ordering)
      case of: OverlayFragment           => OverlayFragmentEmitter(of, ordering)
      case sc: SecurityScheme            => SecuritySchemeFragmentEmitter(sc, ordering)
      //      case _: NamedExample              => Raml10NamedExample
      case _ => throw new UnsupportedOperationException("Unsupported fragment type")
    }
    val references = ReferencesEmitter(fragment.references, ordering)
    val usage: Option[ValueEmitter] =
      fragment.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("x-usage", f))

    YDocument {
      _.map { b =>
        traverse(Seq(OasHeaderEmitter(OasHeader.Oas20), typeEmitter.header)
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

    override val header = OasHeaderEmitter(OasFragmentHeader.Oas20DocumentationItem)

    val emitters: Seq[EntryEmitter] = OasCreativeWorkItemsEmitter(documentationItem.encodes, ordering).emitters()
  }

  case class DataTypeFragmentEmitter(dataType: DataType, ordering: SpecOrdering) extends OasFragmentTypeEmitter {

    override val header = OasHeaderEmitter(OasFragmentHeader.Oas20DataType)

    val emitters: Seq[EntryEmitter] = OasTypeEmitter(dataType.encodes, ordering).entries()
  }

  case class AnnotationFragmentEmitter(annotation: AnnotationTypeDeclaration, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val header = OasHeaderEmitter(OasFragmentHeader.Oas20AnnotationTypeDeclaration)

    val emitters: Seq[EntryEmitter] =
      AnnotationTypeEmitter(annotation.encodes, ordering).emitters()
  }

  case class ResourceTypeFragmentEmitter(resourceTypeFragment: ResourceTypeFragment, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val header = OasHeaderEmitter(OasFragmentHeader.Oas20ResourceType)

    val emitters: Seq[EntryEmitter] =
      DataNodeEmitter(resourceTypeFragment.encodes.asInstanceOf[AbstractDeclaration].dataNode, ordering)
        .emitters() collect {
        case e: EntryEmitter => e
        case other           => throw new Exception(s"Fragment not encoding DataObjectNode but $other")
      }
  }

  case class TraitFragmentEmitter(traitFragment: TraitFragment, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val header = OasHeaderEmitter(OasFragmentHeader.Oas20Trait)

    val emitters: Seq[EntryEmitter] =
      DataNodeEmitter(traitFragment.encodes.asInstanceOf[AbstractDeclaration].dataNode, ordering).emitters() collect {
        case e: EntryEmitter => e
        case other           => throw new Exception(s"Fragment not encoding DataObjectNode but $other")
      }
  }

  case class ExtensionFragmentEmitter(extension: ExtensionFragment, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val header = OasHeaderEmitter(OasFragmentHeader.Oas20Extension)

    val emitters: Seq[EntryEmitter] = {
      val result: ListBuffer[EntryEmitter] = ListBuffer()
      extension.fields
        .entry(ExtensionModel.Extends)
        .foreach(f => result += NamedRefEmitter("x-extends", f.scalar.toString, pos = pos(f.value.annotations)))
      result ++= WebApiEmitter(extension.encodes, ordering, Some(Oas)).emitters
      result
    }
  }

  case class OverlayFragmentEmitter(extension: OverlayFragment, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val header = OasHeaderEmitter(OasFragmentHeader.Oas20Overlay)

    val emitters: Seq[EntryEmitter] = {
      val result: ListBuffer[EntryEmitter] = ListBuffer()
      extension.fields
        .entry(OverlayModel.Extends)
        .foreach(f => result += NamedRefEmitter("x-extends", f.scalar.toString, pos = pos(f.value.annotations)))
      result ++= WebApiEmitter(extension.encodes, ordering, Some(Oas)).emitters
      result
    }
  }

  case class SecuritySchemeFragmentEmitter(securityScheme: SecurityScheme, ordering: SpecOrdering)
      extends OasFragmentTypeEmitter {

    override val header = OasHeaderEmitter(OasFragmentHeader.Oas20SecurityScheme)

    val emitters: Seq[EntryEmitter] =
      OasSecuritySchemeEmitter(securityScheme.encodes,
                               OasSecuritySchemeTypeMapping.fromText(securityScheme.encodes.`type`),
                               ordering).emitters()
  }

  case class OasHeaderEmitter(oasHeader: OasHeader) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      MapEntryEmitter(oasHeader.key, oasHeader.value).emit(b)
    }

    override def position(): Position = Position.ZERO
  }

}
