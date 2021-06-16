package amf.apicontract.client.scala.model.domain.security

import amf.core.client.scala.model.domain.NamedDomainElement
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.security.SecurityRequirementModel
import amf.apicontract.internal.metamodel.domain.security.SecurityRequirementModel._
import org.yaml.model.YPart
import amf.core.internal.utils.AmfStrings

case class SecurityRequirement(fields: Fields, annotations: Annotations) extends NamedDomainElement {

  override def nameField: Field                = Name
  def schemes: Seq[ParametrizedSecurityScheme] = fields.field(Schemes)

  def withSchemes(schemes: Seq[ParametrizedSecurityScheme]): this.type = setArray(Schemes, schemes)

  def withScheme(): ParametrizedSecurityScheme = {
    val scheme = ParametrizedSecurityScheme()
    add(Schemes, scheme)
    scheme
  }

  def withScheme(name: String): ParametrizedSecurityScheme = {
    val scheme = ParametrizedSecurityScheme().withName(name)
    add(Schemes, scheme)
    scheme
  }

  override def meta: SecurityRequirementModel.type = SecurityRequirementModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/" + name.option().getOrElse("default-requirement").urlComponentEncoded
}

object SecurityRequirement {

  def apply(): SecurityRequirement = apply(Annotations())

  def apply(part: YPart): SecurityRequirement = apply(Annotations(part))

  def apply(annotations: Annotations): SecurityRequirement =
    new SecurityRequirement(Fields(), annotations)
}
