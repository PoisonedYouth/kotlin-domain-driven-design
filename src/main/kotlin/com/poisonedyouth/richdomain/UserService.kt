package com.poisonedyouth.richdomain

import com.poisonedyouth.ContactDetailsDto
import com.poisonedyouth.PaymentInformationDto
import com.poisonedyouth.TransactionDto
import com.poisonedyouth.UserDto
import java.util.*

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
        val user = User.createFrom(userDto, contactDetailsDto, paymentInformationDto)
        return userRepository.save(user)
    }

    override fun updatePassword(username: String, password: String): User {
        val user = userRepository.getBy(username)
        return user.updatePassword(password)
    }

    override fun updateContactDetails(username: String, contactDetailsDto: ContactDetailsDto): User {
        val user = userRepository.getBy(username)
        return user.updateStreet(contactDetailsDto.street)
            .updateStreetNumber(contactDetailsDto.streetNumber)
            .updateCity(contactDetailsDto.city)
            .updateZipCode(contactDetailsDto.zipCode)
    }

    override fun addPaymentInformation(username: String, paymentInformationDto: PaymentInformationDto): User {
        val user = userRepository.getBy(username)
        return when (paymentInformationDto) {
            is PaymentInformationDto.PaypalPaymentInformationDto -> user.addPaypalPayment(
                mailAddress = paymentInformationDto.mailAddress
            )

            is PaymentInformationDto.BankPaymentInformationDto -> user.addBankPayment(
                iban = paymentInformationDto.iban,
                owner = paymentInformationDto.owner
            )

            is PaymentInformationDto.CreditCardPaymentInformationDto -> user.addCreditCardPayment(
                number = paymentInformationDto.number,
                owner = paymentInformationDto.owner,
                securityNumber = paymentInformationDto.securityNumber
            )
        }
    }

    override fun removePaymentInformation(username: String, paymentInformationId: UUID): User {
        val user = userRepository.getBy(username)
        return user.removePaymentInformation(paymentInformationId = paymentInformationId)
    }

    override fun addTransaction(username: String, transactionDto: TransactionDto): User {
        val user = userRepository.getBy(username)
        return user.addTransaction(
            amount = transactionDto.amount,
            items = transactionDto.items
        )
    }

}