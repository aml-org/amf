package amf.dialects

import amf.compiler.Root
import amf.document.BaseUnit
import amf.metadata.Type
import amf.spec.dialects._
import amf.validation.Validation
import amf.vocabulary.Namespace

/**
  * Created by Pavel Petrochenko on 14/09/17.
  */
class DialectLanguageNode(override val shortName: String, namespace: Namespace = Namespace.Meta)
    extends DialectNode(shortName, namespace) {
  id = Some((namespace + shortName).iri())
  def refMap(name: String, isDeclaration: Boolean, required: Boolean = true): DialectPropertyMapping = map(name, NodeReference.idProperty, NodeReference, _.copy(required = required, isDeclaration = isDeclaration))
}

object VocabImport extends DialectLanguageNode("VocabImport") {
  val name: DialectPropertyMapping = str("name")
  val uri: DialectPropertyMapping  = str("uri", _.copy(fromVal = true))
}
object NodeReference extends DialectLanguageNode("Declaration") {
  // val name: DialectPropertyMapping = str("name")
  val idProperty: DialectPropertyMapping = str(
    "id",
    _.copy(namespace = Some(Namespace.Schema), jsonld = false, noRAML = true, scalaNameOverride = Some("idProperty")))
  val uri: DialectPropertyMapping = iri(
    "declaredNode",
    _.copy(referenceTarget = Some(NodeDefinition), fromVal = true, scalaNameOverride = Some("uri")))
}

object ModuleDeclaration extends DialectLanguageNode("ModuleDeclaration") {
  val declares: DialectPropertyMapping = refMap("declares", isDeclaration = true)
}

object PropertyMapping extends DialectLanguageNode("PropertyMapping") {
  val name: DialectPropertyMapping = str("name", _.copy(noRAML = true))
  val propertyTerm: DialectPropertyMapping =
    iri("propertyTerm", _.copy(referenceTarget = Some(PropertyTerm), required = true))
  val mandatory: DialectPropertyMapping = bool("mandatory")
  val enum: DialectPropertyMapping      = str("enum", _.copy(collection = true))
  val pattern: DialectPropertyMapping   = str("pattern")
  val minimum: DialectPropertyMapping   = str("minimum")
  val maximum: DialectPropertyMapping   = str("maximum")
  val range: DialectPropertyMapping =
    iri("range", _.copy(referenceTarget = Some(NodeDefinition), allowInplace = true, collection = true))
  val allowMultiple: DialectPropertyMapping = bool("allowMultiple")
  val asMap: DialectPropertyMapping         = bool("asMap")
  val hash: DialectPropertyMapping          = iri("hash", _.copy(referenceTarget = Some(PropertyTerm)))
  val defaultValue: DialectPropertyMapping  = str("defaultValue")
}

object ClassTermRef extends DialectLanguageNode("ClassTermRef") {
  val name: DialectPropertyMapping  = str("name")
  val value: DialectPropertyMapping = ref("value", ClassTerm, _.copy(fromVal = true))
}

object ClassTermMap extends DialectLanguageNode("ClassTermMap") {
  val name: DialectPropertyMapping    = str("name")
  val label: DialectPropertyMapping   = str("label", _.copy(required = true))
  val default: DialectPropertyMapping = ref("default", ClassTerm)
  var values: DialectPropertyMapping  = map("values", ClassTermRef.name, ClassTermRef, _.copy(required = true))
}
object NodeDefinition extends DialectLanguageNode("NodeDefinition") {
  val name: DialectPropertyMapping          = str("name", _.copy(noRAML = true))
  val classTerm: DialectPropertyMapping     = ref("classTerm", ClassTerm, _.copy(required = true))
  val mapping: DialectPropertyMapping       = map("mapping", PropertyMapping.name, PropertyMapping)
  val classTermMap: DialectPropertyMapping  = map("classTermMap", ClassTermMap.name, ClassTerm)
  val traitProperty: DialectPropertyMapping = str("is", _.copy(scalaNameOverride = Some("traitProperty")))

