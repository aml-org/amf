package amf.tasks.tsvimport

import java.io.{BufferedReader, File, FileReader}

import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.validation.AMFValidation

import scala.collection.JavaConverters._

/**
  * Created by antoniogarrote on 17/07/2017.
  */
/**
  * Creates a new Validation File pointing to a TSV file with validations.
  * Includes the logic to parse the validations file.
  * @param validationsFile The TSV file with all the references
  */
class ValidationsFile(validationsFile: File) {

  def parseLine(line: String): Option[AMFValidation] =
    line.split("\t") match {
      case Array(uri,
                 message,
                 spec: String,
                 level,
                 owlClass,
                 owlProperty,
                 shape,
                 target,
                 constraint,
                 value,
                 ramlErrorMessage,
                 openAPIErrorMessage) =>
        Some(
          AMFValidation(
            nonNullString(Namespace.uri(uri.trim).iri()),
            nonNullString(message.trim),
            spec.trim,
            level.trim,
            nonNullString(Namespace.uri(owlClass.trim).iri()),
            nonNullString(Namespace.uri(owlProperty.trim).iri()),
            shape.trim,
            Namespace.uri(target.trim).iri(),
            Namespace.uri(constraint.trim).iri(),
            Namespace.uri(value.trim).iri(), // this might not be a URI, but trying to expand it is still safe
            ramlErrorMessage.trim,
            openAPIErrorMessage.trim
          ))
      case _ => None
    }

  def validations(): List[AMFValidation] =
    new BufferedReader(new FileReader(validationsFile))
      .lines()
      .iterator()
      .asScala
      .drop(1)
      .map(parseLine)
      .filter(_.isDefined)
      .map(_.get)
      .toList

  def lines(): List[String] =
    new BufferedReader(new FileReader(validationsFile))
      .lines()
      .iterator()
      .asScala
      .drop(1)
      .toList

  protected def nonNullString(s: String): Option[String] = if (s == "") { None } else { Some(s.trim) }
}
