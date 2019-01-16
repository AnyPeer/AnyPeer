package any.xxx.anypeer.util;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.elastos.carrier.AbstractCarrierHandler;
import org.elastos.carrier.Carrier;
import org.elastos.carrier.Carrier.Options;
import org.elastos.carrier.ConnectionStatus;
import org.elastos.carrier.FriendInfo;
import org.elastos.carrier.UserInfo;
import org.elastos.carrier.exceptions.CarrierException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import any.xxx.anypeer.bean.EMMessage;
import any.xxx.anypeer.chatbean.ChatMessage;

public class NetUtils {
//	static final String TAG = "NetUtils";
	private static NetUtils sInstance = null;
	private static Carrier mCarrier = null;
	private static FileTransfer sFileTransfer;
	private static String sChatFilesRootPath;
	private Handler mHandler;
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

		public static final String USERID = "NetUtils_id";
		public static final String HELLO = "NetUtils_hello";
		public static final String FROM = "NetUtils_from";
		public static final String MSG = "NetUtils_msg";
		public static final String MSGID = "NetUtils_msgId";
		static final String DATA = "NetUtils_data";
		public static final String FILEDATA = "NetUtils_filedata";
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
	}

	public void addFriend(String address, String hello) {
		try {
			mCarrier.addFriend(address, hello);
		}
		catch (CarrierException e) {
			e.printStackTrace();
		}
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

	public FriendInfo getFriend(String userId) {
		try {
			return mCarrier.getFriend(userId);
		}
		catch (CarrierException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void updateInfo(String name, String gender, String email) {
		UserInfo self = getSelfInfo();
		if (name != null && !name.isEmpty()) {
			self.setName(name);
		}

		if (gender != null && !gender.isEmpty()) {
			self.setGender(gender);
		}

		if (email != null && !email.isEmpty()) {
			self.setEmail(name);
		}

		setSelfInfo(self);
	}

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
		public static String USERID = "";
	}

	public boolean isRobot(String userId) {
		if (ChatRobot.USERID.isEmpty()) {
			initRobotUserId(null);
		}
		return ChatRobot.USERID.equals(userId);
	}

	public void initRobotUserId(String name) {
		//TODO
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
		public void onFriendMessage(Carrier carrier, String from, byte[] message) {
			Message msg = new Message();

			try {
				if (getInstance().isRobot(from)) {
					msg.what = ANYSTATE.FRIENDMESSAGE;
					Bundle data = new Bundle();
					data.putString(ANYSTATE.FROM, from);

					//Don't include the message id.
					String realMsg = new String(message);
					data.putString(ANYSTATE.MSG, realMsg);
					msg.setData(data);
					mHandler.sendMessage(msg);
					return;
				}

				String[] args = parseValue(message);
				if (args != null) {
					//TODO MSGID+EXTRA
					if (args.length == 1) {
						msg.what = ANYSTATE.FILE_TRANSFER_FEEDBACK;
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

						//TODO : 1. give a feedback to the friend; 2. get msgId: Not include the last char which is the extra.
						String msgId = args[0].substring(0, args[0].length() - 1);
						sendMessage(from, msgId, null);
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
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}
