package amf.dialects

import amf.compiler.Root
import amf.metadata.Type
import amf.vocabulary.Namespace

/**
  * Created by Pavel Petrochenko on 14/09/17.
  */

class DialectLanguageNode(override val shortName:String, namespace: Namespace = Namespace.Meta) extends DialectNode(shortName, namespace) {
   def refMap(name:String): DialectPropertyMapping = map(name, NodeReference.name, NodeReference).copy(required = true)
}

object VocabImport extends DialectLanguageNode("VocabImport") {
  val name: DialectPropertyMapping = str("name")
  val uri: DialectPropertyMapping  = str("uri", _.copy(fromVal = true))
}
object NodeReference extends DialectLanguageNode("Declaration") {
  val name: DialectPropertyMapping = str("name")
  val uri: DialectPropertyMapping  = str("declaredNode", _.copy(referenceTarget = Some(NodeDefinition), fromVal = true))
}

object ModuleDeclaration extends DialectLanguageNode("ModuleDeclaration") {
  val declares: DialectPropertyMapping = refMap("declares")
}

object PropertyMapping extends DialectLanguageNode("PropertyMapping") {
  val name: DialectPropertyMapping          = str("name", _.copy(noRAML = true))
  val propertyTerm: DialectPropertyMapping  = str("propertyTerm", _.copy(referenceTarget = Some(PropertyTerm), required = true))
  val mandatory: DialectPropertyMapping     = bool("mandatory")
  val enum: DialectPropertyMapping          = str("enum", _.copy(collection =  true))
  val pattern: DialectPropertyMapping       = str("pattern")
  val minimum: DialectPropertyMapping       = str("minimum")
  val maximum: DialectPropertyMapping       = str("maximum")
  val range: DialectPropertyMapping         = str("range", _.copy(referenceTarget = Some(NodeDefinition)))
  val allowMultiple: DialectPropertyMapping = bool("allowMultiple")
  val asMap: DialectPropertyMapping         = bool("asMap")
  val hash: DialectPropertyMapping          = str("hash")
}

object ClassTermRef extends DialectLanguageNode("ClassTermRef") {
  val name: DialectPropertyMapping  = str("name")
  val value: DialectPropertyMapping = ref("value",ClassTerm, _.copy(fromVal = true))
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
  val traitProperty: DialectPropertyMapping = str("is")

  override val keyProperty = Some(name)
}

object FragmentDeclaration extends DialectLanguageNode("FragmentsDeclaration") {
  val encodes: DialectPropertyMapping = refMap("encodes")
}
object DocumentEncode extends DialectLanguageNode("DocumentContentDeclaration") {
  val declares: DialectPropertyMapping = refMap("declares")
  var encodes: DialectPropertyMapping  = str("encodes", _.copy(referenceTarget = Some(NodeDefinition), required = true))
}

object MainNode extends DialectLanguageNode("Document"){
  val document: DialectPropertyMapping = obj("document", DocumentEncode, _.copy(required = true))
  val module: DialectPropertyMapping   = obj("module", ModuleDeclaration, _.copy(required = true))
  val fragment: DialectPropertyMapping = obj("fragments", FragmentDeclaration, _.copy(required = true))
}

object DialectDefinition extends DialectLanguageNode("dialect") {

  val dialectProperty: DialectPropertyMapping  = str("dialect")
  val version: DialectPropertyMapping          = str("version")
  var usage: DialectPropertyMapping            = str("usage", _.copy(namespace = Some(Namespace.Schema), rdfName = Some("description")))
  var vocabularies: DialectPropertyMapping     = map("vocabularies", External.name, External)
  var externals: DialectPropertyMapping        = map("external", External.name, External)
  var nodeMappings: DialectPropertyMapping     = map("nodeMappings", NodeDefinition.name, NodeDefinition, _.copy(isDeclaration = true))
  var raml: DialectPropertyMapping             = obj("raml", MainNode, _.copy(required = true))

  nameProvider = {
    val localNameProvider: LocalNameProviderFactory = new BasicNameProvider(_, List(externals, vocabularies))
    Some(localNameProvider)
  }
}

case class DialectLanguageResolver(override val root:Root) extends BasicResolver(root, List(DialectDefinition.externals,DialectDefinition.vocabularies)){

  override def resolve(root: Root, name:String, t:Type): Option[String] = {
    t match {
      case NodeDefinition if b2id.get(name).isDefined => Some(b2id(name))
      case _                                          => Some(resolveBasicRef(name,root))
    }
  }
}

object DialectLanguageDefinition extends Dialect("RAML 1.0 Dialect", "", DialectDefinition, r => { DialectLanguageResolver(r) } ){}
