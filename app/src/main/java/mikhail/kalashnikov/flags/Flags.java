package mikhail.kalashnikov.flags;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.StringTokenizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Flags {
	private final String TAG = getClass().getSimpleName();
	public static final int ANSWER_BOTH_WRONG = 0;
	public static final int ANSWER_COUNTRY_CORRECT = 1;
	public static final int ANSWER_CAPITAL_CORRECT = 2;
	public static final int ANSWER_BOTH_CORRECT = 3;
	private static final String SPLIITER = ",";
	private final String[] LANGUAGE_LIST = new String[]{"EN","RU"};//we use upper case here because in json file we use uppercase suffix
	private Map<String, List<CountryInfo>> mCountriesPerLang;
	private Queue<Integer> mUnAnswered;
	private int mCurrentCountryIdx;
	private Random mRandom;
	private String mLanguage;
	private boolean mIsStartNewGame;
	private int mCorrectAnswerPos;
	private String[] mAnswerOptions;
	public enum AnswerType{
		COUNTRY, CAPITAL
	}
	
	public Flags(InputStream inStream, String language, String unAnsweredListStr) throws IOException, JSONException{
		mLanguage = language;
		mRandom = new Random(System.currentTimeMillis());
		parse(inStream);
		
		if(unAnsweredListStr==null || unAnsweredListStr.trim().length() == 0){
			mIsStartNewGame = true;
		}else{
			parseUnAnsweredList(unAnsweredListStr);
			mIsStartNewGame = false;
			mCurrentCountryIdx = mUnAnswered.peek();
		}
	}

	public void setLanguage(String language){
		mLanguage = language;
	}
	
	public boolean isStartNewGame(){
		return mIsStartNewGame;
	}
	
	public String getNextFlag(){
		mUnAnswered.remove();// delete current flag from unanswered list
		nextIdx();
		if(mCurrentCountryIdx == -1){
			return null;
		}else{
			return (mCountriesPerLang.get(mLanguage).get(mCurrentCountryIdx).flag);
		}
	}
	
	public String getFlag(){
		if(mCurrentCountryIdx == -1){
			return null;
		}else{
			return (mCountriesPerLang.get(mLanguage).get(mCurrentCountryIdx).flag);
		}		
	}
	
	public List<String> getCountryList(){
		List<String> list = new ArrayList<String>(mCountriesPerLang.get(mLanguage).size());
		for(CountryInfo ci : mCountriesPerLang.get(mLanguage)){
			list.add(ci.country);
		}
		return list;
	}
	
	public List<String> getCapitalList(){
		List<String> list = new ArrayList<String>(mCountriesPerLang.get(mLanguage).size());
		for(CountryInfo ci : mCountriesPerLang.get(mLanguage)){
			list.add(ci.capital);
		}
		return list;
	}
	
	private void nextIdx(){
		if(mUnAnswered.size() > 0){
			mCurrentCountryIdx = mUnAnswered.peek();
		}else{
			mCurrentCountryIdx = -1;
		}
	}

	public String getUrl(){
		return (mCountriesPerLang.get(mLanguage).get(mCurrentCountryIdx).wiki);
	}
	
	public String getCountry(){
		return (mCountriesPerLang.get(mLanguage).get(mCurrentCountryIdx).country);
	}
	public String getCapital(){
		return (mCountriesPerLang.get(mLanguage).get(mCurrentCountryIdx).capital);
	}
	
	private boolean checkText(String answer, String correctAnswer){
		if(answer != null && answer.length() > 0
				&& (answer.equalsIgnoreCase(correctAnswer)
						|| (correctAnswer.toLowerCase(Locale.getDefault()).startsWith("the ") 
								&& answer.equalsIgnoreCase(correctAnswer.substring(4)))
						|| (answer.equalsIgnoreCase(correctAnswer.replace(" City", ""))))){
			return true;
		}
		return false;
	}
	
	private boolean checkCountry(String country){
		String correctCountry = mCountriesPerLang.get(mLanguage).get(mCurrentCountryIdx).country;
		String correctCountry2 = mCountriesPerLang.get(mLanguage).get(mCurrentCountryIdx).country2;
		return checkText(country.trim(), correctCountry.trim()) || checkText(country.trim(), correctCountry2.trim());
	}
	
	private boolean checkCapital(String capital){
		String correctCapital = mCountriesPerLang.get(mLanguage).get(mCurrentCountryIdx).capital;
		String correctCapital2 = mCountriesPerLang.get(mLanguage).get(mCurrentCountryIdx).capital2;
		return checkText(capital.trim(), correctCapital.trim()) || checkText(capital.trim(), correctCapital2.trim());
	}
	
	public int checkAnswer(String country, String capital){
		int result = ANSWER_BOTH_WRONG;
		if(checkCountry(country)){
			result = result + ANSWER_COUNTRY_CORRECT;
		}
		
		if(checkCapital(capital)){
			result = result + ANSWER_CAPITAL_CORRECT;
		}
		
		return result;
	}
	
	private JSONObject openFileToJSON(InputStream jsonFile) throws IOException, JSONException{
		BufferedReader br = new BufferedReader(new InputStreamReader(jsonFile));
		StringBuffer buf = new StringBuffer();
		
		String nextLine=null;
		while((nextLine=br.readLine()) != null){
			buf.append(nextLine);
		}
		br.close();
		return new JSONObject(buf.toString());
	}
	
	private void parse(InputStream jsonFile) throws IOException, JSONException{
		mCountriesPerLang = new HashMap<String, List<CountryInfo>>();
		for(String lang: LANGUAGE_LIST){
			mCountriesPerLang.put(lang, new ArrayList<CountryInfo>());
		}
		JSONObject raw = openFileToJSON(jsonFile);
		JSONArray countries = raw.optJSONArray("countries");
		for(int i=0; i<countries.length(); i++){
			JSONObject country = countries.optJSONObject(i);
			for(String lang: LANGUAGE_LIST){
				mCountriesPerLang.get(lang).add(new CountryInfo(country, lang));
			}
		}
		
	}
	
	private void reSetUnWatchedList(int mNumberOfFlags){
		//TODO mNumberOfFlags should not be more than flags.size()
		mUnAnswered = new LinkedList<Integer>();
		for(int i=0; i<mNumberOfFlags; i++){
			int pos;
			do{
				pos = mRandom.nextInt(mCountriesPerLang.get(mLanguage).size());

			}while(mUnAnswered.contains(pos));
			
			mUnAnswered.add(pos);
		}
	}

	public void newGame(int mNumberOfFlags) {
		reSetUnWatchedList(mNumberOfFlags);
		mIsStartNewGame = false;
		nextIdx();
	}
	
	public String getUnAnsweredListAsString(){
		StringBuilder sb = new StringBuilder();
		for(int i: mUnAnswered){
			sb.append(i);
			sb.append(SPLIITER);
		}
		
		return sb.toString();
	}
	
	private void parseUnAnsweredList(String strList){
		mUnAnswered = new LinkedList<Integer>();
		StringTokenizer st = new StringTokenizer(strList, SPLIITER);
		while(st.hasMoreTokens()){
			mUnAnswered.add(Integer.parseInt(st.nextToken()));
		}
	}
	
	public int getFlagsLeft(){
		return mUnAnswered.size();
	}
	
	public String[] getAnswerOptions(int numOfOptions, AnswerType answerType, boolean isRequestNew){
		if(mAnswerOptions == null || isRequestNew){
			mAnswerOptions = new String[numOfOptions];
			List<Integer> pos = new ArrayList<Integer>(numOfOptions);
			mCorrectAnswerPos = mRandom.nextInt(numOfOptions);
			if(answerType == AnswerType.COUNTRY){
				mAnswerOptions[mCorrectAnswerPos] = mCountriesPerLang.get(mLanguage).get(mCurrentCountryIdx).country;
			}else {
				mAnswerOptions[mCorrectAnswerPos] = mCountriesPerLang.get(mLanguage).get(mCurrentCountryIdx).capital;
			}
			pos.add(mCurrentCountryIdx);
			for(int i = 0; i < numOfOptions; i++){
				if(i != mCorrectAnswerPos){
					int randPos;
					do{
						randPos = mRandom.nextInt(mCountriesPerLang.get(mLanguage).size());
					}while(pos.contains(randPos));
					
					if(answerType == AnswerType.COUNTRY){
						mAnswerOptions[i] = mCountriesPerLang.get(mLanguage).get(randPos).country;
					}else{ 
						mAnswerOptions[i] =  mCountriesPerLang.get(mLanguage).get(randPos).capital;
					}
					pos.add(randPos);
				}
			}
		}
		Log.d(TAG, "mCorrectAnswerPos="+mCorrectAnswerPos);
		Log.d(TAG, "options="+ Arrays.toString(mAnswerOptions));
		return mAnswerOptions;
	}
	
	public int getCorrectAnswerPos(){
		return mCorrectAnswerPos;
	}
	
	class CountryInfo{
		String country;
		String country2;
		String capital;
		String capital2;
		String flag;
		String wiki;
		
		public CountryInfo(JSONObject jsonObject, String lang) {
			country = jsonObject.optString("country" + lang);
			country2 = jsonObject.optString("country2" + lang);
			capital = jsonObject.optString("capital" + lang);
			capital2 = jsonObject.optString("capital2" + lang);
			flag = jsonObject.optString("flag");
			wiki = jsonObject.optString("wiki" + lang);
		}
		
		@Override
		public String toString() {
			return "CountryInfo [country=" + country + ", capital=" + capital
					+ ", country2=" + country2 + ", capital2=" + capital2
					+ ", flag=" + flag + ", wiki=" + wiki 
					+ "]" + System.getProperty("line.separator");
		} 
		
	}

}
