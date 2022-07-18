package com.example.bustleplayer.vm

sealed class PlayerState() {
    class Stop(): PlayerState()
    class Play(): PlayerState()
    class Pause(): PlayerState()
    class ContinuePlay(): PlayerState()
}
