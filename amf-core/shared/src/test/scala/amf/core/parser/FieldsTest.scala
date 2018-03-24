package amf.core.parser

import amf.client.model._
import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Any, Array, Bool, Double, Int, SortedArray, Str}
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.vocabulary.Namespace.{Http, Meta, Shacl}
import org.scalatest.{FunSuite, Matchers}

class FieldsTest extends FunSuite with Matchers {

  test("Test field method for undefined values") {
    val fields = Fields()

    val strUndefined: StrField = fields.field(StrProperty)
    strUndefined.option() shouldBe None
    strUndefined.value() should be(null)
    strUndefined.nonNull should be(false)
    strUndefined.isNull should be(true)
    strUndefined.isNullOrEmpty should be(true)
    strUndefined.nonEmpty should be(false)
    strUndefined.is("") should be(false)
    strUndefined.is(_ == "") should be(false)

    val intUndefined: IntField = fields.field(IntProperty)
    intUndefined.option() shouldBe None
    intUndefined.value() should be(0)
    intUndefined.nonNull should be(false)
    intUndefined.isNull should be(true)
    intUndefined.is(0) should be(false)
    intUndefined.is(_ == 0) should be(false)

    val doubleUndefined: DoubleField = fields.field(DoubleProperty)
    doubleUndefined.option() shouldBe None
    doubleUndefined.value() should be(0.0)
    doubleUndefined.nonNull should be(false)
    doubleUndefined.isNull should be(true)
    doubleUndefined.is(0.0) should be(false)
    doubleUndefined.is(_ == 0.0) should be(false)

    val boolUndefined: BoolField = fields.field(BoolProperty)
    boolUndefined.option() shouldBe None
    boolUndefined.value() should be(false)
    boolUndefined.nonNull should be(false)
    boolUndefined.isNull should be(true)
    boolUndefined.is(false) should be(false)
    boolUndefined.is(_ == false) should be(false)

    val seqStrUndefined: Seq[StrField] = fields.field(SeqStrProperty)
    seqStrUndefined shouldBe empty

    val seqAnyUndefined: Seq[AnyField] = fields.field(SeqAnyProperty)
    seqStrUndefined shouldBe empty
  }

  test("Test field method for defined values") {
    val fields = Fields()

    fields.set("/", StrProperty, AmfScalar("hello"))
    val strDefined: StrField = fields.field(StrProperty)
    strDefined.option() shouldBe Some("hello")
    strDefined.value() should be("hello")
    strDefined.nonNull should be(true)
    strDefined.isNull should be(false)
    strDefined.isNullOrEmpty should be(false)
    strDefined.nonEmpty should be(true)
    strDefined.is("hello") should be(true)
    strDefined.is(_ == "hello") should be(true)

    fields.set("/", IntProperty, AmfScalar(10))
    val intDefined: IntField = fields.field(IntProperty)
    intDefined.option() shouldBe Some(10)
    intDefined.value() should be(10)
    intDefined.nonNull should be(true)
    intDefined.isNull should be(false)
    intDefined.is(10) should be(true)
    intDefined.is(_ == 10) should be(true)

    fields.set("/", DoubleProperty, AmfScalar(5.5))
    val doubleDefined: DoubleField = fields.field(DoubleProperty)
    doubleDefined.option() shouldBe Some(5.5)
    doubleDefined.value() should be(5.5)
    doubleDefined.nonNull should be(true)
    doubleDefined.isNull should be(false)
    doubleDefined.is(5.5) should be(true)
    doubleDefined.is(_ == 5.5) should be(true)

    fields.set("/", BoolProperty, AmfScalar(true))
    val boolTrue: BoolField = fields.field(BoolProperty)
    boolTrue.option() shouldBe Some(true)
    boolTrue.value() should be(true)
    boolTrue.nonNull should be(true)
    boolTrue.isNull should be(false)
    boolTrue.is(true) should be(true)
    boolTrue.is(_ == true) should be(true)

    fields.set("/", BoolProperty, AmfScalar(false))
    val boolFalse: BoolField = fields.field(BoolProperty)
    boolFalse.option() shouldBe Some(false)
    boolFalse.value() should be(false)
    boolFalse.nonNull should be(true)
    boolFalse.isNull should be(false)
    boolFalse.is(false) should be(true)
    boolFalse.is(_ == false) should be(true)

    fields.set("/", SeqStrProperty, AmfArray(Seq(AmfScalar("hello"))))
    val seqStrDefined: Seq[StrField] = fields.field(SeqStrProperty)
    seqStrDefined shouldNot be(empty)
    seqStrDefined.size should be(1)
    val strHead = seqStrDefined.head
    strHead.option() shouldBe Some("hello")
    strHead.value() should be("hello")
    strHead.nonNull should be(true)
    strHead.isNull should be(false)
    strHead.isNullOrEmpty should be(false)
    strHead.nonEmpty should be(true)

    fields.set("/", SeqAnyProperty, AmfArray(Seq(AmfScalar(10))))
    val seqAnyUndefined: Seq[AnyField] = fields.field(SeqAnyProperty)
    seqAnyUndefined shouldNot be(empty)
    seqAnyUndefined.size should be(1)
    val anyHead = seqAnyUndefined.head
    anyHead.option() shouldBe Some(10)
    anyHead.value() should be(10)
    anyHead.nonNull should be(true)
    anyHead.isNull should be(false)
  }

