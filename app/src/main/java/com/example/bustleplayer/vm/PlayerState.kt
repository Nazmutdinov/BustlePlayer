package com.example.bustleplayer.vm

sealed class PlayerState() {
    class Initial(): PlayerState()
    class Play(): PlayerState()
    class Pause(): PlayerState()
    class ContinuePlay(): PlayerState()
    class Stop(): PlayerState()
}
