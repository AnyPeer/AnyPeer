package any.xxx.anypeer.util;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.elastos.carrier.AbstractCarrierHandler;
import org.elastos.carrier.Carrier;
import org.elastos.carrier.Carrier.Options;
import org.elastos.carrier.ConnectionStatus;
import org.elastos.carrier.FriendInfo;
import org.elastos.carrier.Group;
import org.elastos.carrier.GroupHandler;
import org.elastos.carrier.UserInfo;
import org.elastos.carrier.exceptions.CarrierException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import any.xxx.anypeer.R;
import any.xxx.anypeer.bean.EMMessage;
import any.xxx.anypeer.chatbean.ChatMessage;

public class NetUtils {
	private static final String TAG = "NetUtils";
	private static NetUtils sInstance = null;
	private static Carrier mCarrier = null;
	private static HashMap<String, Group> mDefaultGroupMap = new HashMap<>();
	private static FileTransfer sFileTransfer;
	private static String sChatFilesRootPath;
	private Handler mHandler;
	private Context mContext;
	private static final String SPACE = " ";

	public static class ANYSTATE {
		public static final int READY = 0;
		public static final int CONNECTION = 1;
		public static final int FRIENDCONNECTION = 2;
		public static final int SELFINFOCHANGED = 3;
		public static final int FRIENDINFOCHANGED = 4;
		public static final int FRIENDREQUEST = 5;
		public static final int FRIENDADDED = 6;
		public static final int FRIENDREMOVED = 7;
		public static final int FRIENDMESSAGE = 8;
		public static final int FRIENDINVITEREQUEST = 9;
		public static final int FILE_TRANSFER = 10;
		public static final int FILE_TRANSFER_STATE = 11;
		public static final int FILE_TRANSFER_FEEDBACK = 12;
		public static final int FILE_TRANSFER_SEND_ERROR = 13;
		public static final int GROUP_MESSAGE = 14;
		public static final int GROUP_DEFAULT_ONLINE = 15;
		public static final int GROUP_PEER_CHANGED = 16;
		public static final int GROUP_PEER_NAME = 17;
		private static final int GROUP_TITLE_CHANGED = 18;
		public static final int GROUP_LEAVE = 19;
		public static final int MSG_FORBIDDEN = 20;

		public static final String USERID = "NetUtils_id";
		public static final String HELLO = "NetUtils_hello";
		public static final String FROM = "NetUtils_from";
		public static final String MSG = "NetUtils_msg";
		public static final String MSGID = "NetUtils_msgId";
		public static final String DATA = "NetUtils_data";
		public static final String FILEDATA = "NetUtils_filedata";
		public static final String GROUP_TITLE = "Group_title";
	}

	public static NetUtils getInstance() {
		return sInstance;
	}

	public static NetUtils getInstance(Context context, Handler msghandler) {
		if (sInstance == null) {
			sInstance = new NetUtils(context, msghandler);
		}
		return sInstance;
	}

	private NetUtils(Context context, Handler msghandler) {
		mContext = context;
		mHandler = msghandler;
		ChatOptions options = new ChatOptions(Utils.getRootPath(context));

		ChatHandler handler = new ChatHandler();

		try {
			Carrier.initializeInstance(options, handler);
			mCarrier = Carrier.getInstance();

			mCarrier.start(0);
		} catch (CarrierException /*| InterruptedException*/ e) {
			e.printStackTrace();
		}

		//Initialize the robot's id
		ChatRobot.USERID = NetUtils.getUserIdByAddress(ChatRobot.ADDRESS);
	}

	public void addFriend(String address, String hello) {
		try {
			mCarrier.addFriend(address, hello);
		}
		catch (CarrierException e) {
			e.printStackTrace();
		}
	}

	public static String getUserIdByAddress(String address) {
		return Carrier.getIdFromAddress(address);
	}

	public void removeFriend(String userId) {
		try {
			mCarrier.removeFriend(userId);
		}
		catch (CarrierException e) {
			e.printStackTrace();
		}
	}

	public void accept(String userId) {
		try {
			mCarrier.acceptFriend(userId);
		}
		catch (CarrierException e) {
			e.printStackTrace();
		}
	}

