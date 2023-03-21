package uth.pmo1.agenda;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class Adapter extends ArrayAdapter<Contactos> {

    Context context;
    List<Contactos>contactosList;

    public Adapter(@NonNull Context context, List<Contactos>contactosList) {
        super(context, R.layout.elemento_lista,contactosList);
        this.context=context;
        this.contactosList=contactosList;
    }

    public View getView(int position, @NonNull View context, ViewGroup resource) {
        View view = LayoutInflater.from(resource.getContext()).inflate(R.layout.elemento_lista, null, true);

        TextView id = view.findViewById(R.id.id);
        TextView nombre = view.findViewById(R.id.nombre);
        TextView apellido = view.findViewById(R.id.apellido);
        TextView telefono = view.findViewById(R.id.telefono);
        //ImageView foto = view.findViewById(R.id.foto);
        TextView latitud = view.findViewById(R.id.latitud);
        TextView longitud = view.findViewById(R.id.longitud);

        id.setText(contactosList.get(position).getId());
        nombre.setText(contactosList.get(position).getNombres());
        apellido.setText(contactosList.get(position).getApellidos());
        telefono.setText(contactosList.get(position).getTelefono());
        //foto.setImageBitmap(contactosList.get(position).getFoto());
        latitud.setText(contactosList.get(position).getLatitud());
        longitud.setText(contactosList.get(position).getLongitud());

        return view;
    }
}
