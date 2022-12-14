package com.bcc.exporeal.screen

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bcc.exporeal.R
import com.bcc.exporeal.SnackbarListener
import com.bcc.exporeal.component.AppButton
import com.bcc.exporeal.component.AppText
import com.bcc.exporeal.component.TextType
import com.bcc.exporeal.model.BusinessModel
import com.bcc.exporeal.model.UserModel
import com.bcc.exporeal.navigation.AppNavRoute
import com.bcc.exporeal.ui.style.AppColor
import com.bcc.exporeal.util.ListenAppBackHandler
import com.bcc.exporeal.util.Resource
import com.bcc.exporeal.viewmodel.ProfileViewModel
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(navController: NavController) {
    /**Attrs*/
    val viewModel = hiltViewModel<ProfileViewModel>()
    val userInfo = viewModel.user.collectAsState()
    val business = viewModel.business.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = viewModel.isLoading.value)
    val imagePicker =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent(),
            onResult = {
                it?.let { uri ->
                    userInfo.value?.data?.let { user ->
                        viewModel.pickedImageUri.value = uri
                        viewModel.uploadProfilePicture(uri = uri, user = user, onSuccess = {
                            viewModel.showSuccessChangeProfilePicSnackbar.value = true
                            viewModel.showUploadProgressDialog.value = false
                        }, onFailed = {
                            viewModel.showErrorChangeProfilePicSnackbar.value = true
                            viewModel.showUploadProgressDialog.value = false
                        }, onProgress = { transferred: Long, total: Long ->
                            viewModel.profilePictureTransferredProgress.value = total
                            viewModel.profilePictureTotalProgress.value = total
                            viewModel.showUploadProgressDialog.value = true
                        })
                    }
                }
            })

    /**Function*/
    if (viewModel.isLoading.value) {
        LaunchedEffect(key1 = true) {
            async {
                viewModel.refresh()
            }.join()
            delay(2000)
            viewModel.isLoading.value = false
        }
    }
    if (viewModel.showUploadProgressDialog.value) {
        Dialog(
            onDismissRequest = { /*TODO*/ },
            properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColor.Neutral10), contentAlignment = Alignment.Center
            ) {
                LinearProgressIndicator(
                    progress = (viewModel.profilePictureTransferredProgress.value / viewModel.profilePictureTotalProgress.value).toFloat(),
                    color = AppColor.Blue60
                )
            }
        }
    }
    SnackbarListener(
        "Profile picture has been uploaded", viewModel.showSuccessChangeProfilePicSnackbar
    )
    SnackbarListener(
        "Failed to upload, try again later", viewModel.showErrorChangeProfilePicSnackbar
    )
    SnackbarListener(
        "Please wait, your data is still loading", viewModel.showIsLoadingSnackbar
    )
    SnackbarListener(
        "Your data is not loaded. Try to refresh by swipe the screen",
        viewModel.showPleaseRefreshSnackbar
    )

    /**Content*/
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            viewModel.isLoading.value = true
            viewModel.refresh()
        }
    ) {
        ProfileContent(
            navController = navController,
            viewModel = viewModel,
            userInfo = userInfo,
            business = business,
            imagePicker = imagePicker
        )
    }
}

