package amf.shapes.internal.spec

import amf.core.client.scala.model.document.ExternalFragment
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{
  AnyShape,
  BaseIri,
  ContextMapping,
  CuriePrefix,
  DefaultVocabulary,
  SemanticContext
}
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.{
  InvalidCharacteristicsNode,
  InvalidContextNode,
  InvalidIri,
  InvalidPrefixReference
}
import org.mulesoft.common.net.UriValidator
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar, YSequence, YType}
import amf.core.internal.parser._
import amf.shapes.internal.spec.common.parser.ShapeParserContext

case class SemanticContextParser(map: YMap, shape: AnyShape)(implicit val ctx: ShapeParserContext) {

  def parse(): Option[SemanticContext] = {
    val contextEntry = map.key("@context")
    contextEntry.flatMap { entry =>
      entry.value.tagType match {
        case YType.Map =>
          val semanticContext: SemanticContext = parseMapContext(entry.value.as[YMap])
          shape.withSemanticContext(semanticContext)
          Some(semanticContext)
        case YType.Str =>
          val semanticContext = parseContextFromReference(entry.value.as[String])
          semanticContext.foreach(context => shape.withSemanticContext(context))
          semanticContext
        case _ =>
          ctx.eh.violation(
            InvalidContextNode,
            shape,
            "@context must be an object or a string",
            Annotations(entry.value)
          )
          None
      }
    }
  }

  private def parseContextFromReference(reference: String): Option[SemanticContext] = {
    val unit = ctx.refs.find(p => p.origin.url == reference).map(_.unit)
    unit
      .collect { case fragment: ExternalFragment => fragment.encodes.parsed }
      .flatten
      .map(ast => parseMapContext(ast.as[YMap]))
  }

  private def parseMapContext(m: YMap): SemanticContext = {

    val temporalContext = SemanticContext(m)

    m.entries.foreach { entry =>
      entry.key.as[YScalar].text match {
        case "@base"                              => parseBase(entry.value, temporalContext)
        case "@vocab"                             => parseVocab(entry.value, temporalContext)
        case "@type"                              => parseTypeMapping(entry.value, temporalContext)
        case other if other != "@characteristics" => parseMapping(entry, temporalContext)
        case _                                    => // Ignore, @characteristics is processed separately
      }
    }

    val finalContext = ctx.getSemanticContext match {
      case Some(rootContext) => temporalContext.partialMerge(rootContext)
      case None              => temporalContext
    }
    // @characteriscs must be processed later in case of there are new prefixes declared at the same level
    m.key("@characteristics")
      .foreach(entry => parseCharacteristics(entry.value, finalContext))

    finalContext
  }

  private def parseBase(n: YNode, semanticContext: SemanticContext): Unit = {
    Option(n.as[YScalar]) match {
      case Some(YType.Null) => semanticContext.withBase(BaseIri(n).withNulled(true))
      case Some(s)          => semanticContext.withBase(BaseIri(s).withIri(s.text))
      case _                => // ignore
    }
  }

  private def parseVocab(n: YNode, semanticContext: SemanticContext): Unit = {
    Option(n.as[YScalar]) match {
      case Some(YType.Null) => // ignore
      case Some(s)          => semanticContext.withVocab(DefaultVocabulary(s).withIri(s.text))
      case _                => // ignore
    }
  }

  private def parseTypeMapping(n: YNode, context: SemanticContext): Unit = {
    n.tagType match {
      case YType.Seq =>
        context.withTypeMappings(n.as[YSequence].nodes.map((e) => e.as[YScalar].text))
      case YType.Str =>
        context.withTypeMappings(Seq(n.as[YScalar].text))
      case _ => // ignore
    }
  }

  private def parseCharacteristics(node: YNode, context: SemanticContext): Unit = node.tagType match {
    case YType.Seq =>
      val iris =
        node.as[YSequence].nodes.map { e =>
          val compactIri = e.as[YScalar].text
          validateIri(e, compactIri, context)
        }
      // using context.withOverrideMappings(iris) instead breaks SemanticSchemaTests
      context.withTypeMappings(iris)
    case _ =>
      ctx.eh.violation(
        InvalidCharacteristicsNode,
        shape,
        "@context must be a sequence of strings",
        Annotations(node.value)
      )
  }

  private def validateIri(node: YNode, compactIri: String, context: SemanticContext): String = {
    val prefixes: Map[String, String] = ctx.getSemanticContext.map(_.prefixMap()).getOrElse(Map()) ++ context
      .prefixMap()
    val expandedIri = compactIri.split(':') match {
      case Array(prefix, suffix) if !suffix.startsWith("//") =>
        prefixes.get(prefix) match {
          case Some(mapping) => s"$mapping$suffix"
          case None =>
            ctx.eh.violation(
              InvalidPrefixReference,
              shape,
              "the referenced prefix could not be found in the @context declarations",
              Annotations(node.value)
            )
            compactIri
        }
      case _ => compactIri
    }

    if (!UriValidator.isUri(expandedIri))
      ctx.eh.violation(InvalidIri, shape, "the text must conform the IRI format", Annotations(node.value))

    // the Iri is returned without changes because it will be expanded on emission where is needed
    compactIri
  }

  private def parseMapping(m: YMapEntry, semanticContext: SemanticContext): semanticContext.type = {
    val key = m.key.as[YScalar].text
    m.value.tagType match {
      case YType.Null =>
        val mapping     = ContextMapping(m).withAlias(key).withNulled(true)
        val oldMappings = semanticContext.mapping
        semanticContext.withMapping(oldMappings ++ Seq(mapping))
      case YType.Str =>
        val iri = m.value.as[YScalar].text
        if (iri.endsWith("#") || iri.endsWith("/")) {
          val prefix    = CuriePrefix(m).withAlias(key).withIri(iri)
          val oldCuries = semanticContext.curies
          semanticContext.withCuries(oldCuries ++ Seq(prefix))
        } else {
          val mapping     = ContextMapping(m).withAlias(key).withIri(iri)
          val oldMappings = semanticContext.mapping
          semanticContext.withMapping(oldMappings ++ Seq(mapping))
        }
      case YType.Map =>
        val mapping       = ContextMapping(m).withAlias(key)
        val nestedMapping = m.value.as[YMap]
        nestedMapping
          .key("@id")
          .foreach(e => {
            val iri = e.value.as[YScalar].text
            mapping.withIri(iri)
          })
        nestedMapping
          .key("@type")
          .foreach(e => {
            val iri = e.value.as[YScalar].text
            mapping.withCoercion(iri)
          })
        val oldMappings = semanticContext.mapping
        semanticContext.withMapping(oldMappings ++ Seq(mapping))
    }
  }

}
