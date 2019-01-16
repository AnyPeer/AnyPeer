package any.xxx.anypeer.db;

import android.util.Log;
import java.util.List;
import any.xxx.anypeer.bean.User;
import any.xxx.anypeer.chatbean.ChatConversation;
import io.realm.Realm;

public class FriendManager {
    private static final String TAG = "FriendManager";
    private static FriendManager instance = new FriendManager();

    public static FriendManager getInstance() {
        return instance;
    }

    public void addFriend(User user) {
        try {
            Log.d(TAG, "addOrUpdateFriend " + user.toString());
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(user);
            realm.commitTransaction();

            if (!ChatDBManager.getInstance().isAdd(user.getUserId())) {
                ChatConversation chatConversation = new ChatConversation();
                chatConversation.setmUserId(user.getUserId());
                ChatDBManager.getInstance().addConversation(chatConversation);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<User> getAllFriend() {
        try {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            List<User> users = realm.where(User.class).findAll();
            realm.commitTransaction();
            Log.d(TAG, "getAllFriend " + users.size());
            return users;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isAdd(String userId) {
        try {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            List<User> users = realm.where(User.class).equalTo("userId", userId).findAll();
            realm.commitTransaction();
            Log.d(TAG, "isAdd " + userId + " " + users.size());
            return users.size() != 0;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public User getUserById(String userId) {
        try {
            Realm realm = Realm.getDefaultInstance();
            List<User> users = realm.where(User.class).equalTo("userId", userId).findAll();

            return realm.copyFromRealm(users).get(0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void removeFriend(String userId) {
        try {
            Log.d(TAG, "removeFriend " + userId);
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            User user = realm.where(User.class).equalTo("userId", userId).findFirst();
            if (user != null) {
                user.deleteFromRealm();
            }

            realm.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateUserName(String userId, String userName) {
        try {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            User user = realm.where(User.class).equalTo("userId", userId).findFirst();
            if (user != null) {
                user.setUserName(userName);
            }

            realm.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateUserGender(String userId, String gender) {
        try {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            User user = realm.where(User.class).equalTo("userId", userId).findFirst();
            if (user != null) {
                user.setGender(gender);
            }

            realm.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateUserInfo(String userId, String name, String gender, String region
                    , String email, String address) {
        try {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            User user = realm.where(User.class).equalTo("userId", userId).findFirst();
            if (user != null) {
                user.setUserName(name);
                user.setGender(gender);
                user.setRegion(region);
                user.setEmail(email);
                user.setWalletAddress(address);
            }

            realm.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateUserIsOnline(String userId, boolean isOnline) {
        try {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            User user = realm.where(User.class).equalTo("userId", userId).findFirst();
            if (user != null) {
                user.setOnline(isOnline);
            }

            realm.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
