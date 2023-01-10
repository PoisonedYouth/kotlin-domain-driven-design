package com.poisonedyouth

import java.util.*

data class UserDto(
    val username: String,
    val password: String,
)

data class ContactDetailsDto(
    val street: String,
    val streetNumber: String,
    val zipCode: Int,
    val city: String
)

sealed interface PaymentInformationDto{
    val id: UUID

    data class PaypalPaymentInformationDto(
        override val id: UUID,
        val mailAddress: String
    ) : PaymentInformationDto

    data class BankPaymentInformationDto(
        override val id: UUID,
        val iban: String,
        val owner: String
    ) : PaymentInformationDto

    data class CreditCardPaymentInformationDto(
        override val id: UUID,
        val number: String,
        val owner: String,
        val securityNumber: Int
    ) : PaymentInformationDto
}

data class TransactionDto(
    val amount: Double,
    val items: List<String>
)
