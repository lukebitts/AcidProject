package acid.game;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.vecmath.Vector3f;
import javax.vecmath.Quat4f;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.variables.UserVariable;

import acid.util.Util;

public class Game extends Thread {

	public AtomicBoolean shouldStop = new AtomicBoolean(false);

	private DiscreteDynamicsWorld world;
	private Room room;
	private ConcurrentHashMap<Integer, User> joinedUsers = new ConcurrentHashMap<Integer, User>();
	private ConcurrentHashMap<Integer, User> leftUsers = new ConcurrentHashMap<Integer, User>();
	private ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<Integer, User>();
	private Util.Lock userLock = new Util.Lock();

	public Game(Room room) {
		this.room = room;
	}

	public void userJoin(User user) throws InterruptedException {
		userLock.lock();
		try {
			joinedUsers.put(user.getId(), user);
		} finally {
			userLock.unlock();
		}
	}

	public void userLeave(User user) throws InterruptedException {
		userLock.lock();
		try {
			leftUsers.put(user.getId(), user);
		} finally {
			userLock.unlock();
		}
	}

	@Override
	public void run() {
		BroadphaseInterface broadphase = new DbvtBroadphase();
		DefaultCollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
		CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);

		SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();

		world = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
		world.setGravity(new Vector3f(0, -10, 0));

		/* ///GROUND/// */

		CollisionShape groundShape = new StaticPlaneShape(new Vector3f(0, 1, 0), 1);
		Transform groundTransform = new Transform();
		groundTransform.setRotation(new Quat4f(0.f, 0.f, 0.f, 1.f));
		groundTransform.origin.set(new float[] { 0, -1, 0 });
		DefaultMotionState groundMotionState = new DefaultMotionState(groundTransform);

		RigidBodyConstructionInfo groundRigidBodyCI = new RigidBodyConstructionInfo(0, groundMotionState, groundShape, new Vector3f(0, 0, 0));
		RigidBody groundRigidBody = new RigidBody(groundRigidBodyCI);

		world.addRigidBody(groundRigidBody);

		final float TEN_TO_NINTH = (float) Math.pow(10, 9);
		final float ONE_OVER_FPS = 1.f / 60.f;
		final float ONE_OVER_PHYSICS_FPS = 1.f / 10.f;
		float accumulator = 0;
		float network_accumulator = 0;
		float lastTime = (float) (System.nanoTime() / TEN_TO_NINTH);

		while (shouldStop.get() == false) {

			try {
				userLock.lock();

				for (Entry<Integer, User> pair : joinedUsers.entrySet()) {
					RigidBody rb = spawnPlayer(pair.getValue());
					pair.getValue().setProperty("RigidBody", rb);
					rb.setUserPointer(pair.getValue());
				}

				users.putAll(joinedUsers);
				joinedUsers.clear();

				for (Entry<Integer, User> pair : leftUsers.entrySet()) {
					RigidBody rb = (RigidBody) pair.getValue().getProperty("RigidBody");
					world.removeRigidBody(rb);
					rb.destroy();

					users.remove(pair.getKey());
				}

				leftUsers.clear();
			} catch (InterruptedException ex) {
				System.out.println(ex);
			} finally {
				userLock.unlock();
			}
			
			if (accumulator >= ONE_OVER_FPS) {
				world.stepSimulation(ONE_OVER_FPS, 10);
				accumulator -= ONE_OVER_FPS;
			}

			if(network_accumulator >= ONE_OVER_PHYSICS_FPS) {
				for(User u : users.values()) {
					RigidBody rb = (RigidBody)u.getProperty("RigidBody");
					PlayerMotionState pms = (PlayerMotionState)rb.getMotionState(); 
					pms.setUserPosition(); 
				}
				network_accumulator -= ONE_OVER_PHYSICS_FPS;
			}
			
			float currentTime = (float) (System.nanoTime() / TEN_TO_NINTH);
			float delta = currentTime - lastTime;

			accumulator += delta;
			network_accumulator += delta;
			lastTime = currentTime;
			
			//TODO: Player input
		}

		world.destroy();
	}

	private RigidBody spawnPlayer(User user) {
		//TODO: this should be a character controller, not a rigidbody
		
		CollisionShape fallShape = new SphereShape(1);
		Transform fallTransform = new Transform();
		fallTransform.setRotation(new Quat4f(0, 0, 0, 1));
		
		UserVariable varX = user.getVariable("x");
		UserVariable varY = user.getVariable("y");
		UserVariable varZ = user.getVariable("z");
		
		fallTransform.origin.set(new float[] { varX.getDoubleValue().floatValue(), varY.getDoubleValue().floatValue(), varZ.getDoubleValue().floatValue() });
		PlayerMotionState fallMotionState = new PlayerMotionState(fallTransform, user, room);
		float mass = 1;
		Vector3f fallInertia = new Vector3f(0, 0, 0);
		fallShape.calculateLocalInertia(mass, fallInertia);

		RigidBodyConstructionInfo fallRigidBodyCI = new RigidBodyConstructionInfo(mass, fallMotionState, fallShape, fallInertia);
		RigidBody fallRigidBody = new RigidBody(fallRigidBodyCI);

		world.addRigidBody(fallRigidBody);

		return fallRigidBody;
	}

}
