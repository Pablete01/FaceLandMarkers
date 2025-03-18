package com.app.facelandmarkers.di.appModule


import android.util.Log
import com.app.facelandmarkers.MainViewModel
import com.app.facelandmarkers.faceLandmarkerHelper.FaceLandmarkerHelper
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module


val appModule = module {



        single {
                FaceLandmarkerHelper(
                        context = androidContext(), // Aquí pasamos el contexto de la aplicación
                        currentDelegate = FaceLandmarkerHelper.DELEGATE_GPU,
                        faceLandmarkerHelperListener = object : FaceLandmarkerHelper.LandmarkerListener {
                                override fun onResults(resultBundle: FaceLandmarkerHelper.ResultBundle) {
                                        Log.d("FaceLandmarker", "Results received")
                                }

                                override fun onError(error: String, errorCode: Int) {
                                        Log.e("FaceLandmarker", "Error: $error")
                                }

                                override fun onEmpty() {
                                        Log.d("FaceLandmarker", "No faces detected")
                                }
                        }
                )
        }

        viewModelOf(::MainViewModel)


}