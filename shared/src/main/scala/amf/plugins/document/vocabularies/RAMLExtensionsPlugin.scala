package amf.plugins.document.vocabularies

import amf.client.GenerationOptions
import amf.core.Root
import amf.framework.model.document._
import amf.domain.dialects.DomainEntity
import amf.framework.plugins.{AMFDocumentPlugin, AMFValidationPlugin}
import amf.framework.services.RuntimeValidator
import amf.framework.validation._
import amf.plugins.document.graph.AMFGraphPlugin
import amf.plugins.document.vocabularies.references.RAMLExtensionsReferenceCollector
import amf.plugins.document.webapi.model.DialectFragment
import amf.remote.Platform
import amf.spec.ParserContext
import amf.spec.dialects.{DialectEmitter, DialectParser}
import amf.validation.model.{AMFDialectValidations, ValidationProfile}
import org.yaml.model.{YComment, YDocument}

import scala.concurrent.ExecutionContext.Implicits.global

trait RamlHeaderExtractor {
  def comment(root: Root): Option[YComment] = root.parsed.comment

  def comment(document: YDocument): Option[YComment] =
    document.children.find(v => v.isInstanceOf[YComment]).asInstanceOf[Option[YComment]]
}

object DialectHeader extends RamlHeaderExtractor {
  def apply(root: Root): Boolean = comment(root) match {
    case Some(comment: YComment) => comment.metaText match {
      case t if t.startsWith("%RAML 1.0 Vocabulary") => true
      case t if t.startsWith("%RAML 1.0 Dialect")    => true
      case t if t.startsWith("%RAML 1.0")            => false
      case t if t.startsWith("%RAML 0.8")            => false
      case t if t.startsWith("%")                    => true
      case _                                         => false
    }
    case _                                           => false
  }
}
object RAMLExtensionsPlugin extends AMFDocumentPlugin with AMFValidationPlugin with ValidationResultProcessor with RamlHeaderExtractor {

  override val ID = "RAML Extension"

  val vendors = Seq("RAML Extension", "RAML 1.0")

  override def parse(root: Root, parentContext: ParserContext, platform: Platform): Option[BaseUnit] = {
    implicit val ctx: ParserContext = ParserContext(parentContext.validation, parentContext.refs)
    comment(root) match {
      case Some(comment: YComment) =>
        val header = comment.metaText
        if (platform.dialectsRegistry.knowsHeader(header)) {
          Some(DialectParser(root, header, platform.dialectsRegistry).parseUnit())
        } else {
          None
        }
      case _ => None
    }
  }

  override def canUnparse(unit: BaseUnit) = unit match {
    case document: Document => document.encodes.isInstanceOf[DomainEntity]
    case module: Module =>
      module.declares exists {
        case _: DomainEntity => true
        case _               => false
      }
    case _: DialectFragment => true
    case _                  => false
  }

  def canParse(root: Root): Boolean = DialectHeader(root)

  override def unparse(unit: BaseUnit, options: GenerationOptions) = Some(DialectEmitter(unit).emit())

  override def modelEntities = Nil

  override def documentSyntaxes = Seq(
    "application/raml",
    "application/raml+json",
    "application/raml+yaml",
    "text/yaml",
    "text/x-yaml",
    "application/yaml",
    "application/x-yaml"
  )

  override def referenceCollector() = new RAMLExtensionsReferenceCollector()

  /**
    * Validation profiles supported by this plugin by default
    */
  override def domainValidationProfiles(platform: Platform): Map[String, () => ValidationProfile] = {
    platform.dialectsRegistry.dialects.foldLeft(Map[String, () => ValidationProfile]()) {
      case (acc, dialect) => acc.updated(dialect.name, () => new AMFDialectValidations(dialect).profile())
    }
  }

  /**
    * Request for validation of a particular model, profile and list of effective validations form that profile
    */
  override def validationRequest(baseUnit: BaseUnit, profile: String, validations: EffectiveValidations, platform: Platform) = {
    var aggregatedReport: List[AMFValidationResult] = List()

    for {
      shaclReport <- RuntimeValidator.shaclValidation(baseUnit, validations)
    } yield {

      // aggregating parser-side validations
      var results = aggregatedReport.map(r => processAggregatedResult(r, "RAML", validations))

      // adding model-side validations
      results ++= shaclReport.results
        .map(r => buildValidationResult(baseUnit, r, "RAML", validations))
        .filter(_.isDefined)
        .map(_.get)

      AMFValidationReport(
        conforms = !results.exists(_.level == SeverityLevels.VIOLATION),
        model = baseUnit.id,
        profile = profile,
        results = results
      )
    }
  }

  override def dependencies() = Seq(AMFGraphPlugin)
}
