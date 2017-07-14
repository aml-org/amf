package amf.common

import org.scalatest.{Assertion, Assertions}

/**
  *
  */
trait ListAssertions extends Assertions {

  def assert[E](left: List[E], right: List[E]): Assertion = {
//    println("Actual: ", left.toString)
//    println("Expect: ", right.toString)
    if (left.size == right.size) {
      left.zipWithIndex.foreach {
        case (actual, index) =>
          val expected = right(index)
          if (actual != expected) {
            fail(s"$actual did not equal $expected at index $index")
          }
      }
      succeed
    } else fail(s"$left did not contain the same elements that \n$right")
  }
}
