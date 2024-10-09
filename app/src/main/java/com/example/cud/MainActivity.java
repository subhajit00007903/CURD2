package com.example.cud;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText editTextName, editTextPhone;
    private Button buttonAdd, buttonUpdate, buttonDelete, buttonRead;
    private TextView textViewResult;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonRead = findViewById(R.id.buttonRead);
        textViewResult = findViewById(R.id.textViewResult);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        buttonAdd.setOnClickListener(view -> addUser());
        buttonUpdate.setOnClickListener(view -> updateUser());
        buttonDelete.setOnClickListener(view -> deleteUser());
        buttonRead.setOnClickListener(view -> readUser());
    }

    private void addUser() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = databaseReference.push().getKey();
        User user = new User(name, phone);
        if (id != null) {
            databaseReference.child(id).setValue(user)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "User added", Toast.LENGTH_SHORT).show();
                            editTextName.setText("");
                            editTextPhone.setText("");
                        } else {
                            Toast.makeText(this, "Failed to add user", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void readUser() {
        databaseReference.orderByChild("name").equalTo(editTextName.getText().toString().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                    if (user != null) {
                        textViewResult.setText("Name: " + user.getName() + "\nPhone: " + user.getPhone());
                    }
                } else {
                    Toast.makeText(MainActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUser() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);

        databaseReference.orderByChild("name").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        userSnapshot.getRef().updateChildren(updates)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "User updated", Toast.LENGTH_SHORT).show();
                                        textViewResult.setText("Name: " + name + "\nPhone: " + phone); // Optional: Update TextView to show updated info
                                    } else {
                                        Toast.makeText(MainActivity.this, "Failed to update user", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No user found with that name", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUser() {
        String nameToDelete = editTextName.getText().toString().trim();

        if (nameToDelete.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Search for the user by name
        databaseReference.orderByChild("name").equalTo(nameToDelete).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Loop through all matching users and delete them
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        userSnapshot.getRef().removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "User deleted", Toast.LENGTH_SHORT).show();
                                        textViewResult.setText(""); // Clear the TextView
                                        editTextName.setText("");   // Clear the EditText field
                                        editTextPhone.setText("");  // Clear the EditText field
                                    } else {
                                        Toast.makeText(MainActivity.this, "Failed to delete user", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No user found with that name", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
