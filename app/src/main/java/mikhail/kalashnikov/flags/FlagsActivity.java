package mikhail.kalashnikov.flags;

import mikhail.kalashnikov.flags.Flags.AnswerType;
import mikhail.kalashnikov.flags.NumberPickerDialog.NumberPickerListener;
import mikhail.kalashnikov.flags.SettingsActivity.GameMode;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class FlagsActivity extends ActionBarActivity implements OnEditorActionListener,
		NumberPickerListener{
	private final static String MODEL = "model";
	private final static boolean ONE_ATTEMPT_IN_OPTION_MODE = true;
	private final static int NUM_OF_OPTIONS = 4;
	private final String TAG = getClass().getSimpleName();
	private ImageView mFlagImage;
	private EditText mCountryEditText;
	private EditText mCapitalEditText;
	private TextView mScoreTextView;
	private Button mSubmitButton;
	private Button mNextButton;
	private ModelFragment mModel;
	private Flags mFlags;
	private SharedPreferences mPref;
	private int mNumberOfPlayers;
	private ActionBar mActionBar;
	private RadioGroup mRGOptions;
	private RadioButton[] mAnswerOptionRBs = new RadioButton[NUM_OF_OPTIONS];
	private GameMode mGameMode;
	private int mColorNotAnswered;
	private int mColorCorrect;
	private int mColorWrong;
	private boolean DEVELOPER_MODE = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(DEVELOPER_MODE){
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.detectAll()
					.penaltyLog()
					.build();
			StrictMode.setThreadPolicy(policy);

		}
		Log.d(TAG, "OnCreate");
		super.onCreate(savedInstanceState);
		//PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		if (getSupportFragmentManager().findFragmentByTag(MODEL) == null){
			mModel = new ModelFragment();
			getSupportFragmentManager().beginTransaction().add(mModel, MODEL).commit();
		}else{
			mModel = (ModelFragment) getSupportFragmentManager().findFragmentByTag(MODEL);
		}

		setContentView(R.layout.main);
		mFlagImage = (ImageView) findViewById(R.id.flag);
		mScoreTextView = (TextView) findViewById(R.id.score);
		mScoreTextView.setText(String.format(getResources().getString(R.string.score), 1, 0));
		mSubmitButton = (Button) findViewById(R.id.submit);
		mSubmitButton.setEnabled(false);
		mSubmitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				submit();
			}
		});

		mNextButton = (Button) findViewById(R.id.next);
		mNextButton.setEnabled(false);
		mNextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				next();
			}
		});
		mActionBar = getSupportActionBar();

		mRGOptions = (RadioGroup) findViewById(R.id.radio_group_answers);
		mAnswerOptionRBs[0] = (RadioButton) findViewById(R.id.rb_answer1);
		mAnswerOptionRBs[1] = (RadioButton) findViewById(R.id.rb_answer2);
		mAnswerOptionRBs[2] = (RadioButton) findViewById(R.id.rb_answer3);
		mAnswerOptionRBs[3] = (RadioButton) findViewById(R.id.rb_answer4);

		mColorNotAnswered = getResources().getColor(R.color.not_answered);
		mColorCorrect = getResources().getColor(R.color.correct);
		mColorWrong = getResources().getColor(R.color.wrong);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	public void onDataUploaded(Flags flags, SharedPreferences pref, boolean startNewGame){
		this.mFlags = flags;
		mPref = pref;
		switchGameMode(mPref.getString(SettingsActivity.KEY_PREF_GAME_MODE, ""));

		findViewById(R.id.progressBar1).setVisibility(View.GONE);
		mFlagImage.setVisibility(View.VISIBLE);
		mSubmitButton.setEnabled(true);
		mNextButton.setEnabled(true);
		if(startNewGame){
			NumberPickerDialog dialogNumPicker = new NumberPickerDialog();
			dialogNumPicker.show(getSupportFragmentManager(), "NumberOfPlayersPicker");
		}else{
			showNextFlag(true);
			mScoreTextView.setText(String.format(getResources().getString(R.string.score), getCurrentPlayerNumber(), mModel.getScore()));
		}

		if(mModel != null && mModel.isAnswerShown()){
			showAnswer();
		}
	}

	public void onLanguageChanged(){
		if(mGameMode == GameMode.GAME_MODE_AUTOCOMPLETE_TEXT){
			ArrayAdapter<String> country_adapter = new ArrayAdapter<String>(this,
					R.layout.auto_text_item, mFlags.getCountryList());
			((AutoCompleteTextView) mCountryEditText).setAdapter(country_adapter);
			ArrayAdapter<String> capital_adapter = new ArrayAdapter<String>(this,
					R.layout.auto_text_item, mFlags.getCapitalList());
			((AutoCompleteTextView) mCapitalEditText).setAdapter(capital_adapter);
		}
	}

	public void switchGameMode(String gameModeStr){
		mGameMode = SettingsActivity.prefToGameMode(gameModeStr);
		Log.d(TAG, "switchGameMode " + mGameMode);
		switch (mGameMode) {
			case GAME_MODE_NORMAL:
				mCountryEditText = (EditText) findViewById(R.id.country);
				mCapitalEditText = (EditText) findViewById(R.id.capital);
				findViewById(R.id.country_auto_complete).setVisibility(View.GONE);
				findViewById(R.id.capital_auto_complete).setVisibility(View.GONE);
				mRGOptions.setVisibility(View.GONE);
				findViewById(R.id.country).setVisibility(View.VISIBLE);
				findViewById(R.id.capital).setVisibility(View.VISIBLE);
				mCountryEditText.requestFocus();
				mCountryEditText.setOnEditorActionListener(this);
				mCapitalEditText.setOnEditorActionListener(this);
				mSubmitButton.setText(getResources().getString(R.string.submit));
				break;

			case GAME_MODE_AUTOCOMPLETE_TEXT:
				mCountryEditText = (EditText) findViewById(R.id.country_auto_complete);
				mCapitalEditText = (EditText) findViewById(R.id.capital_auto_complete);
				findViewById(R.id.country).setVisibility(View.GONE);
				findViewById(R.id.capital).setVisibility(View.GONE);
				mRGOptions.setVisibility(View.GONE);
				findViewById(R.id.country_auto_complete).setVisibility(View.VISIBLE);
				findViewById(R.id.capital_auto_complete).setVisibility(View.VISIBLE);

				onLanguageChanged();
				mCountryEditText.requestFocus();
				mCountryEditText.setOnEditorActionListener(this);
				mCapitalEditText.setOnEditorActionListener(this);
				mSubmitButton.setText(getResources().getString(R.string.submit));
				break;
			case GAME_MODE_OPTIONS:
				mRGOptions.setVisibility(View.VISIBLE);
				findViewById(R.id.country_auto_complete).setVisibility(View.GONE);
				findViewById(R.id.capital_auto_complete).setVisibility(View.GONE);
				findViewById(R.id.country).setVisibility(View.GONE);
				findViewById(R.id.capital).setVisibility(View.GONE);
				setAnswerOptions(mModel.getCurrentAnswerType(), false);

				break;
		}
	}

	private int getFlagID(String flag){
		return getResources().getIdentifier(
				getPackageName()+":drawable/" + flag, null, null);
	}

	public void submit(){
		int score = 0;

		switch (mGameMode) {
			case GAME_MODE_NORMAL:
			case GAME_MODE_AUTOCOMPLETE_TEXT: // for both these modes the same handling
				int result = mFlags.checkAnswer(mCountryEditText.getText().toString(),
						mCapitalEditText.getText().toString());

				switch (result) {
					case Flags.ANSWER_BOTH_CORRECT:
						mCountryEditText.setTextColor(mColorCorrect);
						mCapitalEditText.setTextColor(mColorCorrect);
						score = 2;
						mSubmitButton.setEnabled(false);
						break;

					case Flags.ANSWER_CAPITAL_CORRECT:
						mCountryEditText.setTextColor(mColorWrong);
						mCapitalEditText.setTextColor(mColorCorrect);
						score = 1;
						break;

					case Flags.ANSWER_COUNTRY_CORRECT:
						mCapitalEditText.setTextColor(mColorWrong);
						mCountryEditText.setTextColor(mColorCorrect);
						score = 1;
						break;
					default:
						mCapitalEditText.setTextColor(mColorWrong);
						mCountryEditText.setTextColor(mColorWrong);
						score = 0;
						break;
				}
				mScoreTextView.setText(String.format(getResources().getString(R.string.score), getCurrentPlayerNumber(), mModel.addScore(score)));

				break;

			case GAME_MODE_OPTIONS:
				int pos=-1;
				for(int i = 0; i< mAnswerOptionRBs.length; i++){
					mAnswerOptionRBs[i].setTextColor(mColorNotAnswered);
					if(mAnswerOptionRBs[i].getId() == mRGOptions.getCheckedRadioButtonId()){
						pos = i;
					}
				}
				if(mFlags.getCorrectAnswerPos() == pos){
					score = 1;
					mSubmitButton.setEnabled(false);
					mAnswerOptionRBs[pos].setTextColor(mColorCorrect);
					mModel.setAnswerShown();
				}else{
					score = 0;
					mAnswerOptionRBs[pos].setTextColor(mColorWrong);
					if(ONE_ATTEMPT_IN_OPTION_MODE){
						showAnswer();
					}
				}
				mScoreTextView.setText(String.format(getResources().getString(R.string.score), getCurrentPlayerNumber(), mModel.addScoreOptionGameMode(score)));
				break;
		}


	}

	private void resetFields(){
		switch (mGameMode) {
			case GAME_MODE_NORMAL:
			case GAME_MODE_AUTOCOMPLETE_TEXT: // for both these modes the same handling
				mCountryEditText.setText("");
				mCountryEditText.setTextColor(mColorNotAnswered);
				mCapitalEditText.setText("");
				mCapitalEditText.setTextColor(mColorNotAnswered);

				break;
			case GAME_MODE_OPTIONS:
				for(RadioButton rb: mAnswerOptionRBs){
					rb.setTextColor(mColorNotAnswered);
				}
				mAnswerOptionRBs[0].setChecked(true);
				break;
		}
		mSubmitButton.setEnabled(true);

	}
	private void next(){
		Log.d(TAG, "next");
		if(mGameMode == GameMode.GAME_MODE_OPTIONS && mModel.getCurrentAnswerType() == AnswerType.COUNTRY){// show capitals
			resetFields();
			setAnswerOptions(Flags.AnswerType.CAPITAL, true);
		}else{
			resetFields();
			mModel.nextTurn();
			boolean nextFlagAvailable = showNextFlag(false);
			mScoreTextView.setText(String.format(getResources().getString(R.string.score), getCurrentPlayerNumber(), mModel.getScore()));
			if(mGameMode == GameMode.GAME_MODE_OPTIONS && nextFlagAvailable){
				setAnswerOptions(Flags.AnswerType.COUNTRY, true);
			}
		}
	}

	private void setAnswerOptions(Flags.AnswerType answerType, boolean isRequestNew){
		Log.d(TAG, "setAnswerOptions " + answerType);
		String[] answers = mModel.getAnswerOptions(NUM_OF_OPTIONS, answerType, isRequestNew);
		for(int i=0; i< NUM_OF_OPTIONS; i++){
			mAnswerOptionRBs[i].setText(answers[i]);
			mAnswerOptionRBs[i].postInvalidate();
		}
		mAnswerOptionRBs[0].setChecked(true);

		if(answerType == AnswerType.COUNTRY){
			mSubmitButton.setText(getResources().getString(R.string.country));
		}else{
			mSubmitButton.setText(getResources().getString(R.string.capital));
		}
	}

	private void showAnswer(){
		switch (mGameMode) {
			case GAME_MODE_NORMAL:
			case GAME_MODE_AUTOCOMPLETE_TEXT: // for both these modes the same handling
				mFlags.getCountry();
				mCountryEditText.setText(mFlags.getCountry());
				mCountryEditText.setTextColor(mColorNotAnswered);
				mCapitalEditText.setText(mFlags.getCapital());
				mCapitalEditText.setTextColor(mColorNotAnswered);

				break;
			case GAME_MODE_OPTIONS:
				if(!ONE_ATTEMPT_IN_OPTION_MODE){
					for(RadioButton rb: mAnswerOptionRBs){
						rb.setTextColor(mColorNotAnswered);
					}
				}
				mAnswerOptionRBs[mFlags.getCorrectAnswerPos()].setChecked(true);
				mAnswerOptionRBs[mFlags.getCorrectAnswerPos()].setTextColor(mColorCorrect);
				break;
		}
		mModel.setAnswerShown();
		mSubmitButton.setEnabled(false);
	}

	private boolean showNextFlag(boolean isShowCurrent){
		String flagSrc;
		if(isShowCurrent){
			flagSrc = mFlags.getFlag();
		}else{
			flagSrc = mFlags.getNextFlag();
		}
		if(flagSrc != null){
			mFlagImage.setImageResource(getFlagID(flagSrc));
			mActionBar.setSubtitle(getResources().getString(R.string.ab_subtitle_flag_position, mModel.getCurrentFlagPosition(), mModel.getNumberOfFlagsPerPlayer()));
			return true;
		}else{// No more flags. end the game
			mSubmitButton.setEnabled(false);
			mNextButton.setEnabled(false);

			DialogFragment gameOverDialog = new DialogFragment(){
				@Override
				public Dialog onCreateDialog(Bundle savedInstanceState) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle(R.string.game_over)
							//.setMessage(getResources().getString(R.string.game_over_msg) + getFinalScore())
							.setMessage(getFinalScore())
							.setPositiveButton(R.string.new_game, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									NumberPickerDialog dialogNumPicker = new NumberPickerDialog();
									dialogNumPicker.show(getSupportFragmentManager(), "NumberOfPlayersPicker");

								}
							})
							.setNegativeButton(R.string.cancel, null);
					return builder.create();
				}
			};

			gameOverDialog.show(getSupportFragmentManager(), "GameOverDialog");
			return false;
		}

	}

	private String getFinalScore() {
		int[] finalScore = mModel.getFinalScore();
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<finalScore.length; i++){
			sb.append(String.format(getResources().getString(R.string.final_score), (i+1), finalScore[i], mModel.getMaxScore()));
			sb.append("\n");
		}

		return sb.toString();
	}

	private void newGame(){
		mModel.newGame(mNumberOfPlayers);
		mNextButton.setEnabled(true);
		mScoreTextView.setText(String.format(getResources().getString(R.string.score), getCurrentPlayerNumber(), mModel.getScore()));
		resetFields();
		showNextFlag(true);
		if(mGameMode == GameMode.GAME_MODE_OPTIONS){
			setAnswerOptions(AnswerType.COUNTRY, true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.mi_show_answer:
				showAnswer();
				break;
			case R.id.mi_open_wiki:
				showAnswer();

				Intent intent = new Intent(this, AnswerActivity.class);
				intent.putExtra(AnswerActivity.URL, mFlags.getUrl());
				intent.putExtra(AnswerActivity.NAME, mFlags.getCountry() + ", " + mFlags.getCapital());
				startActivity(intent);
				break;
			case R.id.mi_new_game:
				DialogFragment newGameDialog = new DialogFragment(){
					@Override
					public Dialog onCreateDialog(Bundle savedInstanceState) {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setMessage(R.string.start_new_game)
								.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										NumberPickerDialog dialogNumPicker = new NumberPickerDialog();
										dialogNumPicker.show(getSupportFragmentManager(), "NumberOfPlayersPicker");
									}
								})
								.setNegativeButton(R.string.no, null);
						return builder.create();
					}
				};

				newGameDialog.show(getSupportFragmentManager(), "StartGameDialog");
				break;
			case R.id.mi_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if((v.getId() == R.id.capital || v.getId() == R.id.country
				|| v.getId() == R.id.capital_auto_complete || v.getId() == R.id.country_auto_complete)
				&& (event == null || event.getAction() == KeyEvent.ACTION_UP)){
			InputMethodManager imm= (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			if(v.getId() == R.id.country || v.getId() == R.id.country_auto_complete){
				mCapitalEditText.requestFocus();
			}
		}
		return true;
	}

	@Override
	protected void onPause() {
		if(mPref != null){
			Editor prefEditor = mPref.edit();
			prefEditor.putString(SettingsActivity.KEY_PREF_SCORE, mModel.scoreToString());
			prefEditor.putString(SettingsActivity.KEY_PREF_UNANSWERED_LIST, mFlags.getUnAnsweredListAsString());
			prefEditor.putInt(SettingsActivity.KEY_PREF_PLAYER_IDX, mModel.getCurrentPlayerIdx());
			prefEditor.putInt(SettingsActivity.KEY_PREF_LAST_ATTEMPT_SCORE, mModel.getLastAttemptScore());
			prefEditor.putBoolean(SettingsActivity.KEY_PREF_IS_ANSWER_SHOWN, mModel.isAnswerShown());
			prefEditor.putInt(SettingsActivity.KEY_PREF_CURRENT_NUMBER_OF_FLAGS, mModel.getNumberOfFlagsPerPlayer());
			prefEditor.putString(SettingsActivity.KEY_PREF_GAME_MODE, SettingsActivity.GameModeToPref(mGameMode));
			prefEditor.putString(SettingsActivity.KEY_PREF_CURRENT_ANSWER_TYPE, SettingsActivity.answerTypeToPref(mModel.getCurrentAnswerType()));
			prefEditor.apply();
		}
		super.onPause();
	}

	@Override
	public void onNumberSelected(int num) {
		mNumberOfPlayers = num;
		newGame();
	}

	private int getCurrentPlayerNumber(){
		return mModel.getCurrentPlayerIdx() + 1;
	}
}
