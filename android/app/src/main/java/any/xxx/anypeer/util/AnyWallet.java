package any.xxx.anypeer.util;

import android.content.Context;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnyWallet {
	private static final String TAG = "AnyWallet";
	private MasterWalletManager mMasterWalletManager;
	private DIDManagerSupervisor mDIDManagerSupervisor;
	private IMasterWallet mCurrentMasterWallet;
	private ArrayList<IMasterWallet> mMasterWalletList;
	private IDidManager mDidManager;
	private static AnyWallet sInstance;
	private static String mRootPath;
	private static final String mLanguage = "english";
	private static final String mMasterWalletId = "masterWalletId";
	private static final long FeePerKb = 10000;
	public static final String ELA = "ELA";
	private static final String IDCHAIN = "IdChain";
	public static final String MNEMONIC = "mnemonic";
	public static final String DID = "anyWallet_did";
	private ISubWallet mELASubWallet;
	private Context mContext;

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
				mMasterWalletList = mMasterWalletManager.GetAllMasterWallets();
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
			String rawTransaction = mELASubWallet.CreateTransaction(getWalletAddress(), address, amount, memo, "");

			//2. CalculateTransactionFee(String rawTransaction, long feePerKb)
			long fee = mELASubWallet.CalculateTransactionFee(rawTransaction, FeePerKb);

			//3. UpdateTransactionFee(String rawTransaction, long fee)
			rawTransaction = mELASubWallet.UpdateTransactionFee(rawTransaction, fee);

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

			return mELASubWallet.GetBalance();
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

	public String getAllTransaction() {
		try {
			return mELASubWallet.GetAllTransaction(0, 100, "");
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
	}

	public List<TransactionItem> getAllTransactionItems() {
		List<TransactionItem> transactionItems = null;
		try {
			String transactions = getAllTransaction();
			JSONObject jsonObject = new JSONObject(transactions);
			//1. MaxCount: int
			int MaxCount = jsonObject.getInt(TRANSACTIONKEY.MaxCount);
			transactionItems = new ArrayList<TransactionItem>(MaxCount);

			//2. Transactions: JSONArray
			JSONArray transactionsJsonArray = jsonObject.getJSONArray(TRANSACTIONKEY.Transactions);
			Log.d(TAG, String.format("MaxCount=[%d], transactionsJsonArrayLen=[%d]", MaxCount, transactionsJsonArray.length()));
			for (int i = 0; i < transactionsJsonArray.length(); i++) {
				//3. Summary: JSONObject
				JSONObject summarysJson = transactionsJsonArray.getJSONObject(i);
				if (summarysJson != null) {
					//4. Summary content: JSONObject
					JSONObject summaryContentJson = summarysJson.getJSONObject(TRANSACTIONKEY.Summary);
					if (summaryContentJson != null) {
						TransactionItem item = new TransactionItem();

						//4.1 ConfirmStatus: String
						String ConfirmStatus = summaryContentJson.getString(TRANSACTIONKEY.SUMMARY.ConfirmStatus);
						item.mConfirmStatus = ConfirmStatus;

						//4.2 Fee: int
						int Fee = summaryContentJson.getInt(TRANSACTIONKEY.SUMMARY.Fee);

						//4.3 Incoming: JSONObject
						JSONObject Incoming = summaryContentJson.getJSONObject(TRANSACTIONKEY.SUMMARY.Incoming);
						//4.3.1 Amount: long
						long Amount = Incoming.getLong(TRANSACTIONKEY.SUMMARY.Amount);
						item.mInAmount = Amount;

						//4.4 Outcoming: JSONObject
						JSONObject Outcoming = summaryContentJson.getJSONObject(TRANSACTIONKEY.SUMMARY.Outcoming);
						//4.3.1 Amount: long
						Amount = Outcoming.getLong(TRANSACTIONKEY.SUMMARY.Amount);
						item.mOutAmount = Amount;

						//4.5 Remark: String
						String Remark = summaryContentJson.getString(TRANSACTIONKEY.SUMMARY.Remark);

						//4.6 Status: String
						String Status = summaryContentJson.getString(TRANSACTIONKEY.SUMMARY.Status);
						item.mStatus = Status;

						//4.7 Timestamp: long
						long Timestamp = summaryContentJson.getLong(TRANSACTIONKEY.SUMMARY.Timestamp);
						item.mTransactionTime = getDateToString(Timestamp);

						//4.8 TxHash: String
						String TxHash = summaryContentJson.getString(TRANSACTIONKEY.SUMMARY.TxHash);
						item.mTxHash = TxHash;

						Log.d(TAG, String.format("ConfirmStatus=[%s], Fee=[%d], inAmount=[%d], Remark=[%s], Status=[%s], TxHash=[%s], time=[%s]"
								, ConfirmStatus, Fee, Amount, Remark, Status, TxHash, getDateToString(Timestamp)));

						transactionItems.add(item);
					}
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
			private static final String Incoming = "Incoming";
			private static final String Outcoming = "Outcoming";
			private static final String Remark = "Remark";
			private static final String Status = "Status";
			private static final String Timestamp = "Timestamp";
			private static final String TxHash = "TxHash";

			private static final String Amount = "Amount";
		}
	}

	public static final long BASE_TRANSFER = 100000000;
	public static double TOELA(long sela) {
		return (double) sela / BASE_TRANSFER;
	}

	private ISubWallet recoverSubWallet(String chainID, int limitGap, long feePerKb) {
		try {
			if (mCurrentMasterWallet == null) {
				return null;
			}

			return mCurrentMasterWallet.RecoverSubWallet(chainID, limitGap, feePerKb);
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

	private class SubWalletCallback implements ISubWalletCallback {
		@Override
		public void OnTransactionStatusChanged(String txId, String status, String desc,int confirms) {
			//TODO
			Log.d(TAG, String.format("OnTransactionStatusChanged txId=[%s], status=[%s], desc=[%s], confirms=[%d]"
					, txId, status, desc, confirms));
		}

		@Override
		public void OnBlockSyncStarted() {
			//TODO
			Log.d(TAG, "OnBlockSyncStarted");
		}

		@Override
		public void OnBlockHeightIncreased(int currentBlockHeight, int progress) {
			//TODO
//			Log.d(TAG, String.format("OnBlockHeightIncreased  currentBlockHeight=[%d], progress=[%d]"
//					, currentBlockHeight, progress));
		}

		@Override
		public void OnBlockSyncStopped() {
			//TODO
			Log.d(TAG, "OnBlockSyncStopped");
		}

		@Override
		public void OnBalanceChanged(long balance) {
			//TODO
			Log.d(TAG, "OnBalanceChanged  balance="+balance);
		}
	}

	private void destroyWallet(String masterWalletID) {
		Map<String, ArrayList<ISubWallet>> subWalletMap = new HashMap<String, ArrayList<ISubWallet>>();
		ArrayList<IMasterWallet> masterWalletList = mMasterWalletManager.GetAllMasterWallets();
		for (int i = 0; i < masterWalletList.size(); i++) {
			IMasterWallet masterWallet = masterWalletList.get(i);
			subWalletMap.put(masterWallet.GetId(), masterWallet.GetAllSubWallets());
		}

//		IDidManager DIDManager = getDIDManager(masterWalletID);
//		if (DIDManager != null) {
//			// TODO destroy did manager
//		}
		mMasterWalletManager.DestroyWallet(masterWalletID);

		for (Map.Entry<String, ArrayList<ISubWallet>> entry : subWalletMap.entrySet()) {
			Log.i(TAG, "Removing masterWallet[" + entry.getKey() + "]'s callback");
			ArrayList<ISubWallet> subWallets = entry.getValue();
			for (int i = 0; i < subWallets.size(); i++) {
				subWallets.get(i).RemoveCallback();
			}
		}
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
