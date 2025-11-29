package com.example.reglamentoupp;

import android.content.Context;
import android.media.MediaPlayer;
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

    public interface ReglamentoInteractionListener {
        void onQuizClick(String itemText, String itemType);
        void onCaseStudyClick(String itemText, String itemType);
    }

    private ReglamentoInteractionListener mListener;
    private MediaPlayer mediaPlayer;

    // Guardamos la referencia para detener audios si es necesario
    private ReglamentoItem[] mItems;

    protected abstract @LayoutRes int getLayoutRes();
    protected abstract ReglamentoItem[] getItems();
    protected abstract int getRecyclerViewId();
    protected abstract String getItemType();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);

        mItems = getItems();

        RecyclerView recyclerView = view.findViewById(getRecyclerViewId());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        String itemType = getItemType();

        // Pasamos 'this' al adaptador
        ReglamentoAdapter adapter = new ReglamentoAdapter(mItems, itemType, this, mListener);
        recyclerView.setAdapter(adapter);

        return view;
    }

    // --- YA NO USAMOS onResume PARA AUTOPLAY ---
    // El audio solo sonará por interacción del usuario.

    @Override
    public void onPause() {
        super.onPause();
        stopAudio(); // Detiene el audio si sales de la pantalla o se abre otra actividad
    }

    /**
     * Reproduce el audio individualmente.
     */
    public void playAudio(int audioResId) {
        // Detener cualquier audio previo
        stopAudio();

        if (audioResId != 0) {
            mediaPlayer = MediaPlayer.create(getContext(), audioResId);
            if (mediaPlayer != null) {
                // Al terminar, liberamos recursos
                mediaPlayer.setOnCompletionListener(mp -> stopAudio());
                mediaPlayer.start();
            }
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ReglamentoInteractionListener) {
            mListener = (ReglamentoInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ReglamentoInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        stopAudio();
    }
}