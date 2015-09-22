package acid;

import acid.game.Game;
import acid.user.UserJoinRoomHandler;
import acid.user.UserLeaveRoomHandler;

import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class MainRoomExtension extends SFSExtension {

	public Game game;
	
	@Override
	public void init() {
		trace("MainRoomExtension: initializing...");
		
		addEventHandler(SFSEventType.USER_JOIN_ROOM, UserJoinRoomHandler.class);
		addEventHandler(SFSEventType.USER_LEAVE_ROOM, UserLeaveRoomHandler.class);
		
		game = new Game(getParentRoom());
		game.start();
		
		trace("MainRoomExtension: initialization completed.");
	}
	
	@Override
	public void destroy() {
		trace("MainRoomExtension: destroying...");
		super.destroy();
		
		try {
			game.shouldStop.set(true);
			if(game.isAlive())
				game.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		trace("MainRoomExtension: destruction completed.");
	}

}
