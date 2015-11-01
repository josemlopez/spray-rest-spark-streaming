package com.jmlopezm.clients

import akka.actor.{Actor}
import akka.remote.RemotingLifecycleEvent
import com.jmlopezm._
import com.jmlopezm.clients.EventValidator.ValidateEvent
import com.jmlopezm.clients.MetricsActor._
import com.jmlopezm.clients.ResourcesActor._
import com.jmlopezm.domain.Event
import scala.collection.parallel.mutable.ParHashMap

/**
 * This class will take the count of the number of Events
 */
class MetricsActor extends Actor {
  def receive = {
    case AddEvent() => numEvents += 1
  }
}

object MetricsActor {
  private var numEvents = 0
  case class AddEvent()
}

/**
 * This class will be the connection point of the Spark Streaming Jobs and will be in charge of the management of that connections
 */
class ResourcesActor extends Actor {

  val sparksRefContainer = ParHashMap[String, SparkReference]()

  override def preStart() = {
    context.system.eventStream.subscribe(context.self, classOf[RemotingLifecycleEvent])
  }

  def receive = {
    case ReadyToGo(sparkId)  => 
      sparksRefContainer += sparkId -> SparkReference ("New Spark:" + sparkId, sender ().path.address)

    case GetSparkCluster(sparkId) =>
      sender ! SparkForId(sparksRefContainer.get(sparkId).get)

    case event : akka.remote.DisassociatedEvent =>
      val pair = sparksRefContainer.find( ref => ref._2.remoteAddress == event.remoteAddress)
      pair.get match {
        case p : (String, SparkReference) => sparksRefContainer -= p._1
        case _ => print ("I have receive a death of an Actor but he is not in my SparkRef!!!")
      }
  }
}

object ResourcesActor {
  case class GetSparkCluster(sparkId: String)
  case class SparkForId(sparkRef: SparkReference)
  case class ReadyToGo(sparkId: String)
}



/**
 * EventValidator is in charge of the validation of the message received
 *
 */
class EventValidator extends Actor {
  def receive = {
    case ValidateEvent(event) =>
      event.line.contains("error") match {
        case true => sender ! Validation("error!!")
        case _ => sender ! Validation("bien")
      }
  }
}

object EventValidator {
  case class ValidateEvent(event: Event)
}