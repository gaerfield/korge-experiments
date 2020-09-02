import com.soywiz.kds.setExtra
import com.soywiz.klock.hr.hrMilliseconds
import com.soywiz.korev.Key
import com.soywiz.korge.box2d.*
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.*
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World
import kotlin.random.Random
import kotlin.random.nextInt

fun View.advance(amount: Double, rot: Angle = (-90).degrees) = this.apply {
    x += (this.rotation + rot).cosine * amount
    y += (this.rotation + rot).sine * amount
}

class RectanglePrototype() : Scene() {
    override suspend fun Container.sceneInit() {
        views.clearColor = Colors.LIGHTSKYBLUE
        val world = WorldWithGravity(
                width = this.width,
                height = this.height,
                stage = this.stage!!)
        worldView(world)
        addHrUpdater { time -> world.update(time) }

        val flummy = Flummy(stage!!, world, 100, 100, 10,elasticiy = 1.0)

        (1..100).forEach {
            val f = Flummy(stage!!, world, Random.nextInt(100,400), Random.nextInt(100), 4,elasticiy = 1.0)
        }

//                solidRect(20.0,20.0,Colors.BLUE)
//                .position(150,100)
//                .centered

        addHrUpdater { time ->
            val scale = time / 16.hrMilliseconds

            if (views.input.keys[Key.LEFT]) flummy.left()
            if (views.input.keys[Key.RIGHT])  flummy.right()// flummy.wrapped.rotation += 3.degrees * scale
            if (views.input.keys[Key.UP])  flummy.up()// flummy.wrapped.advance(2.0 * scale)
            if (views.input.keys[Key.DOWN])  flummy.down()// flummy.wrapped.advance(-1.5 * scale)

        }


    }
}

class Flummy(
        private val stage: Stage,
        private val world: World,
        x: Int = 0,
        y: Int = 0,
        val size: Int = 3,
        density: Double = 0.5,
        friction: Double = 0.2,
        elasticiy: Double = 0.0
) {
    private val changeImpact = 1f
    private operator fun Vec2.plus(vec2: Vec2) = Vec2(x+vec2.x, y+vec2.y)
    private operator fun Vec2.minus(vec2: Vec2) = Vec2(x-vec2.x, y-vec2.y)
    fun left() { body.linearVelocity -= Vec2(changeImpact,0f) }
    fun right() { body.linearVelocity += Vec2(changeImpact,0f) }
    fun up() { body.linearVelocity -= Vec2(0f,changeImpact) }
    fun down() { body.linearVelocity += Vec2(0f,changeImpact) }

    val wrapped = stage.circle(size.toDouble(), Colors.RED).position(100, 100).centered
    val body = world.createBody {
        this.type = BodyType.DYNAMIC
        this.setPosition(x, y)
    }.fixture {
        this.shape = CircleShape(size)
        this.density = density.toFloat()
        this.friction = friction.toFloat()
        this.restitution = elasticiy.toFloat()
    }
    //var angle = 30.degrees

	init {
        wrapped.speed = 0.6
        body[WorldView.ViewKey] = wrapped
        wrapped.setExtra("box2dBody", body)

        wrapped.addHrUpdater { time ->
            val px = body.position.x.toDouble()
            val py = body.position.y.toDouble()
            wrapped.x = px
            wrapped.y = py
            wrapped.rotationRadians = body.angle.toDouble()
            if(body.getContactList() != null) {
                wrapped.color = Colors.colorsByName.values.toList().get(Random.nextInt(Colors.colorsByName.size))
            }
        }
    }

}
