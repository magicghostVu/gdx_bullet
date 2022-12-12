package pack.logic_data

import com.badlogic.gdx.physics.bullet.collision.btCollisionShape
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState
import com.badlogic.gdx.utils.Disposable

class SphereBodyData(
    val body: btRigidBody,
    val motionState: btMotionState,
    val collisionShape: btCollisionShape
    ) : Disposable {

    override fun dispose() {
        body.dispose()
        motionState.dispose()
    }
}