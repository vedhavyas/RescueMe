package org.rescueme;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RescueMeContacts extends Fragment {


    private Context context;

    public RescueMeContacts() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_contacts, container, false);
        context = rootView.getContext();
        ListView contactsListView = (ListView) rootView.findViewById(R.id.circlesListView);

        List<RescueMeUserModel> emergencyContacts = getContacts();

        RescueMeContactListAdapter contactsAdapter = new RescueMeContactListAdapter(context, emergencyContacts);
        contactsListView.setAdapter(contactsAdapter);

        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RescueMeUserModel contact = (RescueMeUserModel) parent.getItemAtPosition(position);
                startUtilActivity(contact.getId());
            }
        });

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.rescue_me_actionbar_menu, menu);
        MenuItem add = menu.findItem(R.id.add_circle_contact);
        MenuItem logout = menu.findItem(R.id.action_logout);
        add.setVisible(true);
        logout.setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.fbContacts) {
            RescueMeUtilClass.toastAndLog(context, "Coming soon!!");
        } else if (itemId == R.id.localContacts) {
            RescueMeUtilClass.writeToLog("Pick from Local Contacts!!");
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, RescueMeConstants.PICK_CONTACT_FROM_LOCAL);
        }
        return super.onOptionsItemSelected(item);
    }

    private void startUtilActivity(String extraData) {
        Intent intent = new Intent(context, RescueMeUtilActivity.class);
        if (extraData == null) {
            intent.putExtra(RescueMeConstants.FRAGMENT_TAG, RescueMeConstants.NEW_EMERGENCY_CONTACT);
        } else {
            intent.putExtra(RescueMeConstants.FRAGMENT_TAG, RescueMeConstants.UPDATE_EMERGENCY_CONTACT);
            intent.putExtra(RescueMeConstants.COLUMN_ID, extraData);
        }
        startActivity(intent);
    }

    private List<RescueMeUserModel> getContacts() {
        RescueMeDBFactory dbFactory = RescueMeDBFactory.getInstance(context);
        dbFactory.setTableName(RescueMeConstants.CONTACTS_TABLE);
        return dbFactory.getAllContacts();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RescueMeConstants.PICK_CONTACT_FROM_LOCAL &&
                resultCode == Activity.RESULT_OK) {
            pickContactFromContacts(data);
        }
    }

    private void pickContactFromContacts(Intent data) {
        String name, contactId;
        List<String> numbersList = new ArrayList<String>();
        List<String> emailsList = new ArrayList<String>();
        Uri contactData = data.getData();
        Cursor cursor = context.getContentResolver().query(contactData, null, null, null, null);
        RescueMeUserModel contact = new RescueMeUserModel();

        if (cursor.moveToFirst()) {
            contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

            RescueMeUtilClass.writeToLog(contactId + " -- " + name);
            contact.setName(name);

            Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);

            while (phones.moveToNext()) {
                if (!numbersList.contains(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)))) {
                    numbersList.add(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                }
            }
            phones.close();

            Cursor emails = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null);
            while (emails.moveToNext()) {
                if (!emailsList.contains(emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)))) {
                    emailsList.add(emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
                }
            }
            emails.close();

            String contactPicUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
            if (contactPicUri != null) {
                RescueMeUtilClass.writeToLog(Uri.parse(contactPicUri).toString());
                try {
                    Bitmap bitmap = MediaStore.Images.Media
                            .getBitmap(context.getContentResolver(),
                                    Uri.parse(contactPicUri));
                    contact.setProfilePic(RescueMeUtilClass.getBlob(bitmap));
                    RescueMeUtilClass.writeToLog("Got the Blob!!");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (numbersList.size() > 0 && emailsList.size() > 0) {
                if (numbersList.size() > 1 && !(emailsList.size() > 1)) {
                    contact.setEmail(emailsList.get(0));
                    getChoiceFromUser(numbersList, null, RescueMeConstants.PICK_NUMBER, null, contact);
                } else if (!(numbersList.size() > 1) && (emailsList.size() > 1)) {
                    contact.setNumber(numbersList.get(0));
                    getChoiceFromUser(emailsList, null, RescueMeConstants.PICK_EMAIL, null, contact);
                } else if (numbersList.size() > 1 && (emailsList.size() > 1)) {
                    getChoiceFromUser(numbersList, emailsList, RescueMeConstants.PICK_NUMBER, RescueMeConstants.PICK_EMAIL, contact);
                } else {
                    contact.setEmail(emailsList.get(0));
                    contact.setNumber(numbersList.get(0));
                    putContactIntoDB(contact);
                }

            } else {
                if (numbersList.size() > 0 && !(emailsList.size() > 0)) {
                    if (numbersList.size() > 1) {
                        getChoiceFromUser(numbersList, null, RescueMeConstants.PICK_NUMBER, null, contact);
                    } else {
                        contact.setNumber(numbersList.get(0));
                        putContactIntoDB(contact);
                    }
                } else if (emailsList.size() > 0 && !(numbersList.size() > 0)) {
                    if (emailsList.size() > 1) {
                        getChoiceFromUser(emailsList, null, RescueMeConstants.PICK_EMAIL, null, contact);
                    } else {
                        contact.setEmail(emailsList.get(0));
                        putContactIntoDB(contact);
                    }
                }
            }

        }

        cursor.close();
    }

    private void getChoiceFromUser(List<String> data, final List<String> data2, final String title1, final String title2, final RescueMeUserModel contact) {
        String[] list = new String[data.size()];
        list = data.toArray(list);
        final String[] finalList = list;
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title1);
        dialog.setIcon(R.drawable.ic_launcher);
        dialog.setItems(list, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (title1.equalsIgnoreCase(RescueMeConstants.PICK_NUMBER)) {
                    RescueMeUtilClass.writeToLog("Chose Number - " + finalList[i]);
                    contact.setNumber(finalList[i]);
                } else if (title1.equalsIgnoreCase(RescueMeConstants.PICK_EMAIL)) {
                    RescueMeUtilClass.writeToLog("Chose Email - " + finalList[i]);
                    contact.setEmail(finalList[i]);
                }

                if (data2 == null) {
                    putContactIntoDB(contact);
                } else {
                    getChoiceFromUser(data2, null, title2, null, contact);
                }
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    private void putContactIntoDB(RescueMeUserModel contact) {
        RescueMeDBFactory dbFactory = RescueMeDBFactory.getInstance(context);
        dbFactory.setTableName(RescueMeConstants.CONTACTS_TABLE);
        if (dbFactory.addEmergencyContact(contact) > 0) {
            RescueMeUtilClass.toastAndLog(context, "Contact added to the List!!");
            loadAuthenticatedActivity();
        } else {
            RescueMeUtilClass.toastAndLog(context, "Failed to add the Contact!!");
        }
    }

    private void loadAuthenticatedActivity() {
        RescueMeUtilClass.writeToLog("Starting Authenticated activity!!");
        Intent intent = new Intent(context, RescueMeTabViewer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(RescueMeConstants.SELECT_TAG, 1);
        startActivity(intent);
    }
}
