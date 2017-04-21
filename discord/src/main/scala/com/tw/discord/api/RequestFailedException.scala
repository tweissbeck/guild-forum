package com.tw.discord.api


class RequestFailedException(val status: Int, val url: String, val details: Option[String]= None) extends Exception{


}
