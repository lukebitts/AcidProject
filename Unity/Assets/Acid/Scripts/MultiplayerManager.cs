using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using Sfs2X;
using Sfs2X.Core;
using Sfs2X.Logging;
using Sfs2X.Entities;
using Sfs2X.Entities.Data;
using Sfs2X.Entities.Variables;
using System;

public class MultiplayerManager : MonoBehaviour
{
    public GameObject playerPrefab;
    public GameObject networkPlayerPrefab;

    SmartFox sfs;
    Dictionary<int, User> users = new Dictionary<int, User>();

    void Start()
    {
        sfs = Connection.Instance().Sfs();
        
        sfs.AddEventListener(SFSEvent.PROXIMITY_LIST_UPDATE, OnProximityListUpdate);
        sfs.AddEventListener(SFSEvent.USER_VARIABLES_UPDATE, OnUserVariablesUpdate);
        sfs.AddEventListener(SFSEvent.CONNECTION_LOST, OnConnectionLost);

        float x = (float)sfs.MySelf.GetVariable("x").GetDoubleValue();
        float y = (float)sfs.MySelf.GetVariable("y").GetDoubleValue();
        float z = (float)sfs.MySelf.GetVariable("z").GetDoubleValue();

        GameObject playerGo = Instantiate(playerPrefab, new Vector3(x, y, z), Quaternion.identity) as GameObject;
        sfs.MySelf.Properties.Add("GameObject", playerGo);
        users.Add(sfs.MySelf.Id, sfs.MySelf);
    }

    void Update()
    {
        //TODO: input
    }

    private void OnUserVariablesUpdate(BaseEvent evt)
    {
        ArrayList changedVars = (ArrayList)evt.Params["changedVars"];

        SFSUser user = (SFSUser)evt.Params["user"];

        Debug.Log(user.Id);

        if(!users.ContainsKey(user.Id))
        {
            Debug.LogWarning("Got variable update for an user that is not in the users list.");
            return;
        }

        if (changedVars.Contains("x") || changedVars.Contains("y") || changedVars.Contains("z"))
        {
            GameObject playerGo = user.Properties["GameObject"] as GameObject;

            Vector3 newPosition = new Vector3();
            newPosition.x = (float)user.GetVariable("x").GetDoubleValue();
            newPosition.y = (float)user.GetVariable("y").GetDoubleValue();
            newPosition.z = (float)user.GetVariable("z").GetDoubleValue();

            playerGo.transform.position = newPosition;
        }
    }

    private void OnProximityListUpdate(BaseEvent evt)
    {
        Debug.Log("OnProximityListUpdate");

        var added = evt.Params["addedUsers"] as List<User>;
        var removed = evt.Params["removedUsers"] as List<User>;

        foreach(User user in added) {
            var entryPoint = user.AOIEntryPoint;

            GameObject playerGo = Instantiate(networkPlayerPrefab, new Vector3(entryPoint.FloatX, entryPoint.FloatY, entryPoint.FloatZ), Quaternion.identity) as GameObject;
            user.Properties.Add("GameObject", playerGo);

            users.Add(user.Id, user);
        }

        foreach(User user in removed)
        {
            object playerObj;
            user.Properties.TryGetValue("GameObject", out playerObj);

            GameObject playerGo = playerObj as GameObject;
            DestroyImmediate(playerGo);

            users.Remove(user.Id);
        }
    }

    void OnConnectionLost(BaseEvent evt)
    {
        sfs.RemoveEventListener(SFSEvent.PROXIMITY_LIST_UPDATE, OnProximityListUpdate);
        sfs.RemoveEventListener(SFSEvent.USER_VARIABLES_UPDATE, OnUserVariablesUpdate);
        sfs.RemoveEventListener(SFSEvent.CONNECTION_LOST, OnConnectionLost);

        Destroy(Connection.Instance().gameObject);
        Application.LoadLevel("Scene1");
    }

    public void OnDisconnectClick()
    {
        if (sfs.LastJoinedRoom != null)
        {
            sfs.Send(new Sfs2X.Requests.LeaveRoomRequest(sfs.LastJoinedRoom));
        }
        else
            Debug.LogError("No room was found to leave");

        if (sfs.IsConnected)
            sfs.Disconnect();
    }
}
