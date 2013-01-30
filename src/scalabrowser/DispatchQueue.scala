package scalabrowser

import scala.actors._
import scala.actors.Futures._

class DispatchQueue extends Actor {
  private[this] case class Dispatch[T](f: Future[T], cb:(T=>Unit))
  def act(){
    react {
      case Dispatch(f, cb) => cb(f()); act()
      case _ =>
    }
  }
  def dispatch[T](body: => T, cb: T=>Unit = ((r: T)=>{})){
    val f = future {body}
    this ! Dispatch[T](f, cb)
  }
  def stop {
    this ! None
  }
}

object DispatchQueue {
  def apply(body: => Unit) = {
    val dp = new DispatchQueue{body}
    dp.start()
    dp
  }
}