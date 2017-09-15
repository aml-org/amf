package amf.dialects

import amf.compiler.Root
import amf.metadata.{Field, Obj, Type}
import amf.model.{AmfArray, AmfScalar}
import amf.spec.dialect.DomainEntity
import amf.spec.raml.{Entries, ValueNode}
import amf.vocabulary.{Namespace, ValueType}

import scala.collection.mutable



/**
  * Created by kor on 12/09/17.
  */
case class Dialect(val name:String,val root: DialectNode,val resolver:ResolverFactory=r=>null) {

  var refiner:Refiner=_;

  val header: String="#%RAML 1.0 "+name

  root._dialect=this;

}
trait ResolverFactory{
  def resolver(root:Root):ReferenceResolver
}

trait LocalNameProviderFactory{
  def apply(root:DomainEntity):LocalNameProvider
}
trait ReferenceResolver{
  def resolve(root: Root,name:String,t:Type):String;
}

trait LocalNameProvider{
   def localName(refValue: String, property: DialectPropertyMapping):String;
}

trait Refiner{
  def refine(root:DomainEntity);
}

case class DialectPropertyMapping(val name:String, val range:Type, var required:Boolean=false){

  private[dialects] var _collection=false;


  var _referenceTarget:DialectNode=null;

  var _noRAML=false;

  var _noLastSegmentTrimInMaps=false

  def noRAML():DialectPropertyMapping={
    this._noRAML=true;
    return this;
  }

  var _hash:DialectPropertyMapping=null;

  var _fromVal=false;


  var _declaration=false;



  private var _namespace:Namespace=null;

  private var _rdfName: String=null;

  private var _field:amf.metadata.Field=_;

  private var _jsonLd=true;


  def declaration():DialectPropertyMapping={
    this._declaration=true;
    return this;
  }

  def collection(): DialectPropertyMapping = {
    this._collection=true;
    return this;
  }

  def require(): DialectPropertyMapping ={
    this.required=true;
    this
  }

  def noJsonLd():DialectPropertyMapping={this._jsonLd=false;return this;}

  def namespace(n:Namespace):DialectPropertyMapping={ this._namespace=n;this;}

  def rdfName(n:String):DialectPropertyMapping={this._rdfName=n;this;}

  def isRef():Boolean=this._referenceTarget!=null;

  def ref(nd:DialectNode): DialectPropertyMapping ={
    this._referenceTarget=nd;
    this;
  }

  def isScalar()= this.range.isInstanceOf[Type.Scalar]

  def map(h:DialectPropertyMapping):DialectPropertyMapping={
    this._hash=h;
    this;
  }
  def isMap()=_hash!=null;

  def isCollection()=_collection;

  def value():DialectPropertyMapping={this._fromVal=true;this}

  def adopt(dialectNode: DialectNode): Unit ={
    if (this._namespace==null)
    {
        this._namespace=dialectNode.namespace;
    }
  }

  def field():amf.metadata.Field={
    if (_field==null){
      var t=range;
      if (isCollection()||isMap()){
        t=Type.Array(t);
      }
      var shortName=this.name;
      if (this._rdfName!=null){
        shortName=this._rdfName;
      }
      _field=Field(t, this._namespace + shortName,this._jsonLd)
    }
    _field;
  }
}
trait TypeCalculator{
  def calcTypes(d:DomainEntity):List[ValueType];
}

class FieldValueDescriminator(val dialectPropertyMapping: DialectPropertyMapping,val valueMap:mutable.Map[String,ValueType]=mutable.Map()) extends TypeCalculator{

  def add(n:String,v:ValueType):FieldValueDescriminator={valueMap.put(n,v);return this;}

  var defaultValue:ValueType=null;

  def calcTypes(d:DomainEntity):List[ValueType]={
    val scalar=d.fields.get(this.dialectPropertyMapping.field());
    if (scalar.isInstanceOf[AmfScalar]){
      val dv=scalar.asInstanceOf[AmfScalar].toString;
      if (valueMap.contains(dv)){
         return List(valueMap.get(dv).get)
      }
      if (this.defaultValue!=null){
        return List(defaultValue);
      }
    }
    if (scalar.isInstanceOf[AmfArray]){
      val arr=scalar.asInstanceOf[AmfArray];
      var buf:Set[ValueType]=Set();
      arr.values.foreach(r=>{
        val dv=r.toString
        if (valueMap.contains(dv)){
          val v:ValueType=valueMap.get(dv).get;
          buf=buf.+(v);
        }

      })
      if (buf.isEmpty&&this.defaultValue!=null){
        buf=buf.+(defaultValue);
      }
      return buf.toList;
    }
    return List();
  }
}
class Builtins extends LocalNameProvider with ReferenceResolver{

  override def resolve(root: Root, name: String, t: Type): String = b2id.get(name).getOrElse(null);

  override def localName(refValue: String, property: DialectPropertyMapping): String = id2b.get(refValue).getOrElse(refValue);

  val b2id=mutable.HashMap[String,String]();
  val id2b=mutable.HashMap[String,String]();
  val id2t=mutable.HashMap[String,Type]();

