import io.factdriven.flowlang.execute

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow5 = execute<PaymentRetrieval> {
    on message type(RetrievePayment::class) milestone { message ->
        PaymentRetrievalAccepted(paymentId = message.accountId)
    }
    execute service {
        produce intent {
            WithdrawAmount(reference = status.paymentId, payment = status.uncovered)
        }
        on message type(AmountWithdrawn::class) having { "reference" to status.paymentId } success {}
        on compensation {
            produce intent {
                CreditAmount(reference = status.paymentId)
            }
        }
    }
    select one { labeled("Payment covered?")
        given { status.uncovered > 0 } execute { labeled("No")
            execute service {
                produce intent {
                    ChargeCreditCard(reference = status.paymentId, payment = status.uncovered)
                }
                on message type(CreditCardCharged::class) having { "reference" to status.paymentId } success {}
                on message type(CreditCardExpired::class) having { "reference" to status.paymentId } mitigation {
                    execute receive {
                        on message type(CreditCardDetailsUpdated::class) having { "reference" to status.accountId } retry {}
                        on timeout "P14D" failure {
                            PaymentFailed(status.paymentId)
                        }
                    }
                }
            }
        }
    }
    produce success {
        PaymentRetrieved(status.paymentId)
    }
}
