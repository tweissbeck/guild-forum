package filters

import javax.inject.Inject

import akka.util.ByteString
import play.api.Logger
import play.api.libs.streams.Accumulator
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * Log request time handling
  * @author tweissbeck
  */
class LogFilter @Inject()(implicit ec: ExecutionContext) extends EssentialFilter {
  def apply(nextFilter: EssentialAction) = new EssentialAction {
    def apply(requestHeader: RequestHeader) = {

      val startTime = System.currentTimeMillis

      val accumulator: Accumulator[ByteString, Result] = nextFilter(requestHeader)

      accumulator.map { result =>

        val endTime = System.currentTimeMillis
        val requestTime = endTime - startTime
        val requestUri = requestHeader.uri
        val filteredUri = Seq("/javascripts/", "/images/")
        if (filteredUri.exists(uri => uri.containsSlice(requestUri))) {
          Logger.debug(
            s"${requestHeader.method} $requestUri took ${requestTime}ms and returned ${result.header.status}")
        }
        result
      }
    }
  }
}
