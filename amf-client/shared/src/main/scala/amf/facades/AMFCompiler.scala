package amf.facades

import amf.core
import amf.core.model.document.BaseUnit
import amf.core.parser.{ParsedDocument, ParsedReference, ParserContext, ReferenceKind, SyamlParsedDocument}
import amf.core.remote.Syntax.{Json, Yaml}
import amf.core.remote._
import amf.core.{AMFCompiler => ModularCompiler}
import amf.plugins.document.vocabularies.RamlHeaderExtractor
import org.yaml.model.YDocument

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AMFCompiler private (val url: String,
                           val remote: Platform,
                           val base: Option[Context],
                           hint: Hint,
                           val currentValidation: Validation,
                           private val cache: Cache,
                           private val baseContext: Option[ParserContext] = None)
    extends RamlHeaderExtractor {

  implicit val ctx: ParserContext                             = baseContext.getOrElse(ParserContext(url, Seq.empty))
  private lazy val context: Context                           = base.map(_.update(url)).getOrElse(core.remote.Context(remote, url))
  private lazy val location                                   = context.current
  private val references: ListBuffer[Future[ParsedReference]] = ListBuffer()

  def build(): Future[BaseUnit] = {

    val actualVendor = hint.vendor match {
      case AmlVocabulary => "RAML Vocabularies"
      case Raml10         => "RAML 1.0"
      case Raml08         => "RAML 0.8"
      case Raml           => "RAML"
      case Oas3           => "OAS 3.0.0"
      case Oas            => "OAS 2.0"
      case Payload        => "AMF Payload"
      case Amf            => "AMF Graph"
      case Extension      => "RAML Extension"
      case _              => "Unknown Vendor"
    }

    val mediaType = hint match {
      case VocabularyYamlHint => "application/yaml"
      case RamlYamlHint       => "application/yaml"
      case RamlJsonHint       => "application/json"
      case OasJsonHint        => "application/json"
      case OasYamlHint        => "application/yaml"
      case AmfJsonHint        => "application/ld+json"
      case PayloadJsonHint    => "application/amf+json"
      case PayloadYamlHint    => "application/amf+yaml"
      case ExtensionYamlHint  => "application/yaml"
      case _                  => "text/plain"
    }

    new ModularCompiler(
      url,
      remote,
      base,
      Option(mediaType),
      actualVendor,
      hint.kind,
      cache,
      baseContext
    ).build()

    /*
    // Reset the data node counter
    idCounter.reset()

    if (context.hasCycles) failed(new CyclicReferenceException(context.history))
    else
      cache.getOrUpdate(location) { () =>
        compile()
      }
   */
  }

  def root(): Future[Root] = {
    val actualVendor = hint.vendor match {
      case AmlVocabulary => "RAML Vocabularies"
      case Raml10         => "RAML 1.0"
      case Raml08         => "RAML 0.8"
      case Raml           => "RAML"
      case Oas3           => "OAS 3.0.0"
      case Oas            => "OAS 2.0"
      case Payload        => "AMF Payload"
      case Amf            => "AMF Graph"
      case Extension      => "RAML Vocabularies"
      case _              => "Unknown Vendor"
    }

    val mediaType = hint match {
      case VocabularyYamlHint => "application/yaml"
      case RamlYamlHint       => "application/yaml"
      case RamlJsonHint       => "application/json"
      case OasJsonHint        => "application/json"
      case OasYamlHint        => "application/yaml"
      case AmfJsonHint        => "application/ld+json"
      case ExtensionYamlHint  => "application/raml"
      case _                  => "text/plain"
    }

    new ModularCompiler(
      url,
      remote,
      base,
      Option(mediaType),
      actualVendor,
      hint.kind,
      cache,
      Some(ctx)
    ).root() map (root => oldFormat(root))
  }

  def oldFormat(root: amf.core.Root): Root = {

    val mediaType = if (root.mediatype.indexOf("yaml") > -1) {
      Yaml
    } else if (root.mediatype.indexOf("json") > -1) {
      Json
    } else {
      Unknown
    }

    val hint = root.vendor match {
      case "RAML Vocabularies" if mediaType == Yaml              => VocabularyYamlHint
      case "RAML" | "RAML 1.0" | "RAML 0.8" if mediaType == Yaml => RamlYamlHint
      case "RAML" | "RAML 1.0" | "RAML 0.8" if mediaType == Json => RamlJsonHint
      case "OAS 2.0" | "OAS 3.0.0" if mediaType == Json          => OasJsonHint
      case "OAS 2.0" | "OAS 3.0.0" if mediaType == Yaml          => OasYamlHint
      case "AMF Payload" if mediaType == Yaml                    => PayloadYamlHint
      case "AMF Payload" if mediaType == Json                    => PayloadJsonHint
      case "AMF Extension"                                       => ExtensionYamlHint
      case _                                                     => AmfJsonHint
    }

    Root(
      root.parsed,
      location,
      root.references,
      root.referenceKind,
      hint.vendor,
      root.raw
    )
  }
}

case class Root(parsed: ParsedDocument,
                location: String,
                references: Seq[ParsedReference],
                referenceKind: ReferenceKind,
                vendor: Vendor,
                raw: String) {
  val document: YDocument = parsed.asInstanceOf[SyamlParsedDocument].document

  // TODO: remove me, only for compatibility while refactoring
  def newFormat(): amf.core.Root = {
    val actualVendor = vendor match {
      case AmlVocabulary  => "AML 1.0"
      case Raml10         => "RAML 1.0"
      case Raml08         => "RAML 0.8"
      case Raml           => "RAML"
      case Oas3           => "OAS 3.0.0"
      case Oas            => "OAS 2.0"
      case Payload        => "AMF Payload"
      case Amf            => "AMF Graph"
      case Extension      => "RAML Vocabularies"
      case _              => "Unknown Vendor"
    }
    val mediaType = vendor match {
      case AmlVocabulary => "application/yaml"
      case Extension      => "application/yaml"
      case Raml           => "application/yaml"
      case Oas            => "application/json"
      case Payload        => "application/amf+json"
      case Amf            => "application/ld+json"
      case Unknown        => "text/plain"
    }

    amf.core.Root(
      parsed,
      location,
      mediaType,
      references,
      referenceKind,
      actualVendor,
      raw
    )
  }
}

object AMFCompiler {
  def apply(url: String,
            remote: Platform,
            hint: Hint,
            currentValidation: Validation,
            context: Option[Context] = None,
            cache: Option[Cache] = None,
            ctx: Option[ParserContext] = None) =
    new AMFCompiler(url, remote, context, hint, currentValidation, cache.getOrElse(Cache()))

  val RAML_10 = "#%RAML 1.0\n"
}
