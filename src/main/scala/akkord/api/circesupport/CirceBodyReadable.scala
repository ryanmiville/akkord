package akkord.api.circesupport

import io.circe.Json
import io.circe.parser._
import play.api.libs.ws.BodyReadable

trait CirceBodyReadable {
  implicit val circeBodyReadable: BodyReadable[Json] =
    BodyReadable[Json] { response =>
      import play.shaded.ahc.org.asynchttpclient.{Response => AHCResponse}
      val ahcResponse = response.underlying[AHCResponse]
      val responseBody = ahcResponse.getResponseBody
      parse(responseBody).toOption.get
  }
}
