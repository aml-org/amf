package amf.shapes.internal.spec.jsonschema.parser.document

import amf.core.client.scala.model.document.ExternalFragment
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.validation.core.ValidationSpecification
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.annotations.DeclarationsKey
import amf.shapes.internal.spec.common.parser.ShapeParserContext
import amf.shapes.internal.spec.common.{JSONSchemaDraft201909SchemaVersion, JSONSchemaVersion}
import amf.shapes.internal.spec.contexts.ReferenceFinder
import amf.shapes.internal.spec.contexts.ReferenceFinder.getJsonReferenceFragment
import amf.shapes.internal.spec.jsonschema.JsonSchemaEntry
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.{
  InvalidJsonSchemaReference,
  JsonSchemaDefinitionNotFound
}
import org.yaml.model.YNode

trait NameExtraction {
  def extract(ref: String, declarationKey: Option[String]): Either[String, String]
}

trait JsonSchemaRefNameExtraction extends NameExtraction {
  protected def extractShapeName(uriFragment: String, definitionsKey: String): Option[String] = {
    val regex = ("^\\/" + definitionsKey + "\\/(?<shape>[^\\/]+)$").r
    regex.findFirstMatchIn(uriFragment).map(_.group(1))
  }

  protected def validateDeclarationKey(
      ref: String,
      declarationKey: Option[String],
      shape: String
  ): Either[String, String] = declarationKey match {
    case Some(dk) if ref.contains(dk) => Right(shape)
    case Some(dk)                     => Left(s"The definition key present in the ref must be '$dk'")
    case None                         => Right(shape)
  }
}

object Draft2019NameExtraction extends JsonSchemaRefNameExtraction {
  override def extract(ref: String, declarationKey: Option[String]): Either[String, String] =
    extractShapeName(ref, "\\$defs").orElse(extractShapeName(ref, "definitions")) match {
      case Some(some) => validateDeclarationKey(ref, declarationKey, some)
      case None => Left(s"uriFragment '$ref' must be in the format of '#/definitions/<name>' or '#/$$defs/<name>'")
    }
}

object Draft4NameExtraction extends JsonSchemaRefNameExtraction {
  override def extract(ref: String, declarationKey: Option[String]): Either[String, String] =
    extractShapeName(ref, "definitions") match {
      case Some(some) => validateDeclarationKey(ref, declarationKey, some)
      case None       => Left(s"uriFragment '$ref' must be in the format of '#/definitions/<name>'")
    }
}

object JsonSchemaLinker {

  def linkShapeIn(ref: String, ast: YNode)(implicit ctx: ShapeParserContext): Option[AnyShape] = {
    val maybeDoc = findJsonSchemaDocument(ref, ctx)
    maybeDoc flatMap { document =>
      val maybeUriFragment = getJsonReferenceFragment(ref)
      linkShapeFromDocument(ref, document, maybeUriFragment, Annotations(ast))
    }
  }

  def linkShapeFromDocument(
      ref: String,
      document: JsonSchemaDocument,
      maybeUriFragment: Option[String],
      linkAnnotations: Annotations
  )(implicit ctx: ShapeParserContext): Option[AnyShape] = {
    computeShape(ref, document, maybeUriFragment, linkAnnotations)
      .collect { case shape: AnyShape =>
        shape.link(ref, linkAnnotations)
      }
  }

  private def computeShape(
      ref: String,
      document: JsonSchemaDocument,
      maybeUriFragment: Option[String],
      linkAnnotations: Annotations
  )(implicit ctx: ShapeParserContext) = {
    maybeUriFragment match {
      case Some(fragment) =>
        val baseRef    = maybeUriFragment.map(_ => ref.split("#").head).getOrElse(ref)
        val maybeShape = findShapeForReference(baseRef, document, fragment)
        throwErrors(maybeShape, linkAnnotations)
        maybeShape.right.toOption
      case None => Some(document.encodes)
    }
  }

  private def throwErrors(
      maybeShape: Either[(ValidationSpecification, String), AnyShape],
      linkAnnotations: Annotations
  )(implicit ctx: ShapeParserContext): Unit = {
    maybeShape.left.foreach { case (spec, error) =>
      ctx.eh.violation(spec, "", error, linkAnnotations)
    }
  }

  private def findJsonSchemaDocument(ref: String, ctx: ShapeParserContext) = {
    ReferenceFinder.findJsonReferencedUnit(ref, ref, ctx.refs).collect { case unit: JsonSchemaDocument =>
      unit
    }
  }

  private def findShapeForReference(ref: String, document: JsonSchemaDocument, uriFragment: String)(implicit
      ctx: ShapeParserContext
  ): Either[(ValidationSpecification, String), AnyShape] = {
    // TODO: we suppose we have a valid document entry here
    val version        = JsonSchemaEntry(document.schemaVersion.value()).get
    val declarationKey = document.annotations.find(classOf[DeclarationsKey]).map(_.key)
    val extractor      = nameExtractorFor(version)
    extractor
      .extract(uriFragment, declarationKey)
      .left
      .map(error => (InvalidJsonSchemaReference, error))
      .flatMap { name => findShapeWithName(ref, document, name, uriFragment) }
  }

  private def findShapeWithName(ref: String, doc: JsonSchemaDocument, name: String, uriFragment: String)(implicit
      ctx: ShapeParserContext
  ): Either[(ValidationSpecification, String), AnyShape] = {
    findShape(ref, doc, name)
      .toRight(
        (
          JsonSchemaDefinitionNotFound,
          s"Couldn't find schema identified by ${uriFragment} in ${doc.location().getOrElse("")}"
        )
      )
  }

  private def findShape(ref: String, doc: JsonSchemaDocument, name: String)(implicit ctx: ShapeParserContext) = {
    ctx.getJsonSchemaRefGuide.currentUnit match {
      case Some(_: ExternalFragment) => findShapeInDoc(doc, name)
      case _ => ctx.findDeclaredTypeInDocFragment(ref, name).collect { case shape: AnyShape => shape }
    }
  }

  private def findShapeInDoc(doc: JsonSchemaDocument, name: String)(implicit ctx: ShapeParserContext) = {
    doc.declares.collectFirst {
      case shape: AnyShape if shape.name.option().contains(name) => shape
    }
  }

  private def nameExtractorFor(version: JSONSchemaVersion): NameExtraction = {
    if (version >= JSONSchemaDraft201909SchemaVersion) Draft2019NameExtraction
    else Draft4NameExtraction
  }
}