	public boolean isFriend(String userId) {
		try {
			return mCarrier.isFriend(userId);
		}
		catch (CarrierException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setSelfInfo(UserInfo info) {
		try {
			mCarrier.setSelfInfo(info);
		}
		catch (CarrierException e) {
			e.printStackTrace();
		}
	}

	public UserInfo getSelfInfo() {
		try {
			return mCarrier.getSelfInfo();
		}
		catch (CarrierException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<FriendInfo> getFriends() {
		try {
			List<FriendInfo> infoList = mCarrier.getFriends();
			for (int i = 0;i < infoList.size(); i++) {
				if (ChatRobot.USERID.equals(infoList.get(i).getUserId())) {
					infoList.remove(i);
					break;
				}
			}
			return infoList;
		}
		catch (CarrierException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static final int FRIENDS_LIMIT = 2;
	public int getFriendsCount() {
		try {
			return getFriends().size();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

//	public FriendInfo getFriend(String userId) {
//		try {
//			if (!isRobot(userId)) {
//				return mCarrier.getFriend(userId);
//			}
//		}
//		catch (CarrierException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	public void updateInfo(String name, String gender, String email) {
//		UserInfo self = getSelfInfo();
//		if (name != null && !name.isEmpty()) {
//			self.setName(name);
//		}
//
//		if (gender != null && !gender.isEmpty()) {
//			self.setGender(gender);
//		}
//
//		if (email != null && !email.isEmpty()) {
//			self.setEmail(email);
//		}
//
//		setSelfInfo(self);
//	}

	String getChatID() {
		try {
			return mCarrier.getAddress();
		}
		catch (CarrierException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void sendMessage(String userId, String message, String msgId) {
		try {
			mCarrier.sendFriendMessage(userId, combineValue(msgId, message));
		}
		catch (CarrierException e) {
			e.printStackTrace();
		}
	}

	public void sendFile(String userId, File file, ChatMessage.Type msgType, String msgId) {
		try {
			sendFile(userId, file.getAbsolutePath(), msgType, msgId);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendFile(String userId, String filePath, ChatMessage.Type msgType, String msgId) {
		try {
			sFileTransfer.addTransferFile(userId, filePath, msgType, msgId);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String combineValue(String spacial, String value) {
		if (spacial != null && !spacial.isEmpty()) {
			return spacial + SPACE + value;
		}
		return value;
	}

	private static String[] parseValue(byte[] bytes) {
		try {
			String value = new String(bytes);
			return value.split("\\s+");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void closeTransferFile(String userId) {
		try {
			if (sFileTransfer != null) {
				sFileTransfer.closeTransferFile(userId);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isValidAddress(String address) {
		return Carrier.isValidAddress(address);
	}

	static String getChatFilePath() {
		return sChatFilesRootPath;
	}

	public void kill() {
		if (sFileTransfer != null) {
			sFileTransfer.close();
		}

		mCarrier.kill();
		sInstance = null;
	}

	public static class ChatRobot {
		//ABot
		static final String ADDRESS = "DGNTSh5umF5Evg9TiAZxFSrvZE5xyWw6S2w9iTNFFurjCHJTw4CQ";
		public static String USERID = "";
	}

	public boolean isRobot(String userId) {
		if (ChatRobot.USERID.isEmpty()) {
			initRobotUserId(null);
		}
		return ChatRobot.USERID.equals(userId);
	}

	public void initRobotUserId(String name) {
		NetUtils netUtils = NetUtils.getInstance();
		if (netUtils != null) {
			if (!netUtils.isFriend(ChatRobot.USERID)) {
				if (name == null) {
					name = "hello";
				}
				netUtils.addFriend(ChatRobot.ADDRESS, name);
			}
		}
	}

	public void groupSendMessage(String groupId, String message, ChatMessage.Type type) {
		try {
			Group group = mDefaultGroupMap.get(groupId);
			if (group != null) {
				String msg = Integer.toString(type.ordinal()) + " " + message;
				group.sendMessage(msg.getBytes());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(mContext.getString(R.string.group_message_dht_error));
		}
	}

	public List<Group.PeerInfo> peersList(String groupId) {
		try {
			Group group = mDefaultGroupMap.get(groupId);
			//Don't show the Bot
			List<Group.PeerInfo> peers = group.getPeers();
			for (Group.PeerInfo peerInfo: peers) {
				if (isRobot(peerInfo.getUserId())) {
					peers.remove(peerInfo);
					return peers;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public int peersCount(String groupId) {
		try {
			Log.d(TAG, "title================"+groupId);
			Group group = mDefaultGroupMap.get(groupId);
			return group.getPeers().size() - 1;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	public String peerName(String groupId, String userId) {
		try {
			Group group = mDefaultGroupMap.get(groupId);
			for (Group.PeerInfo info: group.getPeers()) {
				if (userId.equals(info.getUserId())) {
					return info.getName();
				}
			}
		}
		catch (Exception e) {
			// e.printStackTrace();
		}

		return "Unknown";
	}

	private boolean mForbidState = false;
	public void forbidDefault() {
		mForbidState = true;
	}

	public boolean isForbidden(String groupId) {
		if (groupId == null || groupId.isEmpty()) return false;
		if (groupId.equals(DEFALUT_GROUP_NAMES[0]) || groupId.equals(DEFALUT_GROUP_NAMES[1])) {
			return mForbidState;
		}
		return false;
	}

	private static final String[] DEFALUT_GROUP_NAMES = {
			"Any Group 中文", "Any Group"};

	public enum DEFAULT_GROUP_TYPE {
		CN, EN
	}

	public void inviteDefault(DEFAULT_GROUP_TYPE type) {
		if (type == DEFAULT_GROUP_TYPE.CN) {
			sendMessage(NetUtils.ChatRobot.USERID, "0", null);
		}
		else if (type == DEFAULT_GROUP_TYPE.EN) {
			sendMessage(NetUtils.ChatRobot.USERID, "1", null);
		}
	}

	public void inviteDefault(String title) {
		if (title == null || title.isEmpty()) {
			return;
		}
		if (DEFALUT_GROUP_NAMES[0].equals(title)) {
			inviteDefault(DEFAULT_GROUP_TYPE.CN);
		}
		else if (DEFALUT_GROUP_NAMES[1].equals(title)) {
			inviteDefault(DEFAULT_GROUP_TYPE.EN);
		}
	}

	public String getGroupTitle(String groupId) {
		try {
			Group group = mDefaultGroupMap.get(groupId);
			String title = group.getTitle();
			if (title != null && !title.isEmpty()) {
				return title;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return DEFALUT_GROUP_NAMES[0];
	}

	public boolean groupIsOnline(String groupId) {
		try {
			return mDefaultGroupMap.get(groupId) != null;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private void updateGroup(Group group) {
		if (group == null) {
			return;
		}

		try {
			String title = group.getTitle();
			if (title != null && !title.isEmpty()) {
				mDefaultGroupMap.put(title, group);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class ChatHandler extends AbstractCarrierHandler {
		@Override
		public void onReady(Carrier carrier) {
			mHandler.sendEmptyMessage(ANYSTATE.READY);
			sFileTransfer = FileTransfer.getInstance(mCarrier, mHandler);
		}

		@Override
		public void onConnection(Carrier carrier, ConnectionStatus status) {
			mHandler.sendEmptyMessage(ANYSTATE.CONNECTION);
		}

		@Override
		public void onSelfInfoChanged(Carrier carrier, UserInfo info) {
			Message msg = new Message();
			msg.what = ANYSTATE.SELFINFOCHANGED;
			msg.obj = info;
			mHandler.sendMessage(msg);
		}

		@Override
		public void onFriendConnection(Carrier carrier, String friendId, ConnectionStatus status) {
			Message msg = new Message();
			msg.what = ANYSTATE.FRIENDCONNECTION;
			msg.arg1 = status.value();
			msg.obj = friendId;
			mHandler.sendMessage(msg);
			if (ConnectionStatus.Connected != status) {
				closeTransferFile(friendId);
			}
		}

		@Override
		public void onFriendRequest(Carrier carrier, String userId, UserInfo info, String hello) {
			Message msg = new Message();
			msg.what = ANYSTATE.FRIENDREQUEST;
			msg.obj = info;
			Bundle data = new Bundle();
			data.putString(ANYSTATE.USERID, userId);
			data.putString(ANYSTATE.HELLO, hello);
			msg.setData(data);
			mHandler.sendMessage(msg);
		}

		@Override
		public void onFriendAdded(Carrier carrier, FriendInfo info) {
			Message msg = new Message();
			msg.what = ANYSTATE.FRIENDADDED;
			msg.obj = info;
			mHandler.sendMessage(msg);
		}

		@Override
		public void onFriendRemoved(Carrier carrier, String friendId) {
			Message msg = new Message();
			msg.what = ANYSTATE.FRIENDREMOVED;
			msg.obj = friendId;
			mHandler.sendMessage(msg);
		}

		@Override
		public void onFriendMessage(Carrier carrier, String from, byte[] message, boolean isOffline) {
			Message msg = new Message();

			try {
				if (getInstance().isRobot(from)) {
					Bundle data = new Bundle();
					data.putString(ANYSTATE.FROM, from);

					//Don't include the message id.
					String realMsg = new String(message);
					Log.d(TAG, "" + realMsg);
					if (realMsg.equals("forbid")) {
						msg.what = ANYSTATE.MSG_FORBIDDEN;
					}
					else {
						msg.what = ANYSTATE.FRIENDMESSAGE;
					}

					data.putString(ANYSTATE.MSG, realMsg);
					msg.setData(data);
					mHandler.sendMessage(msg);
					return;
				}

				String[] args = parseValue(message);
				if (args != null) {
					//MSGID+EXTRA
					if (args.length == 1) {
						msg.what = ANYSTATE.FILE_TRANSFER_FEEDBACK;

						//Compatibility other chat app based on Carrier.
						if (!isValidUUID(args[0])) {
							msg.what = ANYSTATE.FRIENDMESSAGE;
							msg.arg1 = EMMessage.Type.TXT.ordinal();
						}
						Bundle data = new Bundle();
						data.putString(ANYSTATE.FROM, from);
						data.putString(ANYSTATE.MSG, args[0]);
						msg.setData(data);
						mHandler.sendMessage(msg);
					}
					else {
						msg.what = ANYSTATE.FRIENDMESSAGE;
						Bundle data = new Bundle();
						data.putString(ANYSTATE.FROM, from);
						msg.arg1 = EMMessage.Type.TXT.ordinal();

						String msgId = args[0].substring(0, args[0].length() - 1);
						Log.d(TAG, String.format("onFriendMessage====msgId=[%s], message=[%s]", msgId, new String(message)));

						//Compatibility other chat app based on Carrier.
						if (!isValidUUID(msgId)) {
							Log.d(TAG, String.format("222 onFriendMessage====msgId=[%s], message=[%s]", msgId, new String(message)));
							data.putString(ANYSTATE.MSG, new String(message));
							msg.setData(data);
							mHandler.sendMessage(msg);
						}
						else {
							//1. extra: transfer information.
							if (args[0].length() == (FileTransfer.MSG_ID_UUID_LEN + 1)) {
								try {
									int type = Integer.parseInt(args[0].substring(args[0].length() - 1));
									if (type == EMMessage.Type.MONEY.ordinal()) {
										msg.arg1 = type;
									}
								}
								catch (Exception e){
									e.printStackTrace();
								}
							}

							//Don't include the message id.
							String realMsg = new String(message).substring(args[0].length() + 1);
							data.putString(ANYSTATE.MSG, realMsg);
							msg.setData(data);
							mHandler.sendMessage(msg);

							//1. give a feedback to the friend; 2. get msgId: Not include the last char which is the extra.
							msgId = args[0].substring(0, args[0].length() - 1);
							sendMessage(from, msgId, null);
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onFriendInfoChanged(Carrier carrier, String friendId, FriendInfo info) {
			Message msg = new Message();
			msg.what = ANYSTATE.FRIENDINFOCHANGED;
			msg.obj = info;
			Bundle data = new Bundle();
			data.putString(ANYSTATE.USERID, friendId);
			msg.setData(data);
			mHandler.sendMessage(msg);
		}

		@Override
		public void onFriendInviteRequest(Carrier carrier, String from, String data) {
			Message msg = new Message();
			msg.what = ANYSTATE.FRIENDINVITEREQUEST;
			Bundle dataBundle = new Bundle();
			dataBundle.putString(ANYSTATE.FROM, from);
			dataBundle.putString(ANYSTATE.DATA, data);
			msg.setData(dataBundle);
			mHandler.sendMessage(msg);
		}

		@Override
		public void onGroupInvite(Carrier carrier, String from, byte[] cookie) {
			Log.d(TAG, String.format("Group invite from: %s", from));
			try {
				PeerGroupHandler peerGroupHandler = new PeerGroupHandler();
				mCarrier.groupJoin(from, cookie, peerGroupHandler);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static boolean isValidUUID(String uuid) {
		// "b3ad6601-e329-498f-8c51-82d4ea14508c"
		String regex = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
		return uuid != null && uuid.matches(regex);
	}

	private class PeerGroupHandler implements GroupHandler {
		@Override
		public void onGroupConnected(Group group) {
			try {
				Log.d(TAG, "onGroupConnected=================title="+group.getTitle());
				String title = group.getTitle();
				if (title != null && !title.isEmpty()) {
					Message msg = new Message();
					msg.what = ANYSTATE.GROUP_DEFAULT_ONLINE;
					updateGroup(group);
					msg.obj = title;
					mHandler.sendMessage(msg);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onGroupMessage(Group group, String from, byte[] message) {
			try {
				Log.d(TAG, String.format("from=[%s], myId=[%s], title=[%s]", from, mCarrier.getUserId(), group.getTitle()));
				if (!mCarrier.getUserId().equals(from)) {
					Message msg = new Message();
					msg.what = ANYSTATE.GROUP_MESSAGE;
					//UnKnown
					msg.arg1 = -1;
					msg.arg2 = 0;

					Bundle dataBundle = new Bundle();
					dataBundle.putString(ANYSTATE.GROUP_TITLE, group.getTitle());

					if(isRobot(from)) {
						msg.arg1 = EMMessage.Type.TXT.ordinal();
						msg.arg2 = 1;
						dataBundle.putString(ANYSTATE.DATA, new String(message));
					}
					else {
						String[] args = parseValue(message);
						if (args != null) {
							//Type + Message
							try {
								int type = Integer.parseInt(args[0]);
								if (type == EMMessage.Type.TXT.ordinal()) {
									msg.arg1 = type;

									//Don't include the message type.
									String realMsg = new String(message).substring(args[0].length() + 1);
									dataBundle.putString(ANYSTATE.DATA, realMsg);
								}
								else {
									//TODO Will support other type: voice and money?
									msg.arg1 = -1;
									dataBundle.putString(ANYSTATE.DATA, new String(message));
								}
							}
							catch (Exception e) {
								//get type error?
								msg.arg1 = -1;
								dataBundle.putString(ANYSTATE.DATA, new String(message));

								e.printStackTrace();
							}
						}
					}

					dataBundle.putString(ANYSTATE.FROM, from);
					msg.setData(dataBundle);
					mHandler.sendMessage(msg);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onGroupTitle(Group group, String from, String title) {
			try {
				Log.d(TAG, "onGroupTitle ====title="+title+", grouptitle="+group.getTitle());
				if (title != null && !title.isEmpty()) {
					Message msg = new Message();
					msg.what = ANYSTATE.GROUP_TITLE_CHANGED;
					updateGroup(group);
					msg.obj = title;
					mHandler.sendMessage(msg);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onPeerName(Group group, String peerId, String peerName) {
			try {
				Log.d(TAG, String.format("Peer Name id:%s, peerName:%s, title=[%s]", peerId, peerName, group.getTitle()));
				String title = group.getTitle();
				if (title != null && !title.isEmpty()) {
					Message msg = new Message();
					msg.what = ANYSTATE.GROUP_PEER_NAME;
					updateGroup(group);
					msg.obj = title;
					mHandler.sendMessage(msg);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onPeerListChanged(Group group) {
			try {
				Log.d(TAG, "onPeerListChanged=================title="+group.getTitle());
				String title = group.getTitle();
				if (title != null && !title.isEmpty()) {
					if (mDefaultGroupMap.get(title) == null) {
						mCarrier.groupLeave(group);
						sendGroupLeaveMessage(title);
					}
					else {
						updateGroup(group);
						if (peersCount(title) <= 0) {
							mCarrier.groupLeave(group);
							sendGroupLeaveMessage(title);
						}
						else {
							Message msg = new Message();
							msg.obj = title;
							msg.what = ANYSTATE.GROUP_PEER_CHANGED;
							mHandler.sendMessage(msg);
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void sendGroupLeaveMessage(String title) {
		try {
			Message msg = new Message();
			msg.what = ANYSTATE.GROUP_LEAVE;
			msg.obj = title;
			mHandler.sendMessage(msg);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class ChatOptions extends Options {
		private ChatOptions(String rootPath) {
			super();

			String carrierPath = rootPath + "/Carrier";
			File file = new File(carrierPath);
			if (!file.exists()) {
				if (!file.mkdir()) {
					throw new RuntimeException("mkdirs failed");
				}
			}

			//Create the file location
			sChatFilesRootPath = rootPath + "/Chatfiles";
			File chatFile = new File(sChatFilesRootPath);
			if (!chatFile.exists()) {
				if (!chatFile.mkdir()) {
					throw new RuntimeException("mkdirs failed");
				}
			}

			try {
				setUdpEnabled(true);
				setPersistentLocation(carrierPath);

				ArrayList<BootstrapNode> arrayList = new ArrayList<>();
				BootstrapNode node = new BootstrapNode();
				node.setIpv4("13.58.208.50");
				node.setPort("33445");
				node.setPublicKey("89vny8MrKdDKs7Uta9RdVmspPjnRMdwMmaiEW27pZ7gh");
				arrayList.add(node);

				node = new BootstrapNode();
				node.setIpv4("18.216.102.47");
				node.setPort("33445");
				node.setPublicKey("G5z8MqiNDFTadFUPfMdYsYtkUDbX5mNCMVHMZtsCnFeb");
				arrayList.add(node);

				node = new BootstrapNode();
				node.setIpv4("18.216.6.197");
				node.setPort("33445");
				node.setPublicKey("H8sqhRrQuJZ6iLtP2wanxt4LzdNrN2NNFnpPdq1uJ9n2");
				arrayList.add(node);

				node = new BootstrapNode();
				node.setIpv4("52.83.171.135");
				node.setPort("33445");
				node.setPublicKey("5tuHgK1Q4CYf4K5PutsEPK5E3Z7cbtEBdx7LwmdzqXHL");
				arrayList.add(node);

				node = new BootstrapNode();
				node.setIpv4("52.83.191.228");
				node.setPort("33445");
				node.setPublicKey("3khtxZo89SBScAMaHhTvD68pPHiKxgZT6hTCSZZVgNEm");
				arrayList.add(node);

				setBootstrapNodes(arrayList);

				//Hive
				ArrayList<HiveBootstrapNode> hiveArrayList = new ArrayList<>();
				HiveBootstrapNode hiveNode = new HiveBootstrapNode();
				hiveNode.setIpv4("52.83.159.189");
				hiveNode.setPort("9095");
				hiveArrayList.add(hiveNode);

				hiveNode = new HiveBootstrapNode();
				hiveNode.setIpv4("52.83.119.110");
				hiveNode.setPort("9095");
				hiveArrayList.add(hiveNode);

				hiveNode = new HiveBootstrapNode();
				hiveNode.setIpv4("3.16.202.140");
				hiveNode.setPort("9095");
				hiveArrayList.add(hiveNode);

				hiveNode = new HiveBootstrapNode();
				hiveNode.setIpv4("18.217.147.205");
				hiveNode.setPort("9095");
				hiveArrayList.add(hiveNode);

				hiveNode = new HiveBootstrapNode();
				hiveNode.setIpv4("18.219.53.133");
				hiveNode.setPort("9095");
				hiveArrayList.add(hiveNode);

				setHiveBootstrapNodes(hiveArrayList);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}
