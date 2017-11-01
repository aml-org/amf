package amf.model

import java.util.concurrent.{CompletableFuture, Future}

import amf.client.ParserConfig
import amf.client.commands.CommandHelper
import amf.document
import amf.remote.Platform
import amf.validation.AMFValidationReport
import amf.remote.FutureConverter.converters
import amf.vocabulary.Namespace

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global

/** Any parsable unit, backed by a source URI. */
trait BaseUnit {

  private[amf] val element: amf.document.BaseUnit

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  lazy val references: java.util.List[BaseUnit] = {
    val units: Seq[BaseUnit] = element.references map {
      case r: document.Module                             => Module(r)
      case dt: document.Fragment.DataType                 => DataType(dt)
      case a: document.Fragment.AnnotationTypeDeclaration => AnnotationTypeDeclaration(a)
      case t: document.Fragment.TraitFragment             => TraitFragment(t)
      case rt: document.Fragment.ResourceTypeFragment     => ResourceTypeFragment(rt)
      case ne: document.Fragment.NamedExample             => NamedExample(ne)
      case df: document.Fragment.DialectFragment          => DialectFragment(df)
      case di: document.Fragment.DocumentationItem        => DocumentationItem(di)
    }
    units.asJava
  }

  /** Returns the file location for the document that has been parsed to generate this model */
  def location: String = element.location

  def usage: String = element.usage

  /**
    * Validates the model
    * @param profile Name of the standard profile to use in validation: RAML, OpenAPI, AMF
    * @return The validation report
    */
  def validate(profile: String, platform: Platform): CompletableFuture[AMFValidationReport] =
    validateProfile(platform, Some(profile))

  /**
    * Validates the model
    * @param customProfilePath Path to a profile validation file to use in validation
    * @return The validation report
    */
  def customValidation(customProfilePath: String, platform: Platform): CompletableFuture[AMFValidationReport] =
    validateProfile(platform, None, Some(customProfilePath))

  def findById(id: String): DomainElement = {
    element.findById(Namespace.uri(id).iri()) match {
      case Some(e: DomainElement) => DomainElement(e)
      case _                      => null
    }
  }

  def findByType(typeId: String): java.util.List[DomainElement] =
    element.findByType(Namespace.expand(typeId).iri()).map(e => DomainElement(e)).asJava

  /**
    * Validates the model
    * @param p Platform support logic
    * @param profile Name of the standard profile to use in validation: RAML, OpenAPI, AMF
    * @param customProfilePath Path to a profile validation file to use in validation
    * @return The validation report
    */
  protected def validateProfile(p: Platform,
                                profile: Option[String] = Some("RAML"),
                                customProfilePath: Option[String] = None): CompletableFuture[AMFValidationReport] = {
    val config = ParserConfig(customProfile = customProfilePath)
    val helper = new CommandHelper {
      override val platform: Platform = p
    }
    val f = helper.setupValidation(config) flatMap { validation =>
      val profileName = validation.profile match {
        case Some(prof) => prof.name
        case None       => profile.get
      }

      validation.validate(element, profileName)
    }
    f.asJava
  }

}
