import org.mulesoft.typings.generation.ScalaClassFilterBuilder
import org.mulesoft.typings.plugin.ScalaJsTypingsPlugin.autoImport.{
  customMappings,
  namespaceReplacer,
  namespaceTopLevelExports,
  scalaFilteredClasses,
  typingModuleName
}
import org.mulesoft.typings.resolution.BuiltInMappings.{dictionary, option, overwrite}
import org.mulesoft.typings.resolution.MappingFactory
import org.mulesoft.typings.resolution.namespace.PrefixNamespaceReplacer
import sbt.SettingsDefinition

object TypingGenerationSettings {

  val settings: Seq[SettingsDefinition] = Seq(
    typingModuleName := typingsModuleName,
    customMappings := typingsCustomMappings,
    namespaceReplacer := typingsNamespaceReplacer,
    scalaFilteredClasses := typingsFilters,
    namespaceTopLevelExports := false
  )

  def typingsModuleName: String = "amf-client-js"

  def typingsCustomMappings: MappingFactory =
    MappingFactory()
      .map("ClientList")
      .to("Array")
      .map("ClientFuture")
      .to("Promise")
      .map("AmfCustomClass")
      .to("AnotherCustomClass")
      .map("ClientOption")
      .to(option())
      .map("ClientMap")
      .to(dictionary())
      .map("AnyVal")
      .to("any")
      .map("DocBuilder")
      .to(overwrite("JsOutputBuilder"))
      .map("Unit")
      .to("void")

  def typingsNamespaceReplacer: PrefixNamespaceReplacer = PrefixNamespaceReplacer("amf\\.client\\.", "")

  def typingsFilters: ScalaClassFilterBuilder =
    ScalaClassFilterBuilder()
      .withClassFilter("^.*\\.JsFs$")
      .withClassFilter("^.*\\.SysError$")
      .withClassFilter("^.*\\.Main.*$")
      .withClassFilter("^.*\\.Https$")
      .withClassFilter("^.*\\.Http$")
      .withClassFilter("^.*\\.LimitedStringBuffer$")
      .withClassFilter("^.*\\..*Platform.*$")
      .withClassFilter("^amf\\.graphql\\..*")
      .withClassFilter("^amf\\.grpc\\..*")
      .withClassFilter("^amf\\.antlr\\..*")
      .withClassFilter("^amf\\.rdf\\..*")
      .withClassFilter("^org\\.mulesoft\\.antlrast\\..*")
      .withTypeFilter("^.*$", "Option")
      .withTypeFilter("^.*$", "BaseExecutionEnvironment")
      .withTypeFilter("^.*$", "InputRange")
      .withTypeFilter("^.*$", "Seq")
      .withTypeFilter("^.*$", "ExecutionContext")
      .withTypeFilter("^.*$", "AmfObjectWrapper")
      .withTypeFilter("^.*$", "StringBuffer")
      .withMethodFilter("^.*\\.ValidationReport$", "toString")
}
