package amf.convert

import amf.client.AMF
import amf.core.parser.Range
import amf.{RAML08Profile, RAMLStyle}

class JvmWrapperTests extends WrapperTests with NativeOpsFromJvm {

  test("Handle 404 status code while fetching included file") {
    for {
      _ <- AMF.init().asFuture
      a <- AMF
        .raml08Parser()
        .parseFileAsync(
          "file://amf-client/shared/src/test/resources/parser-results/raml/error/not-existing-http-include.raml")
        .asFuture
      r <- AMF.validate(a, RAML08Profile, RAMLStyle).asFuture
    } yield {
      r.conforms should be(false)
      val seq = r.results.asSeq
      seq.size should be(2)
      val statusCode = seq.head
      statusCode.level should be("Violation")

      // hack to avoid that this test fail when you don't have internet connection.If you have internet, the a.ml domain will return an 404 error,
      // but if you dont have internet connection, you will not reach the a.ml host, so it will be an unknown host exception violation.

      statusCode.message should (endWith("Unhandled status code 404 => https://a.ml/notexists") or
        endWith("java.net.UnknownHostException: a.ml") or
        endWith("java.net.SocketTimeoutException: connect timed out") or
        endWith(
          "javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target"))
      statusCode.position should be(Range((6, 10), (6, 41)))

      val unresolvedRef = seq.last
      unresolvedRef.level should be("Violation")
      unresolvedRef.message should startWith("Unresolved reference 'https://a.ml/notexists' from root context")
      unresolvedRef.position should be(Range((6, 10), (6, 41)))
    }
  }

}
