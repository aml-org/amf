package amf

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("ProfileNames")
object ProfileNames {
  val AMF    = AMFProfile
  val OAS    = OASProfile
  val OAS3   = OAS3Profile
  val RAML   = RAMLProfile
  val RAML08 = RAML08Profile
}

case class ProfileName(profile: String, messageStyle: MessageStyle = AMFStyle) {
  @JSExportTopLevel("ProfileName")
  def this(profile: String) = this(profile, AMFStyle)
  override def toString: String = profile
}

object AMFProfile     extends ProfileName("AMF")
object OASProfile     extends ProfileName("OpenAPI", OASStyle)
object OAS3Profile    extends ProfileName("OpenAPI3", OASStyle)
object RAMLProfile    extends ProfileName("RAML", RAMLStyle)
object RAML08Profile  extends ProfileName("RAML08", RAMLStyle)
object PAYLOADProfile extends ProfileName("Payload")

object ProfileName {
  def unapply(name: String): Option[ProfileName] =
    name match {
      case AMFProfile.profile    => Some(AMFProfile)
      case OASProfile.profile    => Some(OASProfile)
      case OAS3Profile.profile   => Some(OAS3Profile)
      case RAMLProfile.profile   => Some(RAMLProfile)
      case RAML08Profile.profile => Some(RAML08Profile)
      case _                     => None
    }

  def apply(profile: String): ProfileName = profile match {
    case "AMF"    => AMFProfile
    case "OAS"    => OASProfile
    case "OAS3"   => OAS3Profile
    case "RAML"   => RAMLProfile
    case "RAML08" => RAML08Profile
    case other    => new ProfileName(other)
  }
}

object MessageStyle {
  def apply(name: String): MessageStyle = name match {
    case "RAML" | "RAML08" => RAMLStyle
    case "OAS" | "OAS3"    => OASStyle
    case _                 => AMFStyle
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
  override def profileName: ProfileName = RAMLProfile
}
object OASStyle extends MessageStyle {
  override def profileName: ProfileName = OASProfile
}
object AMFStyle extends MessageStyle {
  override def profileName: ProfileName = AMFProfile
}
