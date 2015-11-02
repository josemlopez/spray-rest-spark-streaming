package com.jmlopezm.routing

import akka.testkit.{TestActorRef, TestProbe}
import com.jmlopezm._
import org.scalatest.{Matchers, FlatSpec}
import spray.routing._
import spray.testkit.ScalatestRouteTest

class RestRoutingSpec extends FlatSpec with ScalatestRouteTest with Matchers{

  val sparkService = TestProbe()

  def restRouting = TestActorRef(new RestSpark() {
    override def sparkRoute(message : RestMessage): Route =
      ctx => perRequestSupervisorActorCreator(ctx, sparkService.ref, message)
  })

  "The Post Service" should "return 'bien' for POST request on /" in {
    Post("/", """{"idService": "id1", "line" : "string to check"}"""   ) ~> restRouting.underlyingActor.route ~> check{
      responseAs[String] should be ("bien")
    }
  }

}