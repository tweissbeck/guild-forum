package com.tw.discord.api


/**
  * OAuth2 access token <br/>
  * If the access token request is valid and authorized, the
  * authorization server issues an access token and optional refresh
  * token
  *
  * @param accessToken  The access token issued by the authorization server.
  * @param expireIn     The lifetime in seconds of the access token.  For
  *                     example, the value "3600" denotes that the access token will
  *                     expire in one hour from the time the response was generated.
  *                     If omitted, the authorization server SHOULD provide the
  *                     expiration time via other means or document the default value.
  * @param refreshToken The refresh token, which can be used to obtain new
  *                     access tokens using the same authorization grant
  * @param tokenType    The type of the token issued
  */
case class AccessToken(accessToken: String, expireIn: Long, refreshToken: String, tokenType: String)




