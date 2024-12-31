import Config.GRAPHITE_RODS_START_X
import Config.GRAPHITE_ROD_WIDTH
import Config.TEXT_SIZE
import Config.boundary
import Config.boxOffsetH
import Config.boxOffsetV
import Config.colOffset
import Config.colsCount
import Config.fuelTabletRadius
import Config.gridOffsetH
import Config.gridOffsetV
import Config.protonRadius
import Config.rowOffset
import Config.rowsCount
import Config.tabletColor
import Config.tabletColorInactive
import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.loadFont
import org.openrndr.extra.color.presets.DARK_GRAY
import org.openrndr.extra.color.presets.DARK_GREY
import org.openrndr.extra.color.presets.DARK_RED

val reactor = Reactor(
    rowsCount, colsCount, listOf(
        Config.randomProtonCoordinates(boundary),
    ),
    listOf(
        Config.randomProtonVelocities(),
        )
)

fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        val font = loadFont("data/fonts/default.otf", 32.0)
        mouse.buttonDown.listen {
            reactor.addProton(boundary)
        }

        keyboard.keyDown.listen { keyEvent ->
            if (keyEvent.key == KEY_ARROW_DOWN) {
                reactor.startMovingGraphiteRods(Movement.Down)
            } else if (keyEvent.key == KEY_ARROW_UP) {
                reactor.startMovingGraphiteRods(Movement.Up)
            } else if (keyEvent.key == KEY_ENTER) {
                reactor.emergencyShutdown()
            } else if (keyEvent.key == KEY_BACKSPACE) {
                reactor.emergencyStart()
            }
        }

        keyboard.keyUp.listen { keyEvent ->
            if (keyEvent.key == KEY_ARROW_DOWN) {
                reactor.stopMovingGraphiteRods()
            } else if (keyEvent.key == KEY_ARROW_UP) {
                reactor.stopMovingGraphiteRods()
            }
        }

        extend {
            drawer.clear(ColorRGBa.DARK_GREY)
            drawer.fontMap = font
            drawReactor(drawer, reactor)
            printStats(drawer, reactor)
            reactor.moveProtons(boundary)
            reactor.shiftGraphiteRods()
        }
    }
}

fun printStats(drawer: Drawer, reactor: Reactor) {
    drawer.fill = ColorRGBa.BLACK

    drawer.text(
        "Reactivity: ${reactor.protons.size}",
        boundary.x.toDouble(),
        (boundary.y + boundary.height + TEXT_SIZE).toDouble()
    )

    drawer.text(
        "Graphite rods deployment: ${(reactor.graphiteRodsDeployRatio*100).toInt()}%",
        boundary.x.toDouble(),
        (boundary.y + boundary.height + 2*TEXT_SIZE).toDouble()
    )
}

fun drawReactor(drawer: Drawer, reactor: Reactor) {
    drawBox(drawer, reactor)
    drawFuelGrid(drawer, reactor.fuelGrid)
    drawProtons(drawer, reactor.protons)
    drawGraphiteRods(drawer, reactor)
}

fun drawGraphiteRods(drawer: Drawer, reactor: Reactor) {
    if (reactor.graphiteRodsDeployRatio > 0.0) {
        val x = GRAPHITE_RODS_START_X
        val y = boundary.y
        val height = boundary.height * reactor.graphiteRodsDeployRatio
        drawer.fill = ColorRGBa.fromHex("#333333")
        repeat(colsCount-1) {
            drawer.rectangle(x + it * (2* fuelTabletRadius + rowOffset - 6.0), y.toDouble(), GRAPHITE_ROD_WIDTH.toDouble(), height)
        }
    }
}

fun drawBox(drawer: Drawer, reactor: Reactor) {
    val width = reactor.fuelGrid.size * (rowOffset + fuelTabletRadius)
    val height = reactor.fuelGrid[0].size * (colOffset + fuelTabletRadius) + boxOffsetV
    drawer.fill = ColorRGBa.TRANSPARENT
    drawer.stroke = ColorRGBa.BLACK
    drawer.rectangle(boxOffsetH, boxOffsetV, width, height)
}

fun drawFuelGrid(drawer: Drawer, fuelGrid: List<List<FuelTablet>>) {
    fuelGrid.forEach { column ->
        column.forEach { f ->
            drawer.fill = if (f.isActive) tabletColor else tabletColorInactive
            drawer.circle(
                (gridOffsetH + f.col * (rowOffset + fuelTabletRadius)).toDouble(),
                (gridOffsetV + f.row * (colOffset + fuelTabletRadius)).toDouble(),
                fuelTabletRadius
            )
        }
    }
}

fun drawProtons(drawer: Drawer, protons: List<Proton>) {
    drawer.fill = ColorRGBa.DARK_RED
    protons.forEach {
        drawer.circle(it.point.x, it.point.y, protonRadius)
    }
}
