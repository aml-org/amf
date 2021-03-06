package amf.parser

import amf.core.annotations.{Inferred, SourceAST, SynthesizedField, VirtualNode}
import amf.core.metamodel.Field
import amf.core.metamodel.document.DocumentModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{AmfArray, AmfElement, AmfObject}
import amf.core.parser.{Annotations, FieldEntry}
import amf.core.remote._
import amf.core.services.RuntimeCompiler
import amf.core.unsafe.PlatformSecrets
import amf.facades.Validation
import amf.plugins.document.webapi.parser.spec.raml.expression.ExpressionMember
import org.mulesoft.lexer.InputRange
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class SourceMapsAnnotationsTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext = ExecutionContext.Implicits.global
  private val directory: String          = "amf-client/shared/src/test/resources/parser/annotations/"

  test("Test raml 1.0 annotations") {
    runTest("raml10.raml", RamlYamlHint)
  }

  test("Test raml 0.8 annotations") {
    runTest("raml08.raml", RamlYamlHint)
  }

  test("Test oas 2.0 annotations") {
    runTest("oas20.yaml", OasYamlHint)
  }

  test("Test oas 3.0 annotations") {
    runTest("oas30.yaml", OasYamlHint)
  }

  test("Test Async 2.0 annotations") {
    runTest("async2.yaml", AsyncYamlHint)
  }

  private def build(file: String, hint: Hint): Future[BaseUnit] =
    Validation(platform).flatMap { _ =>
      RuntimeCompiler(s"file://$directory$file", None, None, Context(platform), Cache())
    }

  private def runTest(file: String, hint: Hint): Future[Assertion] =
    build(file, hint).map { bu =>
      val strings = checkUnit(bu)
      if (strings.isEmpty) succeed
      else {
        fail("Missing annotations over:\n" + strings.mkString("\n"))
      }
    }

  private def checkUnit(u: BaseUnit): Seq[String] = {
    u.fields.filter({ case (f, _) => f == DocumentModel.Encodes || f == DocumentModel.Declares })
    checkElement(u, RangesChecker(None))
  }

  private def checkElement(e: AmfElement, rangesChecker: RangesChecker): Seq[String] =
    e match {
      case o: AmfObject =>
        if (rangesChecker.insideSynthetized)
          // fail?
          Seq()
        else
          rangesChecker.lastRange match {
            case Some(r) if o.range().forall(oR => r.contains(oR)) => checkObj(o, rangesChecker.on(o))
            case None                                              => checkObj(o, rangesChecker.on(o))
            case r                                                 => Seq() // Seq(rangeNotContained(o, r, o.range()))
          }
      case a: AmfArray =>
        if (rangesChecker.insideSynthetized)
          // fail?
          Seq()
        else
          rangesChecker.lastRange match {
            case Some(r) if a.range().forall(oR => r.contains(oR)) =>
              a.values.flatMap(checkElement(_, rangesChecker.on(a)))
            case None => a.values.flatMap(checkElement(_, rangesChecker.on(a)))
            case r    => Seq() // Seq(rangeNotContained(a, r, a.range()))
          }
      case _ => Seq()
    }

  private def checkFeRange(fe: FieldEntry, meta: String, rangesChecker: RangesChecker): Seq[String] =
    if (rangesChecker.insideSynthetized || rangesChecker.lastRange.isEmpty) Seq()
    else {
      if (!fe.range().forall(fR => rangesChecker.lastRange.exists(_.contains(fR))))
        Seq() // Seq(rangeNotContained(fe.field, meta, rangesChecker.lastRange, fe.range()))
      else Seq()
    }

  private def checkObj(o: AmfObject, rangesChecker: RangesChecker) =
    o.fields
      .fields()
      .flatMap { fe =>
        val meta = o.meta.`type`.head.iri()
        checkInField(fe, meta) ++ (if (fe.isSynthesized) Nil
                                   else
                                     checkFeRange(fe, meta, rangesChecker) ++
                                       checkElement(fe.value.value, rangesChecker.on(fe.value.value)))
      }
      .toSeq

  implicit class FieldEntryIm(fe: FieldEntry) {
    def isSynthesized: Boolean = fe.value.annotations.contains(classOf[SynthesizedField])

    def isExpression: Boolean = fe.value.annotations.find(_.isInstanceOf[ExpressionMember]).isDefined

    def fieldHasMaps(): Boolean = checkAnnotations(fe.value.annotations)

    def valueHasMaps(): Boolean = checkAnnotations(fe.value.value.annotations)

    private def checkAnnotations(a: Annotations): Boolean =
      a.contains(classOf[SourceAST]) || a.find(_.isInstanceOf[VirtualNode]).isDefined

    def range(): Option[InputRange] =
      fe.value.annotations.find(classOf[SourceAST]).map(_.ast.range)
  }

  implicit class AmfElementIm(e: AmfElement) {
    def range(): Option[InputRange] =
      e.annotations.find(classOf[SourceAST]).map(_.ast.range)
  }

  implicit class InputRangeIm(range: InputRange) {
    def contains(other: InputRange): Boolean =
      (range.lineFrom < other.lineFrom || (range.lineFrom == other.lineFrom && range.columnFrom <= other.columnFrom)) &&
        (range.lineTo > other.lineTo || (range.lineTo == other.lineTo && range.columnTo >= other.columnTo))
  }

  private def checkInField(fe: FieldEntry, meta: String) = {
    val f = if (!fe.fieldHasMaps()) Some(missingField(fe.field, meta)) else None
    val v =
      if (!fe.valueHasMaps() && !fe.isSynthesized && !fe.isExpression) Some(missingValue(fe.field, meta)) else None
    (f ++ v).toSeq
  }

  private def missingField(f: Field, meta: String) =
    s"missing annotations for field ${f.value.iri()} at obj type $meta"

  private def missingValue(f: Field, meta: String) =
    s"missing annotations for value of field ${f.value.iri()} at obj type $meta"

  private def rangeNotContained(e: AmfElement, parent: Option[InputRange], self: Option[InputRange]) =
    s"range is not contained in parent $e [$parent - $self]"

  private def rangeNotContained(f: Field, meta: String, parent: Option[InputRange], self: Option[InputRange]) =
    s"range is not contained in parent [$parent - $self] of field ${f.value.iri()} at obj type $meta"

  case class RangesChecker(lastRange: Option[InputRange], insideSynthetized: Boolean = false) {
    def on(e: AmfElement): RangesChecker =
      if (insideSynthetized) this
      else if (e.annotations.contains(classOf[Inferred])) this
      else if (e.annotations.contains(classOf[SynthesizedField])) RangesChecker(lastRange, true)
      else RangesChecker(e.range())
  }
}
