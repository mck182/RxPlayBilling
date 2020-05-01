package com.kinedu.rxplaybilling

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.kinedu.rxplaybilling.model.ConnectionResult
import com.kinedu.rxplaybilling.model.ConsumptionResponse
import com.kinedu.rxplaybilling.model.PurchaseResponse
import com.kinedu.rxplaybilling.model.PurchasesUpdatedResponse
import com.kinedu.rxplaybilling.model.QueryPurchaseHistoryResponse
import com.kinedu.rxplaybilling.model.QueryPurchasesResponse
import com.kinedu.rxplaybilling.model.SkuDetailsResponse
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

/**
 * Default implementation of the [RxBillingClient] interface. It is backed by the [BillingClient]
 * class of the Play Billing Library.
 */
class DefaultRxBillingClient constructor(
    context: Context
) : RxBillingClient, PurchasesUpdatedListener {

    private val billingClient: BillingClient =
        BillingClient
            .newBuilder(context)
            .enablePendingPurchases()
            .setListener(this)
            .build()

    private val purchasesUpdates = PublishSubject.create<PurchasesUpdatedResponse>()

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            purchasesUpdates.onNext(PurchasesUpdatedResponse.Success(purchases ?: listOf()))
        } else {
            purchasesUpdates.onNext(PurchasesUpdatedResponse.Failure(billingResult.responseCode))
        }
    }

    override fun isReady(): Boolean = billingClient.isReady

    override fun connect(): Observable<ConnectionResult> {
        return Observable.create {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        it.onNext(ConnectionResult.Success)
                    } else {
                        it.onNext(ConnectionResult.Failure(billingResult.responseCode))
                    }
                }

                override fun onBillingServiceDisconnected() {
                    it.onNext(ConnectionResult.Disconnected)
                }
            })
        }
    }

    override fun endConnection() {
        billingClient.endConnection()
    }

    override fun purchasesUpdates(): Observable<PurchasesUpdatedResponse> {
        return purchasesUpdates
    }

    override fun queryInAppPurchases(): Single<QueryPurchasesResponse> {
        return Single.create {
            val result = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                it.onSuccess(QueryPurchasesResponse.Success(result.purchasesList))
            } else {
                it.onSuccess(QueryPurchasesResponse.Failure(result.responseCode))
            }
        }
    }

    override fun querySubscriptionPurchases(): Single<QueryPurchasesResponse> {
        return Single.create {
            val result = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                it.onSuccess(QueryPurchasesResponse.Success(result.purchasesList))
            } else {
                it.onSuccess(QueryPurchasesResponse.Failure(result.responseCode))
            }
        }
    }

    override fun queryInAppSkuDetails(skuList: List<String>): Single<SkuDetailsResponse> {
        return Single.create {
            val params = SkuDetailsParams.newBuilder()
                .setSkusList(skuList)
                .setType(BillingClient.SkuType.INAPP)
                .build()

            billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    it.onSuccess(SkuDetailsResponse.Success(skuDetailsList ?: listOf()))
                } else {
                    it.onSuccess(SkuDetailsResponse.Failure(billingResult.responseCode))
                }
            }
        }
    }

    override fun querySubscriptionsSkuDetails(skuList: List<String>): Single<SkuDetailsResponse> {
        return Single.create {
            val params = SkuDetailsParams.newBuilder()
                .setSkusList(skuList)
                .setType(BillingClient.SkuType.SUBS)
                .build()

            billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    it.onSuccess(SkuDetailsResponse.Success(skuDetailsList ?: listOf()))
                } else {
                    it.onSuccess(SkuDetailsResponse.Failure(billingResult.responseCode))
                }
            }
        }
    }

    override fun queryInAppPurchaseHistory(): Single<QueryPurchaseHistoryResponse> {
        return Single.create {
            billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP) {
                billingResult, purchasesList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    it.onSuccess(QueryPurchaseHistoryResponse.Success(purchasesList ?: listOf()))
                } else {
                    it.onSuccess(QueryPurchaseHistoryResponse.Failure(billingResult.responseCode))
                }
            }
        }
    }

    override fun querySubscriptionPurchaseHistory(): Single<QueryPurchaseHistoryResponse> {
        return Single.create {
            billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS) {
                billingResult, purchasesList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    it.onSuccess(QueryPurchaseHistoryResponse.Success(purchasesList ?: listOf()))
                } else {
                    it.onSuccess(QueryPurchaseHistoryResponse.Failure(billingResult.responseCode))
                }

            }
        }
    }

    override fun consumeItem(purchaseToken: String): Single<ConsumptionResponse> {
        return Single.create {
            val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchaseToken)
                    .build()

            billingClient.consumeAsync(consumeParams) { billingResult, outToken ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    it.onSuccess(ConsumptionResponse.Success(outToken))
                } else {
                    it.onSuccess(ConsumptionResponse.Failure(billingResult.responseCode))
                }
            }
        }
    }

    override fun purchaseItem(skuId: String, activity: Activity): Single<PurchaseResponse> {
        return Single.create {
            queryInAppSkuDetails(listOf(skuId))
                    .flatMap { skuDetailsResponse: SkuDetailsResponse ->
                        if (skuDetailsResponse is SkuDetailsResponse.Success) {
                            if (skuDetailsResponse.skuDetailsList.isEmpty()) {
                                throw Throwable("Sku $skuId doesn't exist!")
                            } else {
                                return@flatMap purchaseSku(skuDetailsResponse.skuDetailsList.first(), activity)
                            }
                        } else {
                            throw Throwable("Failed to query Play Store for $skuId details")
                        }
                    }
        }
    }

    override fun purchaseSubscription(skuId: String, activity: Activity): Single<PurchaseResponse> {
        return Single.create {
            querySubscriptionsSkuDetails(listOf(skuId))
                    .flatMap { skuDetailsResponse: SkuDetailsResponse ->
                        if (skuDetailsResponse is SkuDetailsResponse.Success) {
                            if (skuDetailsResponse.skuDetailsList.isEmpty()) {
                                throw Throwable("Sku $skuId doesn't exist!")
                            } else {
                                return@flatMap purchaseSku(skuDetailsResponse.skuDetailsList.first(), activity)
                            }
                        } else {
                            throw Throwable("Failed to query Play Store for $skuId details")
                        }
                    }
        }
    }

    override fun purchaseSku(skuDetails: SkuDetails, activity: Activity): Single<PurchaseResponse> {
        return Single.create {
            val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build()

            val billingResult = billingClient.launchBillingFlow(activity, flowParams)
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                it.onSuccess(PurchaseResponse.Success)
            } else {
                it.onSuccess(PurchaseResponse.Failure(billingResult.responseCode))
            }
        }
    }

    override fun replaceSubscription(
        oldSkuId: String,
        newSkuId: String,
        purchaseToken: String,
        activity: Activity
    ): Single<PurchaseResponse> {
        return Single.create {
            val flowParams = BillingFlowParams.newBuilder()
                    .setOldSku(oldSkuId, purchaseToken)
                    .setSkuDetails(SkuDetails(newSkuId)) //FIXME
                    .build()
            val billingResult = billingClient.launchBillingFlow(activity, flowParams)
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                it.onSuccess(PurchaseResponse.Success)
            } else {
                it.onSuccess(PurchaseResponse.Failure(billingResult.responseCode))
            }
        }
    }
}
