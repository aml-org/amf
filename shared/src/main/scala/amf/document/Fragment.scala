package amf.document

import amf.domain.extensions.CustomDomainProperty
import amf.domain.{Annotations, DomainElement, Fields, UserDocumentation}
import amf.metadata.document.{BaseUnitModel, FragmentModel, ModuleModel}
import amf.shape.Shape

/**
  * RAML Fragments
  */
object Fragment {

  /** Units encoding domain fragments */
  sealed trait Fragment extends BaseUnit with EncodesModel {

    /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
    override val references: Seq[BaseUnit] = Nil

    override val fields: Fields = new Fields()

    /** Set of annotations for object. */
    override val annotations: Annotations = Annotations()

    override def adopted(parent: String): this.type = withId(parent)

    override def usage: String = ""

    override def encodes: DomainElement = fields(FragmentModel.Encodes)

    override def location: String = fields(BaseUnitModel.Location)

    def withLocation(location: String): this.type            = set(BaseUnitModel.Location, location)
    def withReferences(references: Seq[BaseUnit]): this.type = setArrayWithoutId(BaseUnitModel.References, references)
    def withEncodes(encoded: DomainElement): this.type       = set(FragmentModel.Encodes, encoded)
    def withUsage(usage: String): this.type                  = set(BaseUnitModel.Usage, usage)
  }

  // todo review

  case class DocumentationItem() extends Fragment {
    override def encodes: UserDocumentation = super.encodes.asInstanceOf[UserDocumentation]
  }

  case class DataType() extends Fragment {
    override def encodes: Shape = super.encodes.asInstanceOf[Shape]
  }

  case class NamedExample() extends Fragment

  case class ResourceType() extends Fragment

  case class Trait() extends Fragment

  case class AnnotationTypeDeclaration() extends Fragment {
    override def encodes: CustomDomainProperty = super.encodes.asInstanceOf[CustomDomainProperty]
  }

  case class Overlay() extends Fragment

  case class Extension() extends Fragment

  case class SecurityScheme() extends Fragment

  case class Default() extends Fragment
}

trait EncodesModel {

  /** Encoded [[amf.domain.DomainElement]] described in the document element. */
  def encodes: DomainElement
}
