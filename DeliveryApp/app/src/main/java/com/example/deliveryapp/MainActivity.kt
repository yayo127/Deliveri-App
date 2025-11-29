package com.example.deliveryapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.deliveryapp.ui.theme.DeliveryAppTheme
import com.example.deliveryapp.viewModel.DeliveryViewModel
import com.example.deliveryapp.views.HomeView


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel : DeliveryViewModel by viewModels()
        setContent {

            // Se solicita el presmiso si aun no se a obtendido para que se propague en toda la app
            var permission by remember { mutableStateOf(
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            )
            }
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    permission = isGranted
                })

            LaunchedEffect(key1 = permission) {
                if(permission){
                    viewModel.startLocationUpdates()
                }else{
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }

            }
            DisposableEffect(key1 = permission, key2 = Unit) {

                // Si ya tenemos el permiso, iniciamos las actualizaciones de ubicación
                if (permission) {
                    viewModel.startLocationUpdates()
                } else {

                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }

                onDispose {
                    viewModel.stopLocationUpdates()
                }
            }

            DeliveryAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeView(viewModel)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Detener actualizaciones cuando la actividad no está visible
        val viewModel : DeliveryViewModel by viewModels()
        viewModel.stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        // Reiniciar las actualizaciones si ya tenemos permiso
        val viewModel : DeliveryViewModel by viewModels()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.startLocationUpdates()
        }
    }
}

