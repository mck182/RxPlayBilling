package com.kinedu.rxplaybilling.model

import com.android.billingclient.api.BillingClient

sealed class PurchaseResponse {
    object Success : PurchaseResponse()
    data class Failure(@BillingClient.BillingResponseCode val billingResponse: Int) : PurchaseResponse()
}
