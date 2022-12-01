package amf.parser

import amf.apicontract.client.scala.{AMFConfiguration, APIConfiguration}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{AmfArray, AmfElement, AmfObject}
import amf.core.client.scala.parse.AMFParser
import amf.core.internal.annotations._
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.document.DocumentModel
import amf.core.internal.parser.domain.{Annotations, FieldEntry}
import amf.core.internal.remote._
import amf.core.internal.unsafe.PlatformSecrets
import amf.graphql.client.scala.GraphQLConfiguration
import amf.graphqlfederation.client.scala.GraphQLFederationConfiguration
import amf.shapes.internal.spec.raml.parser.expression.ExpressionMember
import org.mulesoft.common.client.lexical.PositionRange
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite

import scala.collection.GenTraversableOnce
import scala.concurrent.{ExecutionContext, Future}

/** iterates the BaseUnit checking if both field and value have the SourceAST or a VirtualNode annotation. The value may
  * not be checked if its synthesized or an expression.
  */
class SourceMapsAnnotationsTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  private val directory: String                            = "amf-cli/shared/src/test/resources/parser/annotations/"

  test("Test raml 1.0 annotations") {
    runTest("raml10.raml")
  }

  test("Test raml 0.8 annotations") {
    runTest("raml08.raml")
  }

  test("Test oas 2.0 annotations") {
    runTest("oas20.yaml")
  }

  test("Test oas 3.0 annotations") {
    runTest("oas30.yaml")
  }

  test("Test Async 2.0 annotations") {
    runTest("async2.yaml")
  }

  ignore("Test GraphQL annotations") {
    runTest("graphql.graphql", Some(GraphQLConfiguration.GraphQL()))
  }

  ignore("Test GraphQLFederation annotations") {
    runTest("federation.graphql", Some(GraphQLFederationConfiguration.GraphQLFederation()))
  }

  private def parse(file: String, config: Option[AMFConfiguration] = None): Future[BaseUnit] = {
    val url           = s"file://$directory$file"
    val configuration = config.getOrElse(APIConfiguration.API())
    AMFParser.parse(url, configuration).map(_.baseUnit)
  }

  private def runTest(file: String, config: Option[AMFConfiguration] = None): Future[Assertion] =
    parse(file, config).map { bu =>
      val strings = checkUnit(bu)
      if (strings.isEmpty) succeed
      else {
        fail("Missing annotations over:\n" + strings.mkString("\n"))
      }
    }

  private def orderResults(results: Seq[String]): Set[String] = results.sorted.toSet

  private def checkUnit(u: BaseUnit): Set[String] = {
    u.fields.filter({ case (f, _) => f == DocumentModel.Encodes || f == DocumentModel.Declares })
    val results = checkElement(u, RangesChecker(None))
    orderResults(results)
  }

  private def checkElement(e: AmfElement, rangesChecker: RangesChecker): Seq[String] =
    e match {
      case o: AmfObject =>
        if (rangesChecker.insideSynthesized) Seq() // fail?
        else
          rangesChecker.lastRange match {
            case Some(r) if o.range().forall(oR => r.contains(oR)) => checkObj(o, rangesChecker.on(o))
            case None                                              => checkObj(o, rangesChecker.on(o))
            case r                                                 => Seq() // Seq(rangeNotContained(o, r, o.range()))
          }
      case a: AmfArray =>
        if (rangesChecker.insideSynthesized) Seq() // fail?
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
    if (rangesChecker.insideSynthesized || rangesChecker.lastRange.isEmpty) Seq()
    else {
      if (!fe.range().forall(fR => rangesChecker.lastRange.exists(_.contains(fR))))
        Seq() // Seq(rangeNotContained(fe.field, meta, rangesChecker.lastRange, fe.range()))
      else Seq()
    }

  private def checkInValue(fe: FieldEntry, meta: String, rangesChecker: RangesChecker): Seq[String] = {
    if (fe.isSynthesized) Nil
    else checkFeRange(fe, meta, rangesChecker) ++ checkElement(fe.value.value, rangesChecker.on(fe.value.value))
  }

  private def checkObj(o: AmfObject, rangesChecker: RangesChecker): Seq[String] =
    o.fields
      .fields()
      .flatMap { fe: FieldEntry =>
        val meta = o.meta.`type`.head.iri()
        checkInField(fe, meta) ++ checkInValue(fe, meta, rangesChecker)
      }
      .toSeq

  implicit class FieldEntryIm(fe: FieldEntry) {
    def isSynthesized: Boolean = fe.value.annotations.isSynthesized

    def isExpression: Boolean = fe.value.annotations.find(_.isInstanceOf[ExpressionMember]).isDefined

    def valueShouldHaveMaps(): Boolean = !fe.isSynthesized && !fe.isExpression

    def fieldHasMaps(): Boolean = hasAnnotations(fe.value.annotations)

    def valueHasMaps(): Boolean = hasAnnotations(fe.value.value.annotations)

    private def hasAnnotations(a: Annotations): Boolean =
      a.contains(classOf[SourceAST]) || a.find(_.isInstanceOf[VirtualNode]).isDefined

    def range(): Option[PositionRange] =
      fe.value.annotations.find(classOf[SourceYPart]).map(_.ast.range)
  }

  implicit class AmfElementIm(e: AmfElement) {
    def range(): Option[PositionRange] =
      e.annotations.find(classOf[SourceYPart]).map(_.ast.range)
  }

  implicit class PositionRangeImp(range: PositionRange) {
    def contains(other: PositionRange): Boolean =
      (range.lineFrom < other.lineFrom || (range.lineFrom == other.lineFrom && range.columnFrom <= other.columnFrom)) &&
        (range.lineTo > other.lineTo || (range.lineTo == other.lineTo && range.columnTo >= other.columnTo))
  }

  private def checkInField(fe: FieldEntry, meta: String): Seq[String] = {
    val fieldStrings = if (!fe.fieldHasMaps()) Some(missingField(fe.field, meta)) else None
    val valueStrings =
      if (fe.valueShouldHaveMaps() && !fe.valueHasMaps()) Some(missingValue(fe.field, meta)) else None
    (fieldStrings ++ valueStrings).toSeq
  }

  private def missingField(f: Field, meta: String) =
    s"missing annotations for field ${f.value.iri()} at obj type $meta"

  private def missingValue(f: Field, meta: String) =
    s"missing annotations for value of field ${f.value.iri()} at obj type $meta"

  private def rangeNotContained(e: AmfElement, parent: Option[PositionRange], self: Option[PositionRange]) =
    s"range is not contained in parent $e [$parent - $self]"

  private def rangeNotContained(f: Field, meta: String, parent: Option[PositionRange], self: Option[PositionRange]) =
    s"range is not contained in parent [$parent - $self] of field ${f.value.iri()} at obj type $meta"

  case class RangesChecker(lastRange: Option[PositionRange], insideSynthesized: Boolean = false) {
    def on(e: AmfElement): RangesChecker =
      if (insideSynthesized) this
      else if (e.annotations.isInferred) this
      else if (e.annotations.isSynthesized) RangesChecker(lastRange, insideSynthesized = true)
      else RangesChecker(e.range())
  }
}
