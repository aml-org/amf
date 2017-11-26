package amf.plugins.features.validation

import java.io.{InputStreamReader, Reader, StringReader}
import java.nio.charset.Charset

import amf.core.validation.core.ValidationReport
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.jena.rdf.model.{Model, Resource}
import org.apache.jena.util.FileUtils
import org.topbraid.shacl.js.{JSScriptEngine, JSScriptEngineFactory, NashornScriptEngine, SHACLScriptEngineManager}
import org.topbraid.shacl.validation.ValidationUtil
import org.topbraid.spin.util.JenaUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

/**
  * Created by antoniogarrote on 17/07/2017.
  */

class SHACLValidator extends amf.core.validation.core.SHACLValidator {

  var functionUrl: Option[String] = None
  var functionCode: Option[String] = None

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
    loadLibrary()
    val res = SHACLScriptEngineManager.begin()
    var report: Option[Resource] = None
    try {
      SHACLScriptEngineManager.getCurrentEngine.executeScriptFromURL(NashornScriptEngine.RDFQUERY_JS)
      report = Some(ValidationUtil.validateModel(dataModel, shapesModel, false))

    } finally {
      SHACLScriptEngineManager.end(res)
    }

    val output = RDFPrinter(report.get.getModel, "JSON-LD")
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

  /**
    * Registers a library in the validator
    *
    * @param url
    * @param code
    * @return
    */
  override def registerLibrary(url: String, code: String): Unit = {
    functionUrl = Some(url)
    functionCode = Some(code)
  }


  protected def loadLibrary(): Unit = {
    JSScriptEngineFactory.set(new JSScriptEngineFactory() {
      override def createScriptEngine: JSScriptEngine = new CachedScriptEngine(functionUrl, functionCode)
    })
  }

}


class CachedScriptEngine(functionUrl: Option[String], functionCode: Option[String]) extends org.topbraid.shacl.js.NashornScriptEngine() {
    @throws[Exception]
  override protected def createScriptReader(url: String): Reader = {
    if (NashornScriptEngine.DASH_JS.equals(url)) {
      new InputStreamReader(classOf[NashornScriptEngine].getResourceAsStream("/etc/dash.js"))
    } else if (NashornScriptEngine.RDFQUERY_JS.equals(url)) {
      new InputStreamReader(classOf[NashornScriptEngine].getResourceAsStream("/etc/rdfquery.js"))
    } else if (functionUrl.isDefined && functionUrl.get.equals(functionUrl.get)) {
      new StringReader(functionCode.get)
    } else {
      new InputStreamReader(new java.net.URL(url).openStream)
    }
  }
}
