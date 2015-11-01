package com.jmlopezm

import akka.io.IO
import com.typesafe.config.ConfigFactory
import spray.can.Http

import akka.actor.{Props, ActorSystem}
import com.jmlopezm.routing.RestSpark
import com.jmlopezm.clients.ResourcesActor

object Boot extends App {
  implicit val system = ActorSystem("rest-SparkStreaming-example")

  val serviceActor = system.actorOf(Props(new RestSpark), name = "rest-routing")


  val resourcesManagerActorSystemConfig = ConfigFactory.load.getConfig("resourcesManagerActorSystem")
  val actorSystemResources = ActorSystem("ResourcesSystem", resourcesManagerActorSystemConfig)
  val localActor = actorSystemResources.actorOf(Props[ResourcesActor], "ResourcesActor")

  system.registerOnTermination {
    system.log.info("Actor per request demo shutdown.")
  }

  IO(Http) ! Http.Bind(serviceActor, "localhost", port = 65432)
}