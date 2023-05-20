import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._

case class test (a: String, b: Int, c: List[Int], d: Map[String, Int])
val t = test("a", 10, List(1,2,3), Map("a"->1, "b"->2,"c"->3))

val testJson: Json = t.asJson

println(testJson.asString)



val t2 = testJson.as[test]

t2