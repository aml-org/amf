package amf.adopter

import amf.apicontract.client.scala._
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.document.FieldsFilter.All
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.traversal.iterator.{AmfElementStrategy, InstanceCollector}
import amf.core.internal.remote.Spec
import org.mulesoft.common.collections.FilterType
import org.scalatest.Assertion
import org.scalatest.Assertions.{fail, succeed}
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

class ParsingDuplicateIdsTest extends AsyncFunSuite with DuplicateIdsTest {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val apiPaths = Seq(
    "file://amf-cli/shared/src/test/resources/validations/reference-jsonschema-property/bad-link.raml",
    "file://amf-cli/shared/src/test/resources/validations/oas3/declarations-same-name.json"
  )

  apiPaths.foreach { path =>
    test(s"Test duplicate ids for parsed $path") { validateParsedModel(path) }
  }

  private def validateParsedModel(path: String): Future[Assertion] = {
    val client = APIConfiguration.API().baseUnitClient()
    for {
      parseResult <- client.parse(path)
    } yield {
      validateIds(parseResult.baseUnit)
    }
  }
}

class ResolvedModelDuplicateIdsTest extends AsyncFunSuite with DuplicateIdsTest {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val apiPaths = Seq(
    "file://amf-cli/shared/src/test/resources/validations/discriminator/discriminator-array-items.raml",
    "file://amf-cli/shared/src/test/resources/production/enum-id-with-applied-trait/api.raml"
  )

  apiPaths.foreach { path =>
    test(s"Test duplicate ids for resolved $path") { validateParsedModel(path) }
  }

  private def validateParsedModel(path: String): Future[Assertion] = {
    val client = APIConfiguration.API().baseUnitClient()
    for {
      parseResult <- client.parse(path)
      transformResult <- Future.successful(
        amfConfigFrom(parseResult.sourceSpec).baseUnitClient().transform(parseResult.baseUnit, PipelineId.Cache))
    } yield {
      validateIds(transformResult.baseUnit)
    }
  }

  private def amfConfigFrom(spec: Spec): AMFConfiguration = spec match {
    case Spec.OAS30   => OASConfiguration.OAS30()
    case Spec.OAS20   => OASConfiguration.OAS20()
    case Spec.RAML10  => RAMLConfiguration.RAML10()
    case Spec.RAML08  => RAMLConfiguration.RAML08()
    case Spec.ASYNC20 => AsyncAPIConfiguration.Async20()
    case _            => throw new IllegalArgumentException
  }
}

trait DuplicateIdsTest {

  def validateIds(unit: BaseUnit): Assertion = {
    val elements = unit
      .iterator(strategy = AmfElementStrategy, fieldsFilter = All, visited = InstanceCollector())
      .toList
    val objs: Seq[AmfObject] = elements.filterType[AmfObject]
    validateUndefinedIds(objs)
    validateDuplicateIds(objs)
    succeed // all ids where defined and not duplicated
  }

  private def validateDuplicateIds(objs: Seq[AmfObject]): Unit = {
    val groupedById = objs.groupBy(_.id)
    groupedById.foreach {
      case (id, elems) =>
        if (elems.size > 1)
          fail(s"Duplicate id: $id")
    }
  }

  private def validateUndefinedIds(objs: Seq[AmfObject]): Unit = {
    objs.filter(_.id == null).foreach(obj => fail(s"Undefined id was found in $obj"))
  }
}
