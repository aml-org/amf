package amf.plugins.document.vocabularies.parser.common

import amf.core.model.document.BaseUnit
import amf.core.model.domain.DomainElement
import amf.core.model.domain.extensions.{CustomDomainProperty, DomainExtension}
import amf.core.parser.{Annotations, ErrorHandler, ParserContext}
import amf.plugins.document.vocabularies.parser.DynamicExtensionParser
import amf.plugins.document.vocabularies.parser.vocabularies.VocabularyDeclarations
import org.yaml.model.{YMap, YNode}

import scala.util.{Failure, Success}

trait AnnotationsParser {

  protected def parseAnnotations(ast: YMap, node: DomainElement, declarations: VocabularyDeclarations)(
      implicit ctx: ParserContext) = {
    val parsedAnnotationProperties: Iterable[((Option[String], String), String, YNode)] = ast.map.map {
      case (k, v) =>
        val key = k.as[String]
        if (key.startsWith("(") && key.endsWith(")")) {
          val base = key.replace("(", "").replace(")", "")
          base.split("\\.") match {
            case Array(prefix, suffix) => Some(((Some(prefix), suffix), key, v))
            case Array(suffix)         => Some(((None, suffix), key, v))
            case _                     => None
          }
        } else if (key.startsWith("x-")) {
          val base = key.replace("x-", "")
          base.split("-") match {
            case Array(prefix, suffix) => Some(((Some(prefix), suffix), key, v))
            case Array(suffix)         => Some(((None, suffix), key, v))
            case _                     => None
          }
        } else {
          None
        }
    } collect {
      case Some(parsed) =>
        parsed
    }

    val parsedExtensions = parsedAnnotationProperties map {
      case ((prefix, suffix), k, v) =>
        declarations.resolveExternalNamespace(prefix, suffix) match {
          case Success(propertyId) =>
            val id               = node.id + s"${prefix.map(_ + "/").getOrElse("/")}$suffix"
            val parsedAnnotation = DynamicExtensionParser(v, Some(id)).parse()
            val property         = CustomDomainProperty(Annotations(v)).withId(propertyId).withName(k)
            val extension = DomainExtension()
              .withId(id)
              .withExtension(parsedAnnotation)
              .withDefinedBy(property)
              .withName(k)
              .add(Annotations(v))
            Some(extension)
          case Failure(ex) =>
            declarations.usedVocabs.get(prefix.getOrElse("")) match {
              case Some(vocabulary) =>
                val id               = node.id + (if (node.id.endsWith("/") || node.id.endsWith("#")) "" else "/") + s"${prefix.map(_ + "/").getOrElse("/")}$suffix"
                val parsedAnnotation = DynamicExtensionParser(v, Some(id)).parse()
                val base             = vocabulary.base.value()
                val propertyId       = if (base.endsWith("#") || base.endsWith("/")) base + suffix else base + "/" + suffix
                val property         = CustomDomainProperty(Annotations(v)).withId(propertyId).withName(k)
                val extension = DomainExtension()
                  .withId(id)
                  .withExtension(parsedAnnotation)
                  .withDefinedBy(property)
                  .withName(k)
                  .add(Annotations(v))
                Some(extension)
              case None =>
                ctx.violation(ex.getMessage, v)
                None
            }
        }
    } collect { case Some(parsed) => parsed }

    if (parsedExtensions.nonEmpty) {
      node.withCustomDomainProperties(parsedExtensions.toSeq)
    }
  }

}
