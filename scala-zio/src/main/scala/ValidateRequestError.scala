package mjs.premsms

sealed trait ValidateRequestError

object SenderUnknownError extends ValidateRequestError

object ProviderUnknownError extends ValidateRequestError

object UnderageError extends ValidateRequestError

object PremiumSmsDisallowedError extends ValidateRequestError

