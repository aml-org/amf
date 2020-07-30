package amf.compiler

import amf.core.annotations.LexicalInformation
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Annotation
import amf.core.parser.{Annotations, Range}
import amf.core.remote.RamlYamlHint
import amf.core.traversal.iterator.AmfElementStrategy
import org.scalatest.{Assertion, AsyncFlatSpec, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class RamlTypeExpressionAnnotationTest extends AsyncFlatSpec with Matchers with CompilerTestBuilder {
  override implicit def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.global

  val rootPath = "file://amf-client/shared/src/test/resources/nodes-annotations-examples/raml-type-expressions"

  behavior of "RAML type expression annotations"

  private val aArray       = LexicalInformation(Range((7, 10), (7, 15)))
  private val aUnionA      = LexicalInformation(Range((8, 10), (8, 16)))
  private val aUnionB      = LexicalInformation(Range((8, 19), (8, 21)))
  private val aUnion2A     = LexicalInformation(Range((9, 11), (9, 17)))
  private val aUnion2B     = LexicalInformation(Range((9, 24), (9, 26)))
  private val tripleUnionA = LexicalInformation(Range((10, 16), (10, 21)))
  private val tripleUnionB = LexicalInformation(Range((10, 24), (10, 29)))
  private val tripleUnionC = LexicalInformation(Range((10, 32), (10, 34)))
  private val unionArrayA  = LexicalInformation(Range((11, 15), (11, 20)))
  private val unionArrayB  = LexicalInformation(Range((11, 23), (11, 28)))

  private val fBu: Future[BaseUnit] = build(s"$rootPath/many-expressions-01.raml", RamlYamlHint)

  it should "annotate range for simple array" in {
    test(aArray)
  }

  it should "annotate range for simple union" in {
    test(aUnionA)
    test(aUnionB)
  }

  it should "annotate range for union with array element" in {
    test(aUnion2A)
    test(aUnion2B)
  }

  // todo: Should work when APIMF-2323 is solved
  ignore should "annotate range for union with three elements" in {
    test(tripleUnionA)
    test(tripleUnionB)
    test(tripleUnionC)
  }

  // todo: Should work when APIMF-2323 is solved
  ignore should "annotate ranges in an array of unions" in {
    test(unionArrayA)
    test(unionArrayB)
  }

  private def test(a: Annotation): Future[Assertion] =
    for {
      bu <- fBu
    } yield {
      assert(bu.containsAnnotation(a))
    }

  implicit class AnnotationsImpl(bu: BaseUnit) {

    private lazy val flattenAnnotations: List[Annotations] =
      bu.iterator(AmfElementStrategy).map(_.annotations).toList

    private lazy val flattenLexical: List[LexicalInformation] =
      flattenAnnotations.flatMap(_.find(classOf[LexicalInformation]))

    def containsAnnotation(a: Annotation): Boolean =
      flattenLexical.contains(a)
  }
}
