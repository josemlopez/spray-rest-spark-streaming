package com.jmlopezm.routing

import akka.testkit.{TestActorRef, TestProbe}
import com.jmlopezm._
import org.scalatest.{Matchers, FlatSpec}
import spray.routing._
import spray.testkit.ScalatestRouteTest

class RestRoutingSpec extends FlatSpec with ScalatestRouteTest with Matchers {

  val petsWithOwnerService = TestProbe()

  def restRouting = TestActorRef(new RestSpark() {
    override def petsWithOwner(message : RestMessage): Route =
      ctx => perRequestSupervisorActor(ctx, petsWithOwnerService.ref, message)
  })

	"RestRouting" should "get Pets" in {
    val getAnimals = Get("/pets?names=PetName") ~> restRouting.underlyingActor.route

    petsWithOwnerService.expectMsg(GetPetsWithOwners("PetName" :: Nil))
    petsWithOwnerService.reply(SparkAndEvent(SparkIDForEvent("PetName", SparkReference("OwnerName")) :: Nil))

    getAnimals ~> check {
      responseAs[String] should equal("""{"pets":[{"name":"PetName","owner":{"name":"OwnerName"}}]}""")
    }
  }
}