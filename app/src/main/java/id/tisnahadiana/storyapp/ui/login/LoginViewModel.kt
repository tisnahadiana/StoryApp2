package id.tisnahadiana.storyapp.ui.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.tisnahadiana.storyapp.data.repository.UserRepository
import id.tisnahadiana.storyapp.di.AppModule

class LoginViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    fun login(email: String, password: String) = userRepository.login(email, password)

    fun saveToken(token: String) {
        userRepository.saveToken(token)
    }

    fun checkIfFirstTime(): LiveData<Boolean> {
        return userRepository.isFirstTime()
    }

    class LoginViewModelFactory private constructor(
        private val userRepository: UserRepository
    ) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(userRepository) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }

        companion object {
            @Volatile
            private var instance: LoginViewModelFactory? = null
            fun getInstance(
                context: Context
            ): LoginViewModelFactory =
                instance ?: synchronized(this) {
                    instance ?: LoginViewModelFactory(
                        AppModule.provideUserRepository(context)
                    )
                }
        }
    }
}