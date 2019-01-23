package amf

import amf.core.remote._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("ProfileNames")
object ProfileNames {
  val AMF: ProfileName    = AmfProfile
  val OAS: ProfileName    = OasProfile
  val OAS20: ProfileName  = Oas20Profile
  val OAS30: ProfileName  = Oas30Profile
  val RAML: ProfileName   = RamlProfile
  val RAML10: ProfileName = Raml10Profile
  val RAML08: ProfileName = Raml08Profile
  val AML: ProfileName    = RamlProfile

  lazy val specProfiles: Seq[ProfileName] =
    Seq(AmfProfile, OasProfile, Oas20Profile, Oas30Profile, RamlProfile, Raml08Profile, Raml10Profile)
}

case class ProfileName(private[amf] val p: String, private val m: MessageStyle = AMFStyle) {
  @JSExportTopLevel("ProfileName")
  def this(profile: String) = this(profile, AMFStyle)
  def profile: String            = p
  def messageStyle: MessageStyle = m
  override def toString: String  = p
}

object AmfProfile     extends ProfileName(Amf.name)
object AmlProfile     extends ProfileName(Aml.name)
object OasProfile     extends ProfileName(Oas.name, OASStyle)
object Oas20Profile   extends ProfileName(Oas20.name, OASStyle)
object Oas30Profile   extends ProfileName(Oas30.name, OASStyle)
object RamlProfile    extends ProfileName(Raml.name, RAMLStyle)
object Raml08Profile  extends ProfileName(Raml08.name, RAMLStyle)
object Raml10Profile  extends ProfileName(Raml10.name, RAMLStyle)
object PayloadProfile extends ProfileName(Payload.name)

object ProfileName {
  def unapply(name: String): Option[ProfileName] =
    name match {
      case AmfProfile.p    => Some(AmfProfile)
      case OasProfile.p    => Some(OasProfile)
      case Oas30Profile.p  => Some(Oas30Profile)
      case RamlProfile.p   => Some(RamlProfile)
      case Raml08Profile.p => Some(Raml08Profile)
      case _               => None
    }

  def apply(profile: String): ProfileName = profile match {
    case Amf.name    => AmfProfile
    case Oas.name    => OasProfile
    case Oas20.name  => Oas20Profile
    case Oas30.name  => Oas30Profile
    case Raml.name   => RamlProfile
    case Raml08.name => Raml08Profile
    case Raml10.name => Raml10Profile
    case custom      => new ProfileName(custom)
  }
}

object MessageStyle {
  def apply(name: String): MessageStyle = name match {
    case Raml.name | Raml10.name | Raml08.name => RAMLStyle
    case Oas.name | Oas20.name | Oas30.name    => OASStyle
    case _                                     => AMFStyle
  }
}

trait MessageStyle {
  def profileName: ProfileName
}

@JSExportAll
@JSExportTopLevel("MessageStyles")
object MessageStyles {
  val RAML: MessageStyle = RAMLStyle
  val OAS: MessageStyle  = OASStyle
  val AMF: MessageStyle  = AMFStyle
}

object RAMLStyle extends MessageStyle {
  override def profileName: ProfileName = RamlProfile
}
object OASStyle extends MessageStyle {
  override def profileName: ProfileName = OasProfile
}
object AMFStyle extends MessageStyle {
  override def profileName: ProfileName = AmfProfile
}
