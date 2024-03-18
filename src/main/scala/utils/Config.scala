package utils

import com.gu.conf.{ConfigurationLoader, SSMConfigurationLocation}
import com.gu.{AppIdentity, AwsIdentity, DevIdentity}
import com.typesafe.config.Config

import scala.util.{Success, Try}

object Config {
  def fetchConfig():Try[Config] = {
    val CredentialsProvider = AWSUtils.credsProvider
    val isDev = Option(System.getenv("DEV_MODE")).isDefined || Option(System.getProperty("DEV_MODE")).isDefined

    for {
      identity <- if (isDev)
        Success(DevIdentity("concierge-graphql"))
      else
        AppIdentity.whoAmI(defaultAppName = "concierge-graphql", CredentialsProvider)
      config <- Try(ConfigurationLoader.load(identity, CredentialsProvider) {
        case identity: AwsIdentity => SSMConfigurationLocation.default(identity)
      })
    } yield config
  }
}
