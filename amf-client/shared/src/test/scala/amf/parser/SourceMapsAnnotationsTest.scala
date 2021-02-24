package amf.parser

import amf.core.annotations.{SourceAST, SynthesizedField, VirtualNode}
import amf.core.client.ParsingOptions
import amf.core.metamodel.Field
import amf.core.metamodel.document.DocumentModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{AmfArray, AmfElement, AmfObject, AmfScalar}
import amf.core.parser.{Annotations, FieldEntry, Value}
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.remote.{AsyncYamlHint, Cache, Context, Hint, OasYamlHint, RamlYamlHint}
import amf.core.services.RuntimeCompiler
import amf.core.unsafe.PlatformSecrets
import amf.facades.{AMFCompiler, Validation}
import amf.io.BuildCycleTests
import amf.plugins.document.webapi.parser.spec.raml.expression.ExpressionMember
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
    checkElement(u)
  }

  private def checkElement(e: AmfElement): Seq[String] =
    e match {
      case o: AmfObject => checkObj(o)
      case a: AmfArray  => a.values.flatMap(checkElement)
      case _            => Seq()
    }

  private def checkObj(o: AmfObject) =
    o.fields
      .fields()
      .flatMap { fe =>
        checkInField(fe, o.meta.`type`.head.iri()) ++ (if (fe.isSynthesized) Nil else checkElement(fe.value.value))
      }
      .toSeq

  implicit class FieldEntryIm(fe: FieldEntry) {
    def isSynthesized: Boolean = fe.value.annotations.contains(classOf[SynthesizedField])

    def isExpression: Boolean = fe.value.annotations.find(_.isInstanceOf[ExpressionMember]).isDefined

    def fieldHasMaps(): Boolean = checkAnnotations(fe.value.annotations)

    def valueHasMaps(): Boolean = checkAnnotations(fe.value.value.annotations)

    private def checkAnnotations(a: Annotations): Boolean =
      a.contains(classOf[SourceAST]) || a.find(_.isInstanceOf[VirtualNode]).isDefined

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

}
