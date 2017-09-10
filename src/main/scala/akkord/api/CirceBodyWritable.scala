package akkord.api

import akka.util.ByteString
import io.circe.Json
import play.api.libs.ws.{BodyWritable, InMemoryBody}

trait CirceBodyWritable {
  implicit val circeBodyWritable: BodyWritable[Json] = BodyWritable[Json]({ json =>
    val body = json.toString()
    val byteString = ByteString.fromString(body)
    InMemoryBody(byteString)
  }, "application/json")
}
