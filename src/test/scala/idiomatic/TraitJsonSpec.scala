package idiomatic

import zio._
import zio.test._
import zio.json._

sealed trait Fruit extends Product with Serializable
case class Banana(curvature: Double) extends Fruit
case class Apple (poison: Boolean)   extends Fruit

object Fruit {
  implicit val decoder: JsonDecoder[Fruit] =
    DeriveJsonDecoder.gen[Fruit]

  implicit val encoder: JsonEncoder[Fruit] =
    DeriveJsonEncoder.gen[Fruit]
}

object TraitJsonSpec extends ZIOSpecDefault {

  def spec = suite("sealed trait decode/encode")(
    test("decode from custom adt") {
      val json =
        """
          |[
          |  {
          |    "Apple": {
          |      "poison": false
          |    }
          |  },
          |  {
          |    "Banana": {
          |      "curvature": 0.5
          |    }
          |  }
          |]
          |""".stripMargin

      val decoded = json.fromJson[List[Fruit]]
      assertTrue(decoded == Right(List(Apple(false), Banana(0.5))))
    },
    test("roundtrip custom atd") {
      val fruits = List(Apple(false), Banana(0.5))
      val json = fruits.toJson
      val roundTrip = json.fromJson[List[Fruit]]
      assertTrue(roundTrip == Right(fruits))
    }
  )
}