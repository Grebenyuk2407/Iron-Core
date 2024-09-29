package dev.androidbroadcast.ironcore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    private val _difficultyLevel = MutableLiveData<String>()
    val difficultyLevel: LiveData<String> get() = _difficultyLevel

    fun setDifficultyLevel(level: String) {
        _difficultyLevel.value = level
    }
}
