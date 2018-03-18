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
    strUndefined.present() shouldBe false
    strUndefined.value() should be(null)
    strUndefined.isNull should be(true)
    strUndefined.isNullOrEmpty should be(true)

    val intUndefined: IntField = fields.field(IntProperty)
    intUndefined.option() shouldBe None
    intUndefined.present() shouldBe false
    intUndefined.value() should be(0)
    intUndefined.isNull should be(true)

    val doubleUndefined: DoubleField = fields.field(DoubleProperty)
    doubleUndefined.option() shouldBe None
    doubleUndefined.present() shouldBe false
    doubleUndefined.value() should be(0.0)
    doubleUndefined.isNull should be(true)

    val boolUndefined: BoolField = fields.field(BoolProperty)
    boolUndefined.option() shouldBe None
    boolUndefined.present() shouldBe false
    boolUndefined.value() should be(false)
    boolUndefined.isNull should be(true)

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
    strDefined.present() shouldBe true
    strDefined.value() should be("hello")
    strDefined.isNull should be(false)
    strDefined.isNullOrEmpty should be(false)

    fields.set("/", IntProperty, AmfScalar(10))
    val intDefined: IntField = fields.field(IntProperty)
    intDefined.option() shouldBe Some(10)
    intDefined.present() shouldBe true
    intDefined.value() should be(10)
    intDefined.isNull should be(false)

    fields.set("/", DoubleProperty, AmfScalar(5.5))
    val doubleDefined: DoubleField = fields.field(DoubleProperty)
    doubleDefined.option() shouldBe Some(5.5)
    doubleDefined.present() shouldBe true
    doubleDefined.value() should be(5.5)
    doubleDefined.isNull should be(false)

    fields.set("/", BoolProperty, AmfScalar(true))
    val boolTrue: BoolField = fields.field(BoolProperty)
    boolTrue.option() shouldBe Some(true)
    boolTrue.present() shouldBe true
    boolTrue.value() should be(true)
    boolTrue.isNull should be(false)

    fields.set("/", BoolProperty, AmfScalar(false))
    val boolFalse: BoolField = fields.field(BoolProperty)
    boolFalse.option() shouldBe Some(false)
    boolFalse.present() shouldBe true
    boolFalse.value() should be(false)
    boolFalse.isNull should be(false)

    fields.set("/", SeqStrProperty, AmfArray(Seq(AmfScalar("hello"))))
    val seqStrDefined: Seq[StrField] = fields.field(SeqStrProperty)
    seqStrDefined shouldNot be(empty)
    seqStrDefined.size should be(1)
    val strHead = seqStrDefined.head
    strHead.option() shouldBe Some("hello")
    strHead.present() shouldBe true
    strHead.value() should be("hello")
    strHead.isNull should be(false)
    strHead.isNullOrEmpty should be(false)

    fields.set("/", SeqAnyProperty, AmfArray(Seq(AmfScalar(10))))
    val seqAnyUndefined: Seq[AnyField] = fields.field(SeqAnyProperty)
    seqAnyUndefined shouldNot be(empty)
    seqAnyUndefined.size should be(1)
    val anyHead = seqAnyUndefined.head
    anyHead.option() shouldBe Some(10)
    anyHead.present() shouldBe true
    anyHead.value() should be(10)
    anyHead.isNull should be(false)
  }

  test("Test field method for <null> values") {
    val fields = Fields()

    fields.set("/", StrProperty, AmfScalar(null))
    val strDefined: StrField = fields.field(StrProperty)
    strDefined.option() shouldBe None
    strDefined.present() shouldBe true
    strDefined.value() should be(null)
    strDefined.isNull should be(true)
    strDefined.isNullOrEmpty should be(true)

    fields.set("/", IntProperty, AmfScalar(null))
    val intDefined: IntField = fields.field(IntProperty)
    intDefined.option() shouldBe Some(0)
    intDefined.present() shouldBe true
    intDefined.value() should be(0)
    intDefined.isNull should be(false) // ...

    fields.set("/", DoubleProperty, AmfScalar(null))
    val doubleDefined: DoubleField = fields.field(DoubleProperty)
    doubleDefined.option() shouldBe Some(0.0)
    doubleDefined.present() shouldBe true
    doubleDefined.value() should be(0.0)
    doubleDefined.isNull should be(false) // ...

    fields.set("/", BoolProperty, AmfScalar(null))
    val boolTrue: BoolField = fields.field(BoolProperty)
    boolTrue.option() shouldBe Some(false)
    boolTrue.present() shouldBe true
    boolTrue.value() should be(false)
    boolTrue.isNull should be(false) // ...

    fields.set("/", SeqStrProperty, AmfArray(Seq(AmfScalar(null))))
    val seqStrDefined: Seq[StrField] = fields.field(SeqStrProperty)
    seqStrDefined shouldNot be(empty)
    seqStrDefined.size should be(1)
    val strHead = seqStrDefined.head
    strHead.option() shouldBe None
    strHead.present() shouldBe true
    strHead.value() should be(null)
    strHead.isNull should be(true)
    strHead.isNullOrEmpty should be(true)

    fields.set("/", SeqAnyProperty, AmfArray(Seq(AmfScalar(null))))
    val seqAnyUndefined: Seq[AnyField] = fields.field(SeqAnyProperty)
    seqAnyUndefined shouldNot be(empty)
    seqAnyUndefined.size should be(1)
    val anyHead = seqAnyUndefined.head
    anyHead.option() shouldBe None
    anyHead.present() shouldBe true
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
