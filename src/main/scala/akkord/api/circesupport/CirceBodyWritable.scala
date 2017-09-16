package akkord.api.circesupport

import akka.util.ByteString
import io.circe.Json
import play.api.libs.ws.{BodyWritable, EmptyBody, InMemoryBody}

trait CirceBodyWritable {
  implicit val circeBodyWritable: BodyWritable[Json] = BodyWritable[Json]({ json =>
    if (json.isNull) {
      EmptyBody
    } else {
      val body = json.toString()
      val byteString = ByteString.fromString(body)
      InMemoryBody(byteString)
    }
  }, "application/json")
}
