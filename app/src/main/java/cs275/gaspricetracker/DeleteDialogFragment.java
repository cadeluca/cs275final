package cs275.gaspricetracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;


public class DeleteDialogFragment extends DialogFragment {
    private static final String ARG_DELETE = "true";
    public static final String EXTRA_DELETE = "jtan5.exampe.criminalintent2.delete";
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle("Are you sure to delete?")
                .setNegativeButton("delete",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                sendResult(Activity.RESULT_OK, null);
                            }
                        })
                .setPositiveButton("cancel",
                        new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                sendResult(Activity.RESULT_CANCELED, null);
                            }
                        }
                )
                .create();
    }
    private void sendResult(int resultCode, String string){
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DELETE, string);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }

}