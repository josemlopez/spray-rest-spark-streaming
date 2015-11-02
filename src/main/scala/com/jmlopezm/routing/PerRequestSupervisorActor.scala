package com.jmlopezm.routing

import akka.actor._
import akka.actor.SupervisorStrategy.Stop
import spray.http.StatusCodes._
import spray.routing.RequestContext
import akka.actor.OneForOneStrategy
import spray.httpx.Json4sSupport
import scala.concurrent.duration._
import org.json4s.DefaultFormats
import spray.http.StatusCode
import com.jmlopezm._
import com.jmlopezm.routing.PerRequestSupervisorActor._

trait PerRequestSupervisorActor extends Actor with Json4sSupport {

  import context._

  val json4sFormats = DefaultFormats

  def r: RequestContext
  def target: ActorRef
  def message: RestMessage

  setReceiveTimeout(2000.seconds)
  target ! message

  def receive = {
    case res: RestMessage => complete(OK, res)
    case v: Validation    => complete(BadRequest, v)
    case ReceiveTimeout   => complete(GatewayTimeout, Error("Request timeout"))
    case e: Error         => complete(NotFound, e.message )
  }

  def complete[T <: AnyRef](status: StatusCode, obj: T) = {
    r.complete(status, obj)
    stop(self)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {
        complete(InternalServerError, Error(e.getMessage))
        Stop
      }
    }
}

object PerRequestSupervisorActor {
  case class WithActorRef(r: RequestContext, target: ActorRef, message: RestMessage) extends PerRequestSupervisorActor

  case class WithProps(r: RequestContext, props: Props, message: RestMessage) extends PerRequestSupervisorActor {
    lazy val target = context.actorOf(props)
  }
}

trait PerRequestCreator {
  this: Actor =>

  def perRequestSupervisorActorCreator(r: RequestContext, target: ActorRef, message: RestMessage) =
    context.actorOf(Props(new WithActorRef(r, target, message)))

  def perRequestSupervisorActorCreator(r: RequestContext, props: Props, message: RestMessage) =
    context.actorOf(Props(new WithProps(r, props, message)))
}