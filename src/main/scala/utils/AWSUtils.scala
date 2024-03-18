package utils

import software.amazon.awssdk.auth.credentials._

object AWSUtils {
  val credsProvider: AwsCredentialsProviderChain = AwsCredentialsProviderChain.builder().credentialsProviders(
    InstanceProfileCredentialsProvider.create(),
    ProfileCredentialsProvider.create("capi"),
    SystemPropertyCredentialsProvider.create(),
    EnvironmentVariableCredentialsProvider.create()
  ).build()
}
