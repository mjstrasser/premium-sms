package mjs.premsms
package accounts

sealed trait AccountError

object InsufficientPrepaidFundsError extends AccountError
