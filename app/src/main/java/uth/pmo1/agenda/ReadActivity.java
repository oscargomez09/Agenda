package uth.pmo1.agenda;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReadActivity extends AppCompatActivity {

    ListView listView;
    Adapter adapter;
    FloatingActionButton inicio;
    Contactos contactos;
    public static ArrayList<Contactos>contactosArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        listView = findViewById(R.id.listview);
        inicio = findViewById(R.id.inicio);
        adapter = new Adapter(this,contactosArrayList);
        listView.setAdapter(adapter);

        inicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateActivity.class);
                startActivity(intent);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                CharSequence[]dialogo={"-> EDITAR DATOS","-> ELIMINAR DATOS"};
                builder.setTitle(contactosArrayList.get(position).getId() + " " + contactosArrayList.get(position).getNombres() + " " + contactosArrayList.get(position).getApellidos());
                builder.setItems(dialogo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        switch (i){
                            case 0:
                                startActivity(new Intent(getApplicationContext(),EditarActivity.class).putExtra("position", position));
                                break;
                            case 1:
                                Delete(contactosArrayList.get(position).getId());
                                break;
                        }
                    }
                });
                builder.show();
            }
        });
        listadatos();
    }

    public void agregar(View view){
        Intent intent = new Intent(ReadActivity.this, CreateActivity.class);
        startActivity(intent);
    }

    public void listadatos(){
        StringRequest request = new StringRequest(Request.Method.POST, "http://192.168.0.3/alumnos/Read2.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        contactosArrayList.clear();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String exito = jsonObject.getString("exito");
                            JSONArray jsonArray = jsonObject.getJSONArray("datos");

                            if (exito.equals("1")){
                                for (int i=0; i<jsonArray.length(); i++){

                                    JSONObject object = jsonArray.getJSONObject(i);

                                    String id = object.getString("id");
                                    String nombre = object.getString("nombres");
                                    String apellido = object.getString("apellidos");
                                    String telefono = object.getString("telefono");
                                    //String foto = object.getString("foto");
                                    String latitud = object.getString("latitud");
                                    String longitud = object.getString("longitud");

                                    contactos = new Contactos(id,nombre,apellido,telefono,latitud,longitud);
                                    contactosArrayList.add(contactos);
                                    adapter.notifyDataSetChanged();

                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ReadActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(ReadActivity.this);
        requestQueue.add(request);
    }

    public void Delete(final String id){
        StringRequest request =new StringRequest(Request.Method.POST, "http://192.168.0.3/alumnos/Delete.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equalsIgnoreCase("Datos eliminados")) {
                            Toast.makeText(ReadActivity.this, "Datos eliminados", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(getApplicationContext(), ReadActivity.class));
                            finish();
                        } else {
                            Toast.makeText(ReadActivity.this, "Error no se puede eliminar", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ReadActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String,String>params=new HashMap<>();
                params.put("id",id);

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(ReadActivity.this);
        requestQueue.add(request);
    }
}