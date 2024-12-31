import Config.COLLISION_NEW_PROTONS
import Config.GRAPHITE_RODS_MAX
import Config.GRAPHITE_RODS_MIN
import Config.GRAPHITE_RODS_STEP
import Config.GRAPHITE_ROD_WIDTH
import Config.INACTIVE_FUEL_LIMIT
import Config.boundary
import Config.colToLeftRodX
import Config.colToRightRodX
import Config.colsCount
import Config.rowsCount
import java.awt.Rectangle

class Reactor constructor(
    private val rows: Int,
    private val cols: Int,
    private val protonsCoordinates: List<Point>,
    private val protonVelocities: List<Point>
) {

    val fuelGrid: List<List<FuelTablet>>
    val protons: MutableList<Proton>
    val protonsToAdd: MutableList<Proton> = mutableListOf()
    val protonsToDelete: MutableList<Proton> = mutableListOf()
    val fuelToReactivate: ArrayDeque<Pair<Int, Int>> = ArrayDeque()
    var graphiteRodsDeployRatio= 0.0
    var graphiteRodsState = Movement.None

    init {
        val intialGrid = mutableListOf<List<FuelTablet>>()
        repeat(cols) { col ->
            val column = mutableListOf<FuelTablet>()
            repeat(rows) { row ->
                column.add(FuelTablet(row, col, true))
            }
            intialGrid.add(column)
        }
        fuelGrid = intialGrid

        val intialProtons = mutableListOf<Proton>()
        protonsCoordinates.forEachIndexed { i, p ->
            intialProtons.add(Proton(Point(p.x, p.y), Point(protonVelocities[i].x, protonVelocities[i].y), false))
        }
        protons = intialProtons
    }

    fun moveProtons(boundary: Rectangle) {
        protons.forEach {
            var newX = it.point.x + it.v.x * Config.ANIMATION_SPEED_MULTIPLIER
            var newY = it.point.y + it.v.y * Config.ANIMATION_SPEED_MULTIPLIER
            if (newY >= boundary.y + boundary.height) {
                it.v = Point(it.v.x, -it.v.y)
            }
            if (newY <= boundary.y) {
                it.v = Point(it.v.x, -it.v.y)
            }
            if (newX >= boundary.x + boundary.width) {
                it.v = Point(-it.v.x, it.v.y)
            }
            if (newX <= boundary.x) {
                it.v = Point(-it.v.x, it.v.y)
            }
            it.point = Point(newX, newY)
            calculateCollisionWithFuel(it)
            calculateCollisionWithRod(it)
        }
        protons.removeAll(protonsToDelete)
        protons.addAll(protonsToAdd)
        protonsToDelete.clear()
        protonsToAdd.clear()
    }

    private fun calculateCollisionWithRod(p: Proton) {
        if (reactor.graphiteRodsDeployRatio == 0.0) {
            return
        }
        val col = Config.xToCol(p.point.x)
        val leftRodStartX = colToLeftRodX(col)
        val rightRodStartX = colToRightRodX(col)
        val rodsHeightY = boundary.y + boundary.height * reactor.graphiteRodsDeployRatio
        if (leftRodStartX != null) {
            if (p.point.y <= rodsHeightY) {
                if (p.point.x >= leftRodStartX && p.point.x <= (leftRodStartX + GRAPHITE_ROD_WIDTH)) {
                    protonsToDelete.add(p)
                    return
                }
            }
        }
        if (rightRodStartX != null) {
            if (p.point.y <= rodsHeightY) {
                if (p.point.x >= rightRodStartX && p.point.x <= (rightRodStartX + GRAPHITE_ROD_WIDTH)) {
                    protonsToDelete.add(p)
                    return
                }
            }
        }
    }

    fun shiftGraphiteRods() {
        when(graphiteRodsState) {
            Movement.Down -> lowerGraphiteRods()
            Movement.Up -> raiseGraphiteRods()
            Movement.None -> {}
        }
    }

    fun startMovingGraphiteRods(direction: Movement) {
        graphiteRodsState = direction
    }

    fun stopMovingGraphiteRods() {
        graphiteRodsState = Movement.None
    }

    fun lowerGraphiteRods() {
        moveGraphiteRods(by = GRAPHITE_RODS_STEP)
    }

    fun raiseGraphiteRods() {
        moveGraphiteRods(by = -GRAPHITE_RODS_STEP)
    }

    private fun moveGraphiteRods(by: Double) {
        val newRatio = graphiteRodsDeployRatio + by
        if (newRatio < GRAPHITE_RODS_MIN) {
            graphiteRodsDeployRatio = GRAPHITE_RODS_MIN
        } else if (newRatio > GRAPHITE_RODS_MAX) {
            graphiteRodsDeployRatio = GRAPHITE_RODS_MAX
        } else {
            graphiteRodsDeployRatio = newRatio
        }
    }

    fun reactivateFuel() {
        if (fuelToReactivate.isNotEmpty()) {
            val coords = fuelToReactivate.removeFirst()
            fuelGrid[coords.first][coords.second].isActive = true
        }
    }

    private fun calculateCollisionWithFuel(p: Proton) {
        val col = Config.xToCol(p.point.x)
        val row = Config.yToRow(p.point.y)
        if (col >= 0 && row >= 0 && col < colsCount && row < rowsCount) {
            if (fuelGrid[col][row].isActive) {
                fuelGrid[col][row].isActive = false
                fuelToReactivate.addLast(Pair(col,row))
                if (fuelToReactivate.size == INACTIVE_FUEL_LIMIT) {
                    reactivateFuel()
                }
                protonsToDelete.add(p)
                punchProton(col, row)
            }
        }
    }

    fun addProton(boundary: Rectangle) {
        protons.add(Proton(Config.randomProtonCoordinates(boundary), Config.randomProtonVelocities(), false))
    }

    fun punchProton(col: Int, row: Int) {
        repeat(COLLISION_NEW_PROTONS) {
            protonsToAdd.add(
                Proton(
                    Point(Config.colToX(col), Config.rowToY(row)),
                    Config.randomProtonVelocities(), false
                )
            )
        }
    }

    fun emergencyShutdown() {
        graphiteRodsDeployRatio = 1.0
    }

    fun emergencyStart() {
        graphiteRodsDeployRatio = 0.0
    }
}

class FuelTablet(var row: Int, var col: Int, var isActive: Boolean) {}

class Proton(var point: Point, var v: Point, var isHollow: Boolean)

enum class Movement {
    Down,
    Up,
    None
}
