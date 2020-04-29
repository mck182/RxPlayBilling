package com.kinedu.rxplaybilling.model

import com.android.billingclient.api.BillingClient

sealed class ConsumptionResponse {
    data class Success(val outToken: String) : ConsumptionResponse()
    data class Failure(
        @BillingClient.BillingResponseCode val billingResponse: Int
    ) : ConsumptionResponse()
}
