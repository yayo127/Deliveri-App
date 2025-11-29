package com.example.deliveryapp.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

import com.google.android.gms.location.*
import com.example.deliveryapp.model.DeliveryState
import com.example.deliveryapp.model.OrdersState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class DeliveryViewModel(private  val app: Application) : AndroidViewModel(app) {
    var mLat = 19.4326
    var mLong = -99.1332

    //Funcion para recalcular la distancia con la nueva ubicación
    private fun recalculateWithNewLocation(myLat: Double, myLng: Double) {
        listShort = processAndSort(
            orders = listShort,
            myLat = myLat,
            myLng = myLng
        )

    }
    //Servicio de ubicación
    private val fused: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(app)
    }

    private val _locationState = MutableStateFlow<Location?>(null)
    val locationState: StateFlow<Location?> = _locationState

    private var locationCallback: LocationCallback? = null

    //inicia la ubicación
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {

        val request = LocationRequest.Builder(1000) // Set interval here
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                _locationState.value = result.lastLocation

                // Cada que llega nueva ubicación actualizamos distancias
                result.lastLocation?.let { loc ->
                    recalculateWithNewLocation(
                        myLat = loc.latitude,
                        myLng = loc.longitude
                    )
                }
            }
        }

        fused.requestLocationUpdates(
            request,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    //Detiene la ubicación
    fun stopLocationUpdates() {
        locationCallback?.let { fused.removeLocationUpdates(it) }
    }

    var state by mutableStateOf(DeliveryState())
        private set
    var ordersDeliveredList by mutableStateOf<List<OrdersState>>(emptyList())
        private set
    var listShort by mutableStateOf<List<OrdersState>>(emptyList())
        private set

    // Lista de pedidos dummy
    val listDummynOrders = listOf(
        OrdersState(lat = 20.6739, lng = -103.6307, title = "GDL"),
        OrdersState(lat = 20.6822, lng = -103.4985, title = "ZAP"),
        OrdersState(lat = 20.5932, lng = -103.36179, title = "TLA"),
        OrdersState(lat = 20.6314, lng = -103.3327, title = "TON"),
        OrdersState(lat = 20.4750, lng = -103.4748 , title = "TLAJ"),
    )

    // Función para ordenar la lista de pedidos y agregar la distancia
    fun processAndSort(
        orders: List<OrdersState>,
        myLat: Double,
        myLng: Double
    ): List<OrdersState> {

        return orders.map { order ->
            // 1. Calculamos la distancia para cada objeto
            val distCalculate = calculateDistHaversine(
                lat1 = myLat,
                lon1 = myLng,
                lat2 = order.lat,
                lon2 = order.lng
            )

            // 2. Creamos una copia del objeto con la nueva distancia
            // Usamos .copy() porque las propiedades son 'val' (inmutables)
            order.copy(dist = distCalculate)
        }
            // 3. Ordenamos la lista resultante de menor a mayor distancia
            .sortedBy { it.dist }
    }

    // Función auxiliar matemática para calcular distancia en Kilómetros metodo Heversine
    fun calculateDistHaversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radioTierra = 6371.0 // Radio de la tierra en km

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return ceil(radioTierra * c)
    }
init {
    listShort = processAndSort(
        orders = listDummynOrders,
        myLat = mLat,
        myLng = mLong
    )
}



    // 1. Agregar un elemento específico a la nueva lista
    fun addNewOrder(orden: OrdersState) {
        ordersDeliveredList = ordersDeliveredList + orden
    }

    // Remueve un elemento específico de la nueva lista
    fun removeOrderByIndex(index: Int) {
        val newList = listShort.toMutableList()
        if (index in 0 until newList.size) {
            newList.removeAt(index)
            listShort = newList
        }
    }

    // Regresar a los valores iniciales
    fun resetToInitialState() {
        state = DeliveryState()
        ordersDeliveredList = emptyList()

        listShort = processAndSort(
            orders = listDummynOrders,
            myLat = mLat,
            myLng = mLong
        )
    }


}
