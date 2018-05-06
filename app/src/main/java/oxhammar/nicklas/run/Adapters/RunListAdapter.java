package oxhammar.nicklas.run.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import oxhammar.nicklas.run.Activities.DisplayFinishedRunActivity;
import oxhammar.nicklas.run.DBHandler;
import oxhammar.nicklas.run.FinishedRun;
import oxhammar.nicklas.run.R;

/**
 * Created by Nick on 2018-03-27.
 */

public class RunListAdapter extends RecyclerView.Adapter<RunListAdapter.ViewHolder> {

    DBHandler db;

    Context mContext;
    LinearLayoutManager llm;
    ArrayList<FinishedRun> runList;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder  {

        protected TextView vRunDuration;
        protected View cardView;
        protected ImageButton deleteRunImageButton;

        public ViewHolder(View v) {
            super(v);

            vRunDuration = (TextView) v.findViewById(R.id.run_card_duration_text);
            cardView = v.findViewById(R.id.card_view_run);
            deleteRunImageButton = v.findViewById(R.id.deleteRunImageButton);

        }

    }
    // Provide a suitable constructor (depends on the kind of dataset)
    public RunListAdapter(Context context, LinearLayoutManager linearLayoutManager, ArrayList<FinishedRun> runList) {
        mContext = context;
        this.runList = runList;
        llm = linearLayoutManager;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public RunListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.run_card_view, parent, false);

        mContext = parent.getContext();
        db = new DBHandler(mContext);

        return new ViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RunListAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        final FinishedRun finishedRun = runList.get(position);

        holder.vRunDuration.setText(finishedRun.getStringDistanceAndDate());
        holder.cardView.setTag(finishedRun.getStringDuration());
        holder.deleteRunImageButton.setTag(finishedRun.getDate().toString());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                displayFinishedRun(finishedRun);

            }
        });

        holder.deleteRunImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                buildAlertMessageDeleteRun(holder.getAdapterPosition());

            }
        });

    }


    public void removeAt(int position) {

        db.deleteRun(runList.get(position).getId());

        runList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, runList.size());

    }

    private void buildAlertMessageDeleteRun(final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(mContext.getString(R.string.delete_run))
                .setCancelable(false)
                .setPositiveButton(mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                        removeAt(position);
                    }
                })
                .setNegativeButton(mContext.getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public int getItemCount() {
        return runList.size();
    }

    public void displayFinishedRun(final FinishedRun run){

        Intent myIntent = new Intent(mContext, DisplayFinishedRunActivity.class);
        myIntent.putExtra("runId", run.getId());
        mContext.startActivity(myIntent);
        
    }
}
