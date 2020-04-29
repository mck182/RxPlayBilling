package com.kinedu.rxplaybilling.model

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PurchaseHistoryRecord

sealed class QueryPurchaseHistoryResponse {
    data class Success(val purchaseList: List<PurchaseHistoryRecord>) : QueryPurchaseHistoryResponse()
    data class Failure(
        @BillingClient.BillingResponseCode val billingResponse: Int
    ) : QueryPurchaseHistoryResponse()
}
