package id.tisnahadiana.storyapp.ui.register

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.tisnahadiana.storyapp.data.repository.UserRepository
import id.tisnahadiana.storyapp.di.AppModule

class RegisterViewModel(private val userRepository: UserRepository) :
    ViewModel() {
    fun registerUser(name: String, email: String, password: String) =
        userRepository.register(name, email, password)

    class RegisterViewModelFactory private constructor(private val userRepository: UserRepository) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                return RegisterViewModel(userRepository) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }

        companion object {
            @Volatile
            private var instance: RegisterViewModelFactory? = null

            fun getInstance(context: Context): RegisterViewModelFactory =
                instance ?: synchronized(this) {
                    instance ?: RegisterViewModelFactory(AppModule.provideUserRepository(context))
                }
        }
    }
}