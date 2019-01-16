package any.xxx.anypeer.bean;

import java.io.Serializable;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject implements Serializable {
	public static final String USER = "user";

	@PrimaryKey
	private String userId = "";
	private String userName = "";
	private String gender = "";
	private String email = "";
	private String region = "";
	private String walletAddress = "";
	private boolean isOnline = false;

	public User() {

	}

	public User(String userName, String userId) {
		this.userId = userId;
		this.userName = userName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getWalletAddress() {
		return walletAddress;
	}

	public void setWalletAddress(String address) {
		this.walletAddress= address;
	}


	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean online) {
		isOnline = online;
	}

	@Override
	public String toString() {
		return "User{" +
				", userId='" + userId + '\'' +
				", userName='" + userName + '\'' +
				", gender='" + gender + '\'' +
				", email='" + email + '\'' +
				", region='" + region + '\'' +
				", isOnline=" + isOnline +
				'}';
	}
}
