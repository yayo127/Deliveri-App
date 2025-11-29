package com.example.deliveryapp.viewModel

import android.app.Application
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.deliveryapp.model.OrdersState
import com.google.android.gms.location.LocationServices
import org.junit.Before
import org.mockito.Mockito.mock

class DeliveryViewModelTest {

    // Necesitamos un Application mock
    private lateinit var mockApplication: Application
    private lateinit var viewmodel: DeliveryViewModel

    // Datos de prueba
    private val ordersTestList = listOf(
        OrdersState(lat = 20.6597, lng = -103.3496, title = "GDL", dist = 0.0), // ~520 km
        OrdersState(lat = 19.4326, lng = -99.1330, title = "CDMX", dist = 0.0),  // ~0.0 km
        OrdersState(lat = 25.6866, lng = -100.3161, title = "MTY", dist = 0.0), // ~720 km
    )

    // Coordenadas de prueba (Ciudad de México)
    private val testLat = 19.4326
    private val testLng = -99.1332

    @Before
    fun setUp() {
        // Inicializa un mock de Application
        mockApplication = mock(Application::class.java)
        // Inicializa el ViewModel con el mock.
        viewmodel = DeliveryViewModel(mockApplication)

        // Reiniciamos el estado del ViewModel a los valores iniciales de prueba
        viewmodel.resetToInitialState()
    }

    // --- Pruebas de Lógica Matemática y Ordenamiento ---

    @Test
    fun `processAndSort_should_calculateDistancesAndSortCorrectly`() {
        // Ejecutamos la función de procesamiento con la ubicación de prueba
        val result = viewmodel.processAndSort(
            orders = ordersTestList,
            myLat = testLat,
            myLng = testLng
        )

        // 1. Verificamos que las distancias se hayan calculado (el campo 'dist' > 0, o ~0 para CDMX)
        assertTrue(result.all { it.dist >= 0.0 })

        // 2. Verificamos el ordenamiento (el elemento más cercano debe ir primero)
        // La lista debe quedar: CDMX (~0 km), GDL (~520 km), MTY (~720 km)
        assertEquals("CDMX", result[0].title)
        assertEquals("GDL", result[1].title)
        assertEquals("MTY", result[2].title)
    }

    @Test
    fun `calculateDistHaversine_should_returnZeroForSameCoordinates`() {
        // Prueba de distancia entre dos puntos iguales
        val distance = viewmodel.calculateDistHaversine(
            lat1 = testLat, lon1 = testLng,
            lat2 = testLat, lon2 = testLng
        )
        // Esperamos 0 km
        assertEquals(0.0, distance, 0.1) // 0.1 es el delta para comparar Doubles
    }

    @Test
    fun `calculateDistHaversine_should_returnCorrectDistance_CDMX_to_GDL`() {
        // Coordenadas CDMX: 19.4326, -99.1332
        // Coordenadas GDL: 20.6597, -103.3496
        // Distancia real aproximada es ~520-530 km.
        val distance = viewmodel.calculateDistHaversine(
            lat1 = 19.4326, lon1 = -99.1332,
            lat2 = 20.6597, lon2 = -103.3496
        )

        // Esperamos que esté en el rango de 520 a 530 (y la función usa ceil)
        assertTrue(distance > 520.0 && distance < 535.0)
    }

    @Test
    fun `addNewOrder_should_increaseOrdersDeliveredListSize`() {
        val initialSize = viewmodel.ordersDeliveredList.size
        val newOrder = OrdersState(lat = 0.0, lng = 0.0, title = "New Order")

        viewmodel.addNewOrder(newOrder)

        // Verificamos que el tamaño haya aumentado en 1
        assertEquals(initialSize + 1, viewmodel.ordersDeliveredList.size)
        // Verificamos que el nuevo elemento esté al final
        assertEquals("New Order", viewmodel.ordersDeliveredList.last().title)
    }

    @Test
    fun `removeOrderByIndex_should_decreaseListShortSize`() {
        // Aseguramos que listShort no esté vacío (se inicializa con listDummynOrders)
        val initialSize = viewmodel.listShort.size
        assertTrue(initialSize > 0)

        // Intentamos remover el primer elemento (índice 0)
        viewmodel.removeOrderByIndex(0)
        assertEquals(initialSize - 1, viewmodel.listShort.size)
    }

    @Test
    fun `removeOrderByIndex_should_removeCorrectElement`() {
        // El estado inicial (listShort) está ordenado por distancia: CDMX, GDL, MTY
        val elementToRemoveTitle = viewmodel.listShort[1].title // GDL
        viewmodel.removeOrderByIndex(1)
        // Verificamos que GDL ya no esté en la lista
        val result = viewmodel.listShort.none { it.title == elementToRemoveTitle }
        assertTrue(result)
    }

    // --- Pruebas de Reinicio de Estado ---

    @Test
    fun `resetToInitialState_should_clearDeliveredListAndResetShortList`() {
        // Pre-condición: Modificamos el estado
        viewmodel.addNewOrder(OrdersState(lat = 1.0, lng = 1.0, title = "Test Delivered"))
        viewmodel.removeOrderByIndex(0) // Removemos un elemento de listShort
        assertTrue(viewmodel.ordersDeliveredList.isNotEmpty())
        assertTrue(viewmodel.listShort.size < viewmodel.listDummynOrders.size)
        viewmodel.resetToInitialState()
        assertTrue(viewmodel.ordersDeliveredList.isEmpty())
        assertEquals(viewmodel.listDummynOrders.size, viewmodel.listShort.size)
        // Y que esté ordenado correctamente (CDMX, GDL, MTY)
        assertEquals("CDMX", viewmodel.listShort[0].title)
    }
}

