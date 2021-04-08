package amf.plugins.domain.webapi.models.security

import amf.core.metamodel.{Field, Obj}
import amf.core.model.domain.NamedDomainElement
import amf.core.parser.{Annotations, Fields}
import amf.core.utils.AmfStrings
import amf.plugins.domain.webapi.metamodel.security.SecurityRequirementModel
import amf.plugins.domain.webapi.metamodel.security.SecurityRequirementModel._
import org.yaml.model.YPart

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
