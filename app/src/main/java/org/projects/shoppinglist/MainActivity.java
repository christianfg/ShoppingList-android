package org.projects.shoppinglist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;



public class MainActivity extends AppCompatActivity implements clearAllFragment.OnPositiveListener {

    private static final String TAG = "com.example.StateChange";

    private String[] spinnerNum = { "0", "1", "2", "3", "4",
            "5", "6", "7", "8", "9" };

    ArrayAdapter<Product> adapter;
    FirebaseListAdapter<Product> fAdapter;
    ListView listView;
    ArrayList<Product> bag = new ArrayList<Product>();
    static clearAllFragment dialog;
    static Context context;

    //SNACKBAR DELETE
    Product backup;
    int currentDel;

    public DatabaseReference firebase;

    public FirebaseListAdapter<Product> getMyAdapter()
    {
        return fAdapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;

        //FIREBASE
        firebase = FirebaseDatabase.getInstance().getReference().child("items");


        if (savedInstanceState!=null)
        {
            bag = savedInstanceState.getParcelableArrayList("savedList");
        }
        //getting our listiew - you can check the ID in the xml to see that it
        //is indeed specified as "list"
        listView = (ListView) findViewById(R.id.list);
        //here we create a new adapter linking the bag and the
        //listview
        adapter =  new ArrayAdapter<Product>(this,android.R.layout.simple_list_item_checked,bag );
        fAdapter = new FirebaseListAdapter<Product>(this, Product.class, android.R.layout.simple_list_item_checked, firebase) {
            @Override
            protected void populateView(View view, Product product, int i) {
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(product.toString());
            }
        };

        //setting the adapter on the listview
        listView.setAdapter(fAdapter);
        //here we set the choice mode - meaning in this case we can
        //only select one item at a time.
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        //EDITTEXT
        final EditText editAdd =  (EditText)findViewById(R.id.editAdd);
        final EditText editQty =  (EditText)findViewById(R.id.editQty);

        //ACTIONBAR
        getSupportActionBar().setHomeButtonEnabled(true);




        //SETTINGS FRAGMENT
        String name = SettingsFragment.getName(this);
        updateSettings(name);

        //WELCOME BACK
        String message = "Welcome back "+name;
        Toast toast = Toast.makeText(this,message,Toast.LENGTH_LONG);
        toast.show();

        //SHOW OR HIDE NAME
        boolean show = SettingsFragment.isHide(this);
        if (show)
        {
            TextView showName = (TextView)findViewById(R.id.welcome);
            showName.setText("Welcome to my shopping list "+name);
        }


        //SPINNERS
        final Spinner QtySpinner = (Spinner) findViewById(R.id.QtySpinner);
        ArrayAdapter<String> sAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerNum);
        QtySpinner.setAdapter(sAdapter);

        QtySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //The AdapterView<?> type means that this can be any type,
            //so we can use both AdapterView<String> or any other
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                //So this code is called when ever the spinner is clicked
                //Brug position til at populate editQty med indhold fra QtySpinner
                editQty.setText("");
                editQty.clearFocus();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // for instace setting the selected item to "null"
                // or something.

            }
        });

        //This line of code can always be used to get the
        //selected position in in the spinner - the first item
        //will have an index of 0.
        int position = QtySpinner.getSelectedItemPosition();

        //This line will get the actual seleted item -
        //in our case the values in the spinner is simply
        //strings, so we need to make a cast to a String
        String item = (String) QtySpinner.getSelectedItem();



        Button addButton = (Button) findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editQty.getText().toString() == null) {
                    Product p = new Product(editAdd.getText().toString(), Integer.parseInt(QtySpinner.getSelectedItem().toString()));
                    firebase.push().setValue(p);
                    getMyAdapter().notifyDataSetChanged();
                }
                else if (QtySpinner.getSelectedItem() == "0" ) {
                    Product p = new Product(editAdd.getText().toString(), Integer.parseInt(editQty.getText().toString()));
                    firebase.push().setValue(p);
                    getMyAdapter().notifyDataSetChanged();
                }
                else {
                    Product p = new Product(editAdd.getText().toString(), Integer.parseInt(QtySpinner.getSelectedItem().toString()));
                    firebase.push().setValue(p);
                    getMyAdapter().notifyDataSetChanged();
                }
                //bag.add(new Product(editAdd.getText().toString(), Integer.parseInt(editQty.getText().toString())));
                //bag.add(new Product(editAdd.getText().toString(), Integer.parseInt(QtySpinner.getSelectedItem().toString())));
                //bag.add(editQty.getText().toString() + " " + editAdd.getText().toString());

                //The next line is needed in order to say to the ListView
                //that the data has changed - we have added stuff now!
                //getMyAdapter().notifyDataSetChanged();
            }
        });


        //BUTTONS
        Button deleteButton = (Button) findViewById(R.id.delButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                /*
                for (int i = adapter.getCount() - 1; i >= 0; i--) {
                    if (checkedItems.get(i)) {
                        // This item is checked and can be removed
                        adapter.remove(adapter.getItem(i));
                    }
                }
                */
                int position = listView.getCheckedItemPosition();

                final Product backup = getMyAdapter().getItem(position);

                if(listView.isItemChecked(position)) {
                    getMyAdapter().getRef(position).setValue(null);
                }

                Snackbar snackbar = Snackbar
                        .make(listView, "Product deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                firebase.push().setValue(backup);

                                Snackbar snackbar = Snackbar.make(listView, "Product restored!", Snackbar.LENGTH_LONG);

                                //show
                                snackbar.show();
                            }
                        });

                snackbar.show();
            }
        });




        Button clearButton = (Button) findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new MyDialog();
                dialog.show(getFragmentManager(), "MyFragment");
            }
        });
    }

    @Override
    public void onPositiveClicked() {
        //Do your update stuff here to the listview
        //and the bag etc
        //just to show how to get arguments from the bag.
        Toast toast = Toast.makeText(context,
                "Bag is now empty", Toast.LENGTH_LONG);

        firebase.setValue(null);
        //adapter.clear();
        getMyAdapter().notifyDataSetChanged();
        toast.show();
    }

    public static class MyDialog extends clearAllFragment {
        @Override
        protected void negativeClick() {
            //Here we override the method and can now do something

        }
    }

    //UPDATE SETTINGS FOR USER BASED ON OWN INPUT
    public void updateSettings(String name)
    {
        //SET TEXTVIEWS
        /*
        TextView nameOfView1 = (TextView) findViewById(R.id.nameOfView);
		TextView nameOfView2 = (TextView) findViewById(R.id.nameOfView);
		nameOfView1.setText(name);
		if (boolean value true)
			nameOfView2.setText("");
		else
			nameOfView2.setText("");
         */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //SETTINGS FRAGMENT RETURN OVERRIDE
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==1) //the code means we came back from settings
        {
            //I can can these methods like this, because they are static
            String name = SettingsFragment.getName(this);
            String message = "Name set to "+name;
            Toast toast = Toast.makeText(this,message,Toast.LENGTH_LONG);
            toast.show();
            updateSettings(name);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case R.id.clear_basket:
                dialog = new MyDialog();
                dialog.show(getFragmentManager(), "MyFragment");
                return true;

            case R.id.shareText:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);

                int countList = listView.getCount();
                String share = "";

                for (int i = countList - 1; i >= 0; i--) {
                    share = fAdapter.getItem(i)+ ", "+share;
                }

                sendIntent.putExtra(Intent.EXTRA_TEXT, share);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);



                return true;

            case R.id.action_settings:
                Intent i = new Intent(this,SettingsActivity.class);
                startActivityForResult(i, 1);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    //This method is called before our activity is destoryed
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //ALWAYS CALL THE SUPER METHOD - To be nice!
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
		/* Here we put code now to save the state */
        outState.putParcelableArrayList("savedList",bag);

    }

}
