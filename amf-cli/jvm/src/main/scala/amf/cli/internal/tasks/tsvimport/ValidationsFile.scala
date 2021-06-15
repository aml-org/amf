package amf.cli.internal.tasks.tsvimport

import amf.core.client.common.validation.SeverityLevels
import amf.plugins.document.apicontract.validation.AMFRawValidations.AMFValidation

import java.io.{BufferedReader, File, FileReader}
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
                 owlClass,
                 owlProperty,
                 target,
                 constraint,
                 value,
                 ramlErrorMessage,
                 openAPIErrorMessage) =>
        Some(
          AMFValidation.fromStrings(
            uri.trim,
            message.trim,
            owlClass.trim,
            owlProperty.trim,
            target.trim,
            constraint.trim,
            value.trim, // this might not be a URI, but trying to expand it is still safe
            ramlErrorMessage.trim,
            openAPIErrorMessage.trim,
            SeverityLevels.VIOLATION
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
}
