package com.kinedu.rxplaybilling.model

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase

sealed class PurchasesUpdatedResponse {
    data class Success(val items: List<Purchase>) : PurchasesUpdatedResponse()
    data class Failure(
        @BillingClient.BillingResponseCode val billingResponse: Int
    ) : PurchasesUpdatedResponse()
}
