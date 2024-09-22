import javax.swing.*
import java.awt.*
import java.awt.event.*
import kotlin.math.sqrt

data class Point(var x: Float, var y: Float, var vx: Float = 0f, var vy: Float = 0f, val mass: Float = 5f)

class Main : JPanel(), ActionListener {
    private val timer = Timer(7, this)
    private val points = mutableListOf<Point>()
    private val G = 9.81f
    private var init = false
    init {
        timer.start()
        points.add(Point(100f, 300f, 0f, 0f, 30f))
        points.add(Point(500f, 200f, 0f, 0f, 30f))
        points.add(Point(300f, 400f, 0f, 0f, 30f))
    }
    fun conditionalBlocking() {
        if (!init) {
            Thread.sleep(70)
            init = true
        }
    }
    override fun paintComponent(g: Graphics) {
        super.background = Color.BLACK
        super.paintComponent(g)
        g.color = Color.WHITE
        points.forEach { point ->
            g.fillOval(point.x.toInt(), point.y.toInt(), point.mass.toInt() + 10, point.mass.toInt() + 10)
            g.drawString("(${point.x.toInt()}, ${point.y.toInt()})", point.x.toInt(), point.y.toInt() - 5)
        }
    }

    override fun actionPerformed(e: ActionEvent) {
        conditionalBlocking()
        points.forEach { point ->
            points.forEach { other ->
                if (point != other) {
                    applyGravity(point, other)
                }
            }

            point.x += point.vx
            point.y += point.vy

            if (point.x < 0) {
                point.x = 0f
                point.vx *= -1
            } else if (point.x > width - (point.mass + 10)) {
                point.x = (width - (point.mass + 10)).toFloat()
                point.vx *= -1
            }
            if (point.y < 0) {
                point.y = 0f
                point.vy *= -1
            } else if (point.y > height - (point.mass + 10)) {
                point.y = (height - (point.mass + 10)).toFloat()
                point.vy *= -1
            }
        }

        for (i in points.indices) {
            for (j in i + 1 until points.size) {
                handleCollision(points[i], points[j])
            }
        }

        repaint()
    }

    private fun applyGravity(p1: Point, p2: Point) {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val distance = sqrt(dx * dx + dy * dy)

        if (distance > 0) {
            val force = G * (p1.mass * p2.mass) / (distance * distance)
            val fx = force * (dx / distance)
            val fy = force * (dy / distance)
            p1.vx += fx / p1.mass
            p1.vy += fy / p1.mass
            p2.vx -= fx / p2.mass
            p2.vy -= fy / p2.mass
        }
    }

    private fun handleCollision(p1: Point, p2: Point) {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val distance = sqrt(dx * dx + dy * dy)

        if (distance < (p1.mass + p2.mass + 10)) {
            val totalMass = p1.mass + p2.mass
            val v1FinalX = (p1.vx * (p1.mass - p2.mass) + (2 * p2.mass * p2.vx)) / totalMass
            val v1FinalY = (p1.vy * (p1.mass - p2.mass) + (2 * p2.mass * p2.vy)) / totalMass
            val v2FinalX = (p2.vx * (p2.mass - p1.mass) + (2 * p1.mass * p1.vx)) / totalMass
            val v2FinalY = (p2.vy * (p2.mass - p1.mass) + (2 * p1.mass * p1.vy)) / totalMass

            p1.vx = v1FinalX
            p1.vy = v1FinalY
            p2.vx = v2FinalX
            p2.vy = v2FinalY

            val overlap = (p1.mass + p2.mass + 10) - distance
            val angle = Math.atan2(dy.toDouble(), dx.toDouble())
            p1.x -= overlap * Math.cos(angle).toFloat() / 2
            p1.y -= overlap * Math.sin(angle).toFloat() / 2
            p2.x += overlap * Math.cos(angle).toFloat() / 2
            p2.y += overlap * Math.sin(angle).toFloat() / 2
        }
    }
}

fun main() {
    JFrame("Gravity Simulation").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        contentPane = Main()
        background = Color.BLACK
        setSize(1000, 1000)
        isVisible = true
    }
}
