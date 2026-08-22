package application.poligraf.engine.billing

import kotlinx.coroutines.flow.StateFlow

interface BillingManager {
    val isProActive: StateFlow<Boolean>
    val availableProducts: StateFlow<List<ProductInfo>>
    
    suspend fun purchase(productId: String): Result<Unit>
    suspend fun restorePurchases(): Result<Unit>
}

data class ProductInfo(
    val id: String,
    val title: String,
    val description: String,
    val price: String
)

class StubBillingManager : BillingManager {
    override val isProActive = kotlinx.coroutines.flow.MutableStateFlow(false)
    override val availableProducts = kotlinx.coroutines.flow.MutableStateFlow(emptyList<ProductInfo>())
    
    override suspend fun purchase(productId: String): Result<Unit> = Result.success(Unit)
    override suspend fun restorePurchases(): Result<Unit> = Result.success(Unit)
}
