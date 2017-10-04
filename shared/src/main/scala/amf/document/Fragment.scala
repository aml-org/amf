package amf.document

import amf.domain.extensions.CustomDomainProperty
import amf.domain.{Annotations, DomainElement, Fields, UserDocumentation}
import amf.metadata.document.{BaseUnitModel, DocumentModel, FragmentModel, ModuleModel}
import amf.model.AmfObject
import amf.shape.Shape
import org.yaml.model.YDocument

/**
  * RAML Fragments
  */
object Fragment {

  /** Units encoding domain fragments */
  trait Fragment extends BaseUnit with EncodesModel {

    /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
    override val references: Seq[BaseUnit] = fields(DocumentModel.References)

//    override val fields: Fields = new Fields()

    /** Set of annotations for object. */
//    override val annotations: Annotations = Annotations()

    override def adopted(parent: String): this.type = withId(parent)

    override def usage: String = ""

    override def encodes: DomainElement = fields(FragmentModel.Encodes)

    override def location: String = fields(BaseUnitModel.Location)
  }

  // todo review

  case class DocumentationItem(fields: Fields, annotations: Annotations) extends Fragment {
    override def encodes: UserDocumentation = super.encodes.asInstanceOf[UserDocumentation]
  }

  case class DataType(fields: Fields, annotations: Annotations) extends Fragment {
    override def encodes: Shape = super.encodes.asInstanceOf[Shape]
  }

  case class NamedExample(fields: Fields, annotations: Annotations) extends Fragment

  case class ResourceTypeFragment(fields: Fields, annotations: Annotations) extends Fragment

  case class TraitFragment(fields: Fields, annotations: Annotations) extends Fragment

  case class AnnotationTypeDeclaration(fields: Fields, annotations: Annotations) extends Fragment {
    override def encodes: CustomDomainProperty = super.encodes.asInstanceOf[CustomDomainProperty]
  }

  case class Overlay(fields: Fields, annotations: Annotations) extends Fragment

  case class Extension(fields: Fields, annotations: Annotations) extends Fragment

  case class SecurityScheme(fields: Fields, annotations: Annotations) extends Fragment

  case class Default(fields: Fields, annotations: Annotations) extends Fragment

  object DocumentationItem {
    def apply(): DocumentationItem = apply(Annotations())

    def apply(annotations: Annotations): DocumentationItem = apply(Fields(), annotations)
  }

  object DataType {
    def apply(): DataType = apply(Annotations())

    def apply(annotations: Annotations): DataType = apply(Fields(), annotations)
  }

  object NamedExample {
    def apply(): NamedExample = apply(Annotations())

    def apply(annotations: Annotations): NamedExample = apply(Fields(), annotations)
  }

  object ResourceTypeFragment {
    def apply(): ResourceTypeFragment = apply(Annotations())

    def apply(annotations: Annotations): ResourceTypeFragment = apply(Fields(), annotations)
  }

  object TraitFragment {
    def apply(): TraitFragment = apply(Annotations())

    def apply(annotations: Annotations): TraitFragment = apply(Fields(), annotations)
  }

  object AnnotationTypeDeclaration {
    def apply(): AnnotationTypeDeclaration = apply(Annotations())

    def apply(annotations: Annotations): AnnotationTypeDeclaration = apply(Fields(), annotations)
  }

}

trait EncodesModel extends AmfObject {

  /** Encoded [[amf.domain.DomainElement]] described in the document element. */
  def encodes: DomainElement

  def withEncodes(encoded: DomainElement): this.type = set(FragmentModel.Encodes, encoded)
}
