package amf.dialects

import amf.vocabulary.Namespace

/**
  * Created by kor on 14/09/17.
  */
object DialectLanguageDefinition extends Dialect("Dialect Definition",DialectDefinition,null){

}
class DialectLanguageNode(override val shortName:String,namespace: Namespace=Namespace.Meta) extends DialectNode(namespace,shortName){
   def refMap(name:String): DialectPropertyMapping =map(name,NodeReference.name,NodeReference).require();
}

object VocabImport extends DialectLanguageNode("VocabImport"){
  val name=str("name");
  val uri=str("uri").value;
}
object NodeReference extends DialectLanguageNode("Declaration"){

  val name=str("name");

  val uri=str("declaredNode").value;
}

object ModuleDeclaration extends DialectLanguageNode("ModuleDeclaration"){

  val declares=refMap("declares");
}
object PropertyMapping extends DialectLanguageNode("PropertyMapping"){

  val name=str("name");

  val mandatory=bool("mandatory")

  val propertyTerm=str("propertyTerm").ref(PropertyTerm).require();

  val enum=str("enum").collection()

  val pattern=str("pattern")

  val minimum=str("minimum")

  val maximum=str("maximum")

  val range=str("range").ref(NodeDefinition);
}
object ClassTermRef extends DialectLanguageNode("ClassTermRef"){
  val name=str("name");

  val value=ref("value",ClassTerm).value;
}
object ClassTermMap extends DialectLanguageNode("ClassTermMap"){
  val name=str("name");

  val label=str("label").require()
  val default=ref("default",ClassTerm)
  var values=map("values",ClassTermRef.name,ClassTermRef).require()
}
object NodeDefinition extends DialectLanguageNode("NodeDefinition"){

  val name=str("name");

  val classTerm=ref("classTerm",ClassTerm).require()

  val mapping=map("mapping",PropertyMapping.name,PropertyMapping);

  val classTermMap=map("classTermMap",ClassTermMap.name,ClassTerm);

  val traitProperty=str("is")
}

object FragmentDeclaration extends DialectLanguageNode("FragmentsDeclaration"){
  val encodes=refMap("encodes");
}
object DocumentEncode extends DialectLanguageNode("DocumentContentDeclaration"){

  val declares=refMap("declares");

  var encodes=str("encodes").require();
}

object MainNode extends DialectLanguageNode("Document"){

  val document=obj("document",DocumentEncode).require()

  val module=obj("module",ModuleDeclaration).require()

  val fragment=obj("fragment",FragmentDeclaration).require()

}

object DialectDefinition extends DialectLanguageNode("dialect"){

  val dialect=str("dialect")

  val version=str("version")


  var usage=str("usage").namespace(Namespace.Schema).rdfName("description")

  var externals=map("external",External.name,External);

  var vocabularies=map("vocabularies",External.name,External);

  var nodeMappings=map("nodeMappings",NodeDefinition.name,NodeDefinition);

  var raml=obj("raml",MainNode).require();
}