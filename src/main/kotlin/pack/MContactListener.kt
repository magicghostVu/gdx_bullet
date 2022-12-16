package pack

import com.badlogic.gdx.physics.bullet.collision.ContactListener
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject
import com.badlogic.gdx.physics.bullet.collision.btManifoldPoint
import com.badlogic.gdx.physics.bullet.collision.btPersistentManifold
import mlogger.MLogger
import org.slf4j.Logger

//chỉ cần một contact listener duy nhất cho hệ thống
// sử dụng user data để thao tác với logic data khi có va chạm xảy ra
class MContactListener : ContactListener() {

    private val logger: Logger = MLogger.logger

    override fun onContactAdded(
        cp: btManifoldPoint,
        colObj0: btCollisionObject,
        partId0: Int,
        index0: Int,
        colObj1: btCollisionObject,
        partId1: Int,
        index1: Int
    ): Boolean {
        //logger.info("contact start")
        val app = colObj0.userData as AppAdapter
        //app.shouldDelSphere = true
        return true;
    }

    override fun onContactProcessed(colObj0: btCollisionObject, colObj1: btCollisionObject) {
        //logger.info("contact processed")
    }

    override fun onContactEnded(manifold: btPersistentManifold?) {
        //logger.info("contact ended", Exception())
    }
}