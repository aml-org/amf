package amf.common

import amf.document.{BaseUnit, Document}
import amf.domain.Annotation
import amf.metadata.Field
import amf.model.AmfObject
import org.scalatest._

/**
  *
  */
trait AmfObjectTestMatcher extends Assertions {

  /** [[AmfObject]] matcher ignoring all [[Annotation]]s */
  case class AmfObjectMatcher(expected: AmfObject) {

    def assert(actual: AmfObject): Unit = {
      if (actual.fields.size != expected.fields.size) {
        fail(s"Expected ${expected.fields} but ${actual.fields} fields have different sizes")
      }

      expected.fields.foreach({
        case (field, _) =>
          val a: Any = actual.fields.raw(field).orNull
          val e: Any = expected.fields.raw(field).orNull
          assertRaw(field, a, e)
      })
    }

    private def assertRaw(field: Field, a: Any, e: Any): Unit = {
      e match {
        case _: String | _: Boolean | _: Integer => if (a != e) fail(s"Expected scalar '$a' but '$e' found for $field")
        case obj: AmfObject                      => AmfObjectMatcher(obj).assert(a.asInstanceOf[AmfObject])
        case values: Seq[_] =>
          val other = a.asInstanceOf[Seq[_]]

          if (values.size != other.size) {
            fail(s"Expected $values but $other fields have different sizes")
          }

          values
            .zip(other)
            .foreach({
              case (exp, act) => assertRaw(field, act, exp)
            })
      }
    }
  }
}
//trait AmfUnitTestMatcher extends Assertions with AmfObjectTestMatcher{
//
//  case class AmfUnitMatcher(expected: BaseUnit){
//
//    def assert(actual:BaseUnit): Unit ={
//      actual match {
//        case a:Document =>
//          expected match {
//            case e:Document => AmfDocumentMatcher(e).assert(a)
//            case _ => fail(" expected type its not of the same type than actual (document)")
//          }
//
//        case _ => fail("unsupported type of base unit")
//      }
//    }
//  }
//
//  case class AmfDocumentMatcher(expected: Document){
//
//    def assert(actual: Document): Unit ={
//      AmfObjectMatcher(expected.encodes).assert(actual.encodes)
//      if(actual.location!= expected.location ) fail(s"Expected location '${expected.location}' but '${actual.location}' found")
//
//      if(actual.declares.size != actual.declares.size)
//        fail(s"Expected declared  ${expected.declares.size} but ${actual.declares.size} fields have different sizes")
//
//
//      actual.de
//    }
//  }
//}
//
//
