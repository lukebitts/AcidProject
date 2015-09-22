package acid.game;

import java.util.Arrays;

import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.api.ISFSMMOApi;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.mmo.Vec3D;

public class PlayerMotionState extends MotionState {

	private Transform initialPosition;
	private Vector3f position;
	private User user;
	private Room room;
	private ISFSMMOApi mmoApi;
	
	private UserVariable varX;
	private UserVariable varY;
	private UserVariable varZ;
	
	public PlayerMotionState(Transform initialPosition, User user, Room room) {		
		this.initialPosition = initialPosition;
		this.user = user;
		this.room = room;

		mmoApi = SmartFoxServer.getInstance().getAPIManager().getMMOApi();
		
		setUserPosition(initialPosition.origin);
	}
	
	public void setUserPosition()
	{
		setUserPosition(position);
	}
	
	private void setUserPosition(Vector3f position)
	{
		varX = new SFSUserVariable("x", position.x);
		varY = new SFSUserVariable("y", position.y);
		varZ = new SFSUserVariable("z", position.z);
		
		SmartFoxServer.getInstance().getAPIManager().getSFSApi().setUserVariables(user, Arrays.asList(varX, varY, varZ));
		mmoApi.setUserPosition(user, new Vec3D(position.x, position.y, position.z), room);
	}
	
	@Override
	public Transform getWorldTransform(Transform worldTransform) {
		worldTransform.set(initialPosition);
		return worldTransform;
	}

	@Override
	public void setWorldTransform(Transform worldTransform) {
		if(user.isJoinedInRoom(room)) {
			/*Quat4f rot = new Quat4f(); 
			worldTransform.getRotation(rot);
			Vector3f pos = worldTransform.origin;*/
			
			this.position = new Vector3f(worldTransform.origin.x, worldTransform.origin.y, worldTransform.origin.z);
		}
	}

}
