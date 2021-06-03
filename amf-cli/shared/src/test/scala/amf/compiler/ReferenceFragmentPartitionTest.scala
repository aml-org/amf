package amf.compiler

import amf.core.parser.ReferenceFragmentPartition
import org.scalatest.{Matchers, WordSpec}

class ReferenceFragmentPartitionTest extends WordSpec with Matchers{


  private case class ParsedFragmentUrl(base:String, path:String, fragment:Option[String])

  Seq(
    ParsedFragmentUrl("schema.xsd#fragment", "schema.xsd", Some("fragment")),
    ParsedFragmentUrl("schema.xsd#", "schema.xsd", None),
    ParsedFragmentUrl("#local/path", "#local/path", None),
    ParsedFragmentUrl("schema.xsd#fragment/other", "schema.xsd", Some("fragment/other")),
    ParsedFragmentUrl("file://schema.xsd#fragment/other", "file://schema.xsd", Some("fragment/other")),
    ParsedFragmentUrl("http://schema.xsd#fragment/other", "http://schema.xsd", Some("fragment/other")),
    ParsedFragmentUrl("file://schema.xsd#fragment#other", "file://schema.xsd#fragment", Some("other")),
    ParsedFragmentUrl("http://schema.xsd#fragment#other", "http://schema.xsd#fragment", Some("other")),
    ParsedFragmentUrl("schema.xsd#fragment#other", "schema.xsd#fragment", Some("other")),
    ParsedFragmentUrl("/schema.xsd#fragment", "/schema.xsd", Some("fragment")),
  ).foreach { pf =>

    s"The url ${pf.base}" when {
      val (path,fragment) = ReferenceFragmentPartition(pf.base)
      " has been parsed" should {
        s"should have path ${pf.path}" in {
          path should be(pf.path)
        }
        pf.fragment match {
          case Some(expectedF) =>
            s" and fragment should be present and equals to $expectedF" in {
              fragment.isDefined should be(true)
              fragment.get should be(expectedF)
            }
          case _ =>
        }
      }
    }
  }

}
