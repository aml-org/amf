package amf.apicontract.internal.spec.raml.parser.document

import amf.shapes.internal.spec.RamlWebApiContextType
import amf.shapes.internal.spec.RamlWebApiContextType.RamlWebApiContextType
import amf.shapes.internal.vocabulary.VocabularyMappings

object RamlAnnotationTargets {

  def targetsFor(contextType: RamlWebApiContextType): List[String] = contextType match {
    case RamlWebApiContextType.DEFAULT   => List(VocabularyMappings.webapi)
    case RamlWebApiContextType.LIBRARY   => List(VocabularyMappings.library)
    case RamlWebApiContextType.OVERLAY   => List(VocabularyMappings.overlay)
    case RamlWebApiContextType.EXTENSION => List(VocabularyMappings.extension)
    case _                               => Nil
  }
}
