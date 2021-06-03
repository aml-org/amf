package amf.plugins.document.apicontract.parser.spec.raml

import amf.plugins.document.apicontract.parser.RamlWebApiContextType
import amf.plugins.document.apicontract.parser.RamlWebApiContextType.RamlWebApiContextType
import amf.plugins.document.apicontract.vocabulary.VocabularyMappings

object RamlAnnotationTargets {

  def targetsFor(contextType: RamlWebApiContextType): List[String] = contextType match {
    case RamlWebApiContextType.DEFAULT   => List(VocabularyMappings.webapi)
    case RamlWebApiContextType.LIBRARY   => List(VocabularyMappings.library)
    case RamlWebApiContextType.OVERLAY   => List(VocabularyMappings.overlay)
    case RamlWebApiContextType.EXTENSION => List(VocabularyMappings.extension)
    case _                               => Nil
  }
}
