package any.xxx.anypeer.util;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.elastos.spvcore.DIDManagerSupervisor;
import com.elastos.spvcore.ElastosWalletUtils;
import com.elastos.spvcore.IDid;
import com.elastos.spvcore.IDidManager;
import com.elastos.spvcore.IMasterWallet;
import com.elastos.spvcore.ISubWallet;
import com.elastos.spvcore.ISubWalletCallback;
import com.elastos.spvcore.MasterWalletManager;
import com.elastos.spvcore.WalletException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnyWallet {
	private static final String TAG = "AnyWallet";
	private MasterWalletManager mMasterWalletManager;
	private DIDManagerSupervisor mDIDManagerSupervisor;
	private IMasterWallet mCurrentMasterWallet;
	private IDidManager mDidManager;
	private static AnyWallet sInstance;
	private static String mRootPath;
	private static final String mLanguage = "english";
	private static final String mMasterWalletId = "masterWalletId";
	private static final long FeePerKb = 10000;
	public static final String ELA = "ELA";
	private static final String IDCHAIN = "IdChain";
	public static final String MNEMONIC = "mnemonic";
	private static final String DID = "anyWallet_did";
	private ISubWallet mELASubWallet;
	private Context mContext;
	public final static String ANYBOT_ELAADDRESS = "EbnPZTrrUpMcLmsxmi68kgQuUWDv1PV6Ng";

	public static AnyWallet getInstance(Context context) {
		if (sInstance == null) {
			mRootPath = Utils.getRootPath(context) + "/AnyWallet";
			File file = new File(mRootPath);
			if (!file.exists()) {
				if (!file.mkdir()) {
					return null;
				}
			}

			sInstance = new AnyWallet(context);
		}
		return sInstance;
	}

	public static AnyWallet getInstance() {
		return sInstance;
	}

	private AnyWallet(Context context) {
		mContext = context;
		ElastosWalletUtils.InitConfig(context, mRootPath);
		mMasterWalletManager = new MasterWalletManager(mRootPath);
		mDIDManagerSupervisor = new DIDManagerSupervisor(mRootPath);
	}

	public boolean login(String password) {
		try {
			if (password == null || password.isEmpty()) {
				//1.restore MasterWallet
				ArrayList<IMasterWallet> mMasterWalletList = mMasterWalletManager.GetAllMasterWallets();
				if (mMasterWalletList != null && mMasterWalletList.size() > 0) {
					mCurrentMasterWallet = mMasterWalletList.get(0);
					ArrayList<ISubWallet> subWallets = mCurrentMasterWallet.GetAllSubWallets();
					if (subWallets.size() > 0) {
						mELASubWallet = subWallets.get(0);
					}
				}
			}
			else {
				//2. Create a new MasterWallet
				//2.1 GenerateMnemonic
				String mnemonic = mMasterWalletManager.GenerateMnemonic(mLanguage);

				//2.2 CreateMasterWallet
				String phrasePassword = "";
				mCurrentMasterWallet = mMasterWalletManager.CreateMasterWallet(mMasterWalletId, mnemonic
						, phrasePassword, getRealPassword(password), true);

				createDIDManager(mCurrentMasterWallet);

				//3 CreateSubWallet: ELA
				mELASubWallet = mCurrentMasterWallet.CreateSubWallet(ELA, FeePerKb);

				//Generate the did
				generateDID(password);
			}

			if (mELASubWallet == null) {
				return false;
			}

			mELASubWallet.AddCallback(new SubWalletCallback());
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public String getWalletAddress() {
		if (mELASubWallet == null) {
			return null;
		}

		try {
			JSONObject jsonObject = new JSONObject(mELASubWallet.GetAllAddress(0, 1));
			return jsonObject.getJSONArray("Addresses").get(0).toString();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public String transfer(String address, long amount, String memo, String password) {
		try {
			if (!mCurrentMasterWallet.IsAddressValid(address) || mELASubWallet == null) {
				return null;
			}

			//1. CreateTransaction(String fromAddress, String toAddress, long amount, String memo, String remark)
			String rawTransaction = mELASubWallet.CreateTransaction(getWalletAddress(), address, amount, memo, "Remark", false);

			//2. CalculateTransactionFee(String rawTransaction, long feePerKb)
			long fee = mELASubWallet.CalculateTransactionFee(rawTransaction, FeePerKb);

			//3. UpdateTransactionFee(String rawTransaction, long fee)
			rawTransaction = mELASubWallet.UpdateTransactionFee(rawTransaction, fee, "");

			//4. SignTransaction(String rawTransaction, String payPassword)
			password = getRealPassword(password);
			rawTransaction = mELASubWallet.SignTransaction(rawTransaction, password);

			//5. PublishTransaction
			return  mELASubWallet.PublishTransaction(rawTransaction);
		}
		catch (WalletException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isAddressValid(String address) {
		try {
			return mCurrentMasterWallet.IsAddressValid(address);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getDID() {
		try {
			String did = PrefereneceUtil.getString(mContext, AnyWallet.DID);
			if (did != null) {
				return did;
			}
		}
		catch (WalletException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public void generateDID(String password) {
		try {
			//If exist, return.
			String did = PrefereneceUtil.getString(mContext, AnyWallet.DID);
			if (did != null && !did.isEmpty()) {
				return;
			}

			if (mDidManager == null) {
				createDIDManager(mCurrentMasterWallet);
			}

			if (mDidManager != null) {
				IDid idid = mDidManager.CreateDID(getRealPassword(password));
				if (idid != null) {
					String didName = idid.GetDIDName();
					PrefereneceUtil.saveString(mContext, AnyWallet.DID, didName);
				}
			}
		}
		catch (WalletException e) {
			e.printStackTrace();
		}
	}

	public void activateDID() {
		//TODO
	}

	public long getBalance() {
		try {
			if (mELASubWallet == null) {
				throw new RuntimeException("mELASubWallet======null");
			}

			return mELASubWallet.GetBalance(0);
		} catch (WalletException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public String getBalanceString() {
		try {
			long balance = getBalance();
			return String.format(Locale.US, "%.4f", TOELA(balance));
		} catch (WalletException e) {
			e.printStackTrace();
		}
		return "0";
	}

	public boolean changePassword(String oldPassword, String newPassword) {
		try {
			if (mCurrentMasterWallet == null) {
				return false;
			}

			oldPassword = getRealPassword(oldPassword);
			newPassword = getRealPassword(newPassword);
			mCurrentMasterWallet.ChangePassword(oldPassword, newPassword);
			return true;
		} catch (WalletException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void onPause() {
		if (mMasterWalletManager != null) {
			mMasterWalletManager.SaveConfigs();
		}
	}

	public void onDestroy() {
		if (mMasterWalletManager != null) {
			Map<String, ArrayList<ISubWallet>> subWalletMap = new HashMap<String, ArrayList<ISubWallet>>();
			ArrayList<IMasterWallet> masterWalletList = mMasterWalletManager.GetAllMasterWallets();
			for (int i = 0; i < masterWalletList.size(); i++) {
				IMasterWallet masterWallet = masterWalletList.get(i);
				subWalletMap.put(masterWallet.GetId(), masterWallet.GetAllSubWallets());
			}

			mMasterWalletManager.DisposeNative();

			for (Map.Entry<String, ArrayList<ISubWallet>> entry : subWalletMap.entrySet()) {
				Log.i(TAG, "Removing masterWallet[" + entry.getKey() + "]'s callback");
				ArrayList<ISubWallet> subWallets = entry.getValue();
				for (int i = 0; i < subWallets.size(); i++) {
					subWallets.get(i).RemoveCallback();
				}
			}
			mMasterWalletManager = null;
		}
	}

	private String getAllTransaction() {
		try {
			return mELASubWallet.GetAllTransaction(0, 200, getWalletAddress());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static class TransactionItem {
		TransactionItem() {
		}

		public String mTransactionTime;
		public String mConfirmStatus;
		public long mInAmount;
		public long mOutAmount;
		public String mStatus;
		public String mTxHash;
		public String mOtherAddress;
	}

	public List<TransactionItem> getAllTransactionItems() {
		List<TransactionItem> transactionItems = null;
		try {
			String transactions = getAllTransaction();
			if (transactions == null) return null;

			JSONObject jsonObject = new JSONObject(transactions);
			//1. MaxCount: int
			int MaxCount = jsonObject.getInt(TRANSACTIONKEY.MaxCount);
			transactionItems = new ArrayList<>(MaxCount);

			//2. Transactions: JSONArray
			JSONArray transactionsJsonArray = jsonObject.getJSONArray(TRANSACTIONKEY.Transactions);
//			Log.d(TAG, String.format("MaxCount=[%d], transactionsJsonArrayLen=[%d]", MaxCount, transactionsJsonArray.length()));
//			Log.d(TAG, "transactionsJsonArray="+transactionsJsonArray.toString());
			for (int i = 0; i < transactionsJsonArray.length(); i++) {
				//3. Summary: JSONObject
				JSONObject summaryContentJson = transactionsJsonArray.getJSONObject(i);
//				Log.d(TAG, "summaryContentJson="+summaryContentJson);
				if (summaryContentJson != null) {
					//4. Summary content: JSONObject
					TransactionItem item = new TransactionItem();

					//4.1 ConfirmStatus: String
					String ConfirmStatus = summaryContentJson.getString(TRANSACTIONKEY.SUMMARY.ConfirmStatus);
					item.mConfirmStatus = ConfirmStatus;

					//4.2 Fee: int
//						int Fee = summaryContentJson.getInt(TRANSACTIONKEY.SUMMARY.Fee);

					//4.3 Direction: JSONObject
					long Amount = summaryContentJson.getLong(TRANSACTIONKEY.SUMMARY.Amount);
					String Direction = summaryContentJson.getString(TRANSACTIONKEY.SUMMARY.Direction);
					if (Direction.equals("Received")) {
						item.mInAmount = Amount;
					}
					else {
						item.mOutAmount = Amount;
					}

					//4.4 Status: String
					String Status = summaryContentJson.getString(TRANSACTIONKEY.SUMMARY.Status);
					item.mStatus = Status;

					//4.5 Timestamp: long
					long Timestamp = summaryContentJson.getLong(TRANSACTIONKEY.SUMMARY.Timestamp);
					item.mTransactionTime = getDateToString(Timestamp);

					//4.6 TxHash: String
					String TxHash = summaryContentJson.getString(TRANSACTIONKEY.SUMMARY.TxHash);
					item.mTxHash = TxHash;

					//4.7 mOtherAddress
					if (summaryContentJson.has(TRANSACTIONKEY.SUMMARY.Outputs)) {
						JSONObject Outputs = summaryContentJson.getJSONObject(TRANSACTIONKEY.SUMMARY.Outputs);
						Iterator<String> iterator = Outputs.keys();
						while (iterator.hasNext()) {
							String address = iterator.next();
							if (address != null && !address.isEmpty() && !address.equals(getWalletAddress())) {
								item.mOtherAddress = address;
								break;
							}
						}
					}

					Log.d(TAG, String.format("ConfirmStatus=[%s], inAmount=[%d], Status=[%s], TxHash=[%s], time=[%s], mOtherAddress=[%s]"
							, ConfirmStatus, Amount, Status, TxHash, getDateToString(Timestamp), item.mOtherAddress));

					transactionItems.add(item);
				}
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}

		return transactionItems;
	}

	private static String getDateToString(long milSecond) {
		Date date = new Date(milSecond* 1000);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		return format.format(date);
	}

	private static class TRANSACTIONKEY {
		private static final String MaxCount = "MaxCount";
		private static final String Transactions = "Transactions";
		private static final String Summary = "Summary";

		private static class SUMMARY {
			private static final String ConfirmStatus = "ConfirmStatus";
			private static final String Fee = "Fee";
			private static final String Remark = "Any Remark";
			private static final String Status = "Status";
			private static final String Timestamp = "Timestamp";
			private static final String TxHash = "TxHash";
			private static final String Direction = "Direction";
			private static final String Amount = "Amount";
			private static final String Outputs = "Outputs";
		}
	}

	public static final long BASE_TRANSFER = 100000000;
	public static double TOELA(long sela) {
		return (double) sela / BASE_TRANSFER;
	}

	public static String TOELAS(long sela) {
		return String.format(Locale.US, "%.4f", TOELA(sela));
	}

	private ISubWallet recoverSubWallet(String chainID, int limitGap, long feePerKb) {
		try {
			if (mCurrentMasterWallet == null) {
				return null;
			}

			//TODO
//			return mCurrentMasterWallet.RecoverSubWallet(chainID, limitGap, feePerKb);
		} catch (WalletException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String createAddress() {
		try {
			if (mELASubWallet == null) {
				return null;
			}

			return mELASubWallet.CreateAddress();
		} catch (WalletException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Handler mHandler;
	public void setMessageHandler(Handler handler) {
		mHandler = handler;
	}

	public static class WalletCallback {
		//Message type
		public static final int TransactionStatusChanged = 0;
		public static final int BlockSyncStarted = 1;
		public static final int BlockSyncProgress = 2;
		public static final int BlockSyncStopped = 3;
		public static final int BalanceChanged = 4;
		public static final int TxPublished = 5;
		public static final int TxDeleted = 6;

		//Message data string
		public static final String SYNCCURRENTHEIGHT = "currentBlockHeight";
		public static final String SYNCESTIMATEHEIGHT = "estimatedHeight";
	}

	private class SubWalletCallback implements ISubWalletCallback {
		@Override
		public void OnTransactionStatusChanged(String txId, String status, String desc,int confirms) {
			//TODO
			Log.d(TAG, String.format("OnTransactionStatusChanged txId=[%s], status=[%s], desc=[%s], confirms=[%d]"
					, txId, status, desc, confirms));
			if (mHandler != null) {
				mHandler.sendEmptyMessage(WalletCallback.TransactionStatusChanged);
			}
		}

		@Override
		public void OnBlockSyncStarted() {
			//TODO
			Log.d(TAG, "OnBlockSyncStarted");
			if (mHandler != null) {
				mHandler.sendEmptyMessage(WalletCallback.BlockSyncStarted);
			}
		}

		@Override
		public void OnBlockSyncProgress(int currentBlockHeight, int estimatedHeight) {
			Log.d(TAG, String.format("OnBlockSyncProgress: %d/%d", currentBlockHeight, estimatedHeight));
			if (mHandler != null) {
				Message msg = new Message();
				msg.what = WalletCallback.BlockSyncProgress;
				Bundle data = new Bundle();
				data.putInt(WalletCallback.SYNCCURRENTHEIGHT, currentBlockHeight);
				data.putInt(WalletCallback.SYNCESTIMATEHEIGHT, estimatedHeight);
				msg.setData(data);
				mHandler.sendMessage(msg);
			}
		}

		@Override
		public void OnBlockSyncStopped() {
			//TODO
			Log.d(TAG, "OnBlockSyncStopped");
			if (mHandler != null) {
				mHandler.sendEmptyMessage(WalletCallback.BlockSyncStopped);
			}
		}

		@Override
		public void OnBalanceChanged(String asset, long balance) {
			//TODO
			Log.d(TAG, "OnBalanceChanged  balance="+balance + ", asset="+asset);
			if (mHandler != null) {
				mHandler.sendEmptyMessage(WalletCallback.BalanceChanged);
			}
		}

		@Override
		public void OnTxPublished(String hash, String result) {
			//TODO
			Log.d(TAG, "OnTxPublished");
			if (mHandler != null) {
				mHandler.sendEmptyMessage(WalletCallback.TxPublished);
			}
		}

		@Override
		public void OnTxDeleted(String hash, boolean notifyUser, boolean recommendRescan) {
			//TODO
			Log.d(TAG, "OnTxDeleted");
			if (mHandler != null) {
				mHandler.sendEmptyMessage(WalletCallback.TxDeleted);
			}
		}
	}

	public boolean destroyWallet() {
		try {
			Map<String, ArrayList<ISubWallet>> subWalletMap = new HashMap<>();
			ArrayList<IMasterWallet> masterWalletList = mMasterWalletManager.GetAllMasterWallets();
			for (int i = 0; i < masterWalletList.size(); i++) {
				IMasterWallet masterWallet = masterWalletList.get(i);
				subWalletMap.put(masterWallet.GetId(), masterWallet.GetAllSubWallets());
			}

			mMasterWalletManager.DestroyWallet(mMasterWalletId);

			for (Map.Entry<String, ArrayList<ISubWallet>> entry : subWalletMap.entrySet()) {
				Log.i(TAG, "Removing masterWallet[" + entry.getKey() + "]'s callback");
				ArrayList<ISubWallet> subWallets = entry.getValue();
				for (int i = 0; i < subWallets.size(); i++) {
					subWallets.get(i).RemoveCallback();
				}
			}

			sInstance = null;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private boolean importWalletWithKeystore(String masterWalletID, String keystoreContent, String backupPassword, String payPassword) {
		try {
			mCurrentMasterWallet = mMasterWalletManager.ImportWalletWithKeystore(
					masterWalletID, keystoreContent, backupPassword, payPassword);
			if (mCurrentMasterWallet == null) {
				return false;
			}

			createDIDManager(mCurrentMasterWallet);

			return true;
		} catch (WalletException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean importWalletWithMnemonic(String masterWalletID, String mnemonic, String phrasePassword
			, String payPassword, boolean singleAddress) {
		try {
			mCurrentMasterWallet = mMasterWalletManager.ImportWalletWithMnemonic(
					masterWalletID, mnemonic, phrasePassword, payPassword, singleAddress);
			if (mCurrentMasterWallet == null) {
				return false;
			}

			createDIDManager(mCurrentMasterWallet);
			return true;
		} catch (WalletException e) {
			e.printStackTrace();
		}
		return false;
	}

	private String exportWalletWithKeystore(String backupPassword, String payPassword) {
		try {
			if (mCurrentMasterWallet == null) {
				return null;
			}

			return mMasterWalletManager.ExportWalletWithKeystore(mCurrentMasterWallet, backupPassword, payPassword);
		} catch (WalletException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String exportWalletWithMnemonic(String backupPassword) {
		try {
			backupPassword = getRealPassword(backupPassword);
			if (mCurrentMasterWallet == null) {
				return null;
			}

			return mMasterWalletManager.ExportWalletWithMnemonic(mCurrentMasterWallet, backupPassword);
		} catch (WalletException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isValidWallet() {
		return mCurrentMasterWallet != null && mELASubWallet != null;
	}

	private String encodeTransactionToString(String txJson) {
		if (txJson == null || txJson.isEmpty()) {
			return null;
		}
		try {
			//cipherJson
			return mMasterWalletManager.EncodeTransactionToString(txJson);
		} catch (WalletException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String decodeTransactionFromString(String cipherJson) {
		if (cipherJson == null || cipherJson.isEmpty()) {
			return null;
		}
		try {
			return mMasterWalletManager.DecodeTransactionFromString(cipherJson);
		} catch (WalletException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void createDIDManager(IMasterWallet masterWallet) {
		try {
			mDidManager = mDIDManagerSupervisor.CreateDIDManager(masterWallet, mRootPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getRealPassword(String password) {
		try {
			byte[] hash = MessageDigest.getInstance("MD5").digest(password.getBytes("UTF-8"));
			StringBuilder hex = new StringBuilder(hash.length * 2);
			for (byte b : hash) {
				if ((b & 0xFF) < 0x10){
					hex.append("0");
				}
				hex.append(Integer.toHexString(b & 0xFF));
			}
			return hex.toString();
		}
		catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null;
	}
}
