package amf.plugins.document.vocabularies.core

import amf.metadata.Type
import amf.spec.dialects.{Dialect, DialectNode, DialectPropertyMapping}

import scala.collection.mutable

/**
  * Created by Pavel Petrochenko on 18/09/17.
  */
class TopLevelGenerator(d: Dialect) {

  val map: mutable.LinkedHashMap[DialectNode, String] = mutable.LinkedHashMap[DialectNode, String]()

  val keywords: mutable.Set[String] = mutable.Set[String]()

  val kw =
    "abstract, strictfp, short, int, do, goto, interface, throw, float, package, implements, enum, this, long, if, switch, native, throws, boolean, catch, else, const, class, assert, public, void, instanceof, protected, static, default, private, finally, synchronized, new, char, extends, final, volatile, for, return, continue, case, import, double, super, byte, while, break, try, transient"

  kw.split("\\s*,\\s*").foreach { v =>
    keywords.add(v)
  }

  def generateProperty(d: DialectNode, p: DialectPropertyMapping): String =
    s"def ${escape(p.name)}(): ${signature(d, p)} = ${impl(d, p)}\n"

  def generateRefResolver(d: DialectNode, p: DialectPropertyMapping): String =
    s"def ${escape("resolved" + p.name.charAt(0).toUpper + p.name.substring(1))}(): ${refSignature(d, p)} = ${implRef(d, p)}\n"

  def implRef(dialectNode: DialectNode, p: DialectPropertyMapping): String = {
    val s               = new mutable.StringBuilder()
    val propName        = scalaName(dialectNode, p)
    val declarationName = escape(declarations(p).head.name)

    /**
      *  resolveReferences(PropertyTerm.domain,
      (r,s)=>{r.asInstanceOf[VocabularyObject].classTerms().find(_.entity.id==s)}
      ,e=>ClassObject(e,Some(this)))
      */
    if (p.isMap || p.collection) {

      s.append(s"resolveReferences2Options($propName,\n")
      s.append(
        s"      (r, s) => { r.asInstanceOf[${d.root.shortName}Object].$declarationName.find(_.entity.id == s) }\n")
      s.append(s"      , e => ${p.referenceTarget.get.shortName}Object(e, Some(this)))")

    } else {
      s.append(s"resolveReference($propName,\n")
      s.append(
        s"      (r, s) => { r.asInstanceOf[${d.root.shortName}Object].$declarationName.find(_.entity.id == s) }\n")
      s.append(s"      , e => ${p.referenceTarget.get.shortName}Object(e,Some(this)))")

    }
    s.toString
    // "???"
  }

  private def declarations(p: DialectPropertyMapping) = {
    d.root.props.values.filter(v => v.isMap && v.range == p.referenceTarget.get)
  }

  def generateBuilderMethod(d: DialectNode, p: DialectPropertyMapping): String =
    s"def with${p.name.charAt(0).toUpper + p.name.substring(1)}(value: ${builderType(p)}): ${d.shortName}Object = ${generateWriter(d, p)}\n"

  def generateWriter(dialectNode: DialectNode, p: DialectPropertyMapping): String = {
    val propName = scalaName(dialectNode, p)
    val op       = if (p.multivalue) "add" else "set"
    val vl       = if (p.isScalar) "AmfScalar(value)" else "value.entity"
    s"{ entity.$op($propName.field() , $vl); this }"
  }

  def escape(p: String): String =
    if (keywords.contains(p)) "`" + p + "`"
    else p

  def builderType(p: DialectPropertyMapping): String =
    if (p.isScalar) scalarName(p.range)
    else p.range.asInstanceOf[DialectNode].shortName + "Object"

  def impl(dialectNode: DialectNode, p: DialectPropertyMapping): String = {
    val propName = scalaName(dialectNode, p)
    if (p.multivalue) {
      if (!p.isScalar) {
        s"entity.entities($propName).map(${p.range.asInstanceOf[DialectNode].shortName}Object(_, Some(this)))"

      } else
        p.range match {
          case Type.Str => s"entity.strings($propName)"
          case Type.Iri => s"entity.strings($propName)"
          case _        => "???"
        }
    } else {
      if (!p.isScalar) {
        s"entity.entity($propName).map(${p.range.asInstanceOf[DialectNode].shortName}Object(_, Some(this)))"
      } else
        p.range match {
          case Type.Str  => s"entity.string($propName)"
          case Type.Iri  => s"entity.string($propName)"
          case Type.Bool => s"entity.boolean($propName)"
          case _         => "???"
        }
    }
  }

  private def scalaName(dialectNode: DialectNode, p: DialectPropertyMapping) = {
    nameOfType(dialectNode) + "." + escape(p.scalaName)
  }

  private def nameOfType(dialectNode: DialectNode): String = {
    val tp = dialectNode.getClass.getSimpleName
    if (tp.endsWith("$")) {
      tp.substring(0, tp.length - 1)
    } else tp
  }

  private def signature(d: DialectNode, p: DialectPropertyMapping): String = {
    generateNodeRangeIfNeeded(p)
    val container = if (p.multivalue) "Seq" else "Option"
    val tpName    = if (p.isScalar) scalarName(p.range) else s"${p.range.asInstanceOf[DialectNode].shortName}Object"
    s"$container[$tpName]"
  }

  private def refSignature(d: DialectNode, p: DialectPropertyMapping): String = {
    val tpName = s"${p.referenceTarget.get.shortName}Object"
    if (p.multivalue) s"List[Option[$tpName]]"
    else s"Option[$tpName]"
  }

  private def generateNodeRangeIfNeeded(p: DialectPropertyMapping): Unit = {
    p.range match {
      case node: DialectNode => appendNodeIfNeeded(node)
      case _                 => // ignore
    }
    p.referenceTarget.foreach { node =>
      appendNodeIfNeeded(node)
    }
  }

  private def appendNodeIfNeeded(node: DialectNode) = {
    if (!map.contains(node)) {
      generateNode(node)
    }
  }

  private def scalarName(value: Type): String = value match {
    case Type.Str  => "String"
    case Type.Int  => "Int"
    case Type.Bool => "Boolean"
    case _         => "String"
  }

  def generate(): String = {
    val bld = new mutable.StringBuilder()
    bld.append(
      "package amf.dialects\nimport amf.dialects._\nimport amf.plugins.document.vocabularies.core._\nimport amf.model.AmfScalar\nimport amf.domain.dialects.DomainEntity\n")
    bld.append(s"object ${d.name.replace(' ', '_').replace('.', '_')}TopLevel {\n")
    generateNode(this.d.root)
    map.values.foreach { v =>
      bld.append(v)
      bld.append("\n")
    }
    bld.append("}\n")
    bld.toString
  }

  def generateNode(d: DialectNode): String = {
    map.put(d, "")
    val sb = new StringBuilder()
    sb.append(
      s"  case class ${d.shortName}Object(entity: DomainEntity = DomainEntity(${nameOfType(d)}), \noverride val parent:Option[TopLevelObject] = None) \nextends TopLevelObject(entity, parent){\n")
    d.props.values.foreach { p =>
      sb.append("    " + generateProperty(d, p))
      sb.append("    " + generateBuilderMethod(d, p))
      if (p.isRef && declarations(p).size == 1) {
        sb.append("    " + generateRefResolver(d, p))
      }
    }
    sb.append("  }\n")
    map.put(d, sb.toString)
    sb.toString
  }
}
