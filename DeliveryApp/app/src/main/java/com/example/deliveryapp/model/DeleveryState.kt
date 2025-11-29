package com.example.deliveryapp.model


data class DeliveryState(
    val lat : Double = 0.0,
    val lng : Double = 0.0,
    val listOrders : List<OrdersState> = listOf(),
)
