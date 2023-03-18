package uth.pmo1.agenda;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import uth.pmo1.agenda.RestApiMethods.Methods;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
public class CreateActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    static final int REQUEST_IMAGE = 101;
    static final int PETICION_ACCESS_CAM = 201;
    static final int Result_galeria = 101;
    ImageView fotografia;
    Button btnguardar;
    ImageButton btnfoto;
    String POSTMethod, currentPath, currentPhotoPath;
    EditText nombre, apellido, telefono, latitud, longitud;
    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        fotografia = (ImageView) findViewById(R.id.imageView);
        btnfoto = (ImageButton) findViewById(R.id.btnfoto);
        btnguardar = (Button) findViewById(R.id.btnguardar);
        nombre = (EditText) findViewById(R.id.txtnombre);
        apellido = (EditText) findViewById(R.id.txtapellido);
        telefono = (EditText) findViewById(R.id.txttelefono);
        latitud = (EditText) findViewById(R.id.txtlatitud);
        longitud = (EditText) findViewById(R.id.txtlongitud);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        btnfoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permisos();
            }
        });

        btnguardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ConsumeCreateApi();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        this.mMap.setOnMapClickListener(this);
        this.mMap.setOnMapLongClickListener(this);

        LatLng honduras = new LatLng(13.3389685,-87.1329178);
        mMap.addMarker(new MarkerOptions().position(honduras).title("Posición actual"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(honduras));

    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        latitud.setText("" + latLng.latitude);
        longitud.setText(""+latLng.longitude);

        mMap.clear();
        LatLng honduras = new LatLng(latLng.latitude,latLng.longitude);
        mMap.addMarker(new MarkerOptions().position(honduras).title(""));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(honduras));
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        latitud.setText("" + latLng.latitude);
        longitud.setText(""+latLng.longitude);

        mMap.clear();
        LatLng honduras = new LatLng(latLng.latitude,latLng.longitude);
        mMap.addMarker(new MarkerOptions().position(honduras).title(""));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(honduras));
    }

    private void permisos(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},PETICION_ACCESS_CAM);
        }
        else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PETICION_ACCESS_CAM) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            }
            else {
                Toast.makeText(getApplicationContext(), "Se necesita el permiso de la camara",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {

            if (currentPhotoPath != null) {
                try {
                    File foto = new File(currentPhotoPath);
                    Bitmap bitmap = BitmapFactory.decodeFile(foto.getAbsolutePath());
                    fotografia.setImageBitmap(bitmap);
                } catch (Exception ex) {
                    ex.toString();
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }catch (Exception ex){
                ex.toString();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "uth.pmo1.agenda.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE);
            }
        }
    }

    private void ConsumeCreateApi() throws IOException {

        String v1=nombre.getText().toString();
        String v2=apellido.getText().toString();
        String v3=telefono.getText().toString();

        if(v1.isEmpty()){
            nombre.setError("Debe ingresar el nombre!");
        }
        else if (v2.isEmpty()){
            apellido.setError("Debe ingresar el apellido");
        }
        else if (v3.isEmpty()) {
            telefono.setError("Debe ingresar el telefono");
        }
        else{
            File imageFile = createImageFile();
            Bitmap bitmap = ((BitmapDrawable)fotografia.getDrawable()).getBitmap();
            fotografia.setImageBitmap(bitmap);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("nombres", nombre.getText().toString());
            parametros.put("apellidos", apellido.getText().toString());
            parametros.put("telefono", telefono.getText().toString());
            parametros.put("foto", byteArray.toString());
            parametros.put("latitud", latitud.getText().toString());
            parametros.put("longitud", longitud.getText().toString());

            POSTMethod = Methods.ApiCreate;
            JSONObject JsonAlumn = new JSONObject(parametros);

            RequestQueue peticion = Volley.newRequestQueue(getApplicationContext());

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, POSTMethod, JsonAlumn, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        for(int i = 0; i<= jsonArray.length(); i++) {
                            JSONObject msg = jsonArray.getJSONObject(i);
                        }
                    } catch (Exception ex) {

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

            peticion.add(jsonObjectRequest);
            mensaje();
            limpiar();
        }
    }

    private void limpiar(){
        nombre.setText("");
        apellido.setText("");
        telefono.setText("");
        fotografia.setImageBitmap(null);
        latitud.setText("");
        longitud.setText("");
        mMap.setMapStyle(null);
    }

    private void mensaje() {
        String mensaje = nombre.getText().toString() +
                "  " + apellido.getText().toString() +
                "  " + "Almacenado";

        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }



}