  test("Test field method for <null> values") {
    val fields = Fields()

    fields.set("/", StrProperty, AmfScalar(null))
    val strDefined: StrField = fields.field(StrProperty)
    strDefined.option() shouldBe None
    strDefined.nonNull shouldBe false
    strDefined.value() should be(null)
    strDefined.isNull should be(true)
    strDefined.isNullOrEmpty should be(true)
    strDefined.is("") should be(false)
    strDefined.is(_ == "") should be(false)
    strDefined.is(_ == null) should be(false) // option() its empty, so fold alwarys return false.

    fields.set("/", IntProperty, AmfScalar(null))
    val intDefined: IntField = fields.field(IntProperty)
    intDefined.option() shouldBe None
    intDefined.nonNull shouldBe false
    intDefined.value() should be(0)
    intDefined.isNull should be(true)
    intDefined.is(0) should be(false)
    intDefined.is(_ == 0) should be(false)
    intDefined.is(_ == null) should be(false)

    fields.set("/", DoubleProperty, AmfScalar(null))
    val doubleDefined: DoubleField = fields.field(DoubleProperty)
    doubleDefined.option() shouldBe None
    doubleDefined.nonNull shouldBe false
    doubleDefined.value() should be(0.0)
    doubleDefined.isNull should be(true)
    doubleDefined.is(0.0) should be(false)
    doubleDefined.is(_ == 0.0) should be(false)
    doubleDefined.is(_ == null) should be(false)

    fields.set("/", BoolProperty, AmfScalar(null))
    val boolTrue: BoolField = fields.field(BoolProperty)
    boolTrue.option() shouldBe None
    boolTrue.nonNull shouldBe false
    boolTrue.value() should be(false)
    boolTrue.isNull should be(true)
    boolTrue.is(false) should be(false)
    boolTrue.is(_ == false) should be(false)
    boolTrue.is(_ == null) should be(false)

    fields.set("/", SeqStrProperty, AmfArray(Seq(AmfScalar(null))))
    val seqStrDefined: Seq[StrField] = fields.field(SeqStrProperty)
    seqStrDefined shouldNot be(empty)
    seqStrDefined.size should be(1)
    val strHead = seqStrDefined.head
    strHead.option() shouldBe None
    strHead.nonNull shouldBe false
    strHead.value() should be(null)
    strHead.isNull should be(true)
    strHead.isNullOrEmpty should be(true)

    fields.set("/", SeqAnyProperty, AmfArray(Seq(AmfScalar(null))))
    val seqAnyUndefined: Seq[AnyField] = fields.field(SeqAnyProperty)
    seqAnyUndefined shouldNot be(empty)
    seqAnyUndefined.size should be(1)
    val anyHead = seqAnyUndefined.head
    anyHead.option() shouldBe None
    anyHead.nonNull shouldBe false
    (anyHead.value() == null) shouldBe true
    anyHead.isNull should be(true)
  }

  private val StrProperty    = Field(Str, Shacl + "name")
  private val IntProperty    = Field(Int, Shacl + "minCount")
  private val DoubleProperty = Field(Double, Shacl + "minInclusive")
  private val BoolProperty   = Field(Bool, Meta + "sorted")
  private val SeqStrProperty = Field(Array(Str), Http + "accepts")
  private val SeqAnyProperty = Field(SortedArray(Any), Shacl + "in")
}
