package com.jmlopezm.routing

import akka.actor.{ActorContext, Props, Actor, ActorLogging}

import spray.httpx.Json4sSupport
import spray.httpx.unmarshalling._
import spray.routing.{Route, HttpService}

import org.json4s.Formats
import org.json4s.DefaultFormats
import org.json4s.JsonAST._

import com.jmlopezm._
import com.jmlopezm.domain.Event
import com.jmlopezm.core.EventManagerActor
import com.jmlopezm.clients.{ResourcesActor, MetricsActor}

/**
 * Here we have the actor in charge of the REST API, he will create a new Actor (Supervisor) each time a new request is
 * received.
 * Then, this supervisor (PerRequest) will create another actor (EventManagerActor) that will be in charge of the event
 * from this moment on
 */
class RestSpark extends HttpService
with Actor
with PerRequestCreator
with Json4sSupport
with ActorLogging{

  implicit def actorRefFactory: ActorContext = context
  implicit def json4sFormats: Formats = DefaultFormats


  def receive = runRoute(route)

  val metricsService = context.actorOf(Props[MetricsActor])
  val resourcesService = context.actorOf(Props[ResourcesActor])

  val route = {
    path ("event") {
      get {
        parameters ('appID.as[String]) { (appId) => sparkRoute{ GetCounter()} }
      } ~ pathEnd {
      post {
        entity (as[JObject]) {
          eventJvalue =>
            val event = eventJvalue.extract[Event]
            log.debug("We have a new Event: " + event.toString)
            sparkRoute {
              SentEventToSpark (event)
            }
        }
      }
    }
  }
  }

  def sparkRoute(message : RestMessage): Route =
    ctx => perRequestSupervisorActorCreator(ctx, Props(new EventManagerActor(metricsService, resourcesService)), message)
}
