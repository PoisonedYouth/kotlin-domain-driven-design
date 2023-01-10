package com.poisonedyouth.anemic

import org.javamoney.moneta.FastMoney
import java.util.*

data class User(
    val username: String,
    val password: String,
    val contactDetails: ContactDetails,
    val paymentInformation: List<PaymentInformation>,
    val transactions: List<Transaction> = emptyList()
)

data class ContactDetails(
    val street: String,
    val streetNumber: String,
    val zipCode: Int,
    val city: String
)

sealed interface PaymentInformation {
    val id: UUID

    data class PaypalPaymentInformation(
        override val id: UUID,
        val mailAddress: String
    ) : PaymentInformation

    data class BankPaymentInformation(
        override val id: UUID,
        val iban: String,
        val owner: String
    ) : PaymentInformation

    data class CreditCardPaymentInformation(
        override val id: UUID,
        val number: String,
        val owner: String,
        val securityNumber: Int
    ) : PaymentInformation
}

enum class TransactionType {
    BUY,
    RETOURE
}

data class Transaction(
    val transactionType: TransactionType,
    val amount: FastMoney,
    val items: List<String>
)
