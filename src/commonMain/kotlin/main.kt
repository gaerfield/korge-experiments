import com.soywiz.kds.setExtra
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.hr.hrMilliseconds
import com.soywiz.korev.Key
import com.soywiz.korge.Korge
import com.soywiz.korge.box2d.*
import com.soywiz.korge.scene.Module
import com.soywiz.korge.view.*
import com.soywiz.korgw.GameWindow
import com.soywiz.korim.color.Colors
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korma.geom.*
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*

suspend fun main() = Korge(
		Korge.Config(module = MainModule)
)

object MainModule : Module() {
	// define the opening scene
	override val mainScene = RectanglePrototype::class

	// define the game configs
	override val title: String = "My Test Game"
	override val size: SizeInt = SizeInt(512, 512)

	// add the scenes to the module
	override suspend fun AsyncInjector.configure() {
		mapPrototype { RectanglePrototype() }
	}
}

suspend fun whoop() = Korge(
		width = 512,
		height = 512,
		bgcolor = Colors["#2b2b2b"]

) {

	val world = FallingRectangles(stage = this)
	worldView(world)
	addUpdater { world.draw(it) }
	val rectangles = listOf(
		solidRect(20, 20, Colors.RED).position(100, 100).centered.rotation(30.degrees),
		solidRect(20, 20, Colors.RED).position(120, 100).centered,
		solidRect(20, 20, Colors.RED).position(80, 100).centered.rotation((-15).degrees)
	)
	world.register(rectangles[0], density = 20, friction = 0.01)
	world.register(rectangles[1])
	world.register(rectangles[2])

	val flummy = solidRect(20.0,20.0,Colors.BLUE)
			.position(150,100)
			.centered

	addHrUpdater { time ->
		val scale = time / 16.hrMilliseconds
		if (views.input.keys[Key.LEFT]) flummy.rotation -= 3.degrees * scale
		if (views.input.keys[Key.RIGHT]) flummy.rotation += 3.degrees * scale
		if (views.input.keys[Key.UP]) flummy.advance(2.0 * scale)
		if (views.input.keys[Key.DOWN]) flummy.advance(-1.5 * scale)

	}

}

//class Asteroid(val asteroidSize: Int = 3) : SolidRect() {
//	var angle = 30.degrees
//
//	init {
//		anchor(.5, .5)
//		scale = asteroidSize.toDouble() / 3.0
//		name = "asteroid"
//		speed = 0.6
//		addHrUpdater { time ->
//			val scale = time / 16.hrMilliseconds
//			val dx = angle.cosine * scale
//			val dy = angle.sine * scale
//			x += dx
//			y += dy
//			rotationDegrees += scale
//			if (y < 0 && dy < 0) angle += 45.degrees
//			if (x < 0 && dx < 0) angle += 45.degrees
//			if (x > WIDTH && dx > 0) angle += 45.degrees
//			if (y > HEIGHT && dy > 0) angle += 45.degrees
//		}
//	}
//
//}


class FallingRectangles(
		gravityX: Double = 0.0,
		gravityY: Double = 10.0,
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
			register(box = centered, dynamic = false, friction = 10.0)

			//register(flummy)
		}
	}

	final fun draw(timeSpan: TimeSpan) {
		step(timeSpan.seconds.toFloat(), velocityIterations, positionIterations)



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
