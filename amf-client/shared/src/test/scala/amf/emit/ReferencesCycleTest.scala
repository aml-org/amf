package amf.emit

import amf.client.render.RenderOptions
import amf.common.ListAssertions
import amf.common.Tests.checkDiff
import amf.compiler.CompilerTestBuilder
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.facades.AMFDumper
import amf.io.BuildCycleTests
import org.mulesoft.common.io.AsyncFile

import scala.concurrent.Future

/**
  * Created by hernan.najles on 9/19/17.
  */
class ReferencesCycleTest extends BuildCycleTests with ListAssertions with CompilerTestBuilder {

  override val basePath = "amf-client/shared/src/test/resources/references/"

  private val fixture = Seq(
    "Simple library raml"                         -> ("libraries.raml", RamlYamlHint)                -> ("lib/lib.raml", Raml),
    "Simple library oas"                          -> ("libraries.json", OasJsonHint)                 -> ("lib/lib.json", Oas),
    "Library raml to oas"                         -> ("libraries.raml", RamlJsonHint)                -> ("lib/lib.raml.json", Oas),
    "Library oas to raml"                         -> ("libraries.json", OasJsonHint)                 -> ("lib/lib.json.raml", Raml),
    "Library raml to amf"                         -> ("libraries.raml", RamlJsonHint)                -> ("lib/lib.raml.jsonld", Amf),
    "Library oas to amf"                          -> ("libraries.json", OasJsonHint)                 -> ("lib/lib.json.jsonld", Amf),
    "Library amf to oas from include"             -> ("libraries.json.jsonld", AmfJsonHint)          -> ("lib/lib.json", Oas),
    "Library amf to raml from include"            -> ("libraries.raml.jsonld", AmfJsonHint)          -> ("lib/lib.raml", Raml),
    "Data type fragment raml to raml"             -> ("data-type-fragment.raml", RamlYamlHint)       -> ("fragments/person.raml", Raml),
    "Data type fragment oas to oas"               -> ("data-type-fragment.json", OasJsonHint)        -> ("fragments/person.json", Oas),
    "Data type fragment raml to oas"              -> ("data-type-fragment.raml", RamlJsonHint)       -> ("fragments/person.json", Oas),
    "Data type fragment oas to raml"              -> ("data-type-fragment.json", OasJsonHint)        -> ("fragments/person.json.raml", Raml),
    "Data type fragment amf to raml from include" -> ("data-type-fragment.raml.jsonld", AmfJsonHint) -> ("fragments/person.raml", Raml),
    "Data type fragment amf to oas from include"  -> ("data-type-fragment.json.jsonld", AmfJsonHint) -> ("fragments/person.json", Oas),
    "Resource type fragment raml to raml"         -> ("resource-type-fragment.raml", RamlYamlHint)   -> ("fragments/resource-type.raml", Raml),
    "Trait fragment raml to raml"                 -> ("trait-fragment.raml", RamlYamlHint)           -> ("fragments/trait.raml", Raml),
    "Alias library reference raml test"           -> ("lib-alias-reference.raml", RamlYamlHint)      -> ("lib/lib-declaration.raml", Raml),
    "Security schemes fragment raml to raml"      -> ("security-scheme-fragment.raml", RamlYamlHint) -> ("fragments/security-scheme.raml", Raml),
    "Security schemes fragment oas to oas"        -> ("security-scheme-fragment.json", OasJsonHint)  -> ("fragments/security-scheme.json", Oas),
    "Named Example fragment raml to raml"         -> ("named-example.raml", RamlYamlHint)            -> ("fragments/named-example.raml", Raml),
    "Named Example fragment oas to oas"           -> ("named-example.json", OasJsonHint)             -> ("fragments/named-example.json", Oas)
  )

  fixture.foreach {
    case ((title, (document, hint)), (reference, vendor)) =>
      test(title) {

        build(s"file://$basePath$document", hint)
          .flatMap(renderReference(reference, vendor, _))
          .flatMap(checkDiff(_, fs.asyncFile(basePath + reference)))
      }
  }

  private def renderReference(reference: String, vendor: Vendor, unit: BaseUnit): Future[AsyncFile] = {
    val ref    = unit.references.head
    val actual = fs.asyncFile(tmp(reference.replace("/", "--")))
    actual
      .write(AMFDumper(ref, vendor, vendor.defaultSyntax, RenderOptions().withSourceMaps).dumpToString)
      .map(_ => actual)
  }

  case class ModuleContent(url: String, content: String)

}
