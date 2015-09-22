package acid.user;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import acid.MainRoomExtension;

public class UserLeaveRoomHandler extends BaseServerEventHandler  {

	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException {
		
		MainRoomExtension ext = (MainRoomExtension)getParentExtension();
		User user = (User)event.getParameter(SFSEventParam.USER);
		
		trace(String.format("User '%s' (%s) left room.",user.getName(), user.getId()));
		
		try {
			ext.game.userLeave(user);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}
