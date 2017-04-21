package com.tw.discord.api


class RequestFailedException(val status: Int, val url: String, val details: Option[String] = None
                            )
  extends Exception(s"$url $status $details") {

}

object RequestFailedException {
  def apply(status: Int, url: String) = new RequestFailedException(status, url, None)

  def apply(status: Int, url: String, details: String) = new RequestFailedException(status, url, Some(details))
}
