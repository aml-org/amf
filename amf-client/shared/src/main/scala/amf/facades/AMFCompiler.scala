package amf.facades

import amf.core
import amf.core.client.ParsingOptions
import amf.core.model.document.BaseUnit
import amf.core.parser.{ParsedReference, ParserContext}
import amf.core.remote._
import amf.core.{Root, AMFCompiler => ModularCompiler}
import amf.plugins.document.vocabularies.RamlHeaderExtractor

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

class AMFCompiler private (val url: String,
                           val remote: Platform,
                           val base: Option[Context],
                           hint: Hint,
                           val currentValidation: Validation,
                           private val cache: Cache,
                           private val baseContext: Option[ParserContext] = None,
                           private val parsingOptions: ParsingOptions = ParsingOptions())
    extends RamlHeaderExtractor {

  implicit val ctx: ParserContext                             = baseContext.getOrElse(ParserContext(url, Seq.empty))
  private lazy val context: Context                           = base.map(_.update(url)).getOrElse(core.remote.Context(remote, url))
  private lazy val location                                   = context.current
  private val references: ListBuffer[Future[ParsedReference]] = ListBuffer()

  def build(): Future[BaseUnit] = {

    val mediaType = hint match {
      case VocabularyYamlHint => "application/yaml"
      case RamlYamlHint       => "application/yaml"
      case RamlJsonHint       => "application/json"
      case OasJsonHint        => "application/json"
      case OasYamlHint        => "application/yaml"
      case AmfJsonHint        => "application/ld+json"
      case PayloadJsonHint    => "application/amf+json"
      case PayloadYamlHint    => "application/amf+yaml"
      case _                  => "text/plain"
    }

    new ModularCompiler(
      url,
      remote,
      base,
      Option(mediaType),
      Some(hint.vendor.name),
      hint.kind,
      cache,
      baseContext,
      parsingOptions = parsingOptions
    ).build()

  }

  def root(): Future[Root] = {

    val mediaType = hint match {
      case VocabularyYamlHint => "application/yaml"
      case RamlYamlHint       => "application/yaml"
      case RamlJsonHint       => "application/json"
      case OasJsonHint        => "application/json"
      case OasYamlHint        => "application/yaml"
      case AmfJsonHint        => "application/ld+json"
      case _                  => "text/plain"
    }

    new ModularCompiler(
      url,
      remote,
      base,
      Option(mediaType),
      Some(hint.vendor.name),
      hint.kind,
      cache,
      Some(ctx)
    ).root()
  }

}

object AMFCompiler {
  def apply(url: String,
            remote: Platform,
            hint: Hint,
            currentValidation: Validation,
            context: Option[Context] = None,
            cache: Option[Cache] = None,
            ctx: Option[ParserContext] = None,
            parsingOptions: ParsingOptions = ParsingOptions()) =
    new AMFCompiler(url,
                    remote,
                    context,
                    hint,
                    currentValidation,
                    cache.getOrElse(Cache()),
                    parsingOptions = parsingOptions)

  val RAML_10 = "#%RAML 1.0\n"
}
