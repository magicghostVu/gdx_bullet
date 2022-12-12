package pack

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.DebugDrawer
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw
import com.badlogic.gdx.utils.Array
import mlogger.MLogger
import pack.logic_data.SphereBodyData


class AppAdapter : ApplicationAdapter() {
    private val logger = MLogger.logger


    private lateinit var camera: PerspectiveCamera

    private lateinit var camController: CameraInputController

    private lateinit var modelBatch: ModelBatch
    private lateinit var environment: Environment

    private lateinit var modelInstances: Array<ModelInstance>


    private lateinit var sphereModelInstance: ModelInstance
    private lateinit var planeModelInstance: ModelInstance


    // collision shape
    private lateinit var planeCollisionShape: btBoxShape


    //private lateinit var sphereCollisionShape: btSphereShape


    // collision object

    private lateinit var planeBody: btRigidBody


    //private lateinit var sphereBody: btRigidBody


    // config and world
    private lateinit var collisionConfig: btDefaultCollisionConfiguration
    private lateinit var collisionDispatcher: btCollisionDispatcher

    private lateinit var broadPhase: btBroadphaseInterface
    private lateinit var world: btDiscreteDynamicsWorld

    private lateinit var constraintSolver: btSequentialImpulseConstraintSolver


    private lateinit var debugDrawer: DebugDrawer


    private lateinit var sphereData: SphereBodyData;


    override fun create() {
        modelBatch = ModelBatch()

        // tạo env
        environment = Environment()
        environment.set(ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f))
        environment.add(DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f))


        // tạo camera
        camera = PerspectiveCamera(67f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        camera.position.set(3f, 7f, 10f)
        camera.lookAt(0f, 4f, 0f)
        camera.update()

        camController = CameraInputController(camera)
        Gdx.input.inputProcessor = camController

        modelInstances = Array()


        setupRendererObject()
        setupPhysic()


        logger.info("app created")
    }


    private fun createSphere(): SphereBodyData {
        val motionState = btDefaultMotionState()
        val sphereCollisionShape = btSphereShape(0.5f)
        val sphereConstructionInfo = btRigidBody.btRigidBodyConstructionInfo(
            1f,
            motionState,
            sphereCollisionShape,
            Vector3()
        )
        val sphereBody = btRigidBody(sphereConstructionInfo)
        sphereBody.worldTransform = sphereModelInstance.transform

        world.addRigidBody(sphereBody)
        sphereConstructionInfo.dispose()
        return SphereBodyData(sphereBody, motionState, sphereCollisionShape)
    }

    private fun setupPhysic() {

        //debugDrawer.ren

        collisionConfig = btDefaultCollisionConfiguration()
        collisionDispatcher = btCollisionDispatcher(collisionConfig)




        broadPhase = btDbvtBroadphase()

        constraintSolver = btSequentialImpulseConstraintSolver()
        world = btDiscreteDynamicsWorld(collisionDispatcher, broadPhase, constraintSolver, collisionConfig)
        world.gravity = Vector3(0f, -9.8f, 0f)




        planeCollisionShape = btBoxShape(Vector3(5f, 0.25f, 5f))


        // constructionInfo có thể dùng lại được
        // nhưng chưa biết có thread-safe hay không
        // khi đặt mass = 0 thì nó có vẻ như là static body
        val planeConstructionInfo = btRigidBody.btRigidBodyConstructionInfo(
            0f, null, planeCollisionShape, Vector3()
        )


        planeBody = btRigidBody(planeConstructionInfo)

        planeBody.collisionShape = planeCollisionShape
        planeBody.worldTransform = planeModelInstance.transform

        world.addRigidBody(planeBody)

        planeConstructionInfo.dispose()



        sphereData = createSphere()


        debugDrawer = DebugDrawer()
        world.debugDrawer = debugDrawer
        debugDrawer.debugMode = btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE

    }


    private fun disposeAllPhysicObject() {
        planeBody.dispose()
        planeCollisionShape.dispose()



        sphereData.dispose()


        //collisionConfig.dispose()
        // dispose world trước
        // dispose world sau cùng sẽ gây crash
        world.dispose()
        broadPhase.dispose()
        collisionDispatcher.dispose()
        constraintSolver.dispose()
        collisionConfig.dispose()
        debugDrawer.dispose()
    }

    private fun setupRendererObject() {
        val mb = ModelBuilder()
        val planeMaterial = Material(ColorAttribute.createDiffuse(Color.CORAL))
        val planeModel = mb.createBox(10f, 0.5f, 10f, planeMaterial, (Usage.Position or Usage.Normal).toLong())
        planeModelInstance = ModelInstance(planeModel)
        //theo mặc định x là trục ngang, y đứng, z xuyên từ bên trong màn hình đi ra
        val pos = Vector3(0f, -0.5f, -2f)
        planeModelInstance.transform.setTranslation(pos)
        modelInstances.add(planeModelInstance)


        // tạo sphere ở trên plane
        val sphereMat = Material(ColorAttribute.createDiffuse(Color.BROWN))
        val sphereModel = mb.createSphere(
            1f,
            1f,
            1f,
            30,
            30,
            sphereMat,
            (Usage.Position or Usage.Normal).toLong()
        )

        sphereModelInstance = ModelInstance(sphereModel)
        sphereModelInstance.transform.setTranslation(
            Vector3(0f, 5f, 0f)
        )
        modelInstances.add(sphereModelInstance)

    }


    override fun render() {
        // do logic


        world.stepSimulation(0.02f)


        /*val v = Vector3()
        sphereBody.worldTransform.getTranslation(v)*/

        //logger.info("after simu $v")

        // draw world


        val sphereMotionState = sphereData.motionState

        val sphereTransform = Matrix4()

        sphereMotionState.getWorldTransform(sphereTransform)

        sphereModelInstance.transform = sphereTransform

        camController.update()
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        modelBatch.begin(camera)
        modelBatch.render(modelInstances, environment)
        modelBatch.end()

        /*debugDrawer.begin(camera);
        world.debugDrawWorld();
        debugDrawer.end();*/
    }

    override fun dispose() {
        modelBatch.dispose()
        modelInstances.forEach {
            it.model.dispose()
        }
        disposeAllPhysicObject()
        logger.info("app disposed")
    }
}