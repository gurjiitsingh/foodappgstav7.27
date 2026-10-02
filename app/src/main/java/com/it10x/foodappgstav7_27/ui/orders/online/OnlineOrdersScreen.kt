package com.it10x.foodappgstav7_27.ui.orders.online

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_27.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_27.data.pos.entities.config.OutletEntity
import com.it10x.foodappgstav7_27.printer.PrinterManager
import com.it10x.foodappgstav7_27.viewmodel.OnlineOrdersViewModel
import com.it10x.foodappgstav7_27.viewmodel.RealtimeOrdersViewModel

@Composable
fun OnlineOrdersScreen(

    printerManager: PrinterManager,
    ordersViewModel: OnlineOrdersViewModel,
    realtimeOrdersViewModel: RealtimeOrdersViewModel,
    outlet: OutletEntity?

) {
    var selectedOrder by remember {
        mutableStateOf<OrderMasterData?>(null)
    }

    // =====================================================
    // CURRENCY
    // Same source as LocalOrdersScreen
    // =====================================================

    val currencyCode =
        outlet?.currencyCode ?: "INR"

    val localeTag =
        outlet?.localeTag ?: "en-IN"


    // =====================================================
    // ORDER DETAIL
    // =====================================================

    if (selectedOrder != null) {

        OnlineOrderDetailScreen(
            order = selectedOrder!!,
            ordersViewModel = ordersViewModel,
            realtimeOrdersViewModel = realtimeOrdersViewModel,

            currencyCode = currencyCode,
            localeTag = localeTag,

            onBack = {
                selectedOrder = null
            }
        )

        return
    }


    // =====================================================
    // LOAD ORDERS
    // =====================================================

    LaunchedEffect(Unit) {

        realtimeOrdersViewModel.startListening()

        ordersViewModel.loadFirstPage()
    }


    val pagedOrders by
    ordersViewModel.orders.collectAsState()

    val realtimeOrders by
    realtimeOrdersViewModel.realtimeOrders.collectAsState()

    val loading by
    ordersViewModel.loading.collectAsState()

    val pageIndex by
    ordersViewModel.pageIndex.collectAsState()


    // =====================================================
    // COMBINE + SORT
    // NEWEST -> OLDEST
    // =====================================================

    val combinedOrders = remember(
        realtimeOrders,
        pagedOrders,
        pageIndex
    ) {

        val isFirstPage =
            pageIndex == 0

        val list =
            if (isFirstPage) {

                val realtimeIds =
                    realtimeOrders
                        .map { it.id }
                        .toSet()

                realtimeOrders +
                        pagedOrders.filter {
                            it.id !in realtimeIds
                        }

            } else {

                pagedOrders
            }

        list.sortedByDescending {
            it.createdAtMillis
        }
    }


    // =====================================================
    // LIST STATE
    // =====================================================

    val listState =
        rememberLazyListState()


    // =====================================================
    // NEWEST REALTIME ORDER
    // =====================================================

    val newestRealtimeOrderId =
        realtimeOrders
            .firstOrNull()
            ?.id


    // =====================================================
    // SCROLL TO TOP WHEN NEW ORDER ARRIVES
    // =====================================================

    LaunchedEffect(newestRealtimeOrderId) {

        if (
            newestRealtimeOrderId != null &&
            combinedOrders.isNotEmpty()
        ) {

            listState.animateScrollToItem(0)
        }
    }


    // =====================================================
    // SCREEN
    // =====================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        Text(
            text = "Online Orders",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            Modifier.height(8.dp)
        )


        when {

            loading &&
                    combinedOrders.isEmpty() -> {

                Text("Loading orders...")
            }


            combinedOrders.isEmpty() -> {

                Text("No orders found")
            }


            else -> {

                OnlineOrderTableHeader()


                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f)
                ) {

                    items(
                        items = combinedOrders,
                        key = { it.id }
                    ) { order ->

                        OnlineOrderTableRow(
                            order = order,

                            currencyCode = currencyCode,
                            localeTag = localeTag,

                            onOrderClick = {
                                selectedOrder = order
                            },

                            onPrintClick = {
                                ordersViewModel.printOrder(order)
                            }
                        )
                    }
                }


                Spacer(
                    Modifier.height(10.dp)
                )


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Button(
                        onClick = {
                            ordersViewModel.loadPrevPage()
                        },
                        enabled = !loading
                    ) {

                        Text("← Previous")
                    }


                    Button(
                        onClick = {
                            ordersViewModel.loadNextPage()
                        },
                        enabled = !loading
                    ) {

                        Text("Next →")
                    }
                }
            }
        }
    }
}