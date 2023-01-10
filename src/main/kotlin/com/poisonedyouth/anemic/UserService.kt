package com.poisonedyouth.anemic

import com.poisonedyouth.ContactDetailsDto
import com.poisonedyouth.PaymentInformationDto
import com.poisonedyouth.TransactionDto
import com.poisonedyouth.UserDto
import nl.hiddewieringa.money.ofCurrency
import java.util.*

private const val MINIMUM_PASSWORD_LENGTH = 16

private val lowerCaseCharacterRegex = Regex("[a-z]+")
private val upperCaseCharacterRegex = Regex("[A-Z]+")
private val specialCharacterRegex = Regex("[!&%?<>-]+")


interface UserService {
    fun createUser(userDto: UserDto, contactDetailsDto: ContactDetailsDto, paymentInformationDto: PaymentInformationDto): User
    fun updatePassword(username: String, password: String): User
    fun updateContactDetails(username: String, contactDetailsDto: ContactDetailsDto): User
    fun addPaymentInformation(username: String, paymentInformationDto: PaymentInformationDto): User
    fun removePaymentInformation(username: String, paymentInformationId: UUID): User
    fun addTransaction(username: String, transactionDto: TransactionDto): User
}

class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {
    override fun createUser(userDto: UserDto, contactDetailsDto: ContactDetailsDto, paymentInformationDto: PaymentInformationDto): User {
        if (userRepository.isUsernameAlreadyUsed(userDto.username)) {
            throw IllegalArgumentException("User with username '${userDto.username}' already exists.")
        }
        validatePassword(userDto.password)
        validateContactDetails(contactDetailsDto)
        validatePaymentInformation(paymentInformationDto)

        return userRepository.save(mapToUser(userDto, contactDetailsDto, paymentInformationDto))
    }

    private fun mapToUser(userDto: UserDto, contactDetailsDto: ContactDetailsDto, paymentInformationDto: PaymentInformationDto): User {
        return User(
            username = userDto.username,
            password = userDto.password,
            contactDetails = ContactDetails(
                street = contactDetailsDto.street,
                streetNumber = contactDetailsDto.streetNumber,
                zipCode = contactDetailsDto.zipCode,
                city = contactDetailsDto.city
            ),
            paymentInformation = listOf(
                paymentInformationDto.toPaymentInformation()
            )
        )
    }

    private fun validatePassword(password: String) {
        require(password.length >= MINIMUM_PASSWORD_LENGTH) {
            "Password (current: ${password.length}) must be at minimum 16 characters long."
        }
        require(password.contains(lowerCaseCharacterRegex)) {
            "Password must contain at minimum 1 lowercase character."
        }
        require(password.contains(upperCaseCharacterRegex)) {
            "Password must contain at minimum 1 uppercase character."
        }
        require(password.contains(specialCharacterRegex)) {
            "Password must contain at minimum 1 special character of (!&%?<>-)."
        }
    }

    private fun validateContactDetails(contactDetailsDto: ContactDetailsDto) {
        require(contactDetailsDto.streetNumber.isNotEmpty()) {
            "Street number must not be empty."
        }
        require(contactDetailsDto.street.isNotEmpty()) {
            "Street must not be empty."
        }
        require(contactDetailsDto.zipCode in 10000..99999) {
            "Zip code must be within range of 10.000 to 99.999."
        }
        require(contactDetailsDto.city.isNotEmpty()) {
            "City must not be empty."
        }
    }

    private fun validatePaymentInformation(paymentInformationDto: PaymentInformationDto) {
        when (paymentInformationDto) {
            is PaymentInformationDto.BankPaymentInformationDto -> {
                require(paymentInformationDto.iban.matches(Regex("^DE[0-9]{20}\$"))) {
                    "IBAN has not the correct format."
                }
                require(paymentInformationDto.owner.isNotEmpty()) {
                    "Bank owner must not be empty."
                }
            }

            is PaymentInformationDto.CreditCardPaymentInformationDto -> {
                require(paymentInformationDto.number.matches(Regex("^[0-9]{4}([ -]?[0-9]{4}){3}\$"))) {
                    "Credit card number has not the correct format."
                }
                require(paymentInformationDto.owner.isNotEmpty()) {
                    "Credit card owner must not be empty."
                }
            }

            is PaymentInformationDto.PaypalPaymentInformationDto -> {
                require(paymentInformationDto.mailAddress.matches(Regex("\"^[A-Za-z](.*)(@)(.+)(\\\\.)(.+)\""))) {
                    "Mail address has not the correct format."
                }
            }
        }
    }

    override fun updatePassword(username: String, password: String): User {
        val user = userRepository.findBy(username)
        require(user != null) {
            "User with username '$username' not found."
        }

        validatePassword(password)

        val updatedUser = user.copy(
            password = password
        )
        return userRepository.update(updatedUser)
    }

    override fun updateContactDetails(username: String, contactDetailsDto: ContactDetailsDto): User {
        val user = userRepository.findBy(username)
        require(user != null) {
            "User with username '$username' not found."
        }

        validateContactDetails(contactDetailsDto)

        val updatedUser = user.copy(
            contactDetails = contactDetailsDto.toContactDetails()
        )
        return userRepository.update(updatedUser)
    }

    override fun addPaymentInformation(username: String, paymentInformationDto: PaymentInformationDto): User {
        val user = userRepository.findBy(username)
        require(user != null) {
            "User with username '$username' not found."
        }

        validatePaymentInformation(paymentInformationDto)

        val updatedUser = user.copy(
            paymentInformation = user.paymentInformation + paymentInformationDto.toPaymentInformation()
        )
        return userRepository.update(updatedUser)
    }

    override fun removePaymentInformation(username: String, paymentInformationId: UUID): User {
        val user = userRepository.findBy(username)
        require(user != null) {
            "User with username '$username' not found."
        }

        val paymentInformation = user.paymentInformation.find { it.id == paymentInformationId }
        require(paymentInformation != null) {
            "No paymentInformation found with id '$paymentInformationId'."
        }
        val updatedUser = user.copy(
            paymentInformation = user.paymentInformation - paymentInformation
        )
        return userRepository.update(updatedUser)
    }

    override fun addTransaction(username: String, transactionDto: TransactionDto): User {
        val user = userRepository.findBy(username)
        require(user != null) {
            "User with username '$username' not found."
        }
        validateTransaction(transactionDto)

        val updatedUser = user.copy(
            transactions = user.transactions + transactionDto.toTransaction()
        )
        return userRepository.update(updatedUser)
    }

    private fun validateTransaction(transactionDto: TransactionDto) {
        require(transactionDto.items.isNotEmpty()) {
            "Items must not be empty."
        }
    }

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

    private fun TransactionDto.toTransaction() = Transaction(
        transactionType = if (this.amount > 0.0) TransactionType.BUY else TransactionType.RETOURE,
        amount = this.amount.ofCurrency("EUR"),
        items = this.items
    )
}