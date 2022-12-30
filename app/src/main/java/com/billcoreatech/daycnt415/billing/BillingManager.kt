package com.billcoreatech.daycnt415.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.*
import com.billcoreatech.daycnt415.R
import com.billcoreatech.daycnt415.util.KakaoToast
import java.text.SimpleDateFormat
import java.util.*

class BillingManager(var mActivity: Activity) : PurchasesUpdatedListener, ConsumeResponseListener {

    var TAG = "BillingManager"
    var mBillingClient: BillingClient

    enum class connectStatusTypes {
        waiting, connected, fail, disconnected
    }

    var connectStatus = connectStatusTypes.waiting

    /**
     * 구글에 설정한 구독 상품 아이디와 일치 하지 않으면 오류를 발생 시킴.
     * 21.04.20 이번에는 1회성 구매로 변경   210414_monthly_bill_999, 210420_monthly_bill
     */
    var punchName = "221230_new_monthly" // ""221230_new_1month" // ""220302_bill_1month_999"
    var payType = BillingClient.ProductType.SUBS // 변경전 SkuType 변경후 ProductType
    var option: SharedPreferences = mActivity.getSharedPreferences("option", Context.MODE_PRIVATE)
    var editor: SharedPreferences.Editor

    init {
        editor = option.edit()
        mBillingClient = BillingClient.newBuilder(mActivity)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        mBillingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.e(TAG, "respCode=" + billingResult.responseCode)
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    connectStatus = connectStatusTypes.connected
                    Log.e(TAG, "connected...")
                    purchaseAsync()
                } else {
                    connectStatus = connectStatusTypes.fail
                    Log.i(TAG, "connected... fail ")
                }
            }

            override fun onBillingServiceDisconnected() {
                connectStatus = connectStatusTypes.disconnected
                Log.i(TAG, "disconnected ")
            }
        })
    }

    /**
     * 정기 결재 소모 여부를 수신 : 21.04.20 1회성 구매의 경우는 결재하면 끝임.
     * @param billingResult
     * @param purchaseToken
     */
    override fun onConsumeResponse(billingResult: BillingResult, purchaseToken: String) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.i(TAG, "사용끝 + $purchaseToken")
            return
        } else {
            Log.i(TAG, "소모에 실패 " + billingResult.responseCode + " 대상 상품 " + purchaseToken)
            return
        }
    }

//    2022.12.30 purchaseProduct 으로 이전
//    private fun purchase(skuDetails: SkuDetails?): Int {
//        val flowParams = BillingFlowParams.newBuilder()
//            .setSkuDetails(skuDetails!!)
//            .build()
//        return mBillingClient.launchBillingFlow(mActivity, flowParams).responseCode
//    }

    private fun purchaseProduct(productDetails: ProductDetails) : BillingResult {

        val productDetailsParamsList = listOf(
            productDetails.subscriptionOfferDetails?.get(0)?.let {
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(it.offerToken)
                    .build()
            }
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList).build()

        return mBillingClient.launchBillingFlow(mActivity, billingFlowParams)
    }

    fun purchaseAsync() {
        Log.e(TAG, "--------------------------------------------------------------")
//        mBillingClient.queryPurchasesAsync(payType)
//  2022.12.30 처리 방법을 변경
        mBillingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(payType).build()
        ) { billingResult, list ->
            Log.e(TAG, "onQueryPurchasesResponse=" + billingResult.responseCode)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            if (list.size < 1) {
                editor = option.edit()
                editor.putBoolean("isBill", false)
                editor.commit()
                Log.e(TAG, "getData=" + list.size)
            } else {
                for (purchase in list) {
                    Log.e(TAG, "getPurchaseToken=" + purchase.purchaseToken)
                    for (str in purchase.skus) {
                        Log.e(TAG, "getSkus=$str")
                    }
                    val now = Date()
                    now.time = purchase.purchaseTime
                    Log.e(TAG, "getPurchaseTime=" + sdf.format(now))
                    Log.e(TAG, "getQuantity=" + purchase.quantity)
                    Log.e(TAG, "getSignature=" + purchase.signature)
                    Log.e(TAG, "isAutoRenewing=" + purchase.isAutoRenewing)
                    Log.e(TAG, "getPurchaseState=" + purchase.purchaseState)
                    editor = option.edit()
                    editor.putBoolean("isBill", purchase.isAutoRenewing)
                    editor.commit()
                }
            }
            Log.e(TAG, "--------------------------------------------------------------")
        }
    }

    val productDetailList: Unit
        get() {

            val productList =
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(punchName)
                        .setProductType(payType)
                        .build()
                )

            Log.e(TAG, "productDetailList ------------------------------- ")

            val params = QueryProductDetailsParams.newBuilder().setProductList(productList)

            mBillingClient.queryProductDetailsAsync(params.build()) {
                    billingResult,
                    productDetailsList ->

//                Log.e(TAG, "$billingResult $productDetailsList")

                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "detail respCode=" + billingResult.responseCode)
                    return@queryProductDetailsAsync
                }
                if (productDetailsList == null) {
                    KakaoToast.makeToast(
                        mActivity,
                        mActivity.getString(R.string.msgNotInfo),
                        Toast.LENGTH_LONG
                    ).show()
                    return@queryProductDetailsAsync
                }
                Log.e(TAG, "listCount=" + productDetailsList.size)
                for (productDetail in productDetailsList) {
                    Log.e(TAG, "\n ${productDetail.productId}" +
                            "\n ${productDetail.title}" +
                            "\n ${productDetail.name}" +
                            "\n ${productDetail.productType}" +
                            "\n ${productDetail.description}" )
                }
                purchaseProduct(productDetailsList[0])
            }

        }