  override val keyProperty = Some(name)
}

object FragmentDeclaration extends DialectLanguageNode("FragmentsDeclaration") {
  val encodes: DialectPropertyMapping = refMap("encodes", isDeclaration = false)
}
object DocumentEncode extends DialectLanguageNode("DocumentContentDeclaration") {
  val declares: DialectPropertyMapping = refMap("declares", isDeclaration = true, required = false)
  var encodes: DialectPropertyMapping = iri("encodes", _.copy(referenceTarget = Some(NodeDefinition), required = true, allowInplace = true))
}

object MainNode extends DialectLanguageNode("Document") {
  val document: DialectPropertyMapping = obj("document", DocumentEncode, _.copy(required = true))
  val module: DialectPropertyMapping   = obj("module", ModuleDeclaration, _.copy(required = false))
  val fragment: DialectPropertyMapping =
    obj("fragments", FragmentDeclaration, _.copy(required = false, scalaNameOverride = Some("fragment")))
}

object DialectDefinition extends DialectLanguageNode("dialect") {

  val dialectProperty: DialectPropertyMapping = str("dialect", _.copy(scalaNameOverride = Some("dialectProperty")))
  val version: DialectPropertyMapping         = str("version")
  var usage: DialectPropertyMapping =
    str("usage", _.copy(namespace = Some(Namespace.Schema), rdfName = Some("description")))
  var vocabularies: DialectPropertyMapping = map("vocabularies", External.name, External)
  var externals: DialectPropertyMapping =
    map("external", External.name, External, _.copy(scalaNameOverride = Some("externals")))
  var nodeMappings: DialectPropertyMapping =
    map("nodeMappings", NodeDefinition.name, NodeDefinition, _.copy(isDeclaration = true))
  var raml: DialectPropertyMapping = obj("raml", MainNode, _.copy(required = true))
  var uses: DialectPropertyMapping = str("uses", _.copy(required = false, jsonld = false))

  nameProvider = {
    val localNameProvider: LocalNameProviderFactory = new BasicNameProvider(_, List(externals, vocabularies))
    Some(localNameProvider)
  }
}
object DialectModuleDefinition extends DialectLanguageNode("module") {

  var usage: DialectPropertyMapping =
    str("usage", _.copy(namespace = Some(Namespace.Schema), rdfName = Some("description")))
  var vocabularies: DialectPropertyMapping = map("vocabularies", External.name, External)
  var externals: DialectPropertyMapping =
    map("external", External.name, External, _.copy(scalaNameOverride = Some("externals")))
  var nodeMappings: DialectPropertyMapping =
    map("nodeMappings", NodeDefinition.name, NodeDefinition, _.copy(isDeclaration = true))
  //var raml: DialectPropertyMapping = obj("raml", MainNode, _.copy(required = true))
  var uses: DialectPropertyMapping = str("uses", _.copy(required = false, jsonld = false))

  nameProvider = {
    val localNameProvider: LocalNameProviderFactory = new BasicNameProvider(_, List(externals, vocabularies))
    Some(localNameProvider)
  }
}

case class DialectLanguageResolver(root: Root, uses: Map[String, BaseUnit], currentValidation: Validation)
    extends BasicResolver(root, List(DialectDefinition.externals, DialectDefinition.vocabularies), uses, currentValidation) {

  override def resolve(root: Root, name: String, t: Type): Option[String] = {
    try {
      t match {
        case NodeDefinition if b2id.get(name).isDefined => Some(b2id(name))
        case _                                          => Some(resolveBasicRef(name))
      }
    } catch {
      case _: Exception => None
    }
  }
}


object DialectLanguageDefinition
    extends Dialect("RAML 1.0 Dialect", "", DialectDefinition, (r, uses, currentValidation) => { DialectLanguageResolver(r, uses, currentValidation) },module = Some(DialectModuleDefinition), fragments = Map().+(("DialectNode",NodeDefinition))){

}
