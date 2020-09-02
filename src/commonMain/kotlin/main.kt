import com.soywiz.kds.setExtra
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.Korge
import com.soywiz.korge.box2d.*
import com.soywiz.korge.view.*
import com.soywiz.korgw.GameWindow
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.degrees
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*

suspend fun main() = Korge(
		width = 512,
		height = 512,
		bgcolor = Colors["#2b2b2b"],
		quality = GameWindow.Quality.PERFORMANCE,
		title = "My Awesome Box2D Game!"
) {

	val world = FallingRectangles(stage = this)
	worldView(world)
	addUpdater { world.draw(it) }
	val rectangles = listOf(
		solidRect(20, 20, Colors.RED).position(100, 100).centered.rotation(30.degrees),
		solidRect(20, 20, Colors.RED).position(109, 75).centered,
		solidRect(20, 20, Colors.RED).position(93, 50).centered.rotation((-15).degrees)
	)
	world.register(rectangles[0], density = 2, friction = 0.01)
	world.register(rectangles[1])
	world.register(rectangles[2])

}

class FallingRectangles(
		gravityX: Double = 0.0,
		gravityY: Double = 100.0,
		private val velocityIterations: Int = 6,
		private val positionIterations: Int = 2,
		val stage: Stage
) : World(Vec2(gravityX.toFloat(), gravityY.toFloat())) {
	private val BOX2D_WORLD_KEY = "box2dWorld"
	private val BOX2D_BODY_KEY = "box2dBody"

	init {
		stage.setExtra(BOX2D_WORLD_KEY, this)
		with(stage) {
			val centered = solidRect(400, 100, Colors.LIMEGREEN)
					.position(100, 300)
					.centered
			register(box = centered, dynamic = false, friction = 0.2)
		}
	}

	final fun draw(timeSpan: TimeSpan) {
		step(timeSpan.seconds.toFloat(), velocityIterations, positionIterations)

		val box = stage.solidRect(2, 2, Colors.BLUE).position(100, 100)
				.centered.rotation(30.degrees)
		register(box)

		forEachBody { node ->
			val px = node.position.x.toDouble()
			val py = node.position.y.toDouble()
			val view = node[WorldView.ViewKey]
			if (view != null) {
				view.x = px
				view.y = py
				view.rotationRadians = node.angle.toDouble()
			}
			//println(node.position)
		}
	}

	fun register(
			box: SolidRect,
			density: Number = 0.5f,
			friction: Number = 0.2f,
			dynamic: Boolean = true
	) {
		val body = createBody {
			this.type = if (dynamic) BodyType.DYNAMIC else BodyType.STATIC
			this.setPosition(box.x, box.y)
			this.angle = box.rotationRadians.toFloat()
		}.fixture {
			this.shape = BoxShape(box.width, box.height)
			this.density = density.toFloat()
			this.friction = friction.toFloat()
		}
		body[WorldView.ViewKey] = box
		box.setExtra(BOX2D_BODY_KEY, body)
	}

}
