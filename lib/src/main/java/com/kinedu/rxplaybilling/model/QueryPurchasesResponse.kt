package com.kinedu.rxplaybilling.model

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PurchaseHistoryRecord

sealed class QueryPurchasesResponse {
    data class Success(val purchaseList: List<PurchaseHistoryRecord>) : QueryPurchasesResponse()
    data class Failure(
        @BillingClient.BillingResponseCode val billingResponse: Int
    ) : QueryPurchasesResponse()
}
