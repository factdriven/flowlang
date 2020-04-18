package io.factdriven.execution.camunda.model.await_first

import io.factdriven.flow
import java.util.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrieval(fact: RetrievePayment) {

    var id = UUID.randomUUID().toString()
    var total = fact.amount
    var covered = 0F

    companion object {

        init {

            flow<PaymentRetrieval> {

                on command RetrievePayment::class

                await first {
                    on event CreditCardUnvalidated::class
                    execute command ChargeCreditCard::class by {
                        ChargeCreditCard(id, 1F)
                    }
                } or {
                    on event CreditCardValidated::class
                }

                execute command ChargeCreditCard::class by {
                    ChargeCreditCard(id, total - 1F)
                }

                emit event PaymentRetrieved::class by {
                    PaymentRetrieved(total)
                }

            }

        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
class CreditCardUnvalidated
class CreditCardValidated
