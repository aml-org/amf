package amf.plugins.document.webapi.model

import amf.domain.extensions.CustomDomainProperty
import amf.domain.{Example, ExternalDomainElement, Fields}
import amf.framework.metamodel.Obj
import amf.framework.model.document.Fragment
import amf.framework.parser.Annotations
import amf.plugins.document.webapi.metamodel.FragmentsTypesModels._
import amf.plugins.domain.shapes.models.Shape
import amf.plugins.domain.webapi.models.CreativeWork

// todo review

case class DocumentationItemFragment(fields: Fields, annotations: Annotations) extends Fragment {
  override def encodes: CreativeWork = super.encodes.asInstanceOf[CreativeWork]

  /** Meta data for the document */
  override def meta: Obj = DocumentationItemFragmentModel
}

case class DataTypeFragment(fields: Fields, annotations: Annotations) extends Fragment {
  override def encodes: Shape = super.encodes.asInstanceOf[Shape]

  /** Meta data for the document */
  override def meta: Obj = DataTypeFragmentModel
}

case class NamedExampleFragment(fields: Fields, annotations: Annotations) extends Fragment {
  override def encodes: Example = super.encodes.asInstanceOf[Example]

  /** Meta data for the document */
  override def meta: Obj = NamedExampleFragmentModel
}

case class ResourceTypeFragment(fields: Fields, annotations: Annotations) extends Fragment {
  /** Meta data for the document */
  override def meta: Obj = ResourceTypeFragmentModel
}

case class TraitFragment(fields: Fields, annotations: Annotations) extends Fragment {
  /** Meta data for the document */
  override def meta: Obj = TraitFragmentModel
}

case class AnnotationTypeDeclarationFragment(fields: Fields, annotations: Annotations) extends Fragment {
  override def encodes: CustomDomainProperty = super.encodes.asInstanceOf[CustomDomainProperty]

  /** Meta data for the document */
  override def meta: Obj = AnnotationTypeDeclarationFragmentModel
}

case class SecuritySchemeFragment(fields: Fields, annotations: Annotations) extends Fragment {
  override def encodes: amf.domain.security.SecurityScheme =
    super.encodes.asInstanceOf[amf.domain.security.SecurityScheme]

  /** Meta data for the document */
  override def meta: Obj = SecuritySchemeFragmentModel
}

case class ExternalFragment(fields: Fields, annotations: Annotations) extends Fragment {
  override def encodes: ExternalDomainElement = super.encodes.asInstanceOf[ExternalDomainElement]

  /** Meta data for the document */
  override def meta: Obj = ExternalFragmentModel
}

case class DialectFragment(fields: Fields, annotations: Annotations) extends Fragment {
  /** Meta data for the document */
  override def meta: Obj = DialectNodeFragmentModel
}

object DocumentationItemFragment {
  def apply(): DocumentationItemFragment = apply(Annotations())

  def apply(annotations: Annotations): DocumentationItemFragment = apply(Fields(), annotations)
}

object DataTypeFragment {
  def apply(): DataTypeFragment = apply(Annotations())

  def apply(annotations: Annotations): DataTypeFragment = apply(Fields(), annotations)
}

object NamedExampleFragment {
  def apply(): NamedExampleFragment = apply(Annotations())

  def apply(annotations: Annotations): NamedExampleFragment = apply(Fields(), annotations)
}

object ResourceTypeFragment {
  def apply(): ResourceTypeFragment = apply(Annotations())

  def apply(annotations: Annotations): ResourceTypeFragment = apply(Fields(), annotations)
}

object TraitFragment {
  def apply(): TraitFragment = apply(Annotations())

  def apply(annotations: Annotations): TraitFragment = apply(Fields(), annotations)
}

object AnnotationTypeDeclarationFragment {
  def apply(): AnnotationTypeDeclarationFragment = apply(Annotations())

  def apply(annotations: Annotations): AnnotationTypeDeclarationFragment = apply(Fields(), annotations)
}

object DialectFragment {
  def apply(): DialectFragment = apply(Annotations())

  def apply(annotations: Annotations): DialectFragment = apply(Fields(), annotations)
}

object ExternalFragment {
  def apply(): ExternalFragment                         = apply(Annotations())
  def apply(annotations: Annotations): ExternalFragment = ExternalFragment(Fields(), annotations)
}

object SecuritySchemeFragment {
  def apply(): SecuritySchemeFragment = apply(Annotations())

  def apply(annotations: Annotations): SecuritySchemeFragment = apply(Fields(), annotations)
}