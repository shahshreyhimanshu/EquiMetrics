package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.OrderAction
import com.example.model.OrderType
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MarketViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("EquiMetrics", appName)
  }

  @Test
  fun `verify market view model default state and navigation`() {
    val vm = MarketViewModel()
    val state = vm.uiState.value
    assertTrue(state.stocks.isNotEmpty())
    assertTrue(state.indices.isNotEmpty())
    assertEquals(AppScreen.OVERVIEW, state.currentScreen)

    // Test selecting a stock opens STOCK_DETAIL
    vm.selectStock("TCS")
    assertEquals("TCS", vm.uiState.value.selectedStockSymbol)
    assertEquals(AppScreen.STOCK_DETAIL, vm.uiState.value.currentScreen)
  }

  @Test
  fun `verify paper trade order execution without fractional shares`() {
    val vm = MarketViewModel()
    val initialCash = vm.uiState.value.virtualCashBalance
    val buyQty = 10
    val buyPrice = 2900.0

    vm.executePaperOrder(
      symbol = "RELIANCE",
      action = OrderAction.BUY,
      orderType = OrderType.MARKET,
      quantity = buyQty,
      price = buyPrice
    )

    val updatedCash = vm.uiState.value.virtualCashBalance
    assertEquals(initialCash - (buyQty * buyPrice), updatedCash, 0.01)
  }

  @Test
  fun `verify full stock coverage across NSE BSE and search dialog`() {
    val vm = MarketViewModel()
    val state = vm.uiState.value
    assertTrue("Expected at least 115 stocks, found ${state.stocks.size}", state.stocks.size >= 115)

    // Check sectors coverage
    val sectors = state.stocks.map { it.sector }.toSet()
    assertTrue(sectors.any { it.contains("Technology") })
    assertTrue(sectors.any { it.contains("Banking") })
    assertTrue(sectors.any { it.contains("Automobile") })
    assertTrue(sectors.any { it.contains("Pharma") })
    assertTrue(sectors.any { it.contains("Defense") })
    assertTrue(sectors.any { it.contains("Energy") })

    // Test search modal state
    vm.openSearchDialog()
    assertTrue(vm.uiState.value.isSearchDialogOpen)

    vm.updateSearchQuery("INFY")
    assertEquals("INFY", vm.uiState.value.searchQuery)

    vm.selectStockFromSearch("INFY")
    assertEquals("INFY", vm.uiState.value.selectedStockSymbol)
    assertEquals(AppScreen.STOCK_DETAIL, vm.uiState.value.currentScreen)
    assertTrue(!vm.uiState.value.isSearchDialogOpen)
  }

  @Test
  fun `verify AI Suite navigation and tier gating`() {
    val vm = MarketViewModel()
    vm.setScreen(AppScreen.AI_SUITE)
    assertEquals(AppScreen.AI_SUITE, vm.uiState.value.currentScreen)

    // Free tier initially
    assertTrue(!vm.uiState.value.isPro)

    // Upgrade to Pro
    vm.toggleProTier()
    assertTrue(vm.uiState.value.isPro)
  }

  @Test
  fun `verify Broker AA initial holdings for portfolio readings`() {
    val holdings = com.example.data.SampleDataProvider.initialBrokerHoldings
    assertTrue(holdings.isNotEmpty())
    assertTrue(holdings.all { it.totalCurrentValue > 0 })
    assertTrue(holdings.all { it.quantity > 0 })
  }
}

