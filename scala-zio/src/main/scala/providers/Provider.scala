package mjs.premsms
package providers

import java.util.UUID

case class Provider(id: UUID,
                    number: String,
                    name: String,
                    cost: BigDecimal,
                    minimumAge: Int)
