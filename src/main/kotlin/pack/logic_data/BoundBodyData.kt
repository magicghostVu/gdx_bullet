package pack.logic_data

import com.badlogic.gdx.physics.bullet.collision.btCollisionShape
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.utils.Disposable

class BoundBodyData(val body: btRigidBody, val collisionShape: btCollisionShape) : Disposable {

    override fun dispose() {
        body.dispose()
        collisionShape.dispose()
    }
}