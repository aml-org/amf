package amf.apicontract.client.scala.model.document

import amf.core.client.scala.model.document.Fragment
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.document.FragmentsTypesModels._
import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.shapes.client.scala.model.domain.Example
import amf.shapes.client.scala.model.domain.{CreativeWork, Example}

case class DocumentationItemFragment(fields: Fields, annotations: Annotations) extends Fragment {
  override def encodes: CreativeWork = super.encodes.asInstanceOf[CreativeWork]

  /** Meta data for the document */
  override def meta: DocumentationItemFragmentModel.type = DocumentationItemFragmentModel
}

case class DataTypeFragment(fields: Fields, annotations: Annotations) extends Fragment {
  override def encodes: Shape = super.encodes.asInstanceOf[Shape]

  /** Meta data for the document */
  override def meta: DataTypeFragmentModel.type = DataTypeFragmentModel
}

case class NamedExampleFragment(fields: Fields, annotations: Annotations) extends Fragment {
  override def encodes: Example = super.encodes.asInstanceOf[Example]

  /** Meta data for the document */
  override def meta: NamedExampleFragmentModel.type = NamedExampleFragmentModel
}

case class ResourceTypeFragment(fields: Fields, annotations: Annotations) extends Fragment {

  /** Meta data for the document */
  override def meta: ResourceTypeFragmentModel.type = ResourceTypeFragmentModel
}

case class TraitFragment(fields: Fields, annotations: Annotations) extends Fragment {

  /** Meta data for the document */
  override def meta: TraitFragmentModel.type = TraitFragmentModel
}

case class AnnotationTypeDeclarationFragment(fields: Fields, annotations: Annotations) extends Fragment {
  override def encodes: CustomDomainProperty = super.encodes.asInstanceOf[CustomDomainProperty]

  /** Meta data for the document */
  override def meta: AnnotationTypeDeclarationFragmentModel.type = AnnotationTypeDeclarationFragmentModel
}

case class SecuritySchemeFragment(fields: Fields, annotations: Annotations) extends Fragment {
  override def encodes: SecurityScheme =
    super.encodes.asInstanceOf[SecurityScheme]

  /** Meta data for the document */
  override def meta: SecuritySchemeFragmentModel.type = SecuritySchemeFragmentModel
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

object SecuritySchemeFragment {
  def apply(): SecuritySchemeFragment = apply(Annotations())

  def apply(annotations: Annotations): SecuritySchemeFragment = apply(Fields(), annotations)
}
