package amf.shapes.internal.validation.shacl

import amf.validation.internal.shacl.custom.CustomShaclValidator.{CustomShaclFunction, CustomShaclFunctions}

trait BaseCustomShaclFunctions {

  protected[amf] val listOfFunctions: Seq[CustomShaclFunction]
  lazy val functions: CustomShaclFunctions = listOfFunctions.map(f => f.name -> f).toMap

}
