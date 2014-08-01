package mikhail.kalashnikov.flags;

import java.util.StringTokenizer;

import mikhail.kalashnikov.flags.Flags.AnswerType;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

public class ModelFragment extends Fragment implements OnSharedPreferenceChangeListener {
	private static final String SPLIITER = ",";
	private final String TAG = getClass().getSimpleName();
	private Flags mFlags=null;
	private FlagsLoadTask mFlagsLoadTask=null;
//	private int mTotalScore=0;
	private int[] mPlayersScore;
	private int mNumberOfPlayers=1;
	private int mCurrentNumberOfFlagsPerPlayer;
	private int mNumberOfFlagsPerPlayer;
	private SharedPreferences mPref;
	private int mCurrentPlayerIdx;
	private int mLastAttemptScore = 0;
	private boolean mAnswerShown = false;
	private AnswerType mCurrentAnswerType = AnswerType.COUNTRY;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		mPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		mPref.registerOnSharedPreferenceChangeListener(this);
		updateFlags();
	}
	
	public int addScore(int score){
		mPlayersScore[mCurrentPlayerIdx] += (score - mLastAttemptScore);
		mLastAttemptScore = score;// we need lastScore in order to calculate Total score correctly when re-submit
		return mPlayersScore[mCurrentPlayerIdx];
	}
	
	public int addScoreOptionGameMode(int score){
		mPlayersScore[mCurrentPlayerIdx] += score;
		return mPlayersScore[mCurrentPlayerIdx];
	}
	
	public int getScore(){
		return mPlayersScore[mCurrentPlayerIdx];
	}
	
	public int[] getFinalScore(){
		return mPlayersScore;
	}
	
	public int getNumberOfPlayers(){
		return mNumberOfPlayers;
	}
	
	synchronized private void updateFlags(){
		if(mFlags != null && (FlagsActivity)getActivity() != null){
			((FlagsActivity)getActivity()).onDataUploaded(mFlags, mPref, mFlags.isStartNewGame());
		}else if(mFlagsLoadTask == null){ // We check task == null here because it can be in process
			mFlagsLoadTask = new FlagsLoadTask();
			executeAsyncTask(mFlagsLoadTask, getActivity().getApplicationContext());
		}
	}
	
	@TargetApi(11)
	static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		}
		else {
			task.execute(params);
		}
	}
	
	private class FlagsLoadTask extends AsyncTask<Context, Void, Void>{
		private Exception e=null;
		private Flags localFlags;
		
		@Override
		protected Void doInBackground(Context... ctxt) {
			try{
				mPref.getAll();
				String language = mPref.getString(SettingsActivity.KEY_PREF_LANGUAGE, "EN");
				String unAnsweredList = mPref.getString(SettingsActivity.KEY_PREF_UNANSWERED_LIST, "");
				parsePlayersScore(mPref.getString(SettingsActivity.KEY_PREF_SCORE, "0"));
				mCurrentPlayerIdx = mPref.getInt(SettingsActivity.KEY_PREF_PLAYER_IDX, 0);
				mLastAttemptScore = mPref.getInt(SettingsActivity.KEY_PREF_LAST_ATTEMPT_SCORE, 0);
				mAnswerShown = mPref.getBoolean(SettingsActivity.KEY_PREF_IS_ANSWER_SHOWN, false);
				mNumberOfFlagsPerPlayer = mPref.getInt(SettingsActivity.KEY_PREF_NUMBER_OF_FLAGS, 10);
				mCurrentNumberOfFlagsPerPlayer = mPref.getInt(SettingsActivity.KEY_PREF_CURRENT_NUMBER_OF_FLAGS, 10);
				mCurrentAnswerType = SettingsActivity.prefToAnswerType(mPref.getString(SettingsActivity.KEY_PREF_CURRENT_ANSWER_TYPE, ""));
				localFlags = new Flags(ctxt[0].getAssets().open("countries.json"), language, unAnsweredList);
				
			}catch (Exception e) {
				this.e = e;
			}
			return null;
		}
		
		
		@Override
		protected void onPostExecute(Void result) {
			if(e==null){
				ModelFragment.this.mFlags=localFlags;
				updateFlags();
			}else{
				Log.e(TAG, "Exception whrn loading flags list", e);
				throw new RuntimeException("Exception whrn loading flags list", e);
			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals(SettingsActivity.KEY_PREF_LANGUAGE)){
			mFlags.setLanguage(mPref.getString(SettingsActivity.KEY_PREF_LANGUAGE, ""));
			((FlagsActivity)getActivity()).onLanguageChanged();
		}
		
		if(key.equals(SettingsActivity.KEY_PREF_GAME_MODE)){
			((FlagsActivity)getActivity()).switchGameMode(
					mPref.getString(SettingsActivity.KEY_PREF_GAME_MODE, ""));
		}
		
		if(key.equals(SettingsActivity.KEY_PREF_NUMBER_OF_FLAGS)){
			mNumberOfFlagsPerPlayer = mPref.getInt(SettingsActivity.KEY_PREF_NUMBER_OF_FLAGS, 1);
		}
		
	}

	public void newGame(int numberOfPlayers) {
		mNumberOfPlayers = numberOfPlayers;
		mPlayersScore = new int[numberOfPlayers];
		mCurrentPlayerIdx = 0;
		mLastAttemptScore = 0;
		mAnswerShown = false;
		mCurrentNumberOfFlagsPerPlayer = mNumberOfFlagsPerPlayer;
		mFlags.newGame(mCurrentNumberOfFlagsPerPlayer * numberOfPlayers);
		
	}

	public String scoreToString(){
		StringBuilder sb = new StringBuilder();
		for(int i: mPlayersScore){
			sb.append(i);
			sb.append(SPLIITER);
		}
		
		return sb.toString();
	}
	
	private void parsePlayersScore(String strList){
		StringTokenizer st = new StringTokenizer(strList, SPLIITER);
		mNumberOfPlayers = st.countTokens();
		mPlayersScore = new int[mNumberOfPlayers];
		for(int i=0; i<mNumberOfPlayers; i++){
			mPlayersScore[i] = Integer.parseInt(st.nextToken());
		}
	}

	public void nextTurn() {
		mCurrentPlayerIdx++;
		mCurrentPlayerIdx = mCurrentPlayerIdx % mNumberOfPlayers;
		mLastAttemptScore = 0;
		mAnswerShown = false;
	}
	
	public int getCurrentPlayerIdx(){
		return mCurrentPlayerIdx;
	}

	public void setAnswerShown() {
		mAnswerShown  = true;
	}
	
	public boolean isAnswerShown() {
		return mAnswerShown;
	}
	
	public int getLastAttemptScore(){
		return mLastAttemptScore;
	}

	public int getMaxScore() {
		return mCurrentNumberOfFlagsPerPlayer * 2;
	}

	public int getNumberOfFlagsPerPlayer() {
		return mCurrentNumberOfFlagsPerPlayer;
	}

	public Object getCurrentFlagPosition() {
		int pos  = mCurrentNumberOfFlagsPerPlayer 
				- mFlags.getFlagsLeft() / mNumberOfPlayers 
				- ((mFlags.getFlagsLeft() % mNumberOfPlayers) == 0 ? -1:0);
		return pos;
	}
	
	public String[] getAnswerOptions(int numOfOptions, Flags.AnswerType answerType, boolean isRequestNew){
		if(isRequestNew){
			mAnswerShown = false;
		}
		mCurrentAnswerType = answerType;
		return mFlags.getAnswerOptions(numOfOptions, answerType, isRequestNew);
	}
	
	public AnswerType getCurrentAnswerType(){
		return mCurrentAnswerType;
	}
}
