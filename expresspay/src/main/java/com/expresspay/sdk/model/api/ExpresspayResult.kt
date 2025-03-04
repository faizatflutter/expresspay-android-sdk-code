/*
 * Property of Expresspay (https://expresspay.sa).
 */

package com.expresspay.sdk.model.api

import com.google.gson.annotations.SerializedName

/**
 * Result – value that system returns on request.
 * @see com.expresspay.sdk.model.response.base.result.IExpresspayResult
 *
 * @property result the result value.
 */
enum class ExpresspayResult(val result: String) {
    /**
     * Action was successfully completed  in Payment Platform.
     */
    @SerializedName("SUCCESS")
    SUCCESS("SUCCESS"),

    /**
     * Result of unsuccessful action in Payment Platform.
     */
    @SerializedName("DECLINED")
    DECLINED("DECLINED"),

    /**
     * Additional action required from requester (Redirect to 3ds).
     */
    @SerializedName("REDIRECT")
    REDIRECT("REDIRECT"),

    /**
     * Action was accepted by Payment Platform, but will be completed later.
     */
    @SerializedName("ACCEPTED")
    ACCEPTED("ACCEPTED"),

    /**
     * Request has errors and was not validated by Payment Platform.
     */
    @SerializedName("ERROR")
    ERROR("ERROR"),
}