//  2022.12.30 productDetailList 으로 이전
//    val skuDetailList: Unit
//        get() {
//            val skuIdList: MutableList<String> = ArrayList()
//            skuIdList.add(punchName)
//            val params = SkuDetailsParams.newBuilder()
//            params.setSkusList(skuIdList).setType(payType)
//            mBillingClient.querySkuDetailsAsync(
//                params.build(),
//                SkuDetailsResponseListener { billingResult, skuDetailsList ->
//                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
//                        Log.i(TAG, "detail respCode=" + billingResult.responseCode)
//                        return@SkuDetailsResponseListener
//                    }
//                    if (skuDetailsList == null) {
//                        KakaoToast.makeToast(
//                            mActivity,
//                            mActivity.getString(R.string.msgNotInfo),
//                            Toast.LENGTH_LONG
//                        ).show()
//                        return@SkuDetailsResponseListener
//                    }
//                    Log.i(TAG, "listCount=" + skuDetailsList.size)
//                    for (skuDetails in skuDetailsList) {
//                        Log.i(TAG, """
//     ${skuDetails.sku}
//     ${skuDetails.title}
//     ${skuDetails.price}
//     ${skuDetails.description}
//     ${skuDetails.freeTrialPeriod}
//     ${skuDetails.iconUrl}
//     ${skuDetails.introductoryPrice}
//     ${skuDetails.introductoryPriceAmountMicros}
//     ${skuDetails.originalPrice}
//     ${skuDetails.priceCurrencyCode}
//     """.trimIndent()
//                        )
//                    }
//                    purchase(skuDetailsList[0])
//                })
//        }

    /**
     * @param billingResult
     * @param purchases
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult == null) {
            Log.wtf(TAG, "onPurchasesUpdated: null BillingResult")
            return
        }
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(TAG, "onPurchasesUpdated: ${responseCode} ${debugMessage}")
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> if (purchases == null) {
                Log.d(TAG, "onPurchasesUpdated: null purchase list")
                processPurchases(null)
            } else {
                processPurchases(purchases)
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> Log.i(
                TAG,
                "onPurchasesUpdated: User canceled the purchase"
            )
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> Log.i(
                TAG,
                "onPurchasesUpdated: The user already owns this item"
            )
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> Log.e(
                TAG, "onPurchasesUpdated: Developer error means that Google Play " +
                        "does not recognize the configuration. If you are just getting started, " +
                        "make sure you have configured the application correctly in the " +
                        "Google Play Console. The SKU product ID must match and the APK you " +
                        "are using must be signed with release keys."
            )
        }
    }

    private fun processPurchases(purchasesList: List<Purchase>?) {
        if (purchasesList != null) {
            Log.d(TAG, "processPurchases: " + purchasesList.size + " purchase(s)")
        } else {
            Log.d(TAG, "processPurchases: with no purchases")
        }
        if (isUnchangedPurchaseList(purchasesList)) {
            Log.d(TAG, "processPurchases: Purchase list has not changed")
            return
        }
    }

    /**
     * subs 의 경우는 아래와 같이 구매확인을 해 주어야 됨.
     * @param purchase
     */
    fun confirmPerchase(purchase: Purchase) {
        //PURCHASED
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                mBillingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    Log.e(TAG, "getResponseCode=" + billingResult.responseCode)
                    editor.putBoolean("isBill", true)
                    editor.commit()
                }
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            //구매 유예
            Log.e(TAG, "//구매 유예")
        } else {
            //구매확정 취소됨(기타 다양한 사유...)
            Log.e(TAG, "//구매확정 취소됨(기타 다양한 사유...)")
        }
    }

    private fun isUnchangedPurchaseList(purchasesList: List<Purchase>?): Boolean {
        for (purchase in purchasesList!!) {
            confirmPerchase(purchase)
        }
        return false
    }
}