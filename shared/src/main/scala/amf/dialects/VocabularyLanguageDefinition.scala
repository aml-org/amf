package amf.dialects

import amf.compiler.Root
import amf.metadata.Type
import amf.spec.dialect.DomainEntity
import amf.spec.raml.{Entries, ValueNode}
import amf.vocabulary.{Namespace, ValueType}

import scala.collection.mutable
/**
* Created by kor on 12/09/17.
*/

class VocabPartDialect(override val shortName:String,namespace: Namespace=Namespace.Meta) extends DialectNode(namespace,shortName){}

case class Declaration(override val shortName:String,override val namespace: Namespace=Namespace.Meta) extends VocabPartDialect(shortName,namespace = namespace){
  val id=str("id").namespace(Namespace.Schema).noJsonLd.noRAML;
}


object ClassTerm extends Declaration("Class",Namespace.Owl){
  val displayName=str("displayName").namespace(Namespace.Schema).rdfName("name")
  val description=str("description").namespace(Namespace.Schema)

  val `extends`= ref("extends",ClassTerm).collection.noJsonLd();
  val `properties`= ref("properties",PropertyTerm).collection.noJsonLd();

  withCalculator((d: DomainEntity) => d.strings(`extends`).map(ValueType(_).asInstanceOf[ValueType]).toList)

}

object PropertyTerm extends Declaration("Property"){
  val description=str("description").namespace(Namespace.Schema)

  val domain= ref("domain",ClassTerm).collection.namespace(Namespace.Rdfs);

  val range=ref("range",ClassTerm).collection.ref(ClassTerm).namespace(Namespace.Rdfs);

  val `extends`= ref("extends",PropertyTerm).collection;

  val DATATYPE_PROPERTY = ValueType("http://www.w3.org/2002/07/owl#DatatypeProperty")
  val OBJECT_PROPERTY   = ValueType("http://www.w3.org/2002/07/owl#ObjectProperty")


  fieldValueDescriminator(range)
    .add(TypeBuiltins.ANY,DATATYPE_PROPERTY)
    .add(TypeBuiltins.STRING,DATATYPE_PROPERTY)
    .add(TypeBuiltins.INTEGER,DATATYPE_PROPERTY)
    .add(TypeBuiltins.NUMBER,DATATYPE_PROPERTY)
    .add(TypeBuiltins.FLOAT,DATATYPE_PROPERTY)
    .add(TypeBuiltins.BOOLEAN,DATATYPE_PROPERTY)
    .add(TypeBuiltins.URI,DATATYPE_PROPERTY)
    .defaultValue=OBJECT_PROPERTY;
}

object External extends VocabPartDialect("External"){
  val name=str("name");
  val uri=str("uri").value;
}

object Vocabulary extends VocabPartDialect("Vocabulary"){

  val base=str("base");
  val dialect=str("dialect")
  val version=str("version")
  var usage=str("usage").namespace(Namespace.Schema).rdfName("description")

  var externals=map("external",External.name,External);

  var classTerms=map("classTerms",ClassTerm.id,ClassTerm).rdfName("classes");
  var propertyTerms=map("propertyTerms",PropertyTerm.id,PropertyTerm).rdfName("properties");

  withGlobalIdField("base")
  withType("http://www.w3.org/2002/07/owl#Ontology");

  nameProvider= (root: DomainEntity) => new BasicNameProvider(root,List(externals));
}

class VocabularyRefiner extends Refiner{

  def refine(voc:DomainEntity){
      voc.entities(Vocabulary.classTerms).foreach(clazz=>{
          clazz.strings(ClassTerm.`properties`).foreach(p=>{
            voc.mapElementWithId(Vocabulary.propertyTerms,p).foreach(pt=>{
                pt.addValue(PropertyTerm.domain,clazz.id);
            })
          })
      })
  }
}

object VocabularyLanguageDefinition extends Dialect("Vocabulary",Vocabulary,resolver = (root: Root) => new BasicResolver(root,List(Vocabulary.externals))){
  refiner=new VocabularyRefiner()
}