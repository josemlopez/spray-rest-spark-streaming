package com.jmlopezm.routing

import akka.actor.{ActorContext, Props, Actor}
import com.jmlopezm._
import com.jmlopezm.domain._
import spray.routing.{Route, HttpService}
import com.jmlopezm.core.EventManagerActor
import com.jmlopezm.clients.{ResourcesActor, MetricsActor}

/**
 * Here we have the actor in charge of the REST API, he will create a new Actor (Supervisor) each time a new request is
 * received.
 * Then, this supervisor (PerRequest) will create another actor (EventManagerActor) that will be in charge of the event
 * from this moment on
 */
class RestSpark extends HttpService with Actor with PerRequestCreator {

  implicit def actorRefFactory: ActorContext = context

  def receive = runRoute(route)

  val metricsService = context.actorOf(Props[MetricsActor])
  val resourcesService = context.actorOf(Props[ResourcesActor])

  val route = {
    post {
      entity(as[Event]) {
        event =>
          petsWithOwner{
            SentEventToSpark(event)
          }
      }
    }
  }

  def petsWithOwner(message : RestMessage): Route =
    ctx => perRequestSuperVisorActor(ctx, Props(new EventManagerActor(metricsService, resourcesService)), message)
}