@Composable
private fun ProfileContent(
    navController: NavController,
    viewModel: ProfileViewModel,
    userInfo: State<Resource<UserModel>?>,
    business: State<Resource<List<BusinessModel>>?>,
    imagePicker: ManagedActivityResultLauncher<String, Uri?>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Info
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = AppColor.Blue10)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (userInfo.value) {
                        is Resource.Error -> {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(AppColor.Neutral60)
                            )
                        }
                        is Resource.Loading -> {
                            // Profile Pic
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .placeholder(
                                        visible = true,
                                        color = AppColor.Neutral50,
                                        highlight = PlaceholderHighlight.shimmer(highlightColor = AppColor.Neutral20)
                                    )
                            )

                            // Identity
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                AppText(
                                    modifier = Modifier.placeholder(
                                        visible = true,
                                        color = AppColor.Neutral50,
                                        highlight = PlaceholderHighlight.shimmer(highlightColor = AppColor.Neutral20),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                    text = "Fahmi Noordin Rumagutawan",
                                    textType = TextType.Body1Semibold
                                )

                                AppText(
                                    modifier = Modifier.placeholder(
                                        visible = true,
                                        color = AppColor.Neutral50,
                                        highlight = PlaceholderHighlight.shimmer(highlightColor = AppColor.Neutral20),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                    text = "fahmigutawan@gmail.com",
                                    textType = TextType.Body2,
                                    color = AppColor.Neutral60
                                )
                            }
                        }
                        is Resource.Success -> {
                            // Profile pic
                            when (userInfo.value?.data?.profile_pic) {
                                "" -> {
                                    when (viewModel.pickedImageUri.value) {
                                        null -> {
                                            AsyncImage(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(CircleShape)
                                                    .clickable { imagePicker.launch("image/*") },
                                                model = R.drawable.ic_profile_pic,
                                                contentDescription = "Profile Pic",
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        else -> {
                                            AsyncImage(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(CircleShape)
                                                    .clickable { imagePicker.launch("image/*") },
                                                model = viewModel.pickedImageUri.value!!,
                                                contentDescription = "Profile Pic",
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    when (viewModel.pickedImageUri.value) {
                                        null -> {
                                            AsyncImage(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(CircleShape)
                                                    .clickable { imagePicker.launch("image/*") },
                                                model = userInfo.value!!.data!!.profile_pic,
                                                contentDescription = "Profile Pic",
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        else -> {
                                            AsyncImage(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(CircleShape)
                                                    .clickable { imagePicker.launch("image/*") },
                                                model = viewModel.pickedImageUri.value!!,
                                                contentDescription = "Profile Pic",
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }

                            // Identity
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                AppText(
                                    text = userInfo.value?.data?.name ?: "",
                                    textType = TextType.Body1Semibold
                                )

                                AppText(
                                    text = userInfo.value?.data?.email ?: "",
                                    textType = TextType.Body2,
                                    color = AppColor.Neutral60
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }

        // Daftarkan perusahaan btn
        item {
            when (business.value) {
                is Resource.Error -> {
                    AppButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        onClick = { viewModel.showPleaseRefreshSnackbar.value = true },
                        text = "REGISTER MY MUSINESS",
                        textColor = AppColor.Blue60,
                        backgroundColor = AppColor.Neutral10,
                        rippleColor = AppColor.Neutral100,
                        borderColor = AppColor.Blue60,
                        borderWidth = 1.dp
                    )
                }
                is Resource.Loading -> {
                    AppButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        onClick = { viewModel.showIsLoadingSnackbar.value = true },
                        text = "REGISTER MY MUSINESS",
                        textColor = AppColor.Blue60,
                        backgroundColor = AppColor.Neutral10,
                        rippleColor = AppColor.Neutral100,
                        borderColor = AppColor.Blue60,
                        borderWidth = 1.dp
                    )
                }
                is Resource.Success -> {
                    business.value?.data?.let {
                        if (it.isEmpty()) {
                            AppButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                onClick = {
                                    navController.navigate(route = AppNavRoute.BusinessRegistrationScreen.name) {
                                        popUpTo(route = AppNavRoute.ProfileScreen.name) {
                                            inclusive = true
                                        }
                                    }
                                },
                                text = "REGISTER MY MUSINESS",
                                textColor = AppColor.Blue60,
                                backgroundColor = AppColor.Neutral10,
                                rippleColor = AppColor.Neutral100,
                                borderColor = AppColor.Blue60,
                                borderWidth = 1.dp
                            )
                        } else {
                            when (it[0].verification_status) {
                                1 -> {
                                    AppButton(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        onClick = {
                                            navController.navigate(route = AppNavRoute.BusinessRegistrationVerificationLandingScreen.name) {
                                                popUpTo(route = AppNavRoute.ProfileScreen.name) {
                                                    inclusive = true
                                                }
                                            }
                                        },
                                        text = "REGISTER MY MUSINESS",
                                        textColor = AppColor.Blue60,
                                        backgroundColor = AppColor.Neutral10,
                                        rippleColor = AppColor.Neutral100,
                                        borderColor = AppColor.Blue60,
                                        borderWidth = 1.dp
                                    )
                                }
                                2 -> {
                                    AppButton(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        onClick = {
                                            navController.navigate(route = AppNavRoute.BusinessRegistrationVerificationLandingScreen.name) {
                                                popUpTo(route = AppNavRoute.ProfileScreen.name) {
                                                    inclusive = true
                                                }
                                            }
                                        },
                                        text = "REGISTER MY MUSINESS",
                                        textColor = AppColor.Blue60,
                                        backgroundColor = AppColor.Neutral10,
                                        rippleColor = AppColor.Neutral100,
                                        borderColor = AppColor.Blue60,
                                        borderWidth = 1.dp
                                    )
                                }
                                3 -> {
                                    AppButton(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        onClick = {
                                            navController.navigate(route = AppNavRoute.BusinessRegistrationVerificationLandingScreen.name) {
                                                popUpTo(route = AppNavRoute.ProfileScreen.name) {
                                                    inclusive = true
                                                }
                                            }
                                        },
                                        text = "REGISTER MY MUSINESS",
                                        textColor = AppColor.Blue60,
                                        backgroundColor = AppColor.Neutral10,
                                        rippleColor = AppColor.Neutral100,
                                        borderColor = AppColor.Blue60,
                                        borderWidth = 1.dp
                                    )
                                }
                                null -> {}
                            }
                        }
                    }
                }
                null -> {}
            }
        }

        // Personal section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AppText(text = "Personal", textType = TextType.H3, color = AppColor.Neutral60)

                // Akun
                AppButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /*TODO*/ },
                    backgroundColor = AppColor.Neutral10,
                    rippleColor = AppColor.Neutral100,
                    borderWidth = 1.dp,
                    borderColor = AppColor.Neutral60
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AsyncImage(
                            modifier = Modifier.size(24.dp),
                            model = R.drawable.ic_profile_akun,
                            contentDescription = "Icon"
                        )

                        AppText(text = "Account", textType = TextType.Body1)
                    }
                }

                // Perusahaan saya
                when (business.value) {
                    is Resource.Error -> {
                        AppButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { viewModel.showPleaseRefreshSnackbar.value = true },
                            backgroundColor = AppColor.Neutral10,
                            rippleColor = AppColor.Neutral100,
                            borderWidth = 1.dp,
                            borderColor = AppColor.Neutral60
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                AsyncImage(
                                    modifier = Modifier.size(24.dp),
                                    model = R.drawable.ic_profile_perusahaansaya,
                                    contentDescription = "Icon"
                                )

                                AppText(text = "My Business", textType = TextType.Body1)
                            }
                        }
                    }
                    is Resource.Loading -> {
                        AppButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {},
                            backgroundColor = AppColor.Neutral10,
                            rippleColor = AppColor.Neutral100,
                            borderWidth = 1.dp,
                            borderColor = AppColor.Neutral60,
                            enabled = false
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                AsyncImage(
                                    modifier = Modifier.size(24.dp),
                                    model = R.drawable.ic_profile_perusahaansaya,
                                    contentDescription = "Icon"
                                )

                                AppText(
                                    text = "My Business",
                                    textType = TextType.Body1,
                                    color = AppColor.Neutral50
                                )
                            }
                        }
                    }
                    is Resource.Success -> {
                        business.value?.data?.let {
                            if (it.isEmpty()) {
                                AppButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {},
                                    backgroundColor = AppColor.Neutral10,
                                    rippleColor = AppColor.Neutral100,
                                    borderWidth = 1.dp,
                                    borderColor = AppColor.Neutral60,
                                    enabled = false
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        AsyncImage(
                                            modifier = Modifier.size(24.dp),
                                            model = R.drawable.ic_profile_perusahaansaya,
                                            contentDescription = "Icon"
                                        )

                                        AppText(
                                            text = "My Business",
                                            textType = TextType.Body1,
                                            color = AppColor.Neutral50
                                        )
                                    }
                                }
                            } else {
                                if (it[0].verification_status != 4) {
                                    AppButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {},
                                        backgroundColor = AppColor.Neutral10,
                                        rippleColor = AppColor.Neutral100,
                                        borderWidth = 1.dp,
                                        borderColor = AppColor.Neutral60,
                                        enabled = false
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            AsyncImage(
                                                modifier = Modifier.size(24.dp),
                                                model = R.drawable.ic_profile_perusahaansaya,
                                                contentDescription = "Icon"
                                            )

                                            AppText(
                                                text = "My Business",
                                                textType = TextType.Body1,
                                                color = AppColor.Neutral50
                                            )
                                        }
                                    }
                                } else {
                                    AppButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { navController.navigate(route = AppNavRoute.ProductOfMerchantScreen.name) },
                                        backgroundColor = AppColor.Neutral10,
                                        rippleColor = AppColor.Neutral100,
                                        borderWidth = 1.dp,
                                        borderColor = AppColor.Neutral60
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            AsyncImage(
                                                modifier = Modifier.size(24.dp),
                                                model = R.drawable.ic_profile_perusahaansaya,
                                                contentDescription = "Icon"
                                            )

                                            AppText(
                                                text = "My Business",
                                                textType = TextType.Body1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    null -> {
                        AppButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {},
                            backgroundColor = AppColor.Neutral10,
                            rippleColor = AppColor.Neutral100,
                            borderWidth = 1.dp,
                            borderColor = AppColor.Neutral60,
                            enabled = false
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                AsyncImage(
                                    modifier = Modifier.size(24.dp),
                                    model = R.drawable.ic_profile_perusahaansaya,
                                    contentDescription = "Icon"
                                )

                                AppText(
                                    text = "My Business",
                                    textType = TextType.Body1,
                                    color = AppColor.Neutral50
                                )
                            }
                        }
                    }
                }

                // Back account
                AppButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /*TODO*/ },
                    backgroundColor = AppColor.Neutral10,
                    rippleColor = AppColor.Neutral100,
                    borderWidth = 1.dp,
                    borderColor = AppColor.Neutral60
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AsyncImage(
                            modifier = Modifier.size(24.dp),
                            model = R.drawable.ic_profile_bank,
                            contentDescription = "Icon"
                        )

                        AppText(text = "Bank Account", textType = TextType.Body1)
                    }
                }

                // Riwayat transaksi
                AppButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /*TODO*/ },
                    backgroundColor = AppColor.Neutral10,
                    rippleColor = AppColor.Neutral100,
                    borderWidth = 1.dp,
                    borderColor = AppColor.Neutral60
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AsyncImage(
                            modifier = Modifier.size(24.dp),
                            model = R.drawable.ic_profile_riwayat,
                            contentDescription = "Icon"
                        )

                        AppText(text = "Transaction History", textType = TextType.Body1)
                    }
                }
            }
        }

        // Umum section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AppText(text = "General", textType = TextType.H3, color = AppColor.Neutral60)

                AppButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /*TODO*/ },
                    backgroundColor = AppColor.Neutral10,
                    rippleColor = AppColor.Neutral100,
                    borderWidth = 1.dp,
                    borderColor = AppColor.Neutral60
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AsyncImage(
                            modifier = Modifier.size(24.dp),
                            model = R.drawable.ic_profile_setting,
                            contentDescription = "Icon"
                        )

                        AppText(text = "Settings", textType = TextType.Body1)
                    }
                }

                AppButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /*TODO*/ },
                    backgroundColor = AppColor.Neutral10,
                    rippleColor = AppColor.Neutral100,
                    borderWidth = 1.dp,
                    borderColor = AppColor.Neutral60
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AsyncImage(
                            modifier = Modifier.size(24.dp),
                            model = R.drawable.ic_profile_keamanan,
                            contentDescription = "Icon"
                        )

                        AppText(text = "Security", textType = TextType.Body1)
                    }
                }

                AppButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /*TODO*/ },
                    backgroundColor = AppColor.Neutral10,
                    rippleColor = AppColor.Neutral100,
                    borderWidth = 1.dp,
                    borderColor = AppColor.Neutral60
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AsyncImage(
                            modifier = Modifier.size(24.dp),
                            model = R.drawable.ic_profile_sk,
                            contentDescription = "Icon"
                        )

                        AppText(text = "Terms and Conditions", textType = TextType.Body1)
                    }
                }

                AppButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /*TODO*/ },
                    backgroundColor = AppColor.Neutral10,
                    rippleColor = AppColor.Neutral100,
                    borderWidth = 1.dp,
                    borderColor = AppColor.Neutral60
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AsyncImage(
                            modifier = Modifier.size(24.dp),
                            model = R.drawable.ic_profile_bantuan,
                            contentDescription = "Icon"
                        )

                        AppText(text = "Help", textType = TextType.Body1)
                    }
                }

                AppButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /*TODO*/ },
                    backgroundColor = AppColor.Neutral10,
                    rippleColor = AppColor.Neutral100,
                    borderWidth = 1.dp,
                    borderColor = AppColor.Neutral60
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AsyncImage(
                            modifier = Modifier.size(24.dp),
                            model = R.drawable.ic_profile_tentang,
                            contentDescription = "Icon"
                        )

                        AppText(text = "About", textType = TextType.Body1)
                    }
                }
            }
        }

        // Keluar btn
        item {
            AppButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = {
                    viewModel.isLoading.value = true
                    viewModel.logout {
                        viewModel.isLoading.value = false
                        navController.navigate(route = AppNavRoute.LoginScreen.name) {
                            navController.backQueue.clear()
                        }
                    }
                },
                backgroundColor = AppColor.Neutral10,
                rippleColor = AppColor.Neutral100,
                borderWidth = 1.dp,
                borderColor = AppColor.Negative60
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AsyncImage(
                        modifier = Modifier.size(24.dp),
                        model = R.drawable.ic_profile_keluar,
                        contentDescription = "Icon"
                    )

                    AppText(text = "Log out", textType = TextType.Body1, color = AppColor.Negative60)
                }
            }
        }
    }
}