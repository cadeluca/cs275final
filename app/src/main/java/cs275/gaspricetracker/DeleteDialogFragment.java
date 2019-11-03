package cs275.gaspricetracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;


public class DeleteDialogFragment extends DialogFragment {
    public static final String EXTRA_DELETE = "delete";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_question)
                .setNegativeButton(R.string.dialog_delete,
                        (dialogInterface, i) -> sendResult(Activity.RESULT_OK, null))
                .setPositiveButton(R.string.dialog_cancel,
                        (dialog, i) -> sendResult(Activity.RESULT_CANCELED, null)
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