package any.xxx.anypeer.util;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.elastos.carrier.Carrier;
import org.elastos.carrier.exceptions.CarrierException;
import org.elastos.carrier.session.CloseReason;
import org.elastos.carrier.session.Manager;
import org.elastos.carrier.session.ManagerHandler;
import org.elastos.carrier.session.Session;
import org.elastos.carrier.session.SessionRequestCompleteHandler;
import org.elastos.carrier.session.Stream;
import org.elastos.carrier.session.StreamHandler;
import org.elastos.carrier.session.StreamState;
import org.elastos.carrier.session.StreamType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import any.xxx.anypeer.chatbean.ChatMessage;

public class FileTransfer {
	private static final String TAG = "FileTransfer";
	private static FileTransfer sFileTransfer;
	private Carrier mCarrier;
	private static Manager mSessionManager;
	private static Handler mHandler;
	private static Map<String, FileTransferInfo> mSessionMap = new HashMap<>();
	private static final SessionManagerHandler sSessionHandler = new SessionManagerHandler();
	private static final StreamType sFileTransferStreamType = StreamType.Application/*StreamType.Text*/;
	private static final int sFileTransferStreamOptions = Stream.PROPERTY_MULTIPLEXING | Stream.PROPERTY_RELIABLE;

	public static FileTransfer getInstance() {
		return sFileTransfer;
	}

	public static FileTransfer getInstance(Carrier carrier, Handler msghandler) {
		if (sFileTransfer == null) {
			mHandler = msghandler;
			sFileTransfer = new FileTransfer(carrier);
		}
		return sFileTransfer;
	}

	private FileTransfer(Carrier carrier) {
		mCarrier = carrier;
		try {
			mSessionManager = Manager.getInstance(carrier, sSessionHandler);
		}
		catch (CarrierException e) {
			e.printStackTrace();
		}
	}

	void addTransferFile(String userId, String file, ChatMessage.Type msgType, String msgId) {
		try {
			if (!mCarrier.isFriend(userId)) {
				return;
			}
			process(userId, file, msgType, msgId);
		}
		catch (CarrierException e) {
			e.printStackTrace();
		}
	}

	void closeTransferFile(String userId) {
		FileTransferInfo info = mSessionMap.get(userId);
		if (info == null) {
			return;
		}

		try {
			if (info.mStream != null) {
				if (info.mSession != null) {
					Log.d(TAG, "closeTransferFile removeStream before");
					info.removeStream();
					Log.d(TAG, "closeTransferFile removeStream after");
				}
			}

			if (info.mSession != null) {
				info.mSession.close();
				info.mSession = null;
			}

			info.sendErrorMessage();
			info.clearSendList();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			mSessionMap.remove(userId);
		}
	}

	public void close() {
		for (Map.Entry<String, FileTransferInfo> pair : mSessionMap.entrySet()) {
			FileTransferInfo info = pair.getValue();
			if (info != null) {
				try {
					Log.d(TAG, "close before");
					info.mSession.removeStream(info.mStream);
					Log.d(TAG, "close after");
					info.mSession.close();
					info.mSession = null;
				}
				catch (CarrierException e) {
					e.printStackTrace();
				}
			}
			mSessionMap.clear();
		}
	}

