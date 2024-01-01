package com.mvukosav.scoreagentsvas.utils.exceptions

/**
 * Exception thrown when API call fails due to connectivity problem.
 */
class NoInternetException(val failedRequestInfo: String) : Exception() {
    override val message: String
        get() = failedRequestInfo + (super.message ?: "")
}
