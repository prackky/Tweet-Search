package com.prakharapps.tweetsearch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.Arrays;

public class Tweet_Search extends AppCompatActivity {

    private SharedPreferences savedSearches;
    private TableLayout queryTablelayout;
    private EditText queryEditText;
    private EditText tagEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet__search);

        // get the SharedPreferences that contains the user's saved searches
        savedSearches = getSharedPreferences("searches", MODE_PRIVATE);

        // get a reference to the queryTableLayout
        queryTablelayout = (TableLayout) findViewById(R.id.queryTableLayout);

        // get refernces to the two edittexts and the Save Button
        queryEditText = (EditText) findViewById(R.id.queryEditText);
        tagEditText = (EditText) findViewById(R.id.tagEditText);

        // register listeners for the Save and Clear Tags buttons
        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(saveButtonListener);
        Button clearTagsButton = (Button) findViewById(R.id.clearTagsButton);
        clearTagsButton.setOnClickListener(clearTagsButtonListener);

        refreshButtons(null); // add previously saved searches to GUI
    }


    public View.OnClickListener saveButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(queryEditText.getText().length() > 0 &&
                    tagEditText.getText().length() > 0){
                makeTag(queryEditText.getText().toString(), tagEditText.getText().toString());
                queryEditText.setText("");
                tagEditText.setText("");

                // hide soft keyboard
                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                        tagEditText.getWindowToken(), 0);
            }
            else{
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(Tweet_Search.this);

                builder.setTitle(R.string.missingTitle);

                builder.setPositiveButton(R.string.OK, null);

                builder.setMessage(R.string.missingMessage);

                AlertDialog errorDialog = builder.create();
                errorDialog.show();
            }
        }
    };

    public View.OnClickListener clearTagsButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            AlertDialog.Builder builder = new AlertDialog.Builder(Tweet_Search.this);

            builder.setTitle(R.string.confirmTitle);

            builder.setPositiveButton(R.string.erase, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    clearButtons();

                    SharedPreferences.Editor preferenceEditor = savedSearches.edit();

                    preferenceEditor.clear();
                    preferenceEditor.apply();
                }
            });

            builder.setCancelable(true);
            builder.setNegativeButton(R.string.cancel, null);
            builder.setMessage(R.string.confirmMessage);

            AlertDialog confirmDialog = builder.create();
            confirmDialog.show();
        }
    };

    public View.OnClickListener queryButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String buttonText = ((Button)v).getText().toString();
            String query = savedSearches.getString(buttonText, null);

            String urlString =  getString(R.string.searchURL) + query;

            Intent getURL = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));

            startActivity(getURL);

        }
    };

    public View.OnClickListener editButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            TableRow buttonTableRow = (TableRow) v.getParent();
            Button searchButton = (Button) buttonTableRow.findViewById(R.id.newTagButton);

            String tag = searchButton.getText().toString();

            tagEditText.setText(tag);
            queryEditText.setText(savedSearches.getString(tag, null));
        }
    };

    //recreate search tag and edit buttons for all saved searches
    // pass null to create all the tag and edit buttons
    private void refreshButtons(String newTag) {

        // store saved tags in the tags array
        String[] tags =  savedSearches.getAll().keySet().toArray(new String[0]);
        Arrays.sort(tags, String.CASE_INSENSITIVE_ORDER); // sort by tag

        // if a new tag was added, insert in GUI at the appropriate location
        if (newTag != null){
            makeTagGUI(newTag, Arrays.binarySearch(tags, newTag));
        }
        else {
            for (int index = 0; index < tags.length; ++index)
                makeTagGUI(tags[index], index);
        }
    }

    private void makeTagGUI(String tag, int index) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newTagView = inflater.inflate(R.layout.new_tag_view, null);

        Button newTagButton = (Button) newTagView.findViewById(R.id.newTagButton);
        newTagButton.setText(tag);
        newTagButton.setOnClickListener(queryButtonListener);

        Button newEditButton = (Button) newTagView.findViewById(R.id.newEditButton);
        newEditButton.setText(R.string.edit);
        newEditButton.setOnClickListener(editButtonListener);

        queryTablelayout.addView(newTagView, index);
    }

    private void makeTag(String query, String tag){

        String  originalQuery =  savedSearches.getString(tag, null);

        SharedPreferences.Editor preferencesEditor = savedSearches.edit();
        preferencesEditor.putString(tag, query);
        preferencesEditor.apply();

        if (originalQuery == null){
            refreshButtons(tag);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tweet__search, menu);
        return true;
    }

    private void clearButtons(){
        queryTablelayout.removeAllViews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
