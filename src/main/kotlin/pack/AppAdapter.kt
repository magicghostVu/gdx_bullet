package pack

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
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
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.bullet.DebugDrawer
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw
import com.badlogic.gdx.physics.bullet.linearmath.btTransform
import com.badlogic.gdx.utils.Array
import mlogger.MLogger
import pack.logic_data.BoundBodyData
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


    // collision object

    private lateinit var planeBody: btRigidBody


    // config and world
    private lateinit var collisionConfig: btDefaultCollisionConfiguration
    private lateinit var collisionDispatcher: btCollisionDispatcher

    private lateinit var broadPhase: btBroadphaseInterface
    private lateinit var world: btDiscreteDynamicsWorld

    private lateinit var constraintSolver: btSequentialImpulseConstraintSolver


    private lateinit var debugDrawer: DebugDrawer


    private val allSphere = mutableListOf<SphereBodyData>()


    private lateinit var contactListener: ContactListener


    var shouldDelSphere = false;


    val allBound = mutableListOf<BoundBodyData>()

    override fun create() {
        modelBatch = ModelBatch()

        // tạo env
        environment = Environment()
        environment.set(ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f))
        environment.add(DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f))


        // tạo camera
        camera = PerspectiveCamera(67f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        camera.position.set(3f, 17f, 20f)
        camera.lookAt(0f, 0f, 0f)
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

        //val c =

        //val sphereCollisionShape = btBoxShape(Vector3(0.5f, 0.5f, 0.5f))
        val sphereCollisionShape = btSphereShape(0.5f)
        val mass = 1f

        val localInertia = Vector3()
        sphereCollisionShape.calculateLocalInertia(mass, localInertia)


        logger.info("local inertia is $localInertia")


        val sphereConstructionInfo = btRigidBody.btRigidBodyConstructionInfo(
            mass,
            motionState,
            sphereCollisionShape,
            localInertia
        )

        sphereConstructionInfo.linearDamping = 0.7f
        //sphereConstructionInfo.friction = 0.5f;
        //sphereConstructionInfo.rollingFriction = 0.1f


        sphereConstructionInfo.angularDamping = 0.5f

        //sphereConstructionInfo.rollingFriction = 0.1f

        // có thể đặt thêm các hệ số về friction để làm giảm tốc

        val sphereBody = btRigidBody(sphereConstructionInfo)



        sphereBody.worldTransform = sphereModelInstance.transform

        sphereBody.restitution = 0.7f;
        //sphereBody.angularFactor = Vector3()


        sphereBody.collisionFlags =
            sphereBody.collisionFlags or btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK

        world.addRigidBody(sphereBody)


        sphereConstructionInfo.dispose()

        sphereBody.userData = this
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




        planeCollisionShape = btBoxShape(Vector3(10f, 0.25f, 10f))


        // constructionInfo có thể dùng lại được
        // nhưng chưa biết có thread-safe hay không
        // khi đặt mass = 0 thì nó là static body
        val planeConstructionInfo = btRigidBody.btRigidBodyConstructionInfo(
            0f, null, planeCollisionShape, Vector3()
        )


        // áp dụng cho ma sát trượt
        // nếu để thấp thì vật dễ trượt đi trên mặt này
        // cản trở vận tốc tuyến tính nhiều hơn
        // hệ số này ngăn cản vật trượt trên bề mặt
        planeConstructionInfo.friction = 1f

        // ma sát lăn
        // càng cao thì sự cản trở lăn càng lớn
        // cản trở vận tốc xoay
        // giống như damping
        //planeConstructionInfo.rollingFriction = 0.5f

        planeConstructionInfo.restitution = 0.7f


        planeBody = btRigidBody(planeConstructionInfo)

        planeBody.collisionShape = planeCollisionShape
        planeBody.worldTransform = planeModelInstance.transform

        planeBody.userData = this

        world.addRigidBody(planeBody)

        // huỷ bỏ an toàn sau khi sử dụng xong
        planeConstructionInfo.dispose()



        allSphere.add(createSphere())

        createAllBound()




        debugDrawer = DebugDrawer()
        world.debugDrawer = debugDrawer
        debugDrawer.debugMode = btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE
        contactListener = MContactListener()
    }

    private fun createAllBound() {
        //
        val downBound = createABound(
            Vector3(0f, 10f, 10f),
            Vector3(20f, 20f, 0.5f)
        )

        world.addRigidBody(downBound.body)
        allBound.add(downBound)

        val upBound = createABound(
            Vector3(0f, 10f, -10f),
            Vector3(20f, 20f, 0.5f)
        )

        world.addRigidBody(upBound.body)
        allBound.add(upBound)


        

    }


    private fun createABound(position: Vector3, size: Vector3): BoundBodyData {

        val boundCollisionShape = btBoxShape(size.cpy().scl(0.5f))


        val boundConstructionInfo = btRigidBody.btRigidBodyConstructionInfo(
            0f, null, boundCollisionShape, Vector3()
        )


        // áp dụng cho ma sát trượt
        boundConstructionInfo.friction = 0.8f
        // ma sát lăn
        boundConstructionInfo.rollingFriction = 0.1f

        boundConstructionInfo.restitution = 0.7f

        val startTransform = boundConstructionInfo.startWorldTransform
        startTransform.origin = position

        val body = btRigidBody(boundConstructionInfo)


        boundConstructionInfo.dispose()

        return BoundBodyData(body, boundCollisionShape)
    }

    private fun disposeAllPhysicObject() {
        planeBody.dispose()
        planeCollisionShape.dispose()

        allBound.forEach {
            it.dispose()
        }

        allSphere.forEach {
            it.dispose()
        }


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
        val pos = Vector3(0f, 0f, 0f)
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


    var spawned = false;

    override fun render() {
        // do logic


        // thu thập input


        if (Gdx.input.isKeyPressed(Input.Keys.B) && !spawned) {
            allSphere.add(createSphere())
            spawned = true
        }


        if (Gdx.input.isKeyPressed(Input.Keys.I)) {
            val j = allSphere[0]
            j.body.applyCentralForce(Vector3(0f, 0f, -17f))
            j.body.activate()
            //logger.info("add force")
        }

        if (Gdx.input.isKeyPressed(Input.Keys.K)) {
            val j = allSphere[0]
            j.body.applyCentralForce(Vector3(0f, 0f, 17f))
            j.body.activate()
            //logger.info("add force")
        }

        if (Gdx.input.isKeyPressed(Input.Keys.J)) {
            val j = allSphere[0]
            j.body.applyCentralForce(Vector3(-17f, 0f, 0f))
            j.body.activate()
            //logger.info("add force")
        }

        if (Gdx.input.isKeyPressed(Input.Keys.L)) {
            val j = allSphere[0]
            j.body.applyCentralForce(Vector3(17f, 0f, 0f))
            j.body.activate()
            //logger.info("add force")
        }


        world.stepSimulation(Gdx.graphics.deltaTime, 10, 1 / 60f)




        if (shouldDelSphere && allSphere.size > 0) {
            val f = allSphere.removeAt(0)
            world.removeRigidBody(f.body)
            f.dispose()
        }

        //world.dispatcher.numManifolds


        /*val v = Vector3()
        sphereBody.worldTransform.getTranslation(v)*/

        //logger.info("after simu $v")

        // draw world


        /*val sphereMotionState = allSphere[0].motionState

        val sphereTransform = Matrix4()

        sphereMotionState.getWorldTransform(sphereTransform)

        sphereModelInstance.transform = sphereTransform*/

        camController.update()
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)


        /*modelBatch.begin(camera)
        modelBatch.render(modelInstances, environment)
        modelBatch.end()*/

        debugDrawer.begin(camera)
        world.debugDrawWorld()
        debugDrawer.end()
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