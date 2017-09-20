package amf.dialects


import scala.collection.mutable;
import amf.metadata.Type;

/**
  * Created by kor on 18/09/17.
  */
class TopLevelGenerator(d:Dialect) {

  val _map=mutable.LinkedHashMap[DialectNode,String]();

  val _keywords=mutable.Set[String]();

  val kw="abstract, strictfp, short, int, do, goto, interface, throw, float, package, implements, enum, this, long, if, switch, native, throws, boolean, catch, else, const, class, assert, public, void, instanceof, protected, static, default, private, finally, synchronized, new, char, extends, final, volatile, for, return, continue, case, import, double, super, byte, while, break, try, transient"

  kw.split(",").foreach(v=>{
    _keywords.add(v.trim);
  })

  def generateProperty(d: DialectNode, p: DialectPropertyMapping): String =s"def ${escape(p.name)}():${signature(d,p)}= ${impl(d,p)}\n"


  def generateBuilderMethod(d: DialectNode, p: DialectPropertyMapping): String
       =s"def with${p.name.charAt(0).toUpper + p.name.substring(1)}(value:${builderType(p)}):${d.shortName}Object= ${generateWriter(d,p)}\n"

  def generateWriter(dialectNode:DialectNode,p: DialectPropertyMapping): String ={
    val propName=scalaName(dialectNode, p);
    val op=if (p.multivalue) "add" else "set"
    var vl=if (p.isScalar)  "AmfScalar(value)" else "value.entity"
    s"{entity.${op}(${propName}.field , ${vl}); this}"
  }

  def escape(p:String): String ={
    if (_keywords.contains(p)){
      ("`"+p+"`")
    }
    else p
  }

  def builderType(p:DialectPropertyMapping):String= if (p.isScalar){
      scalarName(p.range)
    }
    else p.range.asInstanceOf[DialectNode].shortName + "Object"


  def impl(dialectNode: DialectNode,p:DialectPropertyMapping): String ={
    val propName=scalaName(dialectNode, p);
    if (p.multivalue){
      if (!p.isScalar){
        s"entity.entities(${propName}).map(${p.range.asInstanceOf[DialectNode].shortName}Object(_))"

      }
      else p.range match {
        case Type.Str=> s"entity.strings(${propName})"
        case Type.Iri=> s"entity.strings(${propName})"
        case _ => "???"
      }
    }
    else {
      if (!p.isScalar) {
        s"entity.entity(${propName}).map(${p.range.asInstanceOf[DialectNode].shortName}Object(_))"
      }
      else p.range match {
        case Type.Str=> s"entity.string(${propName})"
        case Type.Iri=> s"entity.string(${propName})"
        case Type.Bool=> s"entity.boolean(${propName})"
        case _ => "???"
      }
    }
  }

  private def scalaName(dialectNode: DialectNode, p: DialectPropertyMapping) = {
    nameOfType(dialectNode) + "." + escape(p.scalaName)
  }

  private def nameOfType(dialectNode: DialectNode):String = {
    val tp=dialectNode.getClass.getSimpleName
    if (tp.endsWith("$")){
      tp.substring(0,tp.length-1)
    }
    else tp
  }

  private def signature(d:DialectNode, p:DialectPropertyMapping):String={
    generateNodeRangeIfNeeded(p)
    val container=if (p.multivalue) "Seq" else "Option"
    var tpName= if (p.isScalar) scalarName(p.range) else s"${p.range.asInstanceOf[DialectNode].shortName}Object"
    s"${container}[${tpName}]";
  }

  private def generateNodeRangeIfNeeded(p: DialectPropertyMapping) = {
    if (p.range.isInstanceOf[DialectNode]) {
      val node = p.range.asInstanceOf[DialectNode];
      if (!_map.contains(node)) {
        generateNode(node);
      }
    }
  }

  private def scalarName(value: Type): String = value match {
      case Type.Str => "String"
      case Type.Int => "Int"
      case Type.Bool =>"Boolean"
      case _ => "String"
  }


  def generate(): String ={
    val bld=new mutable.StringBuilder()
    bld.append("package amf.dialects\nimport amf.dialects._;\nimport amf.model.AmfScalar;\n")
    bld.append(s"object ${d.name.replace(' ','_').replace('.','_')}TopLevel {\n")
    generateNode(this.d.root);
    this._map.values.foreach(v=>{
      bld.append(v);
      bld.append("\n")
    })
    bld.append("}")
    bld.toString
  }

  def generateNode(d:DialectNode):String={
    _map.put(d,"");
    val sb=new StringBuilder();
    sb.append(s"case class ${d.shortName}Object(val entity: DomainEntity=DomainEntity(${nameOfType(d)})){\n")
    d.props.values.foreach(p=>{
      sb.append("  " + generateProperty(d,p))
      sb.append("  " + generateBuilderMethod(d,p))
    })
    sb.append("}\n")
    _map.put(d,sb.toString);
    sb.toString;
  }
}
