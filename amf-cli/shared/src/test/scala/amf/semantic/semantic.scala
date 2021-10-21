package amf

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

package object semantic {

  def await[T](future: => Future[T]): T = Await.result(future, Duration.Inf)
}
