package amf.client

import amf.remote._

/**
  *
  */
object HintHelper {

  def ramlYamlHint  = RamlYamlHint
  def oasJsonHint   = OasJsonHint
  def ramlJsonHint  = RamlJsonHint
  def oasyamlHint   = OasYamlHint
  def amfJsonldHint = AmfJsonHint
}

object VendorHelper {

  def raml = Raml
  def oas  = Oas
  def amf  = Amf
}
