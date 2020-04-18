package io.factdriven.execution.camunda.engine.await_first

import io.factdriven.flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrieval(fact: RetrievePayment) {

    val reference = "SomeReference"
    val amount = fact.amount
    var retrieved = false; private set

    fun apply(fact: PaymentRetrieved) {
        retrieved = true
    }

    companion object {

        init {

            flow<PaymentRetrieval> {

                on command RetrievePayment::class

                await first {
                    on event CreditCardUnvalidated::class
                    execute command ChargeCreditCard::class by {
                        ChargeCreditCard(reference, 1F)
                    }
                } or {
                    on event CreditCardValidated::class
                }

                execute command ChargeCreditCard::class by {
                    ChargeCreditCard(reference, amount - 1F)
                }

                emit event PaymentRetrieved::class by {
                    PaymentRetrieved(amount)
                }

            }

        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)

class CreditCardUnvalidated
class CreditCardValidated
