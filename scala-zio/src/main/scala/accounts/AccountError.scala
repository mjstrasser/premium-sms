package mjs.premsms
package accounts

sealed trait AccountError

object InsufficientBalanceError extends AccountError
