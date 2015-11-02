package com.jmlopezm

import akka.actor.Address
import com.jmlopezm.domain.Event

// Messages

trait RestMessage

case class SparkAndEvent(response: SparkIDForEvent) extends RestMessage
case class SentEventToSpark(event: Event) extends RestMessage
case class GetCounter() extends RestMessage

// Domain objects
case class SparkReference(name: String, remoteAddress: Address)
case class SparkIDForEvent(event: Event, owner: SparkReference)
case class Error(message: String)
case class Validation(message: String)

// Exceptions

case object EventValidationError extends Exception("Error in the Event.")