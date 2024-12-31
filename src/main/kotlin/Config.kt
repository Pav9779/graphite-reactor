import org.openrndr.color.ColorRGBa
import java.awt.Rectangle
import kotlin.random.Random

object Config {

    val rowOffset = 20
    val colOffset = 10
    val fuelTabletRadius = 6.0
    val boxOffsetH = 8.0
    val boxOffsetV = 15.0
    val gridOffsetH = 22
    val gridOffsetV = 30
    val tabletColor = ColorRGBa.fromHex("#036694")
    val tabletColorInactive = ColorRGBa.fromHex("#888888")
    val protonRadius = 3.0
    val rowsCount = 20
    val colsCount = 30
    val boundaryX = boxOffsetH
    val boundaryY = boxOffsetV
    val boundaryWidth = colsCount * (rowOffset + fuelTabletRadius)
    val boundaryHeight = rowsCount * (colOffset + fuelTabletRadius) + boxOffsetV
    val boundary = Rectangle(boundaryX.toInt(), boundaryY.toInt(), boundaryWidth.toInt(), boundaryHeight.toInt())

    val r = Random(System.currentTimeMillis())

    const val ANIMATION_SPEED_MULTIPLIER = 2.0
    const val INACTIVE_FUEL_LIMIT = 400 // <--(def: 400)---- More limit makes fuel regenerate slower, keeping lower reactivity
    const val COLLISION_NEW_PROTONS = 2 // <--(def: 2)------ Number of protons spawned at each fuel collision
    const val MAX_PROTON_V_X = 8.0
    const val MAX_PROTON_V_Y = 6.0
    const val MIN_PROTON_V = 1.0
    const val TEXT_SIZE = 32.0
    const val GRAPHITE_RODS_MIN = 0.0
    const val GRAPHITE_RODS_MAX = 1.0
    const val GRAPHITE_RODS_STEP = 0.02
    const val GRAPHITE_ROD_WIDTH = 10.0
    val GRAPHITE_RODS_START_X = boxOffsetH + 4*fuelTabletRadius - 2.0

    fun colToX(col: Int) = (gridOffsetH + col * (rowOffset + fuelTabletRadius)).toDouble()
    fun rowToY(row: Int) = (gridOffsetV + row * (colOffset + fuelTabletRadius)).toDouble()

    fun xToCol(x: Double) = ((x.toInt() - gridOffsetH) / (rowOffset + fuelTabletRadius)).toInt()
    fun yToRow(y: Double) = ((y.toInt() - gridOffsetV) / (colOffset + fuelTabletRadius)).toInt()

    fun colToLeftRodX(col: Int): Double? {
        return if (col == 0) {
            null
        } else {
            GRAPHITE_RODS_START_X + col * (2* fuelTabletRadius + rowOffset - 6.0)
        }
    }

    fun colToRightRodX(col: Int): Double? {
        return if (col == colsCount - 1) {
            null
        } else {
            GRAPHITE_RODS_START_X + (col+1) * (2* fuelTabletRadius + rowOffset - 6.0)
        }
    }

    fun randomProtonCoordinates(boundary: Rectangle): Point {
        val x = r.nextDouble(boundary.x.toDouble(), (boundary.x+boundary.width).toDouble())
        val y = r.nextDouble(boundary.y.toDouble(), (boundary.y+boundary.height).toDouble())
        return Point(x, y)
    }

    fun randomProtonVelocities(): Point {
        val horizontal = if (r.nextBoolean()) 1 else -1
        val vertical = if (r.nextBoolean()) 1 else -1
        return Point(
            horizontal * r.nextDouble(MIN_PROTON_V, MAX_PROTON_V_X),
            vertical * r.nextDouble(MIN_PROTON_V, MAX_PROTON_V_Y)
        )
    }

}