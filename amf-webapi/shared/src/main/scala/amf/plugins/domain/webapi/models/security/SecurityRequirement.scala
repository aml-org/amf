package amf.plugins.domain.webapi.models.security

import amf.core.metamodel.Obj
import amf.core.model.StrField
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.security.SecurityRequirementModel
import amf.plugins.domain.webapi.metamodel.security.SecurityRequirementModel._
import org.yaml.model.YPart
import amf.core.utils.AmfStrings

case class SecurityRequirement(fields: Fields, annotations: Annotations) extends DomainElement {

  def name: StrField                           = fields.field(Name)
  def schemes: Seq[ParametrizedSecurityScheme] = fields.field(Schemes)

  def withName(name: String): this.type                                = set(Name, name)
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

  override def meta: Obj = SecurityRequirementModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/" + name.option().getOrElse("default-requirement").urlComponentEncoded
}

object SecurityRequirement {

  def apply(): SecurityRequirement = apply(Annotations())

  def apply(part: YPart): SecurityRequirement = apply(Annotations(part))

  def apply(annotations: Annotations): SecurityRequirement =
    new SecurityRequirement(Fields(), annotations)
}
