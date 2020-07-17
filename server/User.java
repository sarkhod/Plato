package Plato.server ;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class User implements Serializable {
    private String username;
    private String password ;
    private volatile byte[] profilePic = null ;
    private volatile ArrayList<User> friends;
    private volatile String bioText ;
    private volatile ConcurrentHashMap<String , Integer> gamesList ; // Mapping games to their scores !
    private volatile ConcurrentHashMap<User , Conversation> conversations ;
    private volatile ArrayList<String> friendRequests ;  // String are  usernames (senders)...

    public User(String username , String password) {
        this.password = password ;
        this.username = username;
        this.friends = new ArrayList<>();


        this.gamesList = new ConcurrentHashMap<>();
        gamesList.put("xo" , 0);
        gamesList.put("guessWord" , 0) ;


        this.conversations = new ConcurrentHashMap<>();
        this.friendRequests = new ArrayList<>() ;
    }
    public synchronized void setProfilePic(byte[] profilePic){
        this.profilePic = profilePic ;
    }

    public String getBioText() {
        return bioText;
    }

    public synchronized void setBioText(String bioText) {
        this.bioText = bioText;
    }

    public byte[] getProfilePic() {
        return profilePic;
    }

    public synchronized void addFriendRequest(String username){
        friendRequests.add(username) ;

    }
    public synchronized void removeFriendRequest(String username){
        friendRequests.remove(username) ;
    }
    public synchronized void addFriend(User user){
        friends.add(user );
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public synchronized void addWinScoreToGame(String game){

        gamesList.put(game , gamesList.get(game) + 100 ) ;
    }
    public synchronized void addDrawScoreToGame(String game){

        gamesList.put(game , gamesList.get(game) + 20 ) ;
    }


    public int getGameScore(String game){
        return gamesList.get(game) ;
    }
    public Conversation getConversation(User destUser){
        return conversations.get(destUser) ;
    }
    public synchronized void addConversation(User destUser , Conversation conversation){
        conversations.put(destUser , conversation) ;
    }



    /// Just for Test
    public ConcurrentHashMap<User, Conversation>
    etConversations() {
        return conversations;
    }
}
