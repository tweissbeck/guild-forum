import java.security._
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import javax.xml.bind.DatatypeConverter

import com.typesafe.config.ConfigFactory
import org.jose4j.jws.{AlgorithmIdentifiers, JsonWebSignature}
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.{InvalidJwtException, JwtConsumerBuilder}
import play.api.Logger
import play.api.mvc.Cookie
import services.database.User
import services.{IdEncryptionUtil, KeyUser}

/**
 * Created by tweissbeck on 05/08/2016.
 */
package object controllers {

  /**
   * Helper to build a cookie that contains authentication data
   *
   */
  object AuthenticationCookie {
    val NAME = "token"

    def generateCookie(user: User): Cookie = Cookie(NAME, JWT.build(user), Some(16000), secure = false, httpOnly = true)
  }


  /**
   * JWT builder and validato
   */
  object JWT extends KeyUser {


    val keys: (Key, Key) = {
      val k = KeyFactory.getInstance("RSA")
      val privateKeyAsString = ConfigFactory.load().getString("crypto.key.priv.jwt")
      val publicKeyAsString = ConfigFactory.load().getString("crypto.key.pub.jwt")
      val privateKeyBytes = DatatypeConverter.parseHexBinary(privateKeyAsString)
      val publicKeyAsBytes = DatatypeConverter.parseHexBinary(publicKeyAsString)
      val priv = KeyFactory.getInstance("RSA")
        .generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes))
      val pub = k.generatePublic(new X509EncodedKeySpec(publicKeyAsBytes))
      (priv, pub)
    }
    val privateKey: Key = keys._1
    val publicKey: Key = keys._2

    val ISSUER: String = "#GUILD-FORUM"

    /**
     * Build a JSON WEB TOKEN. The JWT is only signed for now.<br/>
     * For reference : [[https://bitbucket.org/b_c/jose4j/wiki/JWT%20Examples]]
     *
     * @param user the user to authenticate in the JWT
     * @return the generated JWT as String
     */
    def build(user: User): String = {
      /**
       * Build claim
       *
       * @return the built claim
       */
      def buildClaim(): JwtClaims = {
        // Create the Claims, which will be the content of the JWT
        val claims = new JwtClaims();
        claims.setIssuer(ISSUER); // who creates the token and signs it
        claims.setExpirationTimeMinutesInTheFuture(60 * 12); // time when the token will expire (12 hours)
        claims.setGeneratedJwtId(); // a unique identifier for the token
        claims.setIssuedAtToNow(); // when the token was issued/created (now)
        claims.setNotBeforeMinutesInThePast(2); // time before which the token is not yet valid (2 minutes ago)
        claims.setSubject(s"${user.firstName} ${user.lastName}"); // the subject/principal is whom the token is about
        claims.setClaim("email", user.mail);
        // additional claims/attributes about the subject can be added
        claims.setClaim("id", IdEncryptionUtil.encode(user.id))
        claims
      }

      // A JWT is a JWS and/or a JWE with JSON claims as the payload.
      // In this example it is a JWS so we create a JsonWebSignature object.
      val jws = new JsonWebSignature();

      // The payload of the JWS is JSON content of the JWT Claims
      jws.setPayload(buildClaim().toJson());

      // The JWT is signed using the private key
      jws.setKey(privateKey);

      // Set the Key ID (kid) header because it's just the polite thing to do.
      // We only have one key in this example but a using a Key ID helps
      // facilitate a smooth key rollover process
      //jws.setKeyIdHeaderValue(privateKey.getKeyId());

      // Set the signature algorithm on the JWT/JWS that will integrity protect the claims
      jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

      // Sign the JWS and produce the compact serialization or the complete JWT/JWS
      // representation, which is a string consisting of three dot ('.') separated
      // base64url-encoded parts in the form Header.Payload.Signature
      // If you wanted to encrypt it, you can simply set this jwt as the payload
      // of a JsonWebEncryption object and set the cty (Content Type) header to "jwt".
      jws.getCompactSerialization();

    }

    /**
     * Validate the JWT and return the user id when possible.
     *
     * @param jwt the JWT as String
     * @return
     */
    def validateJWT(jwt: String): Option[Long] = {
      // Use JwtConsumerBuilder to construct an appropriate JwtConsumer, which will
      // be used to validate and process the JWT.
      // The specific validation requirements for a JWT are context dependent, however,
      // it typically advisable to require a (reasonable) expiration time, a trusted issuer, and
      // and audience that identifies your system as the intended recipient.
      // If the JWT is encrypted too, you need only provide a decryption key or
      // decryption key resolver to the builder.
      val jwtConsumer = new JwtConsumerBuilder()
        .setRequireExpirationTime() // the JWT must have an expiration time
        .setMaxFutureValidityInMinutes(48 * 60) // but the  expiration time can't be too crazy
        .setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
        .setRequireSubject() // the JWT must have a subject claim
        .setExpectedIssuer(ISSUER) // whom the JWT needs to have been issued by
        .setVerificationKey(publicKey) // verify the signature with the public key
        .build(); // create the JwtConsumer instance

      try {
        //  Validate the JWT and process it to the Claims
        val jwtClaims = jwtConsumer.processToClaims(jwt)
        Logger.info(s"JWT is valid, user: ${jwtClaims.getSubject} - ${jwtClaims.getClaimValue("id")}")
        val userId: Long = IdEncryptionUtil.decodeLong(jwtClaims.getStringClaimValue("id"))
        Some(userId)
      }
      catch {
        case e: InvalidJwtException => {
          // InvalidJwtException will be thrown, if the JWT failed processing or validation in anyway.
          // Hopefully with meaningful explanations(s) about what went wrong.
          Logger.info("Invalid JWT! " + e.getMessage);
          None
        }
      }
    }
  }

}
