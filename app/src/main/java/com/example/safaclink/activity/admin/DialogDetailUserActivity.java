package com.example.safaclink.activity.admin;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.safaclink.R;
import com.example.safaclink.apiserver.ApiServer;
import com.squareup.picasso.Picasso;

public class DialogDetailUserActivity extends AppCompatDialogFragment {
    private Context context;
    private TextView textNama, textRole, textNohp, textEmail, textUsername;
    private ImageView imgProfile;
    private String nama, nohp, email, username, profile, role;

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_dialog_detail_user, null);

        textNama = view.findViewById(R.id.textNama);
        textNohp = view.findViewById(R.id.textNohp);
        textRole = view.findViewById(R.id.textRole);
        textEmail = view.findViewById(R.id.textEmail);
        textUsername = view.findViewById(R.id.textUsername);
        imgProfile = view.findViewById(R.id.imgProfile);

        Bundle arguments = getArguments();
        if (arguments != null) {
            nama = arguments.getString("nama");
            nohp = arguments.getString("nohp");
            email = arguments.getString("email");
            username = arguments.getString("username");
            role = arguments.getString("role");
            profile = arguments.getString("profile");

            if (profile == null || profile.equals("null")) {
                imgProfile.setImageResource(R.mipmap.icon_user_foreground);
            } else {
                Picasso.get()
                        .load(ApiServer.site_url_fotoProfile + profile)
                        .into(imgProfile);
            }

            textNama.setText(nama);
            textNohp.setText(nohp);
            textEmail.setText(email);
            textUsername.setText(username);
            textRole.setText(role);
        }


        builder.setView(view)
                .setTitle("Detail User")
                .setPositiveButton("Oke", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return builder.create();
    }
}