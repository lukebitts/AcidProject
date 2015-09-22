package acid.user;

import java.sql.SQLException;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSConstants;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.exceptions.SFSLoginException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

import acid.util.Util;

/**
 * Classe responsável pelo login do ususário
 * 
 * @author lucasbittencourt
 *
 */
public class LoginEventHandler extends BaseServerEventHandler {

	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException {
		ISFSObject loginData = (ISFSObject) event.getParameter(SFSEventParam.LOGIN_IN_DATA);
		
		if(Util.DEBUG) {
			if(!Util.CheckParameters(loginData, new String[]{"username", "password"}, new Class[]{String.class, String.class})) {
				SFSErrorData errorData = new SFSErrorData(SFSErrorCode.GENERIC_ERROR);
				errorData.addParameter("Login missing parameters or wrong parameter types.");
				
				throw new SFSLoginException(null, errorData);
			}
		}
		
		String username = loginData.getUtfString("username");
		String password = loginData.getUtfString("password");

		try {
			IDBManager dbManager = getParentExtension().getParentZone().getDBManager();
			String userSql = "SELECT * FROM user WHERE name = ?";
			
			ISFSArray userResult = dbManager.executeQuery(userSql, new Object[]{username});
			if(userResult.size() > 0) {
				ISFSObject user = userResult.getSFSObject(0);
				if(!user.getUtfString("password").equals(password)) {
					SFSErrorData errorData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_PASSWORD);
					errorData.addParameter(username);
					
					throw new SFSLoginException(null, errorData);
				}
			}
			else {
				SFSErrorData errorData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_USERNAME);
				errorData.addParameter(username);
				
				throw new SFSLoginException(null, errorData);
			}
			
		}
		catch(SQLException ex) {
			trace(ex);
			SFSErrorData errorData = new SFSErrorData(SFSErrorCode.GENERIC_ERROR);
			errorData.addParameter("SQL Exception: " + ex.toString());
			
			throw new SFSLoginException(null, errorData);
		}
		
		ISFSObject outData = (ISFSObject) event.getParameter(SFSEventParam.LOGIN_OUT_DATA);
		outData.putUtfString(SFSConstants.NEW_LOGIN_NAME, username);
	}
}
