import com.soywiz.kds.setExtra
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.hr.HRTimeSpan
import com.soywiz.korge.box2d.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.World
import org.jbox2d.dynamics.forEachBody

class WorldWithGravity(
        val stage: Stage,
        val width : Double,
        val height: Double,
        gravityX: Double = 0.0,
        gravityY: Double = 20.0,
        private val velocityIterations: Int = 6,
        private val positionIterations: Int = 2
) : World(Vec2(gravityX.toFloat(), gravityY.toFloat())) {

    private val BOX2D_WORLD_KEY = "box2dWorld"
    private val BOX2D_BODY_KEY = "box2dBody"

    init {
        stage.setExtra(BOX2D_WORLD_KEY, this)
        with(stage) {

            val ground = solidRect(this.width, this.height*0.1, Colors.LIMEGREEN)
                    .position(this.width/2, this.height)
                    .centered
            val upperWall = solidRect(this.width, this.height*0.1, Colors.LIMEGREEN)
                    .position(this.width/2, 0.0)
                    .centered
            val leftWall = solidRect(this.width*0.1, this.height, Colors.LIMEGREEN)
                    .position(0.0, this.height/2)
                    .centered
            val rightWall = solidRect(this.width*0.1, this.height, Colors.LIMEGREEN)
                    .position(this.width, this.height/2)
                    .centered
            register(box = ground, dynamic = false, friction = 1.0)
            register(box = upperWall, dynamic = false, friction = 1.0)
            register(box = leftWall, dynamic = false, friction = 1.0)
            register(box = rightWall, dynamic = false, friction = 1.0)
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
            this.restitution = 1.5f
        }
        body[WorldView.ViewKey] = box
        box.setExtra(BOX2D_BODY_KEY, body)
    }

    fun update(time: HRTimeSpan) {
        step(time.secondsDouble.toFloat(), velocityIterations, positionIterations)
    }
}