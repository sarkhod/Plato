package Plato.server;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserHandler implements Runnable {
    //    private final DataInputStream dis ;
//    private final DataOutputStream dos ;
    private final ObjectInputStream ois;
    private final ObjectOutputStream oos;
    private User currentUser = null; // Will Be Assigned after Login .... !
    private volatile ConcurrentHashMap<Integer, Room> rooms;

    private volatile Map<String, User> users = UsersList.getUsersList();

    public UserHandler(Socket socket, ConcurrentHashMap<Integer, Room> rooms) throws IOException {
        this.rooms = rooms;
        oos = new ObjectOutputStream(socket.getOutputStream());
        oos.flush();
        ois = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {


        while (true) {
            try {

                String command = ois.readUTF();
                System.out.println("command :" + command);
                switch (command) {
                    case "login": {
                        // These lines may change in order to simultaneous operations in client
                        String username, password;
                        username = ois.readUTF();
                        password = ois.readUTF();

                        if (users.containsKey(username) && users.get(username).getPassword().equals(password)) {
                            User foundUser = users.get(username);
                            oos.writeObject(foundUser);
                            this.currentUser = foundUser;
                        } else
                            oos.writeObject(null);

                        oos.flush();

                        break;
                    }
                    case "register": {
                        String username, password;
                        username = ois.readUTF();
                        password = ois.readUTF();
                        if (!users.containsKey(username)) {
                            users.put(username, new User(username, password));
                            oos.writeUTF("successful");

                        } else {
                            oos.writeUTF("failed");
                        }
                        oos.flush();

                        /*  Just For Test */
                        System.out.println(users);
                        break;
                    }
                    case "search_for_friend": {
                        String username;
                        username = ois.readUTF();
                        ArrayList<User> compatible = new ArrayList<>();
                        Set<String> usernames = users.keySet();
                        int count = 0;
                        for (String user : usernames) {
                            if (count == 3)
                                break;
                            if (user.contains(username)) {
                                compatible.add(users.get(user));
                                count++;
                            }
                        }
                        oos.writeObject(compatible);
                        oos.flush();

                        break;
                    }
                    case "send_friend_request": {

                        synchronized (oos) {



                        }

                    }
                    case "make_room": {
                        Room room = new Room(new UserAndHandler(currentUser, this),
                                "ranked", "xo", rooms, 2); // roomMaker Will be added automatically to room !
                        rooms.put(Room.number, room);
                        this.wait();
                        room.start();
                        break;
                    }
                    case "join_room": {
                        int roomId = ois.readInt();
                        Room joiningRoom = rooms.get(roomId);
                        joiningRoom.addUser(new UserAndHandler(currentUser, this));
                        this.wait();
                        break;
                        // changing GameRunning boolean is done in addUser method (Room Class)
                    }


                    case "get_rooms": {
                        oos.writeObject(rooms);
                        oos.flush();
                        break;
                    }
                    case "watch": {
                        int roomId = ois.readInt();
                        Room watchingRoom = rooms.get(roomId);
                        this.wait();
                        break;
                    }
                    case "send_message": {
                        String destUsername = ois.readUTF();
                        String messageContent = ois.readUTF();

                        User destUser = users.get(destUsername);
                        if (currentUser.getConversation(destUser) == null) {
                            Conversation conversation = new Conversation() ;
                            currentUser.addConversation(destUser , conversation);
                            destUser.addConversation(currentUser , conversation);
                        }
                        Conversation conversation = currentUser.getConversation(destUser);
                        conversation.sendMessage(new TextMessage(new Date(), currentUser, messageContent));

                        System.out.println(users.get(destUsername).getConversation(currentUser).getMessages());
                        System.out.println(currentUser.getConversation(destUser).getMessages());
                        System.out.println(conversation.getMessages());
                        break;
                    }
                    case "leader_board":{
                        String game = ois.readUTF() ;
                        TreeMap<String , Integer> usersScores = new TreeMap<>() ;
                        for (User user : users.values()){
                            usersScores.put(user.getUsername() , user.getGameScore(game)) ;
                        }
                        oos.writeObject(usersScores);
                        oos.flush();
                    }
                }

            } catch (IOException e) {

                // What if user disconnects ? (maybe in the game )
                System.out.println("!");
                e.printStackTrace();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

//    public DataInputStream getDis() {
//        return dis;
//    }
//
//    public DataOutputStream getDos() {
//        return dos;
//    }

    public ObjectInputStream getOis() {
        return ois;
    }

    public ObjectOutputStream getOos() {
        return oos;
    }
}
