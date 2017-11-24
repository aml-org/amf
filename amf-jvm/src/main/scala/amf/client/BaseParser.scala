package amf.client

import java.util.concurrent.CompletableFuture

import amf.ProfileNames
import amf.framework.model.document.{BaseUnit => CoreBaseUnit, Document => CoreDocument, Fragment => CoreFragment, Module => CoreModule}
import amf.framework.remote.Syntax.Syntax
import amf.framework.remote.{Platform, StringContentPlatform, Vendor}
import amf.framework.validation.AMFValidationReport
import amf.model.{Fragment, _}
import amf.plugins.document.webapi.model.{Extension => CoreExtension, Overlay => CoreOverlay}
import amf.remote.FutureConverter.converters

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

/**
  * Base class for JVM parsers.
  */
abstract class BaseParser(protected val vendor: Vendor, protected val syntax: Syntax) extends PlatformParser {

  private val DEFAULT_DOCUMENT_URL = "http://raml.org/amf/default_document"

  private def unitScalaToJVM(unit: CoreBaseUnit): BaseUnit = unit match {
    case o: CoreOverlay           => new Overlay(o)
    case e: CoreExtension         => new Extension(e)
    case d: CoreDocument          => Document(d)
    case m: CoreModule            => Module(m)
    case f: CoreFragment          => Fragment(f)
  }

  /**
    * Generates a [[amf.model.BaseUnit]] from the api located in the given url.
    * @param url : Location of the api.
    * @param handler Handler object to execute the success or fail functions with the result object model.
    */
  def parseFile(url: String, handler: Handler[BaseUnit]): Unit =
    super.parse(url, BaseUnitHandlerAdapter(handler))

  /**
    * Generates a [[amf.model.BaseUnit]] from the api located in the given url.
    * @param url : Location of the api.
    * @param platform: Platform
    * @param handler Handler object to execute the success or fail functions with the result object model.
    */
  def parseFile(url: String, platform: Platform, handler: Handler[BaseUnit]): Unit =
    super.parse(url, BaseUnitHandlerAdapter(handler), Some(platform))

  /**
    * Generates the [[amf.model.BaseUnit]] from a given string, which should be a valid api.
    * @param stream: The api as a string.
    * @param handler Handler object to execute the success or fail functions with the result object model.
    */
  def parseString(stream: String, handler: Handler[BaseUnit]): Unit =
    super.parse(DEFAULT_DOCUMENT_URL,
                BaseUnitHandlerAdapter(handler),
                Some(StringContentPlatform(DEFAULT_DOCUMENT_URL, stream, platform)))

  /**
    * Generates the [[amf.model.BaseUnit]] from a given string, which should be a valid api.
    * @param stream: The api as a string.
    * @param platform: Platform to wrap
    * @param handler Handler object to execute the success or fail functions with the result object model.
    */
  def parseString(stream: String, platform: Platform, handler: Handler[BaseUnit]): Unit =
    super.parse(DEFAULT_DOCUMENT_URL,
                BaseUnitHandlerAdapter(handler),
                Some(StringContentPlatform(DEFAULT_DOCUMENT_URL, stream, platform)))

  def parseString(url: String, stream: String, platform: Platform, handler: Handler[BaseUnit]): Unit =
    super.parse(url, BaseUnitHandlerAdapter(handler), Some(StringContentPlatform(url, stream, platform)))

  /**
    * Asynchronously generate a [[amf.model.BaseUnit]] from the api located in the given url.
    * @param url : Location of the api.
    * @return A java future that will have a [[amf.model.BaseUnit]] or an error to handle the result of such invocation.
    */
  def parseFileAsync(url: String): CompletableFuture[BaseUnit] = super.parseAsync(url).map(unitScalaToJVM).asJava

  /**
    * Asynchronously generate a [[amf.model.BaseUnit]] from the api located in the given url.
    * @param url : Location of the api.
    * @param platform: Platform to wrap
    * @return A java future that will have a [[amf.model.BaseUnit]] or an error to handle the result of such invocation.
    */
  def parseFileAsync(url: String, platform: Platform): CompletableFuture[BaseUnit] =
    super.parseAsync(url, Some(platform)).map(unitScalaToJVM).asJava

  def parseFileAsync(url: String, platform: Platform, o: ParsingOptions): CompletableFuture[BaseUnit] =
    super.parseAsync(url, Some(platform), o).map(unitScalaToJVM).asJava

  /**
    * Asynchronously generate a [[amf.model.BaseUnit]] from a given string, which should be a valid api.
    * @param stream: The api as a string
    * @return A java future that will have a [[amf.model.BaseUnit]] or an error to handle the result of such invocation.
    */
  def parseStringAsync(stream: String): CompletableFuture[BaseUnit] =
    super
      .parseAsync(DEFAULT_DOCUMENT_URL, Some(StringContentPlatform(DEFAULT_DOCUMENT_URL, stream, platform)))
      .map(unitScalaToJVM)
      .asJava

  /**
    * Asynchronously generate a [[amf.model.BaseUnit]] from a given string, which should be a valid api.
    * @param stream: The api as a string
    * @param platform: Platform to wrap
    * @return A java future that will have a [[amf.model.BaseUnit]] or an error to handle the result of such invocation.
    */
  def parseStringAsync(stream: String, platform: Platform): CompletableFuture[BaseUnit] =
    super
      .parseAsync(DEFAULT_DOCUMENT_URL, Some(StringContentPlatform(DEFAULT_DOCUMENT_URL, stream, platform)))
      .map(unitScalaToJVM)
      .asJava

  def parseStringAsync(textUrl: String, stream: String, platform: Platform): CompletableFuture[BaseUnit] =
    super.parseAsync(textUrl, Some(StringContentPlatform(textUrl, stream, platform))).map(unitScalaToJVM).asJava

  /**
    * Generates the validation report for the last parsed model.
    * @param profileName name of the profile to be parsed
    * @param messageStyle if a RAML/OAS profile, this can be set to the preferred error reporting styl
    * @return the AMF validation report
    */
  def reportValidation(profileName: String, messageStyle: String): CompletableFuture[AMFValidationReport] =
    super.reportValidationImplementation(profileName, messageStyle).asJava

  def reportValidation(profileName: String): CompletableFuture[AMFValidationReport] =
    super.reportValidationImplementation(profileName, ProfileNames.RAML).asJava

  /**
    * Generates a custom validaton profile as specified in the input validation profile file
    * @param profileName name of the profile to be parsed
    * @param customProfilePath path to the custom profile file
    * @return the AMF validation report
    */
  def reportCustomValidation(profileName: String, customProfilePath: String): CompletableFuture[AMFValidationReport] =
    super.reportCustomValidationImplementation(profileName, customProfilePath).asJava

  private case class BaseUnitHandlerAdapter(handler: Handler[BaseUnit]) extends Handler[amf.framework.model.document.BaseUnit] {
    override def success(document: amf.framework.model.document.BaseUnit): Unit = handler.success(unitScalaToJVM(document))

    override def error(exception: Throwable): Unit = handler.error(exception)
  }
}
