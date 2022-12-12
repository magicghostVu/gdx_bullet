package pack

import com.badlogic.gdx.physics.bullet.collision.ContactListener
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject

class MContactListener:ContactListener() {
    override fun onContactAdded(
        colObj0: btCollisionObject?,
        partId0: Int,
        index0: Int,
        colObj1: btCollisionObject?,
        partId1: Int,
        index1: Int
    ): Boolean {
        return super.onContactAdded(colObj0, partId0, index0, colObj1, partId1, index1)
    }
}