package any.xxx.anypeer.bean;

import java.io.Serializable;

public class DeprecatedUser implements Serializable {
	public static final String USER = "user";

	private String mUserId;
	private String mUserName;
	private String mGender;
	private String mEmail;
	private String mRegion;
	private boolean mIsOnline = false;

	public DeprecatedUser() {
	}

	public DeprecatedUser(String userName, String userId) {
		mUserId = userId;
		mUserName = userName;
	}

	public String getId() {
		return mUserId;
	}

	public void setId(String id) {
		this.mUserId = id;
	}

	public String getUserName() {
		return mUserName;
	}

	public void setUserName(String userName) {
		this.mUserName = userName;
	}

	public String getGender() {
		return mGender;
	}

	public void setGender(String gender) {
		this.mGender = gender;
	}

	public String getEmail() {
		return mEmail;
	}

	public void setEmail(String email) {
		this.mEmail = email;
	}

	public String getRegion() {
		return mRegion;
	}

	public void setRegion(String region) {
		this.mRegion = region;
	}

	public boolean isOnline() {
		return mIsOnline;
	}

	public void setOnline(boolean online) {
		mIsOnline = online;
	}
}
