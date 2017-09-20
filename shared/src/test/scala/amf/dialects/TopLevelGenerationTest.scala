package amf.dialects

import amf.common.Tests.checkDiff
import amf.unsafe.PlatformSecrets
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class TopLevelGenerationTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath="file://shared/src/test/resources/vocabularies/"



  test("Generate Vocabulary Domain Objects"){

    val code= new TopLevelGenerator(VocabularyLanguageDefinition).generate()
    //platform.write("file://shared/src/main/scala/amf/dialects/VocabularyTopLevel.scala",code)
    val expected = platform
      .resolve("file://shared/src/main/scala/amf/dialects/VocabularyTopLevel.scala",None)
      .map(_.stream.toString)
    expected
      .zip(Future(code))
      .map(checkDiff)

  }

  test("Generate Class Domain Objects"){

    val code= new TopLevelGenerator(DialectLanguageDefinition).generate()
    //platform.write("file://shared/src/main/scala/amf/dialects/DialectTopLevel.scala",code)

    val expected = platform
      .resolve("file://shared/src/main/scala/amf/dialects/DialectTopLevel.scala",None)
      .map(_.stream.toString)
    expected
      .zip(Future(code))
      .map(checkDiff)

  }
}