  def buitInType(id:String): Type ={
    return id2t.getOrElse(id,null);
  }

  def add(id:String,builtin:String,t:Type):Builtins={
    b2id.put(builtin,id);
    id2t.put(id,t);
    id2b.put(id,builtin);
    return this;
  }
}

class TypeBuiltins extends Builtins{

  add(TypeBuiltins.STRING,"string",Type.Str);
  add(TypeBuiltins.INTEGER,"integer",Type.Int);
  add(TypeBuiltins.NUMBER,"number",Type.Int);
  add(TypeBuiltins.FLOAT,"number",Type.Int);
  add(TypeBuiltins.BOOLEAN,"boolean",Type.Bool);
  add(TypeBuiltins.URI,"uri",Type.Iri);
  add(TypeBuiltins.ANY,"any",null);

}
object TypeBuiltins{
  val STRING="http://www.w3.org/2001/XMLSchema#string";
  val INTEGER= "http://www.w3.org/2001/XMLSchema#integer";
  val FLOAT="http://www.w3.org/2001/XMLSchema#float"
  val NUMBER="http://www.w3.org/2001/XMLSchema#float"
  val BOOLEAN="http://www.w3.org/2001/XMLSchema#boolean"
  val URI="http://www.w3.org/2001/XMLSchema#anyURI"
  val ANY="http://www.w3.org/2001/XMLSchema#anyType"

}
class BasicResolver(val root:Root, val externals:List[DialectPropertyMapping]) extends TypeBuiltins{

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
    externals.foreach(p=> {
      entries.key(p.name, e => {
        val entries = new Entries(e.value).entries
        entries.foreach { case (alias, entry) =>
          ValueNode(entry.value).string().value match {
            case prefix: String => externalsMap.put(alias, prefix)
          }
        }
      });
    })
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

class BasicNameProvider(root:DomainEntity,val namespaceDeclarators:List[DialectPropertyMapping]) extends TypeBuiltins{

  val namespaces=mutable.Map[String,String]();
  var declarations=mutable.Map[String,DomainEntity]();

  {
    namespaceDeclarators.foreach(x=> {
      root.entities(x).foreach(e => {
        namespaces.put(e.string(External.uri).get, e.string(External.name).get);
      })
    })
    root.traverse((r,p)=>{
      if (p._declaration){
        declarations.put(r.id,r);
      }
      true;
    })
  }

  override def localName(uri: String, property: DialectPropertyMapping): String = {
    if (declarations.contains(uri)){
      val entity=declarations.getOrElse(uri,null);
      var kp=entity.definition.keyProperty;
      if (kp!=null){
        return entity.string(kp).getOrElse(uri);
      }
    }
    val ln=super.localName(uri,property );
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
class DialectNode(val namespace:Namespace,val shortName:String) extends Type with Obj{

  protected val props:mutable.Map[String,DialectPropertyMapping]=new mutable.LinkedHashMap();

  override val dynamicType: Boolean  = true
  def obj(propertyMapping: String,dialectNode: DialectNode):DialectPropertyMapping = add(new DialectPropertyMapping(propertyMapping,dialectNode))

  def str(propertyMapping: String):DialectPropertyMapping = add(new DialectPropertyMapping(propertyMapping,Type.Str))
  def bool(propertyMapping: String):DialectPropertyMapping = add(new DialectPropertyMapping(propertyMapping,Type.Bool))
  def ref(propertyMapping: String,d:DialectNode):DialectPropertyMapping = add(new DialectPropertyMapping(propertyMapping,Type.Iri)).ref(d);
  def map(propertyMapping: String, hash:DialectPropertyMapping, node:DialectNode):DialectPropertyMapping = add(new DialectPropertyMapping(propertyMapping,node).map(hash))
  def _props():Seq[DialectPropertyMapping] = props.values.toList;

  protected var _typeCalculator:TypeCalculator=null;
  protected var extraTypes:List[ValueType]=List();
  override val `type`: List[ValueType] = List(ValueType(namespace,shortName))

  private [dialects] var _dialect: Dialect=_

  def add(p:DialectPropertyMapping):DialectPropertyMapping={
    props.put(p.name,p);
    p.adopt(this);
    return p;
 }
  var _id:String=null;

  def withType(t:String): Unit ={
    this.extraTypes=extraTypes.::(ValueType(t));
  }

  def withCalculator(t:TypeCalculator)=this._typeCalculator=t;

  def fieldValueDescriminator(prop:DialectPropertyMapping):FieldValueDescriminator={
    val r=new FieldValueDescriminator(prop);
    withCalculator(r);
    return r;
  }

  def fields():List[Field]={
    return _props().map(x=>x.field()).toList;
  }

  def calcTypes(v:DomainEntity):List[ValueType]={
    if (this._typeCalculator!=null){
      var r:List[ValueType]=this._typeCalculator.calcTypes(v);
      return extraTypes.:::(r);
    }
    return extraTypes;
  }

  var keyProperty:DialectPropertyMapping=null;

  def withGlobalIdField(field:String)()=_id=field;

  var nameProvider: LocalNameProviderFactory=_;
}