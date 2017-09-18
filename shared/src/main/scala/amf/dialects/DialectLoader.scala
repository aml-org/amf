package amf.dialects

import amf.dialects.PropertyMapping.{bool, str}
import amf.document.{BaseUnit, Document}
import amf.spec.dialect.DomainEntity
import amf.vocabulary.Namespace

import scala.collection.mutable




/**
  * Created by kor on 15/09/17.
  */

class NM(val n:Namespace,val name:String){

}
object NM{
  def apply(s:String):NM={
    val ind:Int=Math.max(s.lastIndexOf('/'),s.lastIndexOf('#'));
    if (ind>0){
      val namespace = Namespace(s.substring(0, ind+1))
      val str1 = s.substring(ind + 1)
      return new NM(namespace,str1);
    }
    return null;
  }
}
class DialectLoader {

  val builtins=new TypeBuiltins()

  private def retrieveDomainEntity(unit:BaseUnit) = unit match {
    case document: Document => document.encodes.asInstanceOf[DomainEntity]
  }

  def loadDialect(d:BaseUnit): Dialect = {
     return loadDialect(retrieveDomainEntity(d))
  }
  def loadDialect(d:DomainEntity):Dialect={
    val rootEntity = d.entity(DialectDefinition.raml).get.entity(MainNode.document).get.string(DocumentEncode.encodes)
    val map=mutable.Map[String,DialectNode]()
    d.entities(DialectDefinition.nodeMappings).foreach(n=>{
        val ns=NM(n.string(NodeDefinition.classTerm).get);
        map.put(n.id,new DialectNode(ns.n,ns.name));
    })
    d.entities(DialectDefinition.nodeMappings).foreach(n=>{
      parseNodeMapping(n,map);
    })
    val dialect=d.string(DialectDefinition.dialect).get;
    val dialectDialect = new Dialect(dialect, map.get(rootEntity.get).get)
    return dialectDialect;
  }


  def parsePropertyMapping(pm:DomainEntity,map:mutable.Map[String,DialectNode]): DialectPropertyMapping = {
    val name = pm.string(PropertyMapping.name);
    val range = pm.string(PropertyMapping.range);
    var tp=builtins.buitInType(range.getOrElse(TypeBuiltins.STRING));
    if (tp==null){
      tp=map.getOrElse(range.getOrElse(TypeBuiltins.STRING),null);
    }
    val res=new DialectPropertyMapping(name.get,tp);
    pm.boolean(PropertyMapping.mandatory).foreach(m=>res.required=m);
    pm.boolean(PropertyMapping.allowMultiple).foreach(m=>res._collection=m);

    val term = pm.string(PropertyMapping.propertyTerm).get;
    val nm = NM(term)
    res.namespace(nm.n);
    res.rdfName(nm.name);
    val enum = pm.strings(PropertyMapping.enum)
    val pattern = pm.string(PropertyMapping.pattern);
    val minimum = pm.string(PropertyMapping.minimum);
    val maximum = pm.string(PropertyMapping.maximum);
    val hash=pm.string(PropertyMapping.hash);
    val asMap=pm.boolean(PropertyMapping.asMap);
    return res;
  }

  def parseNodeMapping(pm:DomainEntity,map:mutable.Map[String,DialectNode]):DialectNode={
    val node=map.get(pm.id).get;
    pm.entities(NodeDefinition.mapping).foreach(p=>{
      node.add(parsePropertyMapping(p,map))
    })

    return node;
  }


}