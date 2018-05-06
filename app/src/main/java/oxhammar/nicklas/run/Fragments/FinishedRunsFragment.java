package oxhammar.nicklas.run.Fragments;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONStringer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import oxhammar.nicklas.run.Adapters.RunListAdapter;
import oxhammar.nicklas.run.DBHandler;
import oxhammar.nicklas.run.FinishedRun;
import oxhammar.nicklas.run.R;

import static android.content.ContentValues.TAG;


public class FinishedRunsFragment extends Fragment {

    static private DBHandler db;

    RecyclerView runListRecyclerView;
    RecyclerView.Adapter mAdapter;

    ArrayList<FinishedRun> runList;

    LinearLayoutManager mLayoutManager;


    private OnFragmentInteractionListener mListener;

    public FinishedRunsFragment() {
        // Required empty public constructor
    }

    public static FinishedRunsFragment newInstance() {
        FinishedRunsFragment fragment = new FinishedRunsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DBHandler(getContext());

        runList = new ArrayList<FinishedRun>();

        Gson gson = new Gson();
        Type type = new TypeToken<FinishedRun>(){}.getType();

        ArrayList<String> jsonRunList = db.getAllRuns();

        for (String s : jsonRunList) {
            FinishedRun run = gson.fromJson(s, type);
            runList.add(run);
        }

        Collections.reverse(runList);

        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new RunListAdapter(getContext(), mLayoutManager, runList);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_finished_runs, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        runListRecyclerView = getView().findViewById(R.id.runList_recycler_view);
        runListRecyclerView.setLayoutManager(mLayoutManager);
        runListRecyclerView.setAdapter(mAdapter);

    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void addFinishedRunToRecyclerView(){

        Gson gson = new Gson();
        Type type = new TypeToken<FinishedRun>(){}.getType();

        ArrayList<String> jsonRunList = db.getAllRuns();

        String runJson = jsonRunList.get(jsonRunList.size() - 1);

        try {
            FinishedRun run = gson.fromJson(runJson, type);

            runList.add(0, run);

            mAdapter.notifyItemInserted(0);
            mAdapter.notifyDataSetChanged();
        }
        catch (IllegalStateException | JsonSyntaxException exception){

            Log.d(TAG, "addFinishedRunToRecyclerView: " + exception);
        }


        
    }

}
