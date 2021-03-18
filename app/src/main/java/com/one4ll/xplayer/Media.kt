package com.one4ll.xplayer

data class Media(var name: String, var duration: String, var size: String, var path: String) {
    override fun toString(): String {
        return "name = $name, duration = $duration, size = $size, path = $path"
    }
}
