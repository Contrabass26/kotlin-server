import org.apache.commons.lang3.SystemUtils

val USERNAME: String = System.getProperty("user.name")

val APP_DATA_LOCATION by lazy {
    String.format(when {
        SystemUtils.IS_OS_WINDOWS -> "C:/Users/%s/AppData/Roaming/"
        SystemUtils.IS_OS_MAC -> "/Users/%s/Library/Application Support/"
        SystemUtils.IS_OS_LINUX -> "/home/%s/."
        else -> throw IllegalStateException("Operating system not supported: " + System.getProperty("os.name"))
    }, USERNAME)
}