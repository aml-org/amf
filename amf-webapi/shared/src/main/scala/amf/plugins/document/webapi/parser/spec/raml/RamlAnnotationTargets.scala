package amf.plugins.document.webapi.parser.spec.raml

import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContextType
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContextType.RamlWebApiContextType
import amf.plugins.document.webapi.vocabulary.VocabularyMappings

object RamlAnnotationTargets {

  def targetsFor(contextType: RamlWebApiContextType): List[String] = contextType match {
    case RamlWebApiContextType.DEFAULT   => List(VocabularyMappings.webapi)
    case RamlWebApiContextType.LIBRARY   => List(VocabularyMappings.library)
    case RamlWebApiContextType.OVERLAY   => List(VocabularyMappings.overlay)
    case RamlWebApiContextType.EXTENSION => List(VocabularyMappings.extension)
    case _                               => Nil
  }
}
