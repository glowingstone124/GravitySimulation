import javax.swing.*
import java.awt.*
import java.awt.event.*
import java.text.DecimalFormat
import kotlin.math.sqrt

data class Point(var x: Float, var y: Float, var vx: Float = 0f, var vy: Float = 0f, val mass: Float = 5f, val radius: Float = 3f)

class Main : JPanel(), ActionListener {
    val decimalFormat = DecimalFormat("#.#####")
    private val timer = Timer(7, this)
    private val points = mutableListOf<Point>()
    private val G = 2f
    var step = 0
    private var init = false
    private val objectSize = 3
    init {
        timer.start()
        points.add(Point(100f, 300f, 0f, 0f, 0.5f, 6f))
        points.add(Point(500f, 200f, 0f, 0f, 0.5f, 6f))
        points.add(Point(300f, 400f, 0f, 0f, 0.5f, 6f))
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
        g.drawString("steps: $step", 20,20)
        g.drawString("G: $G", 40, 40)
        points.forEach { point ->
            g.fillOval(point.x.toInt(), point.y.toInt(), point.radius.toInt(), point.radius.toInt())
            g.drawString("(${point.x.toInt()}, ${point.y.toInt()})", point.x.toInt(), point.y.toInt() - 5)
            g.drawString("vel x:${decimalFormat.format(point.vx)} m/s, y:${decimalFormat.format(point.vy)} m/s", point.x.toInt(), point.y.toInt() - 30)
        }
    }

    override fun actionPerformed(e: ActionEvent) {
        conditionalBlocking()
        step++
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
            } else if (point.x > width - (point.mass + objectSize)) {
                point.x = (width - (point.mass + objectSize)).toFloat()
                point.vx *= -1
            }
            if (point.y < 0) {
                point.y = 0f
                point.vy *= -1
            } else if (point.y > height - (point.mass + objectSize)) {
                point.y = (height - (point.mass + objectSize)).toFloat()
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

        val collisionThreshold = (p1.radius + p2.radius)
        if (distance < collisionThreshold) {
            val nx = dx / distance
            val ny = dy / distance

            val relativeVelocityX = p2.vx - p1.vx
            val relativeVelocityY = p2.vy - p1.vy
            val velocityAlongNormal = relativeVelocityX * nx + relativeVelocityY * ny

            if (velocityAlongNormal > 0) return
            val restitution = 1.0f
            val impulse = (-(1 + restitution) * velocityAlongNormal) / (1 / p1.mass + 1 / p2.mass)
            p1.vx += impulse / p1.mass * nx
            p1.vy += impulse / p1.mass * ny
            p2.vx -= impulse / p2.mass * nx
            p2.vy -= impulse / p2.mass * ny

            val overlap = collisionThreshold - distance
            p1.x -= overlap * nx / 2
            p1.y -= overlap * ny / 2
            p2.x += overlap * nx / 2
            p2.y += overlap * ny / 2
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
