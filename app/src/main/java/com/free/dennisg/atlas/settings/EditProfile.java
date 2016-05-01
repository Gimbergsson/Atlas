package com.free.dennisg.atlas.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.free.dennisg.atlas.R;

/**
 * Created by Dennis on 2015-10-04.
 */
public class EditProfile extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        Spinner gender_spinner = (Spinner) findViewById(R.id.gender);
        ArrayAdapter<CharSequence> gender_adapter = ArrayAdapter.createFromResource(this, R.array.gender_options, R.layout.drop_down_list_1);
        gender_adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        gender_spinner.setAdapter(gender_adapter);


        Toast.makeText(EditProfile.this, gender_spinner.getSelectedItem().toString(),Toast.LENGTH_SHORT).show();
        /*
        Spinner birthday_spinner = (Spinner) findViewById(R.id.birthday);
        birthday_spinner.setOnItemClickListener(new View.setOnItemClickListener() {
            public void onClick(View v) {
                DatePickerDialog dateDlg = new DatePickerDialog(EditProfile.this,
                        new DatePickerDialog.OnDateSetListener() {

                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                Time chosenDate = new Time();
                                chosenDate.set(dayOfMonth, monthOfYear, year);
                                long dtDob = chosenDate.toMillis(true);
                                CharSequence strDate = DateFormat.format("MMMM dd, yyyy", dtDob);
                                Toast.makeText(EditProfile.this, "Date picked: " + strDate, Toast.LENGTH_SHORT).show();
                            }
                        }, 2011, 0, 1);

                dateDlg.setMessage("When's Your Birthday?");
                dateDlg.show();
            }
        });
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
