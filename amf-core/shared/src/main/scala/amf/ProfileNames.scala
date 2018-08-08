package amf

import amf.core.vocabulary.Namespace

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("ProfileNames")
object ProfileNames {
  val AMF: ProfileName    = AMFProfile
  val OAS: ProfileName    = OASProfile
  val OAS3: ProfileName   = OAS3Profile
  val RAML: ProfileName   = RAMLProfile
  val RAML08: ProfileName = RAML08Profile
}

case class ProfileName(private[amf] val p: String, private val m: MessageStyle = AMFStyle) {
  @JSExportTopLevel("ProfileName")
  def this(profile: String) = this(profile, AMFStyle)
  def profile: String            = p
  def messageStyle: MessageStyle = m
  override def toString: String  = p
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
      case AMFProfile.p    => Some(AMFProfile)
      case OASProfile.p    => Some(OASProfile)
      case OAS3Profile.p   => Some(OAS3Profile)
      case RAMLProfile.p   => Some(RAMLProfile)
      case RAML08Profile.p => Some(RAML08Profile)
      case _               => None
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

@JSExportAll
@JSExportTopLevel("DataTypes")
object DataTypes {
  val String: String       = (Namespace.Xsd + "string").iri()
  val Integer: String      = (Namespace.Xsd + "integer").iri()
  val Number: String       = (Namespace.Shapes + "number").iri()
  val Long: String         = (Namespace.Xsd + "long").iri()
  val Double: String       = (Namespace.Xsd + "double").iri()
  val Float: String        = (Namespace.Xsd + "float").iri()
  val Decimal: String      = (Namespace.Xsd + "decimal").iri()
  val Boolean: String      = (Namespace.Xsd + "boolean").iri()
  val Date: String         = (Namespace.Xsd + "date").iri()
  val Time: String         = (Namespace.Xsd + "time").iri()
  val DateTime: String     = (Namespace.Xsd + "dateTime").iri()
  val DateTimeOnly: String = (Namespace.Shapes + "dateTimeOnly").iri()
  val File: String         = (Namespace.Shapes + "file").iri()
  val Byte: String         = (Namespace.Xsd + "byte").iri()
  val Binary: String       = (Namespace.Xsd + "base64Binary").iri()
  val Password: String     = (Namespace.Shapes + "password").iri()
  val Any: String          = (Namespace.Xsd + "anyType").iri()
  val Nil: String          = (Namespace.Xsd + "nil").iri()
}
