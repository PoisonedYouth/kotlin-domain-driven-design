package com.poisonedyouth.richdomain

import com.poisonedyouth.ContactDetailsDto
import com.poisonedyouth.PaymentInformationDto
import com.poisonedyouth.UserDto
import java.util.*

data class User private constructor(
    val username: String,
    val password: String,
    val contactDetails: ContactDetails,
    val paymentInformation: List<PaymentInformation>,
    val transactions: List<Transaction> = emptyList()
) {
    init {
        PasswordValidator.validatePassword(password)

        require(paymentInformation.isNotEmpty()) {
            "There must be at minimum one payment information available."
        }
    }

    fun updatePassword(newPassword: String) = copy(
        password = newPassword
    )

    fun updateStreetNumber(newStreetNumber: String) = copy(
        contactDetails = this.contactDetails.copy(
            streetNumber = newStreetNumber
        )
    )

    fun updateStreet(newStreet: String) = copy(
        contactDetails = this.contactDetails.copy(
            street = newStreet
        )
    )

    fun updateZipCode(newZipCode: Int) = copy(
        contactDetails = this.contactDetails.copy(
            zipCode = newZipCode
        )
    )

    fun updateCity(newCity: String) = copy(
        contactDetails = this.contactDetails.copy(
            city = newCity
        )
    )

    fun addPaypalPayment(mailAddress: String) = copy(
        paymentInformation = this.paymentInformation + PaymentInformation.PaypalPaymentInformation(
            id = UUID.randomUUID(),
            mailAddress = mailAddress
        )
    )

    fun addCreditCardPayment(number: String, owner: String, securityNumber: Int) = copy(
        paymentInformation = this.paymentInformation + PaymentInformation.CreditCardPaymentInformation(
            id = UUID.randomUUID(),
            number = number,
            owner = owner,
            securityNumber = securityNumber
        )
    )

    fun addBankPayment(iban: String, owner: String) = copy(
        paymentInformation = this.paymentInformation + PaymentInformation.BankPaymentInformation(
            id = UUID.randomUUID(),
            iban = iban,
            owner = owner,
        )
    )

    fun removePaymentInformation(paymentInformationId: UUID) = copy(
        paymentInformation = this.paymentInformation.filter { it.id != paymentInformationId }
    )

    fun addTransaction(amount: Double, items: List<String>) = copy(
        transactions = this.transactions + Transaction(
            transactionType = if (amount > 0) TransactionType.BUY else TransactionType.RETOURE,
            amount = amount,
            items = items
        )
    )

    companion object {
        fun createFrom(
            userDto: UserDto, contactDetailsDto: ContactDetailsDto, paymentInformationDto: PaymentInformationDto
        ) = User(
            username = userDto.username,
            password = userDto.password,
            contactDetails = contactDetailsDto.toContactDetails(),
            paymentInformation = listOf(
                paymentInformationDto.toPaymentInformation()
            )
        )

        private fun ContactDetailsDto.toContactDetails() = ContactDetails(
            street = this.street,
            streetNumber = this.streetNumber,
            zipCode = this.zipCode,
            city = this.city
        )

        private fun PaymentInformationDto.toPaymentInformation(): PaymentInformation {
            val uuid = UUID.randomUUID()
            return when (this) {
                is PaymentInformationDto.BankPaymentInformationDto -> PaymentInformation.BankPaymentInformation(
                    id = uuid,
                    iban = this.iban,
                    owner = this.owner
                )

                is PaymentInformationDto.CreditCardPaymentInformationDto -> PaymentInformation.CreditCardPaymentInformation(
                    id = uuid,
                    number = this.number,
                    owner = this.owner,
                    securityNumber = this.securityNumber
                )

                is PaymentInformationDto.PaypalPaymentInformationDto -> PaymentInformation.PaypalPaymentInformation(
                    id = uuid,
                    mailAddress = this.mailAddress
                )
            }
        }
    }
}

data class ContactDetails(
    val street: String,
    val streetNumber: String,
    val zipCode: Int,
    val city: String
) {
    init {
        require(streetNumber.isNotEmpty()) {
            "Street number must not be empty."
        }
        require(street.isNotEmpty()) {
            "Street must not be empty."
        }
        require(zipCode in 10000..99999) {
            "Zip code must be within range of 10.000 to 99.999."
        }
        require(city.isNotEmpty()) {
            "City must not be empty."
        }
    }
}

sealed interface PaymentInformation {
    val id: UUID

    class PaypalPaymentInformation(
        override val id: UUID,
        val mailAddress: String
    ) : PaymentInformation {
        init {
            require(mailAddress.matches(Regex("\"^[A-Za-z](.*)(@)(.+)(\\\\.)(.+)\""))) {
                "Mail address has not the correct format."
            }
        }
    }

    class BankPaymentInformation(
        override val id: UUID,
        val iban: String,
        val owner: String
    ) : PaymentInformation {
        init {
            require(iban.matches(Regex("^DE[0-9]{20}\$"))) {
                "IBAN has not the correct format."
            }
            require(owner.isNotEmpty()) {
                "Bank owner must not be empty."
            }
        }
    }

    class CreditCardPaymentInformation(
        override val id: UUID,
        val number: String,
        val owner: String,
        val securityNumber: Int
    ) : PaymentInformation {

        init {
            require(number.matches(Regex("^[0-9]{4}([ -]?[0-9]{4}){3}\$")))
            {
                "Credit card number has not the correct format."
            }
            require(owner.isNotEmpty())
            {
                "Credit card owner must not be empty."
            }
        }
    }
}

enum class TransactionType {
    BUY,
    RETOURE
}

class Transaction(
    val transactionType: TransactionType,
    val amount: Double,
    val items: List<String>
){
    init {
        require(items.isNotEmpty()) {
            "Items must not be empty."
        }
    }
}
