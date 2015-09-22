using UnityEngine;
using System.Collections;
using Sfs2X;
using Sfs2X.Core;
using Sfs2X.Logging;
using Sfs2X.Entities.Data;

public class Login : MonoBehaviour
{

    SmartFox sfs;

    public GameObject connectionWindow;
    public GameObject loginWindow;
    public GameObject loggingWindow;

    public UIInput usernameField;
    public UIInput passwordField;

    void SetupListeners()
    {
        sfs.AddEventListener(SFSEvent.CONNECTION, OnConnection);
        sfs.AddEventListener(SFSEvent.CONNECTION_LOST, OnConnectionLost);
        sfs.AddEventListener(SFSEvent.LOGIN, OnLogin);
        sfs.AddEventListener(SFSEvent.LOGIN_ERROR, OnLoginError);
        sfs.AddEventListener(SFSEvent.ROOM_JOIN, OnRoomJoin);
        sfs.AddEventListener(SFSEvent.ROOM_JOIN_ERROR, OnRoomJoinError);
    }

    void Start()
    {
        sfs = Connection.Instance().Sfs();
        SetupListeners();

        loginWindow.SetActive(true);

#if !UNITY_EDITOR
        usernameField.value = "luke";
        passwordField.value = "123";
#endif
    }

    void Connect()
    {
        sfs.Connect("127.0.0.1", 9933);
    }

    public void LoginClick()
    {
        loginWindow.SetActive(false);
        connectionWindow.SetActive(true);
        Connect();
    }

    void OnLogin(BaseEvent evt)
    {
        var user = (Sfs2X.Entities.User)evt.Params["user"];
        Debug.Log("Login success. " + user.Name);

        loggingWindow.SetActive(false);
        sfs.Send(new Sfs2X.Requests.JoinRoomRequest("MainRoom"));
    }

    void OnLoginError(BaseEvent evt)
    {
        Debug.Log("Login error: " + evt.Params["errorMessage"] + " - Code: " + evt.Params["errorCode"]);

        loginWindow.SetActive(true);
        loggingWindow.SetActive(false);

        sfs.Disconnect();
    }

    void OnConnection(BaseEvent evt)
    {
        bool connectionSuccess = (bool)evt.Params["success"];

        Debug.Log("OnConn:" + connectionSuccess);

        if (connectionSuccess)
        {
            SFSObject loginData = new SFSObject();
            loginData.PutUtfString("username", usernameField.value);
            loginData.PutUtfString("password", passwordField.value);

            sfs.Send(new Sfs2X.Requests.LoginRequest("", "", "MainZone", loginData));

            connectionWindow.SetActive(false);
            loggingWindow.SetActive(true);
        }
        else
        {
            sfs = Connection.Instance().Reset();
            SetupListeners();

            loginWindow.SetActive(true);
            connectionWindow.SetActive(false);
        }
    }

    void OnConnectionLost(BaseEvent evt)
    {
        string reason = evt.Params["reason"] as string;

        print("Connection lost: " + reason);

        sfs = Connection.Instance().Reset();
        SetupListeners();

        loginWindow.SetActive(true);
        loggingWindow.SetActive(false);
        connectionWindow.SetActive(false);
    }

    void OnRoomJoin(BaseEvent evt)
    {
        Debug.Log("Room joined.");

        sfs.RemoveEventListener(SFSEvent.CONNECTION, OnConnection);
        sfs.RemoveEventListener(SFSEvent.CONNECTION_LOST, OnConnectionLost);
        sfs.RemoveEventListener(SFSEvent.LOGIN, OnLogin);
        sfs.RemoveEventListener(SFSEvent.LOGIN_ERROR, OnLoginError);
        sfs.RemoveEventListener(SFSEvent.ROOM_JOIN, OnRoomJoin);
        sfs.RemoveEventListener(SFSEvent.ROOM_JOIN_ERROR, OnRoomJoinError);

        Application.LoadLevel("Scene2");
    }

    void OnRoomJoinError(BaseEvent evt)
    {
        Debug.Log("Room join error.");
    }

}
