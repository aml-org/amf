package amf.adopter

import amf.adopter.IdAssertion.UniqueDeclaresIds
import amf.apicontract.client.scala.APIConfiguration
import amf.core.client.scala.model.document.Document
import org.scalatest.Assertion
import org.scalatest.Assertions.{fail, succeed}
import org.scalatest.funsuite.AsyncFunSuite

import scala.concurrent.{ExecutionContext, Future}

/**
  * suite that verifies that ids are defined correctly
  */
class ParsedModelIdConformityTest extends AsyncFunSuite {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val tests = Seq(
    UniqueDeclaresIds -> "file://amf-cli/shared/src/test/resources/validations/oas3/declarations-same-name.json"
  )

  tests.foreach {
    case (idAssertion, path) =>
      test(s"Id conformity - ${idAssertion.label} in $path") { validateParsedModel(path, idAssertion.assertion) }
  }

  private def validateParsedModel(path: String, assertion: Document => Assertion): Future[Assertion] = {
    val client = APIConfiguration.API().baseUnitClient()
    for {
      parseResult <- client.parseDocument(path)
    } yield {
      assertion(parseResult.document)
    }
  }
}

case class IdAssertion(label: String, assertion: Document => Assertion)

object IdAssertion {
  val UniqueDeclaresIds: IdAssertion = IdAssertion(
    "declares path defined",
    doc => {
      val declaresIds = doc.declares.map(_.id)
      declaresIds.foreach { id =>
        if (id.endsWith("_1")) fail(s"element defined in declares does not have a unique id: $id")
      }
      succeed
    }
  )

}
