using UnityEngine;
using System.Collections;
using Sfs2X;
using Sfs2X.Core;
using Sfs2X.Logging;

/// <summary>
/// Classe utilizada para guardar a instância do SmartFox. O propósito dela é chamar o método ProcessEvents() e oferecer um jeito
/// simples de acessar/reinicializar a instância. Ela também tem lógica para quando é destruída garantir que será disconectada do
/// servidor, porém é bom que quem utilizar essa classe faça a própria limpeza (e de certa forma, repita a lógica do OnDestroy, ex:
/// MultiplayerManager.OnDisconnectClick e MultiplayerManager.OnConnectionLost.)
/// </summary>
public class Connection : MonoBehaviour
{
    private SmartFox sfs = null;
    private static Connection instance = null;

    void Start()
    {
        if (instance != null && instance != this)
        {
            Debug.LogWarning("Another connection was found, deleting this one [Attached to GameObject '" + gameObject.name + "']");
            DestroyImmediate(this);
        }
        else
        {
            instance = this;
            DontDestroyOnLoad(gameObject);
        }
    }

    /// <summary>
    /// Método para achar a instância da Connection. Lança uma NullReferenceException caso nenhum GameObject com esse MonoBehaviour exista.
    /// </summary>
    /// <returns>A instância atual da Connection.</returns>
    public static Connection Instance()
    {
        if (instance == null) throw new System.NullReferenceException();
        if (instance.sfs == null)
            instance.Reset();
        return instance;
    }

    /// <summary>
    /// Método que recria a instância do SmartFox. É necessário ser chamado sempre que o cliente perde a conexão com o servidor. Em modo de
    /// debug essa instância também redireciona as mensagens do log para o console da Unity.
    /// </summary>
    /// <returns>A instância do SmartFox.</returns>
    public SmartFox Reset()
    {
        if (sfs != null)
            sfs.RemoveAllEventListeners();

#if DEBUG
        sfs = new SmartFox(true);
        sfs.Logger.LoggingLevel = LogLevel.INFO;
        
        sfs.AddLogListener(LogLevel.INFO, OnInfoMessage);
        sfs.AddLogListener(LogLevel.WARN, OnWarnMessage);
        sfs.AddLogListener(LogLevel.ERROR, OnErrorMessage);
#else
        sfs = new SmartFox(false);
#endif
        return sfs;
    }

    /// <returns>A instância atual do SmartFox, nunca é null.</returns>
    public SmartFox Sfs()
    {
        return sfs;
    }

    void FixedUpdate()
    {
        if (sfs != null)
            sfs.ProcessEvents();
    }

    void OnDestroy()
    {
        if (sfs != null)
        {
            if (sfs.LastJoinedRoom != null)
                sfs.Send(new Sfs2X.Requests.LeaveRoomRequest(sfs.LastJoinedRoom));
            if (sfs.IsConnected)
                sfs.Disconnect();
        }
        if (instance == this) instance = null;
    }

    void OnInfoMessage(BaseEvent evt)
    {
        string message = (string)evt.Params["message"];
        Debug.Log(message);
    }

    void OnWarnMessage(BaseEvent evt)
    {
        string message = (string)evt.Params["message"];
        Debug.LogWarning(message);
    }

    void OnErrorMessage(BaseEvent evt)
    {
        string message = (string)evt.Params["message"];
        Debug.LogError(message);
    }
}
