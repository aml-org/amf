package amf.plugins.document.vocabularies

import amf.client.plugins.{AMFDocumentPlugin, AMFPlugin, AMFValidationPlugin}
import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.emitter.RenderOptions
import amf.core.metamodel.Obj
import amf.core.model.document.BaseUnit
import amf.core.model.domain.AnnotationGraphLoader
import amf.core.parser.{
  DefaultParserSideErrorHandler,
  ErrorHandler,
  ParserContext,
  ReferenceHandler,
  SyamlParsedDocument
}
import amf.core.rdf.RdfModel
import amf.core.registries.AMFDomainEntityResolver
import amf.core.remote.{Aml, Platform}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.services.{RuntimeValidator, ValidationOptions}
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.core.ValidationProfile
import amf.core.validation.{AMFValidationReport, EffectiveValidations, SeverityLevels, ValidationResultProcessor}
import amf.internal.environment.Environment
import amf.plugins.document.vocabularies.annotations.{AliasesLocation, CustomId, JsonPointerRef, RefInclude}
import amf.plugins.document.vocabularies.emitters.dialects.{DialectEmitter, RamlDialectLibraryEmitter}
import amf.plugins.document.vocabularies.emitters.instances.DialectInstancesEmitter
import amf.plugins.document.vocabularies.emitters.vocabularies.VocabularyEmitter
import amf.plugins.document.vocabularies.metamodel.document._
import amf.plugins.document.vocabularies.metamodel.domain._
import amf.plugins.document.vocabularies.model.document._
import amf.plugins.document.vocabularies.parser.ExtensionHeader
import amf.plugins.document.vocabularies.parser.common.SyntaxExtensionsReferenceHandler
import amf.plugins.document.vocabularies.parser.dialects.{DialectContext, DialectsParser}
import amf.plugins.document.vocabularies.parser.instances.{DialectInstanceContext, DialectInstanceParser}
import amf.plugins.document.vocabularies.parser.vocabularies.{VocabulariesParser, VocabularyContext}
import amf.plugins.document.vocabularies.resolution.pipelines.{
  DialectInstancePatchResolutionPipeline,
  DialectInstanceResolutionPipeline,
  DialectResolutionPipeline
}
import amf.plugins.document.vocabularies.validation.AMFDialectValidations
import amf.{ProfileName, RamlProfile}
import org.yaml.model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait RamlHeaderExtractor {
  def comment(root: Root): Option[YComment] = {
    root.parsed match {
      case parsed: SyamlParsedDocument => parsed.comment
      case _                           => None
    }

  }

  def comment(document: YDocument): Option[YComment] =
    document.children.find(v => v.isInstanceOf[YComment]).asInstanceOf[Option[YComment]]
}

trait JsonHeaderExtractor {
  def dialect(root: Root): Option[String] = {
    root.parsed match {
      case parsedInput: SyamlParsedDocument =>
        val parsed: Seq[Option[String]] = parsedInput.document.children.collect {
          case n: YNode =>
            n.asOption[YMap] match {
              case Some(m) =>
                try {
                  m.entries.find(_.key.as[YScalar].text == "$dialect") map { entry =>
                    entry.value.as[String]
                  }
                } catch {
                  case _: YException => None
                }
              case None => None
            }

        }
        parsed.collectFirst { case Some(metaText) => metaText }
      case _ => None
    }

  }

}

object DialectHeader extends RamlHeaderExtractor with JsonHeaderExtractor {
  def apply(root: Root): Boolean = comment(root) match {
    case Some(comment: YComment) =>
      comment.metaText match {
        case t if t.startsWith("%Vocabulary 1.0") => true
        case t if t.startsWith("%Dialect 1.0")    => true
        case t if t.startsWith("%RAML 1.0")       => false
        case t if t.startsWith("%RAML 0.8")       => false
        case t if t.startsWith("%")               => true
        case _                                    => false
      }
    case _ =>
      dialect(root) match {
        case Some(_) => true
        case _       => false
      }
  }
}

