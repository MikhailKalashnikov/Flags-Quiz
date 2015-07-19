package mikhail.kalashnikov.flags;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class NumberPickerPreference extends DialogPreference {
	private static final int MIN_NUMBER = 1;
	private static final int MAX_NUMBER = 100;
	private static final int DEFAULT_VALUE = MIN_NUMBER;
	private TextView mNumberView;
	private int mCurrentNumber = MIN_NUMBER;
	
	public NumberPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		setDialogLayoutResource(R.layout.number_picker);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);
		setDialogIcon(null);
	}

	@Override
	protected void onBindDialogView(View view) {
		mNumberView = (TextView) view.findViewById(R.id.num_of_players);
		mNumberView.setText(String.valueOf(mCurrentNumber));
		
		view.findViewById(R.id.add_number).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mCurrentNumber < MAX_NUMBER){
					mCurrentNumber++;
					mNumberView.setText(String.valueOf(mCurrentNumber));
				}
			}
		});

		view.findViewById(R.id.subtract_number).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mCurrentNumber > MIN_NUMBER){
					mCurrentNumber--;
					mNumberView.setText(String.valueOf(mCurrentNumber));
				}
			}
		});
		super.onBindDialogView(view);
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult){
		if(positiveResult){
			persistInt(mCurrentNumber);
		}
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
	    final Parcelable superState = super.onSaveInstanceState();
	    // Check whether this Preference is persistent (continually saved)
	    if (isPersistent()) {
	        // No need to save instance state since it's persistent, use superclass state
	        return superState;
	    }

	    // Create instance of custom BaseSavedState
	    final SavedState myState = new SavedState(superState);
	    // Set the state's value with the class member that holds current setting value
	    myState.value = mCurrentNumber;
	    return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
	    // Check whether we saved the state in onSaveInstanceState
	    if (state == null || !state.getClass().equals(SavedState.class)) {
	        // Didn't save the state, so call superclass
	        super.onRestoreInstanceState(state);
	        return;
	    }

	    // Cast state to custom BaseSavedState and pass to superclass
	    SavedState myState = (SavedState) state;
	    super.onRestoreInstanceState(myState.getSuperState());
	    
	    // Set this Preference's widget to reflect the restored state
	    mCurrentNumber=myState.value;
	    mNumberView.setText(myState.value);
	}
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		if (restorePersistedValue) {
	        // Restore existing state
			mCurrentNumber = this.getPersistedInt(DEFAULT_VALUE);
	    } else {
	        // Set default state from the XML attribute
	    	mCurrentNumber = (Integer) defaultValue;
	        persistInt(mCurrentNumber);
	    }
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, DEFAULT_VALUE);
	}
	
	public int getValue(){
		return mCurrentNumber;
	}
	
	
	private static class SavedState extends BaseSavedState {
	    // Member that holds the setting's value
	    // Change this data type to match the type saved by your Preference
	    int value;

	    public SavedState(Parcelable superState) {
	        super(superState);
	    }

	    public SavedState(Parcel source) {
	        super(source);
	        // Get the current preference's value
	        value = source.readInt(); 
	    }

	    @Override
	    public void writeToParcel(Parcel dest, int flags) {
	        super.writeToParcel(dest, flags);
	        // Write the preference's value
	        dest.writeInt(value); 
	    }

	    // Standard creator object using an instance of this class
	    public static final Parcelable.Creator<SavedState> CREATOR =
	            new Parcelable.Creator<SavedState>() {

	        public SavedState createFromParcel(Parcel in) {
	            return new SavedState(in);
	        }

	        public SavedState[] newArray(int size) {
	            return new SavedState[size];
	        }
	    };
	}
}
