package android.scroll.tlllllll;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;

public class Fragment2 extends Fragment {

    RecyclerView recyclerView;
    NoteAdapter adapter;
    Context context;
    TabLayout.OnTabSelectedListener listener;

    public void onAttach(Context context) {

        super.onAttach(context);
        this.context = context;

        if(context instanceof TabLayout.OnTabSelectedListener) {
            listener = (TabLayout.OnTabSelectedListener) context;
        }
    }

    public void onDetach() {

        super.onDetach();
        if(context != null) {
            context = null;
            listener = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_2, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new NoteAdapter();
        adapter.addItem(new Note(0, "제목1", "7월 1일", "주소", "x", "y",
                "String picture" , "즐거운 하루"
    ));
        adapter.addItem(new Note(0, "제목1", "7월 1일", "주소", "x", "y",
                "String picture" , "즐거운 하루"));
        adapter.addItem(new Note(0, "제목1", "7월 1일", "주소", "x", "y",
                "String picture" , "즐거운 하루"));

        recyclerView.setAdapter(adapter);

        return rootView;

    }

    private void init(ViewGroup rootView) {
        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);



    }



}