package mjs.premsms

sealed trait SendToProviderError

object SenderError extends SendToProviderError

object ProviderUrlNotFoundError extends SendToProviderError

object ProviderError extends SendToProviderError
