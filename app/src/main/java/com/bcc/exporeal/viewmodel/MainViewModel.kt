package com.bcc.exporeal.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.bcc.exporeal.model.PermintaanModel
import com.bcc.exporeal.model.ProductModel
import com.bcc.exporeal.navigation.AppNavRoute
import com.bcc.exporeal.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo:AppRepository
) :ViewModel() {
    val showBottomBar = mutableStateOf(false)
    val showTopBar = mutableStateOf(false)
    val currentRoute = mutableStateOf(AppNavRoute.MySplashScreen.name)
    val searchState = mutableStateOf("")

    val pickedProductToProductDetailScreen = mutableStateOf<ProductModel?>(null)
    val pickedPermintaanToPermintaanDetailScreen = mutableStateOf<PermintaanModel?>(null)
}