package akkord.api

import io.circe.{Json, ParsingFailure}
import io.circe.parser._
import play.api.libs.ws.BodyReadable

trait CirceBodyReadable {
  implicit val circeBodyReadable: BodyReadable[Either[ParsingFailure, Json]] =
    BodyReadable[Either[ParsingFailure, Json]] { response =>
      import play.shaded.ahc.org.asynchttpclient.{ Response => AHCResponse }
      val ahcResponse = response.underlying[AHCResponse]
      val responseBody = ahcResponse.getResponseBody
      parse(responseBody)
  }
}
