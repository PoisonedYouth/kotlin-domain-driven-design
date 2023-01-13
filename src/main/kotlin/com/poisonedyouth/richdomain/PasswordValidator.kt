package com.poisonedyouth.richdomain

private const val MINIMUM_PASSWORD_LENGTH = 16

private val lowerCaseCharacterRegex= Regex("[a-z]+")
private val upperCaseCharacterRegex= Regex("[A-Z]+")
private val specialCharacterRegex= Regex("[!&%?<>-]+")
object PasswordValidator {

    fun validatePassword(password: String) {
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
}
