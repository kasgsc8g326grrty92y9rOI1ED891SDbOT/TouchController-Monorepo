package top.fifthlight.touchcontroller.ui.component

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior

@Composable
fun TouchControllerNavigator(
    screen: Screen,
    disposeBehavior: NavigatorDisposeBehavior = NavigatorDisposeBehavior(),
) {
    Navigator(
        screen = screen,
        disposeBehavior = disposeBehavior,
    ) { navigator ->
        DismissHandler(navigator.canPop || navigator.parent?.canPop == true) {
            navigator.pop()
        }
        CurrentScreen()
    }
}