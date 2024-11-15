package com.sagar.fluenty.ui.utils

import com.google.gson.Gson

data class ErrorResponse(
    val error: Error? = null
)

data class Error(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)
fun extractErrorMessage(errorString: String): String {
    // Assuming the JSON response starts after "Unexpected Response:"
    val startIndex = errorString.indexOf("{")
    val endIndex = errorString.lastIndexOf("}")

    if (startIndex != -1 && endIndex != -1) {
        val jsonPart = errorString.substring(startIndex, endIndex + 1)

        val gson = Gson()
        val errorResponse = gson.fromJson(jsonPart, ErrorResponse::class.java)

        return errorResponse.error?.message ?: "Unknown error occurred"
    } else {
        return "Unknown error occurred"
    }
}