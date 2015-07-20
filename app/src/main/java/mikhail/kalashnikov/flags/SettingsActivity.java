package mikhail.kalashnikov.flags;

import mikhail.kalashnikov.flags.Flags.AnswerType;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity 
			implements OnSharedPreferenceChangeListener{
	final static String KEY_PREF_LANGUAGE = "pref_language";
	final static String KEY_PREF_SCORE = "pref_score";
	//final static String KEY_PREF_AUTO_COMPLETE_TEXT = "pref_auto_complete_text";
	final static String KEY_PREF_GAME_MODE = "pref_game_mode";
	final static String KEY_PREF_UNANSWERED_LIST = "pref_unanswered_list";
	final static String KEY_PREF_PLAYER_IDX = "pref_player_idx";
	final static String KEY_PREF_LAST_ATTEMPT_SCORE = "pref_last_attempt_score";
	final static String KEY_PREF_IS_ANSWER_SHOWN = "pref_is_answer_shown";
	final static String KEY_PREF_NUMBER_OF_FLAGS = "pref_number_of_flags";
	final static String KEY_PREF_CURRENT_NUMBER_OF_FLAGS = "pref_current_number_of_flags";
	final static String KEY_PREF_CURRENT_ANSWER_TYPE = "pref_current_answer_type";

	public enum GameMode{
		GAME_MODE_NORMAL,GAME_MODE_AUTOCOMPLETE_TEXT,GAME_MODE_OPTIONS
	};
	
	public static AnswerType prefToAnswerType(String str){
		if(str!=null || str.equals("CAPITAL")){
			return AnswerType.CAPITAL;
		}else{
			return AnswerType.COUNTRY;
		}
	}
	
	public static String answerTypeToPref(AnswerType answerType){
		switch (answerType) {
		case CAPITAL:
			return "CAPITAL";
		default:
			return "COUNTRY";
		}
	}
	
	public static GameMode prefToGameMode(String str){
		if(str==null){
			return GameMode.GAME_MODE_OPTIONS;
		}else if(str.equals("1")){
			return GameMode.GAME_MODE_NORMAL;
		}else if(str.equals("2")){
			return GameMode.GAME_MODE_AUTOCOMPLETE_TEXT;
		}else if(str.equals("3")){
			return GameMode.GAME_MODE_OPTIONS;
		}else {
			return GameMode.GAME_MODE_OPTIONS;
		}
	}
	
	public static String GameModeToPref(GameMode gameMode){
		switch (gameMode) {
		case GAME_MODE_NORMAL:
			return "1";
		case GAME_MODE_AUTOCOMPLETE_TEXT:
			return "2";
		case GAME_MODE_OPTIONS:
			return "3";
		default:
			return "3";
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		ListPreference langugePref = (ListPreference) findPreference(KEY_PREF_LANGUAGE);
		langugePref.setSummary(langugePref.getEntry());
		NumberPickerPreference numOfFlagsPref = (NumberPickerPreference) findPreference(KEY_PREF_NUMBER_OF_FLAGS);
		numOfFlagsPref.setSummary(String.valueOf(numOfFlagsPref.getValue()));
		
		ListPreference gameModePref = (ListPreference) findPreference(KEY_PREF_GAME_MODE);
		gameModePref.setSummary(gameModePref.getEntry());
	}
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals(KEY_PREF_LANGUAGE)){
			@SuppressWarnings("deprecation")
			ListPreference langugePref = (ListPreference) findPreference(KEY_PREF_LANGUAGE);
			langugePref.setSummary(langugePref.getEntry());
		}else if(key.equals(KEY_PREF_NUMBER_OF_FLAGS)){
			@SuppressWarnings("deprecation")
			NumberPickerPreference numOfFlagsPref = (NumberPickerPreference) findPreference(KEY_PREF_NUMBER_OF_FLAGS);
			numOfFlagsPref.setSummary(String.valueOf(numOfFlagsPref.getValue()));
		}else if(key.equals(KEY_PREF_GAME_MODE)){
			@SuppressWarnings("deprecation")
			ListPreference gameModePref = (ListPreference) findPreference(KEY_PREF_GAME_MODE);
			gameModePref.setSummary(gameModePref.getEntry());
		}
		
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}
}
