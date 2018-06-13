package amf.wrapper

import java.util
import java.util.concurrent.CompletableFuture

import amf.ProfileNames
import amf.client.AMF
import amf.client.convert.VocabulariesClientConverter._
import amf.core.parser.Range

import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters._
import scala.compat.java8.OptionConverters._
import scala.concurrent.Future

class JvmWrapperTests extends WrapperTests {

  override implicit def toNativeOption[E](client: ClientOption[E]): NativeOption[E]    = new JvmNativeOption[E](client)
  override implicit def toNativeList[E](client: ClientList[E]): JvmNativeList[E]       = new JvmNativeList(client)
  override implicit def toNativeFuture[T](client: ClientFuture[T]): JvmNativeFuture[T] = new JvmNativeFuture(client)

  protected class JvmNativeOption[E](list: ClientOption[E]) extends NativeOption[E] {
    override val native: util.Optional[E] = list.asInstanceOf[util.Optional[E]]
    override def asOption: Option[E]      = native.asScala
  }

  protected class JvmNativeList[E](list: ClientList[E]) extends NativeList[E] {
    override val native: util.List[E] = list.asInstanceOf[util.List[E]]
    override def asSeq: Seq[E]        = native.asScala
  }

  protected class JvmNativeFuture[T](future: ClientFuture[T]) extends NativeFuture[T] {
    override val native: CompletableFuture[T] = future.asInstanceOf[CompletableFuture[T]]
    override def asFuture: Future[T]          = native.toScala
  }

  test("Handle 404 status code while fetching included file") {
    for {
      _ <- AMF.init().asFuture
      a <- AMF
        .raml08Parser()
        .parseFileAsync(
          "file://amf-client/shared/src/test/resources/parser-results/error/not-existing-http-include.raml")
        .asFuture
      r <- AMF.validate(a, ProfileNames.RAML08, ProfileNames.RAML08).asFuture
    } yield {
      r.conforms should be(false)
      val seq = r.results.asSeq
      seq.size should be(2)
      val statusCode = seq.head
      statusCode.level should be("Violation")

      // hack to avoid that this test fail when you don't have internet connection.If you have internet, the raml.org domain will return an 404 error,
      // but if you dont have internet connection, you will not reach the raml.org host, so it will be an unknown host exception violation.

      statusCode.message should (endWith("Unhandled status code 404 => https://raml.org/notexists") or
        endWith("java.net.UnknownHostException: raml.org"))
      statusCode.position should be(Range((6, 10), (6, 45)))

      val unresolvedRef = seq.last
      unresolvedRef.level should be("Violation")
      unresolvedRef.message should startWith("Unresolved reference 'https://raml.org/notexists' from root context")
      unresolvedRef.position should be(Range((6, 10), (6, 45)))
    }
  }

}
