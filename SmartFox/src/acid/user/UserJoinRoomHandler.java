package acid.user;

import java.util.Arrays;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.api.ISFSMMOApi;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import com.smartfoxserver.v2.mmo.Vec3D;

import acid.MainRoomExtension;

public class UserJoinRoomHandler extends BaseServerEventHandler {

	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException {
		
		MainRoomExtension ext = (MainRoomExtension)getParentExtension();
		User user = (User)event.getParameter(SFSEventParam.USER);
		
		trace(String.format("User '%s' (%s) joined room.",user.getName(), user.getId()));
		
		Room room = ext.getParentRoom();
		ISFSMMOApi mmoApi = SmartFoxServer.getInstance().getAPIManager().getMMOApi();
		
		
		float randX = (float)Math.random() * 2.f;
		
		mmoApi.setUserPosition(user, new Vec3D(randX, 50.f, 0.f), room);
		
		UserVariable varX = new SFSUserVariable("x", randX);
		UserVariable varY = new SFSUserVariable("y", 50.f);
		UserVariable varZ = new SFSUserVariable("z", 0.f);
		
		SmartFoxServer.getInstance().getAPIManager().getSFSApi().setUserVariables(user, Arrays.asList(varX, varY, varZ));
		
		try {
			ext.game.userJoin(user);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
}
