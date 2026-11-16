package com.example.registrolineascampo.ui.Records;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.registrolineascampo.R;
import com.example.registrolineascampo.data.entity.Trabajador; // Ajusta al nombre de tu entidad

import java.util.ArrayList;
import java.util.List;

public class TrabajadorAdapter extends RecyclerView.Adapter<TrabajadorAdapter.ViewHolder> {

    private List<Trabajador> listaTrabajadores = new ArrayList<>();
    private final OnLineaClickListener listener;

    public interface OnLineaClickListener {
        void onSumarClick(Trabajador trabajador);
        void onRestarClick(Trabajador trabajador);
    }

    public TrabajadorAdapter(OnLineaClickListener listener) {
        this.listener = listener;
    }

    public void setLista(List<Trabajador> nuevaLista) {
        this.listaTrabajadores = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trabajador, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trabajador t = listaTrabajadores.get(position);
        holder.txtNombre.setText(t.nombre);

        if (t.totalLineas == 0) {
            holder.txtLineas.setText("Sin líneas");
        } else {
            holder.txtLineas.setText("Línea " + t.lineasTexto);
        }

        holder.txtCantidad.setText(String.valueOf(t.totalLineas));

        holder.btnSumar.setOnClickListener(v -> listener.onSumarClick(t));
        holder.btnRestar.setOnClickListener(v -> listener.onRestarClick(t));
    }

    @Override
    public int getItemCount() {
        return listaTrabajadores.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtLineas, txtCantidad, btnRestar, btnSumar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreTrabajador);
            txtLineas = itemView.findViewById(R.id.txtLineasTrabajador);
            txtCantidad = itemView.findViewById(R.id.txtCantidadLineas);
            btnRestar = itemView.findViewById(R.id.btnRestarLinea);
            btnSumar = itemView.findViewById(R.id.btnSumarLinea);
        }
    }
}