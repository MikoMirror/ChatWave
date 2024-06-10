class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override fun isAndroid(): Boolean = false
}

actual fun getPlatform(): Platform = JVMPlatform()
