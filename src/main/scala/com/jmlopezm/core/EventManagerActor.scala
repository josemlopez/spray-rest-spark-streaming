package com.jmlopezm.core

import akka.actor.{Actor, ActorRef}
import com.jmlopezm.{SparkReference, _}
import com.jmlopezm.clients.MetricsActor.AddEvent
import com.jmlopezm.clients.ResourcesActor.{GetSparkCluster, SparkForId}
import com.jmlopezm.domain.Event

/**
 * This actor is the center of the action. He is in charge of asking about the Spark where to send the event and send if
 * if is possible
 */
class EventManagerActor(metricsActor: ActorRef, resourcesActor: ActorRef) extends Actor {
  
  var sparkRef: Option[SparkReference] = Option.empty[SparkReference]
  var eventToSend: Option[Event] = Option.empty[Event]

  def receive = {
    case SentEventToSpark(event) =>
      eventToSend = Some(event)
      resourcesActor ! GetSparkCluster(event.idService)
      context.become(waitingForSparkID)
  }

  def waitingForSparkID: Receive = {
    case SparkForId(sparkReference) =>
      sparkRef = Some(sparkReference)
      sendEventWhenPossible

    case f: Validation => context.parent ! f
  }

  def sendEventWhenPossible =
    if(sparkRef.nonEmpty) {
      val enrichedRes: SparkIDForEvent = SparkIDForEvent(eventToSend.get, sparkRef.get)
      metricsActor ! AddEvent()
      print ("Here we send the event to : " + sparkRef.get.name)
      context.parent ! SparkAndEvent(enrichedRes)
    }
}
