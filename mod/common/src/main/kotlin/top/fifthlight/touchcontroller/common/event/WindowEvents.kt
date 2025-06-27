package top.fifthlight.touchcontroller.common.event

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import top.fifthlight.touchcontroller.common.gal.PlatformWindowProvider
import top.fifthlight.touchcontroller.common.platform.PlatformProvider
import top.fifthlight.touchcontroller.proxy.message.InitializeMessage

object WindowEvents : KoinComponent {
    private val logger = LoggerFactory.getLogger(WindowEvents::class.java)
    private val platformProvider: PlatformProvider by inject()
    private lateinit var windowProvider: PlatformWindowProvider
    val windowWidth: Int
        get() = windowProvider.windowWidth
    val windowHeight: Int
        get() = windowProvider.windowHeight

    fun onWindowCreated(windowProvider: PlatformWindowProvider) {
        this.windowProvider = windowProvider
        platformProvider.load(windowProvider)
        platformProvider.platform?.sendEvent(InitializeMessage)
        logger.info("Loaded platform")
    }
}