	private static byte[] getFileData(String filePath) {
		if (filePath == null || filePath.isEmpty()) {
			return null;
		}

		File file = new File(filePath);
		if (file.isFile()) {
			FileInputStream fis;
			try {
				fis = new FileInputStream(file);
				byte[] buffer = new byte[1024];
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				int len;
				while ((len = fis.read(buffer)) != -1) {
					outputStream.write(buffer, 0, len);
				}
				return outputStream.toByteArray();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static final int MIN_PACKET_SIZE = 1024;
	private static final int PACKET_COUNT_LEN = 4;
	private static final int INDEX_BYTES_LEN = 4;
	private static final int MSG_TYPE_LEN = 1;
	static final int MSG_ID_UUID_LEN = 36;
	private static final int FIRST_PACKET_BYTE_SIZE = PACKET_COUNT_LEN + INDEX_BYTES_LEN
						+ MSG_TYPE_LEN + MSG_ID_UUID_LEN;

	private static byte[] mDataBlock = new byte[MIN_PACKET_SIZE];
	private static class FileTransferInfo {
		private Session mSession;
		private Stream mStream;
		private FileTransferStreamHandler mStreamHandler;
		private FileTransferSessionRequestCompleteHandler mCompleteHandler;
		private String mSdp;
		private StreamState mStreamState;
		private String mUserId;
		private LinkedList<DataInfo> mDataList = new LinkedList<>();
		private Thread mThread;
		private int mWaitTime = 5;

		class DataInfo {
			DataInfo(String dataFile, ChatMessage.Type msgType, String msgId) {
				mDataFile = dataFile;
				mIsSent = false;
				mMsgType = msgType;
				mMsgId = msgId;
			}
			private String mDataFile;
			private boolean mIsSent;
			private ChatMessage.Type mMsgType;
			private String mMsgId;
		}

		FileTransferInfo(String userId, String file, ChatMessage.Type msgType, String msgId) {
			mUserId = userId;
			if (file != null) {
				addData(file, msgType, msgId);
			}

			mStreamState = null;
		}

		FileTransferInfo(String userId) {
			mUserId = userId;
			mStreamState = null;
		}

		void addData(String file, ChatMessage.Type msgType, String msgId) {
			DataInfo data = new DataInfo(file, msgType, msgId);
			mDataList.add(data);
			doSendingData();
		}

		void clearSendList() {
			if (mDataList != null) {
				mDataList.clear();
			}
		}

		void sendErrorMessage() {
			//TODO
			if (mDataList != null) {
				for (DataInfo info: mDataList) {
					if (!info.mIsSent) {
						_sendFileErrorMessge(mUserId, info.mMsgId);
					}
				}
			}
		}

		void removeStream() {
			if (mSession != null && mStream != null) {
				try {
					mSession.removeStream(mStream);
					mStream = null;
					mStreamState = null;
				}
				catch (CarrierException e) {
					e.printStackTrace();
				}
			}
		}

		void restart() {
			try {
				if (mSession != null && mStream != null) {
					Log.d(TAG, "restart removeStream before");
					mSession.removeStream(mStream);
					mStream = null;
					Log.d(TAG, "restart removeStream after");
				}

				if (mSession != null) {
					mStream = mSession.addStream(sFileTransferStreamType, sFileTransferStreamOptions, mStreamHandler);
				}
			}
			catch (CarrierException e) {
				e.printStackTrace();
			}
		}

		boolean hasFiletoSend() {
			if (mDataList.size() > 0) {
				DataInfo info = mDataList.getFirst();
				return info != null && info.mDataFile != null && !info.mDataFile.isEmpty();
			}
			return false;
		}

		/*
		 * First packet: 9bytes, first 4 bytes save packet count; second 4 bytes save file size,
		 *              the last byte save the message type.
		 * Data packet: the first 4 bytes save the packet's index in packets, the others is real data.
		 */
		private void doSendingData() {
			Log.d(TAG, "sendingData: file count="+mDataList.size());
			if (!hasFiletoSend()) {
				return;
			}

			if (mStreamState == StreamState.Closed || mStreamState == StreamState.Error) {
				restart();
			}

			if (mThread == null) {
				mThread = new Thread(new Runnable() {
					@Override
					public void run() {
						while (true) {
							if (mStreamState != StreamState.Connected || mDataList.size() <= 0) {
								Log.d(TAG, "doSendingData streamState="+mStreamState);
								try {
									mWaitTime --;
									Thread.sleep(5 * 1000);
									if (mWaitTime == 0) {
										mThread = null;
										return;
									}
								}
								catch (InterruptedException e) {
									e.printStackTrace();
								}
								continue;
							}
							else {
								mWaitTime = 5;
							}

							try {
								DataInfo info = mDataList.getFirst();
								if (info == null) continue;

								if (info.mIsSent) {
									mDataList.removeFirst();
									continue;
								}

								Log.d(TAG, "doSendingData send file="+info.mDataFile);
								final byte[] data = getFileData(info.mDataFile);

								if (data == null || data.length <= 0) {
									continue;
								}

								final int MIN_PACKET_DATA_SIZE = MIN_PACKET_SIZE - INDEX_BYTES_LEN;
								final int SENDING_SIZE = data.length;
								int packetSize = SENDING_SIZE > MIN_PACKET_SIZE ? MIN_PACKET_SIZE : SENDING_SIZE;
								final int PACKET_DATA_SIZE = SENDING_SIZE > MIN_PACKET_DATA_SIZE ? MIN_PACKET_DATA_SIZE : SENDING_SIZE;
								final int PACKET_COUNT = SENDING_SIZE / PACKET_DATA_SIZE + 1;

								//TODO : Add the msgId.
								byte[] firstBlock = new byte[FIRST_PACKET_BYTE_SIZE];
								byte[] countArray = int2BytesArray(PACKET_COUNT);
								//Fill packet count: firstBlock[0~3]
								fillByteArray(firstBlock, countArray);

								countArray = int2BytesArray(SENDING_SIZE);
								//Fill the real packet size: firstBlock[4~7]
								fillByteArray(firstBlock, PACKET_COUNT_LEN, countArray);

								//Fill message type: firstBlock[8]
								firstBlock[PACKET_COUNT_LEN + INDEX_BYTES_LEN] = (byte)info.mMsgType.ordinal();

								//Fill message type: firstBlock[9~44]
								fillByteArray(firstBlock, PACKET_COUNT_LEN + INDEX_BYTES_LEN + MSG_TYPE_LEN, info.mMsgId.getBytes());

								//Send the first packet
								mStream.writeData(firstBlock);

								for (int i = 0; i < PACKET_COUNT; i++) {
									//Fill the real package.
									fillByteArray(mDataBlock, int2BytesArray(i));

									if (i == (PACKET_COUNT - 1)) {
										packetSize = SENDING_SIZE - i * PACKET_DATA_SIZE;
										fillByteArray(mDataBlock, INDEX_BYTES_LEN, data, i * PACKET_DATA_SIZE, packetSize);
										packetSize += INDEX_BYTES_LEN;
									} else {
										fillByteArray(mDataBlock, INDEX_BYTES_LEN, data, i * PACKET_DATA_SIZE, PACKET_DATA_SIZE);
									}

									int sent = 0, len;
									do {
										try {
											len = mStream.writeData(mDataBlock, sent, packetSize - sent);
										} catch (CarrierException e) {
											e.printStackTrace();
											if (e.getErrorCode() == 0x81000010) {
												try {
													Thread.sleep(100);
												} catch (InterruptedException ie) {
													ie.printStackTrace();
												}

												continue;
											} else {
												Log.d(TAG, String.format("Write data failed: %s.", Integer.toHexString(e.getErrorCode())));
												return;
											}
										}

										sent += len;
									} while (sent < packetSize);
								}

								info.mIsSent = true;
								Log.d(TAG, "Finished writing");
								_sendMessge("doSendingData Finish");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});

				mThread.start();
			}
		}
	}

	private static byte[] int2BytesArray(int a) {
		byte[] data = new byte[4];
		data[0] = (byte) ((a >> 24) & 0xFF);
		data[1] = (byte) ((a >> 16) & 0xFF);
		data[2] = (byte) ((a >> 8) & 0xFF);
		data[3] = (byte) (a & 0xFF);
		return data;
	}

	private static int byteArray2Int(byte[] b) {
		return byteArray2Int(b, 0);
	}

	private static int byteArray2Int(byte[] b, int pos) {
		return b[3 + pos] & 0xFF |
				(b[2 + pos] & 0xFF) << 8 |
				(b[1 + pos] & 0xFF) << 16 |
				(b[pos] & 0xFF) << 24;
	}

	private void process(String userId, String file, ChatMessage.Type msgType, String msgId) {
		try {
			FileTransferInfo info = mSessionMap.get(userId);
			if (info == null) {
				info = new FileTransferInfo(userId, file, msgType, msgId);

				try {
					info.mSession = mSessionManager.newSession(userId);
				}
				catch (CarrierException e) {
					e.printStackTrace();

					//Create session error.
					_sendFileErrorMessge(userId, msgId);
					return;
				}

				info.mStreamHandler = new FileTransferStreamHandler(info, true);
				info.mCompleteHandler = new FileTransferSessionRequestCompleteHandler();
				info.mStream = info.mSession.addStream(sFileTransferStreamType, sFileTransferStreamOptions, info.mStreamHandler);
				mSessionMap.put(userId, info);
			}
			else {
				info.addData(file, msgType, msgId);
				info.doSendingData();
			}
		} catch (CarrierException e) {
			e.printStackTrace();
		}
	}

	static class SessionManagerHandler implements ManagerHandler {
		@Override
		public void onSessionRequest(Carrier carrier, String from, String sdp) {
			Log.d(TAG, String.format("Session Request from %s", from));

			_sendMessge("onSessionRequest");

			try {
				FileTransferInfo info = mSessionMap.get(from);
				if (info == null) {
					info = new FileTransferInfo(from);
					info.mSession = mSessionManager.newSession(from);
					info.mStreamHandler = new FileTransferStreamHandler(info, false);
					info.mCompleteHandler = new FileTransferSessionRequestCompleteHandler();
					info.mSdp = sdp;
					Log.d(TAG, "onSessionRequest request");
					info.mStream = info.mSession.addStream(sFileTransferStreamType, sFileTransferStreamOptions, info.mStreamHandler);

					mSessionMap.put(from, info);
				}
				else {
					if (info.mStreamState != StreamState.Connected) {
						info.mSdp = sdp;
						Log.d(TAG, "onSessionRequest removeStream before");
						info.removeStream();
						Log.d(TAG, "onSessionRequest removeStream after");

						info.mStream = info.mSession.addStream(sFileTransferStreamType, sFileTransferStreamOptions, info.mStreamHandler);
					}
				}
			} catch (CarrierException e) {
				e.printStackTrace();
			}
		}
	}

	static class FileTransferSessionRequestCompleteHandler implements SessionRequestCompleteHandler {
		FileTransferSessionRequestCompleteHandler() {
		}

		@Override
		public void onCompletion(Session session, int status, String reason, String sdp) {
			Log.d(TAG, String.format("Session complete, status: %d, reason: %s, sdp=[%s]", status, reason, sdp));
			_sendMessge("onCompletion");

			if (status == 0) {
				try {
					session.start(sdp);
				} catch (CarrierException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void _sendMessge(String tag) {
		Message msg = new Message();
		msg.what = NetUtils.ANYSTATE.FILE_TRANSFER_STATE;
		msg.obj = tag;
		mHandler.sendMessage(msg);
	}

	private static void _sendFileErrorMessge(String userId, String msgId) {
		Log.d(TAG, String.format("_sendFileErrorMessge userId=[%s], msgId=[%s]", userId, msgId));
		Message msg = new Message();
		msg.what = NetUtils.ANYSTATE.FILE_TRANSFER_SEND_ERROR;
		Bundle data = new Bundle();
		data.putString(NetUtils.ANYSTATE.USERID, userId);
		data.putString(NetUtils.ANYSTATE.MSGID, msgId);
		msg.setData(data);
		mHandler.sendMessage(msg);
	}

	private static void fillByteArray(byte[] dest, byte[] src) {
		fillByteArray(dest, 0, src);
	}

	private static void fillByteArray(byte[] dest, int pos, byte[] src) {
		fillByteArray(dest, pos, src, 0, src.length);
	}

	private static void fillByteArray(byte[] dest, int pos, byte[] src, int srcPos, int srcLen) {
		System.arraycopy(src, srcPos, dest, pos, srcLen);
	}

	static class FileTransferStreamHandler implements StreamHandler {
		private FileTransferInfo mFileTransferInfo;
		private boolean mActive;

		FileTransferStreamHandler(FileTransferInfo info, boolean active) {
			mFileTransferInfo = info;
			mActive = active;
		}

		@Override
		public void onStateChanged(Stream stream, StreamState state) {
			Log.d(TAG, "Stream state changed to:" + state);
			_sendMessge(state.toString());

			try {
				mFileTransferInfo.mStreamState = state;
				if (mActive) {
					if (state == StreamState.Initialized) {
						_sendMessge("mSession.request");
						try {
							mFileTransferInfo.mSession.request(mFileTransferInfo.mCompleteHandler);
						}
						catch (CarrierException e) {
							e.printStackTrace();
							Log.d(TAG, "onStateChanged request has error");
							mFileTransferInfo.sendErrorMessage();
						}
					}
					else if (state == StreamState.Connected) {
						//Send the file data.
						_sendMessge("StreamState.Connected");
						mFileTransferInfo.doSendingData();
					}
					else if (state == StreamState.Error || state == StreamState.Closed
							|| state == StreamState.Deactivated) {
						//TODO
//						mFileTransferInfo.restart();
						mFileTransferInfo.sendErrorMessage();
					}
				}
				else {
					if (state == StreamState.Initialized) {
						_sendMessge("mSession.replyRequest");
						mFileTransferInfo.mSession.replyRequest(0, null);
					}
					else if (state == StreamState.TransportReady) {
						_sendMessge("mSession.start");
						mFileTransferInfo.mSession.start(mFileTransferInfo.mSdp);
					}
				}
			}
			catch (CarrierException e) {
				e.printStackTrace();
			}
		}

		private int mPacketCount = 0;
		private int mPacketCountPos = 0;
		private int mFileSize = 0;
		private int mMsgType = -1;
		private String mMsgId;
		private byte[] mFileData;
		@Override
		public void onStreamData(Stream stream, byte[] data) {
			//TODO : several data block combine the whole file?
			if (mPacketCount == 0) {
				if (data.length == FIRST_PACKET_BYTE_SIZE) {
					mPacketCount = byteArray2Int(data);
					mFileSize = byteArray2Int(data, PACKET_COUNT_LEN);
					mFileData = new byte[mFileSize];
					mPacketCountPos = 0;
					mMsgType = data[8];

					//TODO: Get the message id.
					int baseLen = PACKET_COUNT_LEN + INDEX_BYTES_LEN + MSG_TYPE_LEN;
					mMsgId = new String(data, baseLen, MSG_ID_UUID_LEN);
					Log.d(TAG, String.format("Stream received data packageCount=[%d], mFileSize=[%d]", mPacketCount, mFileSize));
				}
				else {
					throw new InvalidParameterException("Invalid Parameter");
				}
			}
			else {
				int index = byteArray2Int(data);
				int realLen = (MIN_PACKET_SIZE - INDEX_BYTES_LEN);
				Log.d(TAG, String.format("Stream received data index=[%d], data.length=[%d], realLen=[%d], mPacketCount=[%d], mPacketCountPos=[%d]"
						, index, data.length, realLen, mPacketCount, mPacketCountPos));
				fillByteArray(mFileData, index * realLen, data, INDEX_BYTES_LEN, data.length - INDEX_BYTES_LEN);
				mPacketCountPos++;

				if (mPacketCountPos == mPacketCount) {
					Message msg = new Message();
					msg.what = NetUtils.ANYSTATE.FILE_TRANSFER;
					msg.arg1 = mMsgType;
					Bundle dataBundle = new Bundle();
					dataBundle.putString(NetUtils.ANYSTATE.FROM, mFileTransferInfo.mUserId);
					dataBundle.putByteArray(NetUtils.ANYSTATE.FILEDATA, mFileData);
					msg.setData(dataBundle);
					mHandler.sendMessage(msg);

					//reset mPacketCount
					mPacketCount = 0;
					Log.d(TAG, String.format("Stream received data mMsgId=[%s]", mMsgId));
					//Send a feedback to the friend.
					if (mMsgId != null && !mMsgId.isEmpty()) {
						if (NetUtils.getInstance() != null) {
							NetUtils.getInstance().sendMessage(mFileTransferInfo.mUserId, mMsgId, null);
						}
					}
				}
			}
		}

		@Override
		public boolean onChannelOpen(Stream stream, int channel, String cookie) {
			Log.d(TAG, "Stream request open new channel: " + channel);
			return true;
		}

		@Override
		public void onChannelOpened(Stream stream, int channel) {
			Log.d(TAG, "Opened Channel :" + channel);
		}

		@Override
		public void onChannelClose(Stream stream, int channel, CloseReason reason) {
			Log.d(TAG, String.format("Channel %d closeing with %s.", channel, reason.toString()));
		}

		@Override
		public boolean onChannelData(Stream stream, int channel, byte[] data) {
			Log.d(TAG, String.format("Channel [%d] received data [%s]",
					channel, (new String(data))));
			return true;
		}

		@Override
		public void onChannelPending(Stream stream, int channel) {
			Log.d(TAG, String.format("Stream channel [%d] pend data.", channel));
		}

		@Override
		public void onChannelResume(Stream stream, int channel) {
			Log.d(TAG, String.format("Stream channel [%d] resume data.", channel));
		}
	}
}
