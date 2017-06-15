package amf.common

import org.scalatest.Assertions
import org.scalatest.Matchers._

/**
  * Created by hernan.najles on 6/13/17.
  */
trait ListAssertions  extends Assertions{

  def assert[E](left: List[E], right: List[E]):Unit = {
    if (left.size == right.size) {
      left.zipWithIndex.foreach {
        case (actual, index) =>
          val expected = right(index)
          if (actual != expected) {
            fail(s"$actual did not equal $expected at index $index")
          }
      }
    } else left should contain theSameElementsInOrderAs right
  }
}
