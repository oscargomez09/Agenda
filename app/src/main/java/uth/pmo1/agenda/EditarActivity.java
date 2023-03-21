package uth.pmo1.agenda;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import uth.pmo1.agenda.RestApiMethods.Methods;

public class EditarActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener{

    static final int REQUEST_IMAGE = 101;
    static final int PETICION_ACCESS_CAM = 201;
    static final int Result_galeria = 101;
    ImageView edfotografia;
    Button btnactualizar, btncontactos;
    ImageButton btnfoto;
    String PUTMethod, currentPath, currentPhotoPath;
    EditText edid, ednombre, edapellido, edtelefono, edlatitud, edlongitud;
    GoogleMap mMap;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);

        btnfoto = (ImageButton) findViewById(R.id.btnfoto);
        btnactualizar = (Button) findViewById(R.id.btnactualizar);

        edid = (EditText) findViewById(R.id.txtid);
        ednombre = (EditText) findViewById(R.id.txtnombre);
        edapellido = (EditText) findViewById(R.id.txtapellido);
        edtelefono = (EditText) findViewById(R.id.txttelefono);
        edfotografia = (ImageView) findViewById(R.id.imageView);
        edlatitud = (EditText) findViewById(R.id.txtlatitud);
        edlongitud = (EditText) findViewById(R.id.txtlongitud);

        Intent intent = getIntent();
        position = intent.getExtras().getInt("position");

        edid.setText(ReadActivity.contactosArrayList.get(position).getId());
        ednombre.setText(ReadActivity.contactosArrayList.get(position).getNombres());
        edapellido.setText(ReadActivity.contactosArrayList.get(position).getApellidos());
        //edfotografia.setImageBitmap(ReadActivity.contactosArrayList.get(position).getFoto());
        edtelefono.setText(ReadActivity.contactosArrayList.get(position).getTelefono());
        edlatitud.setText(ReadActivity.contactosArrayList.get(position).getLatitud());
        edlongitud.setText(ReadActivity.contactosArrayList.get(position).getLongitud());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        btnfoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permisos();
            }
        });

        btnactualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Actualizar();
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
        edlatitud.setText("" + latLng.latitude);
        edlongitud.setText(""+latLng.longitude);

        mMap.clear();
        LatLng honduras = new LatLng(latLng.latitude,latLng.longitude);
        mMap.addMarker(new MarkerOptions().position(honduras).title(""));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(honduras));
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        edlatitud.setText("" + latLng.latitude);
        edlongitud.setText(""+latLng.longitude);

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
                    edfotografia.setImageBitmap(bitmap);
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

   /* private void ConsumeCreateApi() throws IOException {

        String vid =edid.getText().toString().trim();
        String vnombre=ednombre.getText().toString().trim();
        String vapellido=edapellido.getText().toString().trim();
        String vtelefono=edtelefono.getText().toString().trim();
        String vfoto=edfotografia.getDrawable().toString().trim();
        String vlatitud=edlatitud.getText().toString().trim();
        String vlongitud=edlongitud.getText().toString().trim();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Actualizando...");

        if(vnombre.isEmpty()){
            Toast.makeText(this, "Debe de ingresar el nombre!", Toast.LENGTH_SHORT).show();
        }
        else if (vapellido.isEmpty()){
            Toast.makeText(this, "Debe de ingresar el apellido!", Toast.LENGTH_SHORT).show();
        }
        else if (vtelefono.isEmpty()) {
            Toast.makeText(this, "Debe de ingresar el telefono!", Toast.LENGTH_SHORT).show();
        }
        else{
            File imageFile = createImageFile();
            Bitmap bitmap = ((BitmapDrawable)edfotografia.getDrawable()).getBitmap();
            edfotografia.setImageBitmap(bitmap);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();


            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("id", vid);
            parametros.put("nombres", vnombre);
            parametros.put("apellidos", vapellido);
            parametros.put("telefono", vtelefono);
            parametros.put("foto", byteArray.toString());
            parametros.put("latitud", vlatitud);
            parametros.put("longitud", vlongitud);

            PUTMethod = Methods.ApiUpdate;
            JSONObject JsonAlumn = new JSONObject(parametros);

            RequestQueue peticion = Volley.newRequestQueue(getApplicationContext());

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, PUTMethod, JsonAlumn, new Response.Listener<JSONObject>() {
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
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String,String>params=new HashMap<>();
                    params.put("id",vid);
                    params.put("nombres",vnombre);
                    params.put("apellidos",vapellido);
                    params.put("telefono",vtelefono);
                    params.put("foto",byteArray.toString());
                    params.put("latitud",vlatitud);
                    params.put("longitud",vlongitud);

                    return params;
                }
            };
            peticion.add(jsonObjectRequest);
            mensaje();
            limpiar();

        }
    }*/

    private void Actualizar() {

        String id=edid.getText().toString().trim();
        String nombre=ednombre.getText().toString().trim();
        String apellido=edapellido.getText().toString().trim();
        String telefono=edtelefono.getText().toString().trim();
        //String foto=edfotografia.getDrawable().toString().trim();
        String latitud=edlatitud.getText().toString().trim();
        String longitud=edlongitud.getText().toString().trim();

        ProgressDialog progressDialog =new ProgressDialog(this);
        progressDialog.setMessage("Actualizando...");


        if (nombre.isEmpty()){
            Toast.makeText(this,"Debe ingresar el nombre!",Toast.LENGTH_SHORT).show();
        }else if (apellido.isEmpty()){
            Toast.makeText(this,"Debe ingresar el apellido!",Toast.LENGTH_SHORT).show();
        }else if (telefono.isEmpty()){
            Toast.makeText(this,"Debe ingresar el telefono!",Toast.LENGTH_SHORT).show();
        }else {
            progressDialog.show();
            StringRequest request =new StringRequest(Request.Method.POST, "http://192.168.0.3/alumnos/Update.php",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            Toast.makeText(EditarActivity.this, "Actualizado correctamente!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), ReadActivity.class));
                            finish();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(EditarActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
            ){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    try {
                        File imageFile = createImageFile();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Bitmap bitmap = ((BitmapDrawable)edfotografia.getDrawable()).getBitmap();
                    edfotografia.setImageBitmap(bitmap);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    Map<String,String>params=new HashMap<>();
                    params.put("id",id);
                    params.put("nombres",nombre);
                    params.put("apellidos",apellido);
                    params.put("telefono",telefono);
                    params.put("foto",byteArray.toString());
                    params.put("latitud",latitud);
                    params.put("longitud",longitud);

                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(EditarActivity.this);
            requestQueue.add(request);
        }
    }

    private void limpiar(){
        edid.setText("");
        ednombre.setText("");
        edapellido.setText("");
        edtelefono.setText("");
        edfotografia.setImageBitmap(null);
        edlatitud.setText("");
        edlongitud.setText("");
        mMap.setMapStyle(null);
    }

    private void mensaje() {
        String mensaje = ednombre.getText().toString() +
                "  " + edapellido.getText().toString() +
                "  " + "Actualizado";

        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }


}