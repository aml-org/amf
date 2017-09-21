package amf.validation

import java.nio.charset.Charset

import amf.validation.core.ValidationReport
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.jena.rdf.model.Model
import org.apache.jena.util.FileUtils
import org.topbraid.shacl.validation.ValidationUtil
import org.topbraid.spin.util.JenaUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

/**
  * Created by antoniogarrote on 17/07/2017.
  */

class SHACLValidator extends amf.validation.core.SHACLValidator {

  val formats = Map(
    "application/ld+json" -> "JSON-LD",
    "application/json" -> "JSON-LD",
    "JSON-LD" -> "JSON-LD",
    "text/n3" -> FileUtils.langN3,
    "test/turtle" -> FileUtils.langTurtle
  )

  override def validate(data: String, dataMediaType: String, shapes: String, shapesMediaType: String): Future[String] = {
    val promise = Promise[String]()
    val dataModel = loadModel(StringUtils.chomp(data), dataMediaType)
    val shapesModel = loadModel(StringUtils.chomp(shapes), shapesMediaType)
    val report = ValidationUtil.validateModel(dataModel, shapesModel, false)
    val output = RDFPrinter(report.getModel, "JSON-LD")
    promise.success(output)
    promise.future

  }

  private def loadModel(data: String, mediaType: String): Model = {
    formats.get(mediaType) match {
      case Some(format) =>
        val dataModel = JenaUtil.createMemoryModel()
        dataModel.read(IOUtils.toInputStream(data, Charset.defaultCharset()), "urn:dummy", format)
        dataModel
      case None => throw new Exception(s"Unsupported media type $mediaType")
    }
  }

  override def report(data: String, dataMediaType: String, shapes: String, shapesMediaType: String): Future[ValidationReport] =
    validate(data, dataMediaType, shapes, shapesMediaType).map(new JVMValidationReport(_))

}
