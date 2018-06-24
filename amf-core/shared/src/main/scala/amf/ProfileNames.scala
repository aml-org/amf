package amf

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("ProfileNames")
object ProfileNames {

  case class ProfileName(profile: String, messageStyle: MessageStyle = AMFStyle) {

    @JSExportTopLevel("ProfileName")
    def this(profile: String) = this(profile, AMFStyle)
    override def toString: String = profile
  }

  object AMF     extends ProfileName("AMF")
  object OAS     extends ProfileName("OpenAPI", OASStyle)
  object OAS3    extends ProfileName("OpenAPI3", OASStyle)
  object RAML    extends ProfileName("RAML", RAMLStyle)
  object RAML08  extends ProfileName("RAML08", RAMLStyle)
  object PAYLOAD extends ProfileName("Payload")

  object ProfileName {
    def unapply(name: String): Option[ProfileName] =
      name match {
        case AMF.profile    => Some(AMF)
        case OAS.profile    => Some(OAS)
        case OAS3.profile   => Some(OAS3)
        case RAML.profile   => Some(RAML)
        case RAML08.profile => Some(RAML08)
        case _              => None
      }

    def apply(profile: String): ProfileName = profile match {
      case "AMF"    => AMF
      case "OAS"    => OAS
      case "OAS3"   => OAS3
      case "RAML"   => RAML
      case "RAML08" => RAML08
      case other    => new ProfileName(other)
    }
  }

  trait MessageStyle {
    def profileName: ProfileName
  }

  object RAMLStyle extends MessageStyle {
    override def profileName: ProfileName = RAML
  }
  object OASStyle extends MessageStyle {
    override def profileName: ProfileName = OAS
  }
  object AMFStyle extends MessageStyle {
    override def profileName: ProfileName = AMF
  }

}
