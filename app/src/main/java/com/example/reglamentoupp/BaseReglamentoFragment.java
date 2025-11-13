package com.example.reglamentoupp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseReglamentoFragment extends Fragment {

    // --- Esta es la interfaz correcta que tu ReglamentoAdapter espera ---
    public interface ReglamentoInteractionListener {
        void onQuizClick(String itemText, String itemType);
        void onCaseStudyClick(String itemText, String itemType);
    }
    // ----------------------------------------------------

    private ReglamentoInteractionListener mListener;

    // --- Estos son los métodos abstractos correctos que tus Fragmentos implementan ---
    protected abstract @LayoutRes int getLayoutRes();
    protected abstract String[] getItems();
    protected abstract int getRecyclerViewId();
    protected abstract String getItemType();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);

        // --- CORRECCIÓN 1: (Error: cannot find symbol rv_reglamento) ---
        // Busca el RecyclerView usando el ID abstracto que provee el fragmento
        RecyclerView recyclerView = view.findViewById(getRecyclerViewId());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        String itemType = getItemType();
        String[] items = getItems();

        // --- CORRECCIÓN 2: (Error: constructor ReglamentoAdapter...) ---
        // Llama al constructor correcto de tu ReglamentoAdapter (Turno 7)
        ReglamentoAdapter adapter = new ReglamentoAdapter(items, itemType, mListener);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ReglamentoInteractionListener) {
            mListener = (ReglamentoInteractionListener) context;
        } else {
            // Este error te dice si la Actividad (MainActivity o GameLevelActivity)
            // no está implementando la interfaz correcta.
            throw new RuntimeException(context.toString()
                    + " must implement ReglamentoInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}