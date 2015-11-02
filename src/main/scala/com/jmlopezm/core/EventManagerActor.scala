package com.jmlopezm.core

import akka.actor.{ActorLogging, Actor, ActorRef}
import com.jmlopezm.{SparkReference, _}
import com.jmlopezm.clients.MetricsActor.AddEvent
import com.jmlopezm.clients.ResourcesActor.{GetSparkCluster, SparkForId}
import com.jmlopezm.domain.Event

/**
 * This actor is the center of the action. He is in charge of asking about the Spark where to send the event and send if
 * if is possible
 */
class EventManagerActor(metricsActor: ActorRef, resourcesActor: ActorRef) extends Actor with ActorLogging {
  
  var sparkRef: Option[SparkReference] = Option.empty[SparkReference]
  var eventToSend: Option[Event] = Option.empty[Event]

  def receive = {
    case SentEventToSpark(event) =>
      log.debug("We'll get the SparkID and send the message to the Actor of this Spark")
      eventToSend = Some(event)
      resourcesActor ! GetSparkCluster(event.idService)
      context.become(waitingForSparkID)
  }

  def waitingForSparkID: Receive = {
    case SparkForId(sparkReference) =>
      sparkRef = sparkReference
      log.debug("It appear that we have the reference and here it's: " + sparkRef)
      sendEventWhenPossible

    case f: Validation => context.parent ! f
  }

  def sendEventWhenPossible =
    if(sparkRef.nonEmpty) {
      val enrichedRes: SparkIDForEvent = SparkIDForEvent(eventToSend.get, sparkRef.get)
      metricsActor ! AddEvent()
      print ("We have sent the event to : " + sparkRef.get.name)
      context.parent ! SparkAndEvent(enrichedRes)
    } else {
      val errorMessage = "There is no Spark Actor for this ID: " + eventToSend.get.idService
      log.debug(errorMessage)
      context.parent ! Error(errorMessage)
    }
}