object AMLPlugin
    extends AMFDocumentPlugin
    with RamlHeaderExtractor
    with JsonHeaderExtractor
    with AMFValidationPlugin
    with ValidationResultProcessor
    with PlatformSecrets {

  val registry = new DialectsRegistry

  override val ID: String = Aml.name

  override val vendors: Seq[String] = Seq(Aml.name)

  override def init(): Future[AMFPlugin] = Future { this }

  override def modelEntities: Seq[Obj] = Seq(
    VocabularyModel,
    ExternalModel,
    VocabularyReferenceModel,
    ClassTermModel,
    ObjectPropertyTermModel,
    DatatypePropertyTermModel,
    DialectModel,
    NodeMappingModel,
    PropertyMappingModel,
    DocumentsModelModel,
    PublicNodeMappingModel,
    DocumentMappingModel,
    DialectLibraryModel,
    DialectFragmentModel,
    DialectInstanceModel,
    DialectInstanceLibraryModel,
    DialectInstanceFragmentModel,
    DialectInstancePatchModel
  )

  override def serializableAnnotations(): Map[String, AnnotationGraphLoader] = Map(
    "aliases-location" -> AliasesLocation,
    "custom-id"        -> CustomId,
    "ref-include"      -> RefInclude,
    "json-pointer-ref" -> JsonPointerRef
  )

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit,
                       errorHandler: ErrorHandler,
                       pipelineId: String = ResolutionPipeline.DEFAULT_PIPELINE): BaseUnit =
    unit match {
      case patch: DialectInstancePatch => new DialectInstancePatchResolutionPipeline(errorHandler).resolve(patch)
      case dialect: Dialect            => new DialectResolutionPipeline(errorHandler).resolve(dialect)
      case dialect: DialectInstance    => new DialectInstanceResolutionPipeline(errorHandler).resolve(dialect)
      case _                           => unit
    }

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  override def documentSyntaxes: Seq[String] = Seq(
    "application/aml+json",
    "application/aml+yaml",
    "application/raml",
    "application/raml+json",
    "application/raml+yaml",
    "text/yaml",
    "text/x-yaml",
    "application/yaml",
    "application/x-yaml",
    "application/json"
  )

  /**
    * Parses an accepted document returning an optional BaseUnit
    */
  override def parse(document: Root,
                     parentContext: ParserContext,
                     platform: Platform,
                     options: ParsingOptions): Option[BaseUnit] = {
    val maybeMetaText = comment(document) match {
      case Some(comment) => Some(comment.metaText)
      case _ =>
        dialect(document) match {
          case Some(metaText) => Some("%" + metaText)
          case None           => None
        }
    }
    maybeMetaText match {
      case None => None
      case Some(metaText) =>
        metaText match {
          case ExtensionHeader.VocabularyHeader =>
            Some(new VocabulariesParser(document)(new VocabularyContext(parentContext)).parseDocument())
          case ExtensionHeader.DialectLibraryHeader =>
            Some(new DialectsParser(document)(new DialectContext(parentContext)).parseLibrary())
          case ExtensionHeader.DialectFragmentHeader =>
            Some(new DialectsParser(document)(new DialectContext(parentContext)).parseFragment())
          case ExtensionHeader.DialectHeader => parseAndRegisterDialect(document, parentContext)
          case header                        => parseDialectInstance(header, document, parentContext)
        }
    }
  }

  protected def unparseAsYDocument(unit: BaseUnit, renderOptions: RenderOptions): Option[YDocument] = {
    unit match {
      case vocabulary: Vocabulary  => Some(VocabularyEmitter(vocabulary).emitVocabulary())
      case dialect: Dialect        => Some(DialectEmitter(dialect).emitDialect())
      case library: DialectLibrary => Some(RamlDialectLibraryEmitter(library).emitDialectLibrary())
      case instance: DialectInstance =>
        Some(DialectInstancesEmitter(instance, registry.dialectFor(instance).get).emitInstance())
      case _ => None
    }
  }

  /**
    * Decides if this plugin can parse the provided document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will parse the document base on information from
    * the document structure
    */
  override def canParse(document: Root): Boolean =
    document.parsed.isInstanceOf[SyamlParsedDocument] && DialectHeader(document)

  /**
    * Decides if this plugin can unparse the provided model document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will unparse the document base on information from
    * the instance type and properties
    */
  override def canUnparse(unit: BaseUnit): Boolean = unit match {
    case _: Vocabulary             => true
    case _: Dialect                => true
    case _: DialectLibrary         => true
    case instance: DialectInstance => registry.knowsDialectInstance(instance)
    case _                         => false
  }

  override def referenceHandler(eh: ErrorHandler): ReferenceHandler =
    new SyntaxExtensionsReferenceHandler(registry, eh)

  override def dependencies(): Seq[AMFPlugin] = Seq()

  override def modelEntitiesResolver: Option[AMFDomainEntityResolver] = Some(registry)

  private def parseAndRegisterDialect(document: Root, parentContext: ParserContext) = {
    new DialectsParser(document)(new DialectContext(parentContext)).parseDocument() match {
      case dialect: Dialect =>
        registry.register(dialect)
        Some(dialect)
      case unit => Some(unit)
    }
  }

  protected def parseDialectInstance(header: String, document: Root, parentContext: ParserContext): Option[BaseUnit] = {
    val headerKey = header.split("\\|").head.replace(" ", "")
    registry.withRegisteredDialect(header) { dialect =>
      if (headerKey == dialect.header)
        new DialectInstanceParser(document)(new DialectInstanceContext(dialect, parentContext)).parseDocument()
      else if (dialect.isFragmentHeader(headerKey))
        new DialectInstanceParser(document)(new DialectInstanceContext(dialect, parentContext)).parseFragment()
      else if (dialect.isLibraryHeader(headerKey))
        new DialectInstanceParser(document)(new DialectInstanceContext(dialect, parentContext)).parseLibrary()
      else if (dialect.isPatchHeader(headerKey))
        new DialectInstanceParser(document)(new DialectInstanceContext(dialect, parentContext).forPatch()).parsePatch()
      else
        throw new Exception(s"Unknown type of dialect header $header")
    }
  }

  // Cache for the validation profiles
  var validationsProfilesMap: Map[String, ValidationProfile] = Map()

  /**
    * Validation profiles supported by this plugin. Notice this will be called multiple times.
    */
  override def domainValidationProfiles(platform: Platform): Map[String, () => ValidationProfile] = {
    registry.allDialects().foldLeft(Map[String, () => ValidationProfile]()) {
      case (acc, dialect) if !dialect.nameAndVersion().contains("Validation Profile") =>
        acc.updated(dialect.nameAndVersion(), () => { computeValidationProfile(dialect) })
      case (acc, _) => acc
    }
  }

  protected def computeValidationProfile(dialect: Dialect): ValidationProfile = {
    val profileName = dialect.nameAndVersion()
    validationsProfilesMap.get(profileName) match {
      case Some(profile) => profile
      case _ =>
        val resolvedDialect = new DialectResolutionPipeline(DefaultParserSideErrorHandler(dialect)).resolve(dialect)
        val profile         = new AMFDialectValidations(resolvedDialect).profile()
        validationsProfilesMap += (profileName -> profile)
        profile
    }
  }

  def aggregateValidations(validations: EffectiveValidations,
                           dependenciesValidations: Seq[ValidationProfile]): EffectiveValidations = {
    dependenciesValidations.foldLeft(validations) {
      case (effective, profile) => effective.someEffective(profile)
    }
  }

  /**
    * Request for validation of a particular model, profile and list of effective validations for that profile
    */
  override def validationRequest(baseUnit: BaseUnit,
                                 profile: ProfileName,
                                 validations: EffectiveValidations,
                                 platform: Platform,
                                 env: Environment): Future[AMFValidationReport] = {
    baseUnit match {
      case dialectInstance: DialectInstance =>
        val resolvedModel =
          new DialectInstanceResolutionPipeline(DefaultParserSideErrorHandler(baseUnit)).resolve(dialectInstance)

        val dependenciesValidations: Future[Seq[ValidationProfile]] = Future.sequence(
          dialectInstance.graphDependencies.map { instance =>
            registry.registerDialect(instance.value())
          }) map { dialects =>
          dialects.map(computeValidationProfile)
        }

        for {
          validationsFromDeps <- dependenciesValidations
          shaclReport <- RuntimeValidator.shaclValidation(resolvedModel,
                                                          aggregateValidations(validations, validationsFromDeps),
                                                          options = new ValidationOptions().withFullValidation())
        } yield {

          // adding model-side validations
          val results = shaclReport.results.flatMap { r =>
            buildValidationResult(baseUnit, r, RamlProfile.messageStyle, validations)
          }

          AMFValidationReport(
            conforms = !results.exists(_.level == SeverityLevels.VIOLATION),
            model = baseUnit.id,
            profile = profile,
            results = results
          )
        }

      case _ =>
        throw new Exception(s"Cannot resolve base unit of type ${baseUnit.getClass}")
    }
  }

  /**
    * Does references in this type of documents be recursive?
    */
  override val allowRecursiveReferences: Boolean = false

  def shapesForDialect(dialect: Dialect, validationFunctionsUrl: String): RdfModel = {
    val validationProfile = computeValidationProfile(dialect)
    val validations       = validationProfile.validations

    RuntimeValidator.shaclModel(validations, validationFunctionsUrl)
  }
}
