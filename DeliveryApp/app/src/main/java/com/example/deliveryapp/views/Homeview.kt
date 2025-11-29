package com.example.deliveryapp.views

import android.location.Location
import android.widget.Toast
import com.example.deliveryapp.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deliveryapp.components.MainButton
import com.example.deliveryapp.components.MainCard
import com.example.deliveryapp.components.SpaceH
import com.example.deliveryapp.components.TextWithVectorImage
import com.example.deliveryapp.viewModel.DeliveryViewModel



@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeView(viewModel: DeliveryViewModel){

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text(text = "Delivery App", color = Color.White) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        )
    }) {
        ContentHomeView(it, viewModel)
    }
}

@Composable
fun ContentHomeView(paddingValues: PaddingValues, viewModel: DeliveryViewModel) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val location = viewModel.locationState.collectAsState()
    val currentLocation = location.value

    LaunchedEffect(Unit) {
        viewModel.startLocationUpdates()
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.stopLocationUpdates() }
    }
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .padding(10.dp)
            .verticalScroll(scrollState)
            .fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center // Aligns all children to the center
        ) {

            SpaceH(10.dp)
            MainButton("Limpiar") {
                viewModel.resetToInitialState()
            }
        }


        SpaceH(20.dp)

        Card(
            modifier = Modifier
                .width(340.dp)
                .padding(start = 30.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.LightGray
            )
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                TextWithVectorImage(text = "Ubicacion actual", icon = R.drawable.location)
                if (currentLocation != null) {
                Text(text = "Lat: " + currentLocation.latitude + " Long: " + currentLocation.longitude, color = Color.Black, fontSize = 10.sp)
                }
            }
        }

        SpaceH(20.dp)

        Card(
            modifier = Modifier
                .width(340.dp)
                .padding(start = 30.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.LightGray
            )
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                TextWithVectorImage(text = "Entregas Completadas", icon = R.drawable.check)
                viewModel.ordersDeliveredList.forEach { element ->
                    Text(text = "Lat: " + element.lat + " Long: " + element.lng+
                            " "+ element.title, color = Color.Black, fontSize = 10.sp)
                    Toast.makeText(context,"Orden entregada", Toast.LENGTH_SHORT).show()
                }
            }
        }

        SpaceH(20.dp)

        Card(
            modifier = Modifier
                .width(340.dp)
                .padding(start = 30.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.LightGray
            )
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                TextWithVectorImage(text = "Entrega mas cercana", icon = R.drawable.star)
                if (viewModel.listShort.isNotEmpty()){
                    val element = viewModel.listShort.first()
                    Text(text = "Lat: " + element.lat + " Long: " + element.lng+
                            " "+ element.title, color = Color.Black, fontSize = 10.sp)
                    MainButton("Entregar") {
                        if(currentLocation != null){
                            if (viewModel.listShort.isNotEmpty()){
                                viewModel.removeOrderByIndex(0)
                                viewModel.addNewOrder(element)
                            }
                        }else{
                            Toast.makeText(context,"No se puede entregar sin ubicacion", Toast.LENGTH_SHORT).show()
                        }

                    }
                }

            }
        }

        SpaceH(20.dp)

        Card(
            modifier = Modifier
                .width(340.dp)
                .padding(start = 30.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.LightGray
            )
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                TextWithVectorImage(text = "Ordenes pendientes", icon = R.drawable.box)
                viewModel.listShort.drop(1)
                val dropList = viewModel.listShort.drop(1)
                dropList.forEach { item ->
                    Text(text = "Lat: " + item.lat + " Long: " + item.lng+
                            " "+ item.title + " Dist: "+item.dist+" Km", color = Color.Black, fontSize = 10.sp)
                }
            }
        }
    }
}


