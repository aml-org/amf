package amf.apicontract.internal.spec.raml.parser.document

object RamlAnnotationTargets {

  def targetsFor(contextType: RamlWebApiContextType): List[String] = contextType match {
    case RamlWebApiContextType.DEFAULT   => List(VocabularyMappings.webapi)
    case RamlWebApiContextType.LIBRARY   => List(VocabularyMappings.library)
    case RamlWebApiContextType.OVERLAY   => List(VocabularyMappings.overlay)
    case RamlWebApiContextType.EXTENSION => List(VocabularyMappings.extension)
    case _                               => Nil
  }
}
