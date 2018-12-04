package io.factdriven.flowlang.example.paymentretrieval

import io.factdriven.flowlang.execute

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow4 = execute <PaymentRetrieval> {
    on message type(RetrievePayment::class) create acceptance("Payment retrieval accepted") by { message ->
        PaymentRetrievalAccepted(paymentId = message.accountId)
    }
    execute service {
        create intent("Withdraw amount") by {
            WithdrawAmount(
                reference = status.paymentId,
                payment = status.uncovered
            )
        }
        on message pattern(AmountWithdrawn(reference = status.paymentId)) create success("Amount withdrawn")
    }
    select one {
        topic("Payment covered?")
        given("No") { status.uncovered > 0 } execute service {
            create intent("Charge credit card") by {
                ChargeCreditCard(
                    reference = status.paymentId,
                    payment = status.uncovered
                )
            }
            on message type(CreditCardCharged::class) having { "reference" to status.paymentId } create success()
            on message type(CreditCardExpired::class) having { "reference" to status.paymentId } mitigation {
                execute receive {
                    on message type(CreditCardDetailsUpdated::class) having { "reference" to status.accountId } create fix()
                    on timeout "P14D" create failure() by {
                        PaymentFailed(status.paymentId)
                    }
                }
            }
        }
    }
    create success("Payment retrieved") by {
        PaymentRetrieved(status.paymentId)
    }
}
