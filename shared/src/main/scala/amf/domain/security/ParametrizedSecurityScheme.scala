package amf.domain.security

import amf.domain.{Annotations, DomainElement, Fields}
import amf.metadata.domain.security.ParametrizedSecuritySchemeModel.{Settings => SettingsField, _}
import org.yaml.model.YPart

case class ParametrizedSecurityScheme(fields: Fields, annotations: Annotations) extends DomainElement {
  def name: String        = fields(Name)
  def scheme: String      = fields(Scheme)
  def scopes: Seq[String] = fields(Scopes)
  def settings: String    = fields(SettingsField)

  def withName(name: String): this.type           = set(Name, name)
  def withScheme(scheme: String): this.type       = set(Scheme, scheme)
  def withScopes(scopes: Seq[String]): this.type  = set(Scopes, scopes)
  def withSettings(settings: Settings): this.type = set(SettingsField, settings)

  override def adopted(parent: String): this.type = withId(parent + "/" + name)
}

object ParametrizedSecurityScheme {

  def apply(): ParametrizedSecurityScheme = apply(Annotations())

  def apply(part: YPart): ParametrizedSecurityScheme = apply(Annotations(part))

  def apply(annotations: Annotations): ParametrizedSecurityScheme =
    new ParametrizedSecurityScheme(Fields(), annotations)
}
