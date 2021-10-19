package K5s.storage;

import K5s.connectionManager.ClientMessageThread;

public class ChatClient {
    private final String chatClientID;
    private ChatRoom room;
    private ClientMessageThread messageThread;

    /**
     *
     * @param chatClientID unique identifier of the user
     * @param messageThread the messageReceive thread bind to the user
     */
    public ChatClient(String chatClientID, ClientMessageThread messageThread){
        this.room = null;
        this.chatClientID = chatClientID;
        this.messageThread = messageThread;
    }

    public void setRoom(ChatRoom room){ this.room = room;}
    /**
     *
     * @return current room of the user
     */
    public synchronized ChatRoom getRoom(){
        return this.room;
    }

    /**
     *
     * @return      users identity
     */
    public synchronized String getChatClientID(){
        return this.chatClientID;
    }


    /**
     *
     * @return   messageReceive thread bind to the user
     */
    public synchronized ClientMessageThread getMessageThread(){
        return this.messageThread;
    }

}
