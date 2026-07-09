package application.liedetector

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform