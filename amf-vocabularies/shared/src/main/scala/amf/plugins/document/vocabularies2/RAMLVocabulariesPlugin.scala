package amf.plugins.document.vocabularies2

import amf.core.Root
import amf.core.client.GenerationOptions
import amf.core.metamodel.Obj
import amf.core.model.document.BaseUnit
import amf.core.model.domain.AnnotationGraphLoader
import amf.core.parser.{AbstractReferenceCollector, ParserContext}
import amf.core.plugins.{AMFDocumentPlugin, AMFPlugin}
import amf.core.remote.Platform
import amf.plugins.document.vocabularies.references.RAMLExtensionsReferenceCollector
import amf.plugins.document.vocabularies.{DialectHeader, RamlHeaderExtractor}
import amf.plugins.document.vocabularies2.emitters.dialects.{RamlDialectEmitter, RamlDialectLibraryEmitter}
import amf.plugins.document.vocabularies2.emitters.vocabularies.RamlVocabularyEmitter
import amf.plugins.document.vocabularies2.metamodel.document.{DialectFragmentModel, DialectLibraryModel, DialectModel, VocabularyModel}
import amf.plugins.document.vocabularies2.metamodel.domain._
import amf.plugins.document.vocabularies2.model.document.{Dialect, DialectLibrary, Vocabulary}
import amf.plugins.document.vocabularies2.parser.ExtensionHeader
import amf.plugins.document.vocabularies2.parser.dialects.{DialectContext, RamlDialectsParser}
import amf.plugins.document.vocabularies2.parser.vocabularies.{RamlVocabulariesParser, VocabularyContext}
import org.yaml.model.YDocument

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object RAMLVocabulariesPlugin extends AMFDocumentPlugin with RamlHeaderExtractor {

  override val ID: String = "RAML Vocabularies2"

  override val vendors: Seq[String] = Seq("RAML Vocabularies2")

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
    DialectFragmentModel
  ) // TODO

  override def serializableAnnotations(): Map[String, AnnotationGraphLoader] = Map.empty

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit): BaseUnit = unit // TODO

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  override def documentSyntaxes: Seq[String] = Seq(
    "application/raml",
    "application/raml+json",
    "application/raml+yaml",
    "text/yaml",
    "text/x-yaml",
    "application/yaml",
    "application/x-yaml"
  )

  /**
    * Parses an accepted document returning an optional BaseUnit
    */
  override def parse(document: Root, parentContext: ParserContext, platform: Platform): Option[BaseUnit] = {
    comment(document) match {
      case Some(comment) =>
        comment.metaText match {
          case ExtensionHeader.VocabularyHeader      => Some(new RamlVocabulariesParser(document)(new VocabularyContext(parentContext)).parseDocument())
          case ExtensionHeader.DialectHeader         => Some(new RamlDialectsParser(document)(new DialectContext(parentContext)).parseDocument())
          case ExtensionHeader.DialectLibraryHeader  => Some(new RamlDialectsParser(document)(new DialectContext(parentContext)).parseLibrary())
          case ExtensionHeader.DialectFragmentHeader => Some(new RamlDialectsParser(document)(new DialectContext(parentContext)).parseFragment())
          case _ => None
        }
      case _ => None
    }
  }

  /**
    * Unparses a model base unit and return a document AST
    */
  override def unparse(unit: BaseUnit, options: GenerationOptions): Option[YDocument] = unit match {
    case vocabulary: Vocabulary  => Some(RamlVocabularyEmitter(vocabulary).emitVocabulary())
    case dialect: Dialect        => Some(RamlDialectEmitter(dialect).emitDialect())
    case library: DialectLibrary => Some(RamlDialectLibraryEmitter(library).emitDialectLibrary())
    case _                       => None
  }

  /**
    * Decides if this plugin can parse the provided document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will parse the document base on information from
    * the document structure
    */
  override def canParse(document: Root): Boolean = DialectHeader(document)

  /**
    * Decides if this plugin can unparse the provided model document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will unparse the document base on information from
    * the instance type and properties
    */
  override def canUnparse(unit: BaseUnit): Boolean = unit match {
    case _: Vocabulary     => true
    case _: Dialect        => true
    case _: DialectLibrary => true
    case _                 => false
  }

  override def referenceCollector(): AbstractReferenceCollector = new RAMLExtensionsReferenceCollector()

  override def dependencies(): Seq[AMFPlugin] = Seq()

}
