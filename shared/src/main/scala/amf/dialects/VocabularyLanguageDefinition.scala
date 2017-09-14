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

  nameProvider= (root: DomainEntity) => VocabularyNameProvider(root);
}

case class VocabularyNameProvider(root:DomainEntity) extends TypeBuiltins{

  val namespaces=mutable.Map[String,String]();

  {
    root.entities(Vocabulary.externals).foreach(e=>{
      namespaces.put(e.string(External.uri).get,e.string(External.name).get);
    })
  }

   override def localName(uri: String): String = {
        val ln=super.localName(uri);
        if (ln!=uri){
           return ln
        }
        if (uri.indexOf(root.id) > -1) {
          return uri.replace(root.id, "")
        } else {
            namespaces.find { case (p, v) =>
            uri.indexOf(p) > -1
          } match {
            case Some((p, v)) => return uri.replace(p, s"$v.")
            case res => return uri
          }
        }
        return uri;
   }
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

class VocabularyResolver(val root:Root) extends TypeBuiltins{

  val REGEX_URI = "^([a-z][a-z0-9+.-]*):(?://((?:(?=((?:[a-z0-9-._~!$&'()*+,;=:]|%[0-9A-F]{2})*))(\\3)@)?(?=([[0-9A-F:.]{2,}]|(?:[a-z0-9-._~!$&'()*+,;=]|%[0-9A-F]{2})*))\\5(?::(?=(\\d*))\\6)?)(\\/(?=((?:[a-z0-9-._~!$&'()*+,;=:@\\/]|%[0-9A-F]{2})*))\\8)?|(\\/?(?!\\/)(?=((?:[a-z0-9-._~!$&'()*+,;=:@\\/]|%[0-9A-F]{2})*))\\10)?)(?:\\?(?=((?:[a-z0-9-._~!$&'()*+,;=:@\\/?]|%[0-9A-F]{2})*))\\11)?(?:#(?=((?:[a-z0-9-._~!$&'()*+,;=:@\\/?]|%[0-9A-F]{2})*))\\12)?$"

  private var externalsMap:mutable.HashMap[String,String]=new mutable.HashMap();

  private var base:String=root.location + "#";

  def resolveBasicRef(name: String, root: Root):String = {
    if (name.indexOf(".") > -1) {
      name.split("\\.") match {
        case Array(alias, name) =>
          this.externalsMap.get(alias) match {
            case Some(resolved) => return s"$resolved${name}"
            case _              => throw new Exception(s"Cannot find prefix $name")
          }
        case _ => throw new Exception(s"Error in class/property name $name, multiple .")
      }
    } else {
      if (name.matches(REGEX_URI)) {
        name
      } else {
        s"$base$name"
      }
    }
  }

  initReferences(root);

  private def initReferences(root: Root) = {
    val ast = root.ast.last
    val entries = new Entries(ast)
    entries.key("external", e => {
        val entries = new Entries(e.value).entries
        entries.foreach { case (alias, entry) =>
          ValueNode(entry.value).string().value match {
            case prefix: String => externalsMap.put(alias, prefix)
          }
        }
      });
    entries.key("base", entry => {
        if (entry.value != null) {
          val value = ValueNode(entry.value)
          value.string().value match {
            case base: String => this.base = base;
          }
        }
    })
  }

  override def resolve(root: Root, name:String, t:Type):String={
    if (t==ClassTerm){
      val range = if (name != null) {
        val bid=super.resolve(root,name,t);
        if (bid!=null){
          return bid;
        }
      } else {
        return "http://www.w3.org/2001/XMLSchema#anyType"
      }
    }
    return resolveBasicRef(name,root);
  }
}

object VocabularyLanguageDefinition extends Dialect("Vocabulary",Vocabulary,resolver = (root: Root) => new VocabularyResolver(root)){
  refiner=new VocabularyRefiner()
}