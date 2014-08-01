package mikhail.kalashnikov.flags;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

public class NumberPickerDialog extends DialogFragment {
	private final int MIN_NUMBER = 1;
	private final int MAX_NUMBER = 100;
	private TextView mNumberView;
	private int mCurrentNumber = MIN_NUMBER;
	
	public interface NumberPickerListener{
		public void onNumberSelected(int num);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View view = getActivity().getLayoutInflater().inflate(R.layout.number_picker, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
		
		builder.setTitle(R.string.number_of_players_title).setView(view)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try{
						((NumberPickerListener) getActivity()).onNumberSelected(mCurrentNumber);
					} catch (ClassCastException e) {
			            throw new ClassCastException(getActivity().toString()
			                    + " must implement NumberPickerListener");
			        }
					
				}
			});
		
		return builder.create();
	}
	
}
