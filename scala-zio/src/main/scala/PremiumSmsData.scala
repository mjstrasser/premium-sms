package mjs.premsms

import providers.Provider
import senders.Sender

/**
 * Container of data used to send a Premium SMS message.
 *
 * @param request  the incoming request
 * @param sender   the sender whose msisdn is in the reuqest
 * @param provider the provider whose number is in the request
 */
case class PremiumSmsData(request: PremiumSmsRequest,
                          sender: Sender,
                          provider: Provider)
