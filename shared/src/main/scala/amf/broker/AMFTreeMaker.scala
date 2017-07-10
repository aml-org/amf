package amf.broker

import amf.common.AMFASTNode
import amf.common.AMFToken.{Entry, MapToken, SequenceToken, StringToken}
import amf.remote.Vendor

/**
  * Created by hernan.najles on 7/6/17.
  */
abstract class AMFTreeMaker[T] {

  def make(webApiSubClass: T, vendor: Vendor): AMFASTNode

  def makeStruct(node: (String, Any)): AMFASTNode = {

    val result: AMFASTNode = node._2 match {
      case x: List[_] =>
        x match {
          case d: List[(String, Any)] =>
            new AMFASTNode(MapToken, "", null, d.map(t => { makeStruct(t) }))
          case d: List[String] =>
            new AMFASTNode(SequenceToken, "", null, d.map(t => { new AMFASTNode(StringToken, t, null) }))
          case _ => ???
        }
      case x: (String, String) => makeStruct(x)
      case x: String           => new AMFASTNode(StringToken, x, null, null)
      case _                   => ???
    }

    new AMFASTNode(Entry, null, null, Seq(new AMFASTNode(StringToken, node._1, null), result))
  }

  def makePropertyNode(key: String, value: Any): AMFASTNode = {

    new AMFASTNode(Entry, null, null, Seq(new AMFASTNode(StringToken, key, null), value match {
      case x: String     => new AMFASTNode(StringToken, x, null)
      case x: AMFASTNode => x
      case _             => ???
    }))
  }

  def makePropertyNode(key: String, value: AMFASTNode): AMFASTNode = {

    new AMFASTNode(Entry, "", null, Seq(new AMFASTNode(StringToken, key, null), value))
  }

  def makeListNode(name: String, values: List[String]): AMFASTNode = {
    new AMFASTNode(Entry,
                   "",
                   null,
                   List(
                     new AMFASTNode(StringToken, name, null),
                     new AMFASTNode(SequenceToken, "", null, values.map(s => new AMFASTNode(StringToken, s, null)))
                   ))
  }

  def makeJsonPropertyNode(key: String, value: Any): AMFASTNode = {
    value match {
      case x: String =>
        makePropertyNode(key,
                         new AMFASTNode(SequenceToken,
                                        "",
                                        null,
                                        List(
                                          new AMFASTNode(MapToken, "", null, List(makePropertyNode("@value", x)))
                                        )))
      case l: List[Any] if l.head.isInstanceOf[AMFASTNode] =>
        makePropertyNode(key,
                         new AMFASTNode(SequenceToken,
                                        "",
                                        null,
                                        List(new AMFASTNode(MapToken, "", null, l.asInstanceOf[List[AMFASTNode]]))))
      case x: List[_] =>
        makePropertyNode(
          key,
          new AMFASTNode(SequenceToken,
                         "",
                         null,
                         x.map(v => new AMFASTNode(MapToken, "", null, List(makePropertyNode("@value", v))))))
    }
  }
}